package com.peterlavalle.degen.extractors.util;

import com.google.common.collect.Lists;
import com.peterlavalle.degen.extractors.util.FileHook;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Peter LaValle
 */
public class MasterURL {
	
	public static final Replacor NIL_REPLACOR = new Replacor("{.*@$0}");

	public MasterURL(final String string) throws MalformedURLException {

		replacor = string.matches(".*\\{[^\\}]+\\}\\s*") ? new Replacor(string.replaceAll(".*(\\{[^\\}]+\\})\\s*", "$1")) : NIL_REPLACOR;
		final String[] split = string.trim().replaceAll("(.*)\\{[^\\}]+\\}", "$1").trim().replaceAll("\\s*\\@\\s*", " @").split("\\s");

		this.url = new URL(split[0]);

		final List<String> strings = Lists.newLinkedList();

		{
			for (int i = 1; i < split.length && split[i].startsWith("@"); i++) {
				final String substring = split[i].substring(1);

				// TODO : error checking

				strings.add(substring);
			}
			zips = strings;
		}
	}
	public final URL url;
	public final List<String> zips;
	public final Replacor replacor;

	public Iterable<FileHook> listFiles(final File cacheDir) throws IOException {

		final File file;
		{
			File archiveFile = Files.downloadFile(cacheDir, url);

			for (final String string : zips) {
				archiveFile = Files.extractArchiveFile(archiveFile, string);
			}
			file = archiveFile;
		}

		final ZipFile zipFile = new ZipFile(file);
		return new Iterable<FileHook>() {
			@Override
			public Iterator<FileHook> iterator() {
				return new Iterator<FileHook>() {
					final Enumeration<? extends ZipEntry> entries = zipFile.entries();
					ZipEntry nextElement = null;

					@Override
					public boolean hasNext() {

						while (nextElement == null) {
							if (!entries.hasMoreElements()) {
								return false;
							}

							nextElement = entries.nextElement();

							if (nextElement.getName().endsWith("/") || replacor.apply(nextElement.getName()) == null) {
								nextElement = null;
							}
						}

						return nextElement != null;
					}

					@Override
					public FileHook next() {
						final ZipEntry element = this.nextElement;
						this.nextElement = null;

						return new FileHook() {
							@Override
							public String getName() {
								return replacor.apply(element.getName());
							}

							@Override
							public InputStream openInputStream() throws IOException {
								return zipFile.getInputStream(element);
							}

							@Override
							public long lastModified() {
								return Math.max(file.lastModified(), element.getTime());
							}
						};
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
