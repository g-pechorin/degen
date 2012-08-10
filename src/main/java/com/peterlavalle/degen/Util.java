/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author peterlavalle
 */
public class Util {

	private Util() {
	}

	public static void extractZip(final File sourceZip, final File destinationDir) throws MojoExecutionException {
		try {

			final ZipFile zipFile = new ZipFile(sourceZip);

			for (final ZipEntry entry : Collections.list(zipFile.entries())) {
				try {
					Util.copyFile(zipFile.getInputStream(entry), new FileOutputStream(new File(destinationDir, entry.getName())));
				} catch (IOException e) {
					throw new MojoExecutionException("while trying to extract `" + entry + "` from `" + sourceZip + "`", e);
				}
			}


		} catch (IOException e) {
			throw new MojoExecutionException("while trying to extract from `" + sourceZip + "`", e);
		}
	}

	public static void copyFile(String file, ZipFile source, File destination) throws MojoExecutionException {
		try {
			new File(destination,file.replaceAll("[^\\/]*$", "")).mkdirs();
			copyFile(source.getInputStream(source.getEntry(file)),new FileOutputStream(new File(destination,file)));
		} catch (IOException ex) {
			throw new MojoExecutionException("", ex);
		}
	}

	public static void copyFile(String file, File source, File destination) throws MojoExecutionException {

		final File src = new File(source, file);
		final File dst = new File(destination, file);

		try {

			copyFile(new FileInputStream(src), new FileOutputStream(dst));

		} catch (IOException e) {
			throw new MojoExecutionException("Problem while copying `" + file + "` from `" + source + "`(" + src + ") to `" + destination + "`(" + dst + ")", e);
		}
	}

	public static void copyFile(final InputStream inputStream, final OutputStream outputStream) throws IOException {

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

	public static List<String> getFiles(final File root) {

		if (root == null) {
			throw new IllegalArgumentException();
		}

		final LinkedList<String> files = new LinkedList<String>();


		if (!root.exists()) {
			return files;
		}

		final String rootString = root.toString().endsWith("/") ? root.toString() : (root.toString() + "/");

		for (final File file : root.listFiles()) {
			final String fileString = file.toString().replaceFirst(rootString, "");

			if (!file.isDirectory()) {
				files.add(fileString);
			} else {

				for (final String subFile : getFiles(file)) {
					files.add(fileString + "/" + subFile);
				}
			}
		}

		return files;

	}

	public static List<String> getFiles(final ZipFile zipFile) {
		final LinkedList<String> files = new LinkedList<String>();

		for (final ZipEntry zipEntry : Collections.list(zipFile.entries())) {


			if (!zipEntry.isDirectory()) {
				files.add(zipEntry.getName());
			}
		}

		return files;

	}

	public static File getTemporaryFileFromUrl(final String urlString) throws IOException {

		return getTemporaryFileFromStream(//
				new URL(urlString).openStream(), //
				urlString.replaceAll("^.*\\/", "").replaceAll("^.*\\.", "").replaceAll("[\\#\\?].*$", ""));
	}

	public static File getTemporaryFileFromStream(final InputStream inputStream, final String extension) throws IOException {


		final String prefix = Util.class.getName() + ".getTemporaryFileFromStream.";
		final File file = new File(prefix + Long.toHexString(Double.doubleToLongBits(Math.random())) + '.' + extension);
		file.deleteOnExit();

		copyFile(inputStream, new FileOutputStream(file));


		return file;
	}

	public static File getTemporaryFileFromZip(final ZipFile zipFile, final String name) throws IOException {
		final ZipEntry entry = zipFile.getEntry(name);

		if (zipFile.getEntry(name) == null) {
			throw new IllegalArgumentException();
		}

		return getTemporaryFileFromStream(zipFile.getInputStream(entry), name.replaceAll("^.*\\.", ""));

	}
}
