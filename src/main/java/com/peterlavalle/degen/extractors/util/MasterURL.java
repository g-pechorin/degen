package com.peterlavalle.degen.extractors.util;

import com.google.common.collect.Lists;
import com.peterlavalle.degen.extractors.util.FileHook;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author Peter LaValle
 */
public class MasterURL {

	public MasterURL(final String string) throws MalformedURLException {
		final String[] split = string.replaceAll("@\\s+([^\\s])", "@$1").split("\\s");

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

		{
			final String text = split[split.length - 1];

			replacor = !text.matches("^\\{(.*)\\}$") ? new Replacor("{.*@$0}") : new Replacor(text);
		}
	}
	public final URL url;
	public final List<String> zips;
	public final Replacor replacor;

	public Iterable<FileHook> listFiles(final File cacheDir) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
