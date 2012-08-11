/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @goal remote
 * @phase generate-sources
 * @requiresDependencyResolution compile
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
	 * Controls where I place the generated (well, extracted) source files
	 *
	 * @parameter expression="${degen.classesFolder}" default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File classesFolder;
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
	
	
	
	private ZipFile distributionZipFile;
	private ZipFile resourcesZipFile;

	protected ZipFile getDistributionFile() throws MojoExecutionException {

		if (distributionZipURL == null || distributionZipURL.equals("")) {
			throw new IllegalArgumentException();
		}

		if (this.distributionZipFile == null) {
			try {
				// read it as a zip file
				this.distributionZipFile = new ZipFile(Files.getTemporaryFileFromURL(distributionZipURL));
			} catch (IOException e) {
				throw new MojoExecutionException("couldn't read the file from `" + distributionZipURL + "` as a zip file", e);
			}
		}

		return this.distributionZipFile;
	}

	@Override
	public void execute() throws MojoExecutionException {
		distributionZipFile = null;
		resourcesZipFile = null;

//		getDistributionFile();

//		classesFolder.delete();
//		classesFolder.mkdirs();

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
					getLog().info("`" + binary + "` will not be extracted");
				}
			}

			getLog().debug("`" + file + "` will be compiled as normal");
		}


		// if there's a .java file in extractedSourceFiles then
		for (final String file : resourceFiles) {

			// copy this source file
			try {
				Files.copyStream(getSourcesZipFile().getInputStream(getSourcesZipFile().getEntry(file)), new File(this.classesFolder, file));
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}

			getLog().info("`" + file + "` will be compiled as a generated source");
		}
	}

	public ZipFile getSourcesZipFile() throws MojoExecutionException {

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

		for (final String file : Files.getFileNamesInDirectory(projectSources)) {

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
		for (final ZipEntry zipEntry : Collections.list(getSourcesZipFile().entries())) {

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
