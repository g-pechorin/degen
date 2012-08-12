/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.InputStreamFacade;

/**
 *
 * @goal remote
 * @phase generate-sources
 * @version $Id$
 */
public class RemoteDegen extends AbstractMojo {

	/**
	 * the directory to output the generated sources to
	 *
	 * @parameter expression="${project.build.directory}/generated-sources/degen"
	 */
	private String outputDirectory;
	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.skipRegexs}" default-value=""
	 */
	protected String[] skipRegexs;
	/**
	 * URL (possibly HTTP://) to the distribution's zip file
	 *
	 * @parameter expression="${degen.distribution}"
	 * @required
	 */
	protected String distribution;
	/**
	 * A cached handle for the ZipFile that we're pulling stuff out of
	 */
	private ZipFile distributionZipFile;

	/**
	 * Retrieves or creates a handle to the ZipFile that we're pulling stuff out of
	 */
	protected ZipFile getDistributionFile() throws MojoExecutionException {

		if (distribution == null || distribution.equals("")) {
			throw new IllegalArgumentException();
		}

		if (this.distributionZipFile == null) {
			final File fileFromURL;
			try {
				fileFromURL = distribution.matches("^\\w+\\:.*$") ? Files.getTemporaryFileFromURL(distribution) : new File(project.getBasedir(), distribution);

			} catch (IOException e) {

				getLog().debug("distribution.matches()=" + distribution.matches("^\\w+\\:.*$"));
				getLog().debug("File(\".\").getAbsolutePath()=" + new File(".").getAbsolutePath());

				throw new MojoExecutionException("couldn't locate the file `" + distribution + "`", e);
			}

			try {
				// read it as a zip file
				this.distributionZipFile = new ZipFile(fileFromURL);
			} catch (IOException e) {

				getLog().debug("fileFromURL.getAbsolutePath()=" + fileFromURL.getAbsolutePath());

				throw new MojoExecutionException("couldn't read the file `" + distribution + "` as a zip file", e);
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

		// tell maven to compile all .java in the outputDirectory
		project.addCompileSourceRoot(outputDirectory);

		// get the list of "extracted" sources
		final List<String> resourceFiles = getExtractedArchive().linkedList;

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
		for (final String file : new LinkedList<String>(resourceFiles)) {



			// copy this source file
			getExtractedArchive().copy(file, new File(outputDirectory, file));

			getLog().debug("`" + file + "` will be compiled as a generated source");
		}
	}

	public class Archive {

		private final ZipFile zipFile;

		public Archive(final File file) throws IOException {
			this.zipFile = new ZipFile(file);

			nextEntry:
			for (final ZipEntry entry : Collections.list(this.zipFile.entries())) {
				
				if ( entry.isDirectory() ) {
					continue nextEntry;
				}
				
				final String name = entry.getName();
				
				for (final String skipRegex : skipRegexs) {
					if (name.matches(skipRegex)) {
						getLog().info("Skipping `" + name + "` due to skipRegexs");
						continue nextEntry;
					}
				}

				linkedList.add(name);
			}

		}
		public final LinkedList<String> linkedList = new LinkedList<String>();

		//		public File file(final String name) throws MojoExecutionException {
		//			try {
		//				final File file = File.createTempFile(getClass().getName(), ".tmp");
		//				copy(name, file);
		//				return file;
		//			} catch (IOException ex) {
		//				throw new MojoExecutionException("name=`" + name + "`", ex);
		//			}
		//		}
		
		public void copy(final String name, final File file) throws MojoExecutionException {
			try {
				Files.copyStream(zipFile.getInputStream(zipFile.getEntry(name)), file);
			} catch (IOException ex) {
				throw new MojoExecutionException("name=`" + name + "` file=`" + file + "`", ex);
			}
		}
	}
	/**
	 * Path within the distribution's zip file to the source zip file we want
	 *
	 * @parameter expression="${degen.extracted}"
	 * @required
	 */
	protected String extracted;
	private Archive extractedArchive;

	public Archive getExtractedArchive() throws MojoExecutionException {
		if (extractedArchive == null) {
			try {
				extractedArchive = new Archive(Files.getTemporaryFileFromZip(getDistributionFile(), extracted));
			} catch (IOException ex) {
				throw new MojoExecutionException("extracted=`" + extracted + "`", ex);
			}
		}
		return extractedArchive;
	}
	/**
	 * Where should we copy the project's source files from?
	 *
	 * @parameter expression="${degen.projectSources}" default-value="src/main/java"
	 * @required
	 */
	protected String projectSources;

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
}
