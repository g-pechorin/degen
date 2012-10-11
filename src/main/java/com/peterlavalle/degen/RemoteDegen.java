/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import com.google.common.collect.Lists;
import com.peterlavalle.degen.extractors.AExtractionList;
import com.peterlavalle.degen.extractors.FileSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 *
 * @goal remote
 * @phase generate-sources
 * @version $Id$
 */
public class RemoteDegen extends AbstractMojo {

	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.excludeAny}" default-value=""
	 */
	private String[] excludeAny;
	/**
	 * Path within the distribution's zip file to the source zip file we want
	 *
	 * @parameter expression="${degen.extracted}"
	 * @required
	 */
	private String extracted;
	/**
	 * If set it'll limit what is included
	 *
	 * @parameter expression="${degen.includeOnly}" default-value=""
	 */
	private String[] includeOnly;
	/**
	 * the directory to output the generated sources to
	 *
	 * @parameter expression="${project.build.directory}/generated-sources/degen"
	 */
	private String outputDirectory;
	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;
	/**
	 * the maven project helper class for adding resources
	 *
	 * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
	 */
	private MavenProjectHelper projectHelper;
	/**
	 * Where should we copy the project's source files from?
	 *
	 * @parameter expression="${degen.projectSources}" default-value="src/main/java"
	 * @required
	 */
	private String projectSources;

	/**
	 * Runs the guts of this plugin
	 */
	@Override
	public void execute() throws MojoExecutionException {

		// anything in the outputDirectory which is NOT a .java file should be copied as-is into our output
		projectHelper.addResource(project, outputDirectory, new ArrayList(), Collections.singletonList("**/**.java"));

		// tell maven to compile all .java in the outputDirectory
		project.addCompileSourceRoot(outputDirectory);


		final List<AExtractionList> extractedArchives = new LinkedList<AExtractionList>();
		try {
			// archives can be separated by the pipe character or the newline
			for (final String line : extracted.split("(\\||\n)")) {

				// skip empty entries
				if (line.trim().equals("")) {
					continue;
				}

				extractedArchives.add(new FileSource(line).getExtractionList(this));
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("extracted=`" + extracted + "`", ex);
		}

		// get the lists to manipulate
		final List<String> excludeList = Arrays.asList(getExcludeAny());
		final List<String> includeOnlyList = Arrays.asList(getIncludeOnly());
		for (AExtractionList list : extractedArchives) {
			next_listed_file:
			for (final String file : Lists.newArrayList(list)) {

				// handle exclusions
				for (final String pattern : excludeList) {
					if (file.matches(pattern)) {
						list.removeResource(file);
					}
				}

				// handle inclusions
				if (!includeOnlyList.isEmpty()) {
					for (final String pattern : includeOnlyList) {
						if (file.matches(pattern)) {
							continue next_listed_file;
						}
					}
					list.removeResource(file);
				}
			}
		}

		// cascade the archives. if a file is in an earlier archive, skip it from a later one
		{
			List<String> currentSources = Lists.newArrayList(getProjectSourceFiles()); //

			for (int i = 0; i < extractedArchives.size(); i++) {

				// check every source file in the "currentSources" ...
				for (final String sourceFile : currentSources) {

					// ... and remove it from any following archive
					for (AExtractionList archive : extractedArchives.subList(i, extractedArchives.size())) {
						archive.removeAny(sourceFile);
					}
				}

				// advance to the next set of sources
				currentSources = Lists.newArrayList(extractedArchives.get(i));
			}
		}

		// we've checked everything - so dump what's left
		for (final AExtractionList extractedArchive : extractedArchives) {
			for (final String sourceFile : extractedArchive) {
				extractedArchive.extractTo(outputDirectory, sourceFile);
			}
		}
	}

	public String[] getExcludeAny() {
		return Arrays.copyOf(excludeAny, excludeAny.length);
	}

	public String[] getIncludeOnly() {
		return Arrays.copyOf(includeOnly, includeOnly.length);
	}

	/**
	 * Produces a list of files that will be compiled
	 */
	public List<String> getProjectSourceFiles() throws MojoExecutionException {

		// alert
		getLog().debug("Scanning `" + projectSources + "` for .java sources");

		final LinkedList<String> files = new LinkedList<String>();
		for (final String file : Files.getFileNamesInDirectory(project.getBasedir(), projectSources)) {

			// how does this happen? oh I don't care!
			if (file.equals(projectSources)) {
				continue;
			}

			try {
				files.add(file.substring(projectSources.length() + 1));
			} catch (StringIndexOutOfBoundsException e) {
				throw new MojoExecutionException("file=`" + file + "` projectSources=`" + projectSources + "`", e);
			}
		}

		return files;
	}
}
