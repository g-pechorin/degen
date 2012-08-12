/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Peter LaValle
 */
public class Files {

	public static void copyStream(final InputStream inputStream, final File output) throws IOException {
		output.getParentFile().mkdirs();
		
		final FileOutputStream outputStream = new FileOutputStream(output);
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

	/**
	 * Creates a temporary file, and fills it with the contents of a stream
	 */
	public static File makeTemporaryFileFromStream(final InputStream inputStream) throws IOException {
		final File file = File.createTempFile(RemoteDegen.class.getName(), "");
		copyStream(inputStream, file);
		return file;
	}

	public static File getTemporaryFileFromZip(final ZipFile zipFile, final String name) throws IOException {
		final ZipEntry entry = zipFile.getEntry(name);
		if (zipFile.getEntry(name) == null) {
			throw new IllegalArgumentException();
		}
		return makeTemporaryFileFromStream(zipFile.getInputStream(entry));
	}
	private final static Map<String, File> downloadedFiles = new HashMap<String, File>();

	public static synchronized File getTemporaryFileFromURL(final String urlString) throws IOException {
		if (!downloadedFiles.containsKey(urlString)) {

			downloadedFiles.put(urlString, Files.makeTemporaryFileFromStream(new URL(urlString).openStream()));
		}

		return downloadedFiles.get(urlString);
	}

	public static List<String> getFileNamesInDirectory(File basedir, final String root) {
		if (root == null) {
			throw new IllegalArgumentException();
		}
		final LinkedList<String> files = new LinkedList<String>();
		final File file = new File(basedir,root);
		assert file.exists();
		if (!file.isDirectory()) {
			files.add(root.replace("./", ""));
			return files;
		} else {
			for (final String name : file.list()) {
				files.addAll(getFileNamesInDirectory(basedir,root + '/' + name));
			}
		}
		return files;
	}
}
