/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import com.google.common.collect.Lists;
import com.peterlavalle.degen.extractors.ArchiveExtractionList;
import com.peterlavalle.degen.extractors.AExtractionList;
import com.peterlavalle.degen.extractors.ReplacementExtractionList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
	 * URL (possibly HTTP://) to the distribution's zip file
	 *
	 * @parameter expression="${degen.distribution}"
	 * @required
	 */
	private String distribution;
	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.excludeAny}" default-value=""
	 */
	private String[] excludeAny;

	public String[] getExcludeAny() {
		return Arrays.copyOf(excludeAny, excludeAny.length);
	}
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

	public String[] getIncludeOnly() {
		return Arrays.copyOf(includeOnly, includeOnly.length);
	}
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
			// get the distribution archive
			final ZipFile distributionFile = getDistributionFile();

			// archives can be separated by the pipe character or the newline
			for (final String line : extracted.split("(\\||\n)")) {

				// skip empty entries
				if (line.trim().equals("")) {
					continue;
				}

				if (!line.contains(":")) {
					extractedArchives.add(new ArchiveExtractionList(this, line.trim()));
				} else {
					extractedArchives.add(new ReplacementExtractionList(this, line.split(":")[0].trim(), line.split(":")[1].trim()));
					//} else {
					//	throw new UnsupportedOperationException("TODO : name=`" + name + "` line=`" + line + "`");
				}
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("extracted=`" + extracted + "`", ex);
		}


		// cascade the archives
		{
			List<String> currentSources = getProjectSourceFiles();

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
	private transient ZipFile distributionFile = null;

	/**
	 * Retrieves or creates a handle to the ZipFile that we're pulling stuff out of
	 *
	 * @return
	 * @throws MojoExecutionException
	 */
	public ZipFile getDistributionFile() throws MojoExecutionException {

		if (distributionFile != null && distributionFile.getName().equals(distribution)) {
			return distributionFile;
		}

		assert distribution != null;
		assert !distribution.equals("");

		// the file handle
		final File fileFromURL;
		try {
			// either constructs a loca file (if you're reading data from the local machine) or a cached file URL (if you're downloading it from teh interwebz)
			fileFromURL = Files.getTemporaryFileFromURL(project, getLog(), distribution);

		} catch (IOException e) {

			// displays the file name
			getLog().debug("distribution=" + distribution);

			// displays wheter or not it's a URL
			getLog().debug("distribution.matches()=" + distribution.matches("^\\w+\\:.*$"));

			// displays the path we're working from
			getLog().debug("File(\".\").getAbsolutePath()=" + new File(".").getAbsolutePath());

			throw new MojoExecutionException("couldn't locate the file `" + distribution + "`", e);
		}
		assert fileFromURL != null;
		assert fileFromURL.exists();
		assert fileFromURL.canRead();

		try {
			// read it as a zip file
			distributionFile = new ZipFile(fileFromURL);

		} catch (IOException e) {

			getLog().debug("fileFromURL.getAbsolutePath()=" + fileFromURL.getAbsolutePath());

			throw new MojoExecutionException("couldn't read the file `" + distribution + "` as a zip file", e);
		}

		distribution = distributionFile.getName();
		return distributionFile;
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
