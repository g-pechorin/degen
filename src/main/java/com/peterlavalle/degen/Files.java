/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Peter LaValle
 */
public final class Files {

	public static final int BUFFER_SIZE = 128;
	/**
	 * Private variable that will be used to cache handles to downloaded files.
	 */
	private static final Map<String, File> DOWNLOADED_FILES = new HashMap<String, File>();

	private static final Logger LOGGER = Logger.getLogger(Files.class.getName());

	/**
	 * Copies the data from the stream to the specified file, then closes it.
	 *
	 * @param inputStream a stream with data. it will be closed
	 * @param output the file to store the data in. it will be overwritten
	 * @throws IOException if any other method does, or if the output file has no parent
	 */
	public static void copyStream(final InputStream inputStream, final File output) throws IOException {

		if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
			throw new IOException("I was not able to create the folder `" + output.getParentFile() + "`");
		}

		final FileOutputStream outputStream = new FileOutputStream(output);
		try {

			// we'll need a buffer of bytes
			final byte[] buffer = new byte[BUFFER_SIZE];

			// read all bytes from the file
			for (int read = 0; read != -1; read = inputStream.read(buffer)) {
				outputStream.write(buffer, 0, read);
			}
		} finally {

			// close both handles
			outputStream.close();
			inputStream.close();
		}
	}

	/**
	 * Lists all files in root relative to basedir
	 *
	 * @param basedir
	 * @param root
	 * @return
	 */
	public static List<String> getFileNamesInDirectory(File basedir, final String root) {
		final LinkedList<String> files = new LinkedList<String>();

		assert basedir != null;
		assert basedir.isDirectory();
		assert basedir.exists();
		assert root != null;

		// get the file we're going to search in
		final File file = new File(basedir, root);

		assert file.exists();

		if (!file.isDirectory()) {

			// if we're looking at a real file, we should add it after we remove the './' thing
			files.add(root.replace("./", ""));

			return files;
		} else {

			// loop through every name ...
			for (final String name : file.list()) {

				// and add whatever you find there
				files.addAll(getFileNamesInDirectory(basedir, root + '/' + name));
			}
		}

		return files;
	}

	/**
	 * Copies a file from a URL into a temporary file.
	 *
	 * @param urlString a string with the URL of the file
	 * @return a file handle for the temporary copy
	 * @throws IOException
	 */
	public static synchronized File getTemporaryFileFromURL(final MavenProject project, final Log log, final String urlString) throws IOException {

		// if it is a URL ...
		if (urlString.matches("^\\w+\\:.*$")) {
			// if the file hasn't been downloaded ...
			if (!DOWNLOADED_FILES.containsKey(urlString)) {
				final byte[] bytes = urlString.getBytes();

				final IntBuffer wrap = ByteBuffer.wrap(Arrays.copyOf(bytes, (bytes.length % 4) != 0 ? ((bytes.length / 4) + 1) * 4 : bytes.length)).asIntBuffer();

				final StringBuilder name = new StringBuilder(Files.class.getSimpleName() + ".");
				while (wrap.hasRemaining()) {
					name.append(Integer.toString(wrap.get(), 64));
				}

				MavenProject rootProject = project;
				if (rootProject.getParent() != null && rootProject.getBasedir().getParentFile().equals(rootProject.getParent().getBasedir())) {
					rootProject = rootProject.getParent();
				}
				
				final File file = new File(rootProject.getBuild().getDirectory(), name.toString());

				if (file.exists()) {
					log.info("reusing `" + urlString + "` -> `" + file + "`");
				} else {
					log.info("Downloading `" + urlString + "` ...");
					copyStream(new URL(urlString).openStream(), file);
					log.info("... to `" + file + "`");
				}

				// ... download it now
				DOWNLOADED_FILES.put(urlString, file);
			}

			// retrieve whatever has been downloaded
			return DOWNLOADED_FILES.get(urlString);
		} else {
			return new File(project.getBasedir(), urlString);
		}
	}
	/**
	 * Extracts a named file form a zip archive into a temporary file and returns a handle to it.
	 *
	 * @param zipFile
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static File getTemporaryFileFromZip(final ZipFile zipFile, final String name) throws IOException {

		// the zipEntry is like a File object, but for files inside of a zipfile
		final ZipEntry entry = zipFile.getEntry(name);

		// check to make sure that we got something
		if (entry == null || entry.isDirectory()) {
			throw new IllegalArgumentException("`" + zipFile.getName() + (entry == null ? "` has no entry named `" : "` has no FILE named `") + name + "`");
		}

		// we'll need a temporary file to store our data
		final File temporaryFile = makeTemporaryFileFromStream(zipFile.getInputStream(entry));

		// I don't know if this is needed
		temporaryFile.deleteOnExit();

		// set the time
		if (!temporaryFile.setLastModified(entry.getTime())) {
			LOGGER.log(Level.WARNING, "Unable to set time for `{0}` @ `{1}`", new Object[]{name, temporaryFile});
		}

		return temporaryFile;
	}

	/**
	 * Creates a temporary file, and fills it with the contents of a stream
	 */
	public static File makeTemporaryFileFromStream(final InputStream inputStream) throws IOException {

		// create our temporary file
		final File file = File.createTempFile(RemoteDegen.class.getName(), "");
		file.deleteOnExit();

		// copy the data to it
		copyStream(inputStream, file);

		// return the handle that we've created
		return file;
	}

	/**
	 * Just shuts up sonar
	 */
	private Files() {
	}
}
