/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
	 * Where should we copy the project's source files from?
	 *
	 * @parameter expression="${degen.projectSources}" default-value="src/main/java"
	 * @required
	 */
	protected String projectSources;
	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.skipRegexs}" default-value=""
	 */
	protected String[] skipRegexs;
	/**
	 * the directory to output the generated sources to
	 *
	 * @parameter expression="${project.build.directory}/generated-sources/degen"
	 */
	private String outputDirectory;
	/**
	 * URL (possibly HTTP://) to the distribution's zip file
	 *
	 * @parameter expression="${degen.distributionZipURL}"
	 * @required
	 */
	protected String distributionZipURL;
	/**
	 * Path within the distribution's zip file to the source zip file we want
	 *
	 * @parameter expression="${degen.extractedArchive}"
	 * @required
	 */
	protected String extractedArchive;
	/**
	 * A cached handle for the ZipFile that we're pulling stuff out of
	 */
	private ZipFile distributionZipFile;

	/**
	 * Retrieves or creates a handle to the ZipFile that we're pulling stuff out of
	 */
	protected ZipFile getDistributionFile() throws MojoExecutionException {

		if (distributionZipURL == null || distributionZipURL.equals("")) {
			throw new IllegalArgumentException();
		}

		if (this.distributionZipFile == null) {
			final File fileFromURL;
			try {
				fileFromURL = distributionZipURL.matches("^\\w+\\:.*$") ? Files.getTemporaryFileFromURL(distributionZipURL) : new File(project.getBasedir(), distributionZipURL);

			} catch (IOException e) {

				getLog().debug("distributionZipURL.matches()=" + distributionZipURL.matches("^\\w+\\:.*$"));
				getLog().debug("File(\".\").getAbsolutePath()=" + new File(".").getAbsolutePath());

				throw new MojoExecutionException("couldn't locate the file `" + distributionZipURL + "`", e);
			}

			try {
				// read it as a zip file
				this.distributionZipFile = new ZipFile(fileFromURL);
			} catch (IOException e) {

				getLog().debug("fileFromURL.getAbsolutePath()=" + fileFromURL.getAbsolutePath());

				throw new MojoExecutionException("couldn't read the file `" + distributionZipURL + "` as a zip file", e);
			}
		}

		return this.distributionZipFile;
	}
	/**
	 * the maven project helper class for adding resources
	 *
	 * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
	 */
	private MavenProjectHelper projectHelper;
	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {

		// anything int the outputDirectory which is NOT a .java file should be copied as-is into our output
		projectHelper.addResource(project, outputDirectory, new ArrayList(), Collections.singletonList("**/**.java"));

		// compile all .java in the outputDirectory
		project.addCompileSourceRoot(outputDirectory);

		// get the list of "extracted" sources
		final List<String> resourceFiles = getResourceNames();

		// if we have a project source that looks like an extracted resource, we want to ignore that extracted resource
		for (final String file : getProjectSourceFiles()) {

			// remove it from resourceFiles
			resourceFiles.remove(file);

			// remove any pre-compiled classes
			for (final String binary : new LinkedList<String>(resourceFiles)) {
				if (binary.startsWith(file.replaceAll("\\.java$", "")) && binary.endsWith(".class")) {
					resourceFiles.remove(binary);
					getLog().debug("`" + binary + "` will not be extracted");
				}
			}

			getLog().debug("`" + file + "` will be compiled as normal");
		}


		// if there's a .java file in extractedSourceFiles then
		for (final String file : resourceFiles) {

			// copy this source file
			try {
				Files.copyStream(getResourcesZipFile().getInputStream(getResourcesZipFile().getEntry(file)), new File(outputDirectory, file));
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}

			getLog().debug("`" + file + "` will be compiled as a generated source");
		}
	}
	private ZipFile resourcesZipFile;

	public ZipFile getResourcesZipFile() throws MojoExecutionException {

		if (resourcesZipFile == null) {
			try {
				resourcesZipFile = new ZipFile(Files.getTemporaryFileFromZip(getDistributionFile(), extractedArchive));
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}
		}

		return resourcesZipFile;
	}

	/**
	 * Produces a list of files that will be compiled
	 */
	public List<String> getProjectSourceFiles() throws MojoExecutionException {
		final LinkedList<String> files = new LinkedList<String>();

		getLog().debug("Scanning `" + projectSources + "` for .java sources");

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

	protected List<String> getResourceNames() throws MojoExecutionException {

		final LinkedList<String> files = new LinkedList<String>();

		nextFile:
		for (final ZipEntry zipEntry : Collections.list(getResourcesZipFile().entries())) {

			if (zipEntry.isDirectory()) {
				continue;
			}

			final String name = zipEntry.getName();

			for (final String skipRegex : skipRegexs) {
				if (name.matches(skipRegex)) {
					getLog().info("`" + name + "` will be skipped");
					continue nextFile;
				}
			}
			files.add(name);

		}

		return files;
	}
}
