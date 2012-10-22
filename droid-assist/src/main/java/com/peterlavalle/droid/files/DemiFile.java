/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.droid.files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
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
}
