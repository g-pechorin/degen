/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
	 * the directory to output the generated sources to
	 *
	 * @parameter expression="${project.build.directory}/generated-sources/degen"
	 */
	private String outputDirectory;
	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.excludeAny}" default-value=""
	 */
	private String[] excludeAny;
	/**
	 * If set it'll limit what is included
	 *
	 * @parameter expression="${degen.includeOnly}" default-value=""
	 */
	private String[] includeOnly;
	/**
	 * URL (possibly HTTP://) to the distribution's zip file
	 *
	 * @parameter expression="${degen.distribution}"
	 * @required
	 */
	private String distribution;

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
			fileFromURL = Files.getTemporaryFileFromURL(project.getBasedir(), distribution);

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

	/**
	 * Runs the guts of this plugin
	 */
	@Override
	public void execute() throws MojoExecutionException {

		// anything in the outputDirectory which is NOT a .java file should be copied as-is into our output
		projectHelper.addResource(project, outputDirectory, new ArrayList(), Collections.singletonList("**/**.java"));

		// tell maven to compile all .java in the outputDirectory
		project.addCompileSourceRoot(outputDirectory);


		final List<ArchiveExtractionList> extractedArchives = makeListOfExtractedArchives();

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

		// we've checked everything - so dump what's left
		for (final ArchiveExtractionList extractedArchive : extractedArchives) {
			extractedArchive.extractTo(outputDirectory);
		}
	}

	/**
	 * Contains a list of files that will be extracted from a single ZipFile
	 */
	public class ArchiveExtractionList implements Iterable<String> {

		// handle tot eh actual zipFile
		private final ZipFile zipFile;

		/**
		 * Constructs a new object to represent the named zipfile.
		 *
		 * @param file
		 * @throws IOException
		 */
		public ArchiveExtractionList(final File file) throws IOException {
			this.zipFile = new ZipFile(file);

			nextEntry:
			for (final ZipEntry entry : Collections.list(this.zipFile.entries())) {

				if (entry.isDirectory()) {
					continue nextEntry;
				}

				final String name = entry.getName();

				if (includeOnly != null && includeOnly.length > 0) {
					boolean keep = false;

					for (final String includeRegex : includeOnly) {
						keep = name.matches(includeRegex);
						if (keep) {
							break;
						}
					}

					if (!keep) {
						getLog().info("Skipping `" + name + "` due to include rules");
						continue nextEntry;
					}
				}

				for (final String skipRegex : excludeAny) {
					if (name.matches(skipRegex)) {
						getLog().info("Skipping `" + name + "` due to exclude rules");
						continue nextEntry;
					}
				}

				linkedList.add(name);
			}
		}
		
		// a list of all files that we might extract
		private final List<String> linkedList = new LinkedList<String>();

		/**
		 * Extracts a single pathed file to the named parent folder
		 *
		 * @param parent
		 * @param name
		 * @throws MojoExecutionException
		 */
		public void extractTo(final String parent, final String name) throws MojoExecutionException {
			final File file = new File(parent, name);
			try {
				Files.copyStream(zipFile.getInputStream(zipFile.getEntry(name)), file);
			} catch (IOException ex) {
				throw new MojoExecutionException("name=`" + name + "` file=`" + file + "`", ex);
			}
		}

		/**
		 * Extracts all files remaining in the list into the passed directory
		 *
		 * @param outputFolder
		 * @throws MojoExecutionException
		 */
		public void extractTo(String outputFolder) throws MojoExecutionException {
			for (final String resourceFile : this) {
				extractTo(outputFolder, resourceFile);
			}
		}

		/**
		 * Removes any source file with the passed name, or any class file (based on folder paths) likely to have been compiled from that source file.
		 *
		 * @param sourceFile
		 */
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

		/**
		 * Iterates through the remaining entries that will be copied out of this archive
		 *
		 * @return an iterator for the member list
		 */
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
	private String extracted;

	/**
	 * makes a list of all archives that we want from the distributionFile.
	 *
	 * @return
	 * @throws MojoExecutionException
	 */
	public List<ArchiveExtractionList> makeListOfExtractedArchives() throws MojoExecutionException {
		try {
			
			// get the distribution archive
			final ZipFile distributionFile = getDistributionFile();

			final LinkedList<ArchiveExtractionList> archives = new LinkedList<ArchiveExtractionList>();

			// archives can be separated by the pipe character or the newline
			for (final String name : extracted.split("(\\||\n)")) {
				
				// skip empty entries
				final String trim = name.trim();
				if (trim.equals("")) {
					continue;
				}

				archives.add(new ArchiveExtractionList(Files.getTemporaryFileFromZip(distributionFile, trim)));
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
	private String projectSources;

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
