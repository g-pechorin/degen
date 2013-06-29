package com.peterlavalle.degen.extractors.util;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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

	public static org.apache.maven.plugin.logging.Log LOG = null;

	public MasterURL(final String source) throws MalformedURLException {
		this.source = source;
		replacors = source.contains("{") ? Replacor.buildReplacors(source.trim().replaceAll("^[^\\{]+(\\+?\\{.*\\})$", "$1")) : null;

		final String[] split = source.trim().replaceAll("^([^\\{]+)\\+?\\{.*\\}$", "$1").trim().replaceAll("\\s*\\@\\s*", " @").split("\\s");

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
	public final String source;
	public final URL url;
	public final List<String> zips;
	public final Replacor.ReplacorList replacors;

	public Iterable<FileHook> listFiles(final File cacheDir) throws IOException {

		final File file;
		{
			File archiveFile = Files.cacheFile(cacheDir, url);

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

					{
						if (LOG != null) {
							LOG.debug("source=`" + source + "` >>");
							
							LOG.debug("source=`" + source + "` URL="+url);
							LOG.debug("source=`" + source + "` zips="+zips);
							LOG.debug("source=`" + source + "` replacors="+replacors);
						}
					}

					@Override
					public boolean hasNext() {

						while (nextElement == null) {
							if (!entries.hasMoreElements()) {

								if (LOG != null) {
									LOG.debug("source=`" + source + "` <<");
								}
								return false;
							}

							nextElement = entries.nextElement();

							if (LOG != null) {
								LOG.debug("source=`" + source + "` nextElement.getName()=" + nextElement.getName());
							}

							if (nextElement.getName().endsWith("/") || applyReplacors(nextElement.getName()) == null) {
								LOG.debug("source=`" + source + "` nextElement.getName()=" + nextElement.getName() + ": skipped");
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
								return applyReplacors(element.getName());
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

	/**
	 * applies each replacor in order to the input string. if any return null - the method bails out
	 */
	public String applyReplacors(final String input) {
		return replacors == null ? input : replacors.apply(input);
	}
}
