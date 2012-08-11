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
	 * Where should we copy the "real" source files from?
	 *
	 * @parameter expression="${degen.sources}" default-value="src/main/java"
	 * @required
	 */
	protected String sources;
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
	private ZipFile sourcesZipFile;

	protected ZipFile getDistributionFile() throws MojoExecutionException {

		if (distributionZipURL == null || distributionZipURL.equals("")) {
			throw new IllegalArgumentException();
		}

		if (this.distributionZipFile == null) {
			final File temporaryFileFromUrl;
			try {

				temporaryFileFromUrl = getTemporaryFileFromStream(new URL(distributionZipURL).openStream());

			} catch (IOException e) {
				throw new MojoExecutionException("couldn't download the file `" + distributionZipURL + "`", e);
			}
			try {
				this.distributionZipFile = new ZipFile(temporaryFileFromUrl);
			} catch (IOException e) {
				throw new MojoExecutionException("couldn't read the file from `" + distributionZipURL + "`", e);
			}
		}

		return this.distributionZipFile;
	}

	public static File getTemporaryFileFromStream(final InputStream inputStream) throws IOException {
		final File file = File.createTempFile(RemoteDegen.class.getName(), "");

		copyStream(inputStream, new FileOutputStream(file));

		return file;
	}

	@Override
	public void execute() throws MojoExecutionException {
		distributionZipFile = null;
		sourcesZipFile = null;

		getDistributionFile();

		classesFolder.delete();
		classesFolder.mkdirs();

		// get the list of "extracted" sources
		final List<String> extractedSourceFiles = getExtractedSourceNames();

		// get the list of binaries and resources
		// final List<String> extractedBinaryFiles = getExtractedBinaryNames();

		// if there's a .java file in sourceFiles then
		for (final String file : getSourceFileNames()) {

			// remove it from extractedSourceFiles
			extractedSourceFiles.remove(file);

			// remove any pre-generated classes
			for (final String binary : new LinkedList<String>(extractedSourceFiles)) {
				if (binary.startsWith(file.replaceAll("\\.java$", "")) && binary.endsWith(".class")) {
					extractedSourceFiles.remove(binary);
					getLog().info("`" + binary + "` will not be extracted");
				}
			}

			getLog().debug("`" + file + "` will be compiled as normal");
		}


		// if there's a .java file in extractedSourceFiles then
		nextFile:
		for (final String file : extractedSourceFiles) {

			// if we should skip this one
			for (final String skipRegex : skipRegexs) {
				if (skipRegex != null && !skipRegex.equals("") && file.matches(skipRegex)) {
					getLog().info("`" + file + "` will be skipped");
					continue nextFile;
				}
			}

			// copy this source file
			copySource(file);

			getLog().info("`" + file + "` will be compiled as a generated source");
		}
	}

	public static File getTemporaryFileFromZip(final ZipFile zipFile, final String name) throws IOException {
		final ZipEntry entry = zipFile.getEntry(name);

		if (zipFile.getEntry(name) == null) {
			throw new IllegalArgumentException();
		}

		return getTemporaryFileFromStream(zipFile.getInputStream(entry));
	}

	public ZipFile getSourcesZipFile() throws MojoExecutionException {

		if (sourcesZipFile == null) {
			try {
				sourcesZipFile = new ZipFile(getTemporaryFileFromZip(getDistributionFile(), extractedArchive));
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}
		}

		return sourcesZipFile;
	}

	public static List<String> getFiles(final String root) {

		if (root == null) {
			throw new IllegalArgumentException();
		}

		final LinkedList<String> files = new LinkedList<String>();
		final File file = new File(root);

		assert file.exists();

		if (!file.isDirectory()) {
			files.add(root.replace("./", ""));
			return files;
		} else {

			for (final String name : file.list()) {
				files.addAll(getFiles(root + '/' + name));
			}
		}
		return files;
	}

	public List<String> getSourceFileNames() throws MojoExecutionException {
		final LinkedList<String> files = new LinkedList<String>();

		getLog().debug("Scanning `" + sources + "` for .java sources");

		for (final String file : getFiles(sources)) {
			
			// how does this happen? oh I don't care ...
			if (file.equals(sources) ) {
				continue;
			}
			
			try {
				files.add(file.substring(sources.length() + 1));
			} catch (StringIndexOutOfBoundsException e) {
				throw new MojoExecutionException("file=`" + file + "` sources=`" + sources + "`", e);
			}
		}

		return files;
	}

	protected List<String> getExtractedSourceNames() throws MojoExecutionException {


		final LinkedList<String> files = new LinkedList<String>();

		for (final ZipEntry zipEntry : Collections.list(getSourcesZipFile().entries())) {


			if (!zipEntry.isDirectory()) {
				files.add(zipEntry.getName());
			}
		}

		return files;
	}

	public static void copyStream(final InputStream inputStream, final OutputStream outputStream) throws IOException {

		final byte[] buffer = new byte[128];

		while (true) {
			int read = inputStream.read(buffer);

			if (read == -1) {
				break;
			}

			outputStream.write(buffer, 0, read);
		}

		outputStream.close();
		inputStream.close();

	}

	protected void copySource(String file) throws MojoExecutionException {
		try {
			new File(this.classesFolder, file.replaceAll("[^\\/]*$", "")).mkdirs();
			copyStream(getSourcesZipFile().getInputStream(getSourcesZipFile().getEntry(file)), new FileOutputStream(new File(this.classesFolder, file)));
		} catch (IOException ex) {
			throw new MojoExecutionException("", ex);
		}
	}
}
