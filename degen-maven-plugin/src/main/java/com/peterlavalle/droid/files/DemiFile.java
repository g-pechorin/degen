/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.droid.files;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author peter
 */
public class DemiFile {

	private final File parent;
	private final String path;

	public DemiFile(final File parent, final String path) {
		this.parent = parent;
		this.path = path;
	}

	public static Iterable<DemiFile> listFiles(File path) throws MojoExecutionException {
		final ZipFile zipFile;
		try {
			zipFile = new ZipFile(path);
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to open `" + path.getPath() + "`", ex);
		}
		return new Iterable<DemiFile>() {

			@Override
			public Iterator<DemiFile> iterator() {
				final Enumeration<? extends ZipEntry> entries = zipFile.entries();

				return new Iterator<DemiFile>() {

					private ZipEntry nextElement = null;

					@Override
					public boolean hasNext() {

						if (!entries.hasMoreElements()) {
							return false;
						}

						if (nextElement != null) {
							return true;
						}

						do {
							nextElement = entries.nextElement();

							if (nextElement.isDirectory()) {
								nextElement = null;
							}


						} while (entries.hasMoreElements() && nextElement == null);

						return true;
					}

					@Override
					public DemiFile next() {
						hasNext();

						final ZipEntry entry = nextElement;
						nextElement = null;

						return new DemiFile(null, entry.getName()) {

							@Override
							protected InputStream openInputStream() throws IOException {
								return zipFile.getInputStream(entry);
							}

							@Override
							public Iterable<DemiFile> listFiles() {
								return null;
							}
						};
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported yet.");
					}
				};
			}
		};
	}

	public static Iterable<DemiFile> listFiles(final String path) {
		final File parent = new File(path);
		return new Iterable<DemiFile>() {

			@Override
			public Iterator<DemiFile> iterator() {

				final Iterator<String> iterator = Arrays.asList(parent.list()).iterator();

				return new Iterator<DemiFile>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public DemiFile next() {
						return new DemiFile(parent, iterator.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported.");
					}
				};
			}
		};

	}

	public Iterable<DemiFile> listFiles() {
		final File file = new File(parent, path);
		return !file.isDirectory() ? null : new Iterable<DemiFile>() {

			@Override
			public Iterator<DemiFile> iterator() {
				final Iterator<String> iterator = Arrays.asList(file.list()).iterator();
				return new Iterator<DemiFile>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public DemiFile next() {
						return new DemiFile(parent, path + File.separatorChar + iterator.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported.");
					}
				};
			}
		};
	}

	public String getName() {
		return path;
	}

	public void copyTo(File folder) throws MojoExecutionException {

		// get teh file object
		final File outputFile = new File(folder, getName());

		// make the parent directories
		outputFile.getParentFile().mkdirs();

		// get an input stream to this file
		final InputStream inputStream;
		try {
			inputStream = openInputStream();
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to openInputStream() `" + getName() + "`", ex);
		}

		// open the output stream
		final OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(outputFile);
		} catch (FileNotFoundException ex) {
			throw new MojoExecutionException("While trying to open FileOutputStream `" + getName() + "`", ex);
		}

		// copy bytes
		{
			final byte[] data = new byte[1024];
			try {
				// read bytes
				for (int i = 0; (i = inputStream.read(data)) != -1; ) {
					// write bytes
					try {
						outputStream.write(data, 0, i);
					} catch (IOException ex) {
						throw new MojoExecutionException("While writing bytes for `" + getName() + "`", ex);
					}
				}
			} catch (IOException ex) {
				throw new MojoExecutionException("While reading bytes from `" + getName() + "`", ex);
			}
		}

		// close the streams here to avoid swallowing the exception
		try {
			outputStream.close();
		} catch (IOException ex) {
			throw new MojoExecutionException("While closing output stream for `" + getName() + "`", ex);
		}
		try {
			inputStream.close();
		} catch (IOException ex) {
			throw new MojoExecutionException("While closing input stream for `" + getName() + "`", ex);
		}
	}

	protected InputStream openInputStream() throws IOException {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
