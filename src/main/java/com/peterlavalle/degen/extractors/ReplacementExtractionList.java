/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors;

import com.peterlavalle.degen.Files;
import com.peterlavalle.degen.RemoteDegen;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author Peter LaValle
 */
public class ReplacementExtractionList extends AExtractionList {

	private final Map<String, String> finalToOriginal = new HashMap<String, String>();
	private final String regex, replacement;

	public ReplacementExtractionList(final RemoteDegen outer, final String regex, final String replacement) throws IOException, MojoExecutionException {
		super(outer);

		this.regex = regex;
		this.replacement = replacement;

		for (final ZipEntry entry : Collections.list(outer.getDistributionFile().entries())) {
			final String entryName = entry.getName();

			if (!entryName.matches(regex) || entryName.endsWith("/")) {
				continue;
			}

			finalToOriginal.put(entryName.replaceAll(regex, replacement), entryName);
		}
	}

	@Override
	public void extractTo(String parent, String name) throws MojoExecutionException {
		assert finalToOriginal.containsKey(name);
		
		final File file = new File(parent, name);
		
		try {
			Files.copyStream(outer.getDistributionFile().getInputStream(outer.getDistributionFile().getEntry(finalToOriginal.get(name))), file);
		} catch (IOException ex) {
			throw new MojoExecutionException("name=`" + name + "` file=`" + file + "`", ex);
		}
	}

	@Override
	public String getName() {
		return "ReplacementExtractionList("+regex+","+replacement+")";
	}

	@Override
	public Iterator<String> iterator() {
		return finalToOriginal.keySet().iterator();
	}

	@Override
	public void removeResource(String resourceFile) {
		assert finalToOriginal.containsKey(resourceFile);
		finalToOriginal.remove(resourceFile);
	}
}
