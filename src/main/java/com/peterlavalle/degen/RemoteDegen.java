/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * Retrieves or creates a handle to the ZipFile that we're pulling stuff out of
	 */
	protected ZipFile getDistributionFile() throws MojoExecutionException {

		assert distribution != null;
		assert !distribution.equals("");

		final File fileFromURL;
		try {
			fileFromURL = distribution.matches("^\\w+\\:.*$") ? Files.getTemporaryFileFromURL(distribution) : new File(project.getBasedir(), distribution);

		} catch (IOException e) {

			getLog().debug("distribution.matches()=" + distribution.matches("^\\w+\\:.*$"));
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


		final ZipFile distributionFile = getDistributionFile();
		final List<Archive> extractedArchives = getExtractedArchives(distributionFile);

		List<String> nextSources = getProjectSourceFiles();

		for (int i = 0; i < extractedArchives.size(); i++) {

			final List<Archive> remainingArchives = extractedArchives.subList(i, extractedArchives.size());

			for (final String sourceFile : nextSources) {

				for (Archive archive : remainingArchives) {

					archive.removeAny(sourceFile);

				}
			}

			nextSources = extractedArchives.get(i).linkedList;
		}

		for (final Archive extractedArchive : extractedArchives) {
			extractedArchive.extractTo(outputDirectory);
		}
	}

	public class Archive implements Iterable<String> {

		private final ZipFile zipFile;

		public Archive(final File file) throws IOException {
			this.zipFile = new ZipFile(file);

			nextEntry:
			for (final ZipEntry entry : Collections.list(this.zipFile.entries())) {

				if (entry.isDirectory()) {
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
		private final LinkedList<String> linkedList = new LinkedList<String>();

		protected void extractTo(final String name, final String parent) throws MojoExecutionException {
			final File file = new File(parent, name);
			try {
				Files.copyStream(zipFile.getInputStream(zipFile.getEntry(name)), file);
			} catch (IOException ex) {
				throw new MojoExecutionException("name=`" + name + "` file=`" + file + "`", ex);
			}
		}

		public void extractTo(String file) throws MojoExecutionException {
			for (final String resourceFile : this) {
				extractTo(resourceFile, file);
			}
		}

		public void removeAny(String sourceFile) {
			final String substring = sourceFile.endsWith(".java") ? sourceFile.substring(0, sourceFile.length() - ".java".length()) : null;

			for (final String resourceFile : this) {
				if (sourceFile.equals(resourceFile)) {
					getLog().debug("Archive `" + this.zipFile.getName() + "` is dropping `" + resourceFile + "`");
					this.linkedList.remove(resourceFile);
				}

				if (sourceFile.endsWith(".java") && resourceFile.endsWith(".class")) {
					if (substring == null || !resourceFile.startsWith(substring)) {
						continue;
					}

					final String resourceSubstring = resourceFile.substring(substring.length(), resourceFile.length() - ".class".length());

					assert resourceSubstring.matches("^[a-zA-Z0-9$_]*$");

					getLog().debug("Archive `" + this.zipFile.getName() + "` is dropping `" + resourceFile + "`");
					this.linkedList.remove(resourceFile);
				}
			}
		}

		@Override
		public Iterator<String> iterator() {
			return new LinkedList<String>(linkedList).iterator();
		}
	}
	/**
	 * Path within the distribution's zip file to the source zip file we want
	 *
	 * @parameter expression="${degen.extracted}"
	 * @required
	 */
	protected String extracted;

	public List< Archive> getExtractedArchives(final ZipFile distributionFile) throws MojoExecutionException {
		try {
			final LinkedList<Archive> archives = new LinkedList<Archive>();
			for (final String name : extracted.split("(\\n|\\|)")) {
				if ( name.trim().equals("") ) {
					continue;
				}
				
				archives.add(new Archive(Files.getTemporaryFileFromZip(distributionFile, name.trim())));
			}
			return archives;
		} catch (IOException ex) {
			throw new MojoExecutionException("extracted=`" + extracted + "`", ex);
		}
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
