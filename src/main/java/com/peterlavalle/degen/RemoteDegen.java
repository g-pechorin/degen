/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

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


		final List<ArchiveExtractionList> extractedArchives = new LinkedList<ArchiveExtractionList>();
		try {
			// get the distribution archive
			final ZipFile distributionFile = getDistributionFile();

			// archives can be separated by the pipe character or the newline
			for (final String name : extracted.split("(\\||\n)")) {

				// skip empty entries
				final String[] split =  name.trim().split(":");
				final String	trim = split[0].trim();
				if (trim.equals("")) {
					continue;
				}

				if (split.length == 1) {
					extractedArchives.add(new ArchiveExtractionList(this,Files.getTemporaryFileFromZip(distributionFile, trim)));
				} else if (split.length == 3) {
					
					
					final String regex = split[1].trim();
					final String replacement = split[2].trim();
					
					extractedArchives.add(new ArchiveExtractionList(this,Files.getTemporaryFileFromZip(distributionFile, trim),regex,replacement));
				}else {
					throw new UnsupportedOperationException("TODO : name="+name);
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
					for (ArchiveExtractionList archive : extractedArchives.subList(i, extractedArchives.size())) {
						archive.removeAny(sourceFile);
					}
				}

				// advance to the next set of sources
				currentSources = extractedArchives.get(i).linkedList;
			}
		}

		// we've checked everything - so dump what's left
		for (final ArchiveExtractionList extractedArchive : extractedArchives) {
			extractedArchive.extractTo(outputDirectory);
		}
	}

	/**
	 * Retrieves or creates a handle to the ZipFile that we're pulling stuff out of
	 *
	 * @return
	 * @throws MojoExecutionException
	 */
	protected ZipFile getDistributionFile() throws MojoExecutionException {

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
			return new ZipFile(fileFromURL);
		} catch (IOException e) {

			getLog().debug("fileFromURL.getAbsolutePath()=" + fileFromURL.getAbsolutePath());

			throw new MojoExecutionException("couldn't read the file `" + distribution + "` as a zip file", e);
		}
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
