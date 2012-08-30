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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Contains a list of files that will be extracted from a single ZipFile
 */
public class ArchiveExtractionList extends AExtractionList {

	// a list of all files that we might extract
	final List<String> linkedList = new LinkedList<String>();
	// handle to the actual zipFile
	private final ZipFile zipFile;

	/**
	 * Constructs a new object to represent the named zipfile.
	 *
	 * @param file
	 * @throws IOException
	 */
	public ArchiveExtractionList(final RemoteDegen outer, final String fileName) throws IOException, MojoExecutionException {
		super(outer);

		final File file = Files.getTemporaryFileFromZip(outer.getDistributionFile(), fileName);

		if (file.isDirectory()) {
			throw new UnsupportedOperationException("TODO : Directory archives are still not ready here");
		}
		this.zipFile = new ZipFile(file);
		nextEntry:
		for (final ZipEntry entry : Collections.list(this.zipFile.entries())) {
			if (entry.isDirectory()) {
				continue nextEntry;
			}
			final String name = entry.getName();
			if (outer.getIncludeOnly() != null && outer.getIncludeOnly().length > 0) {
				boolean keep = false;
				for (final String includeRegex : outer.getIncludeOnly()) {
					keep = name.matches(includeRegex);
					if (keep) {
						break;
					}
				}
				if (!keep) {
					outer.getLog().info("Skipping `" + name + "` due to include rules");
					continue nextEntry;
				}
			}
			for (final String skipRegex : outer.getExcludeAny()) {
				if (name.matches(skipRegex)) {
					outer.getLog().info("Skipping `" + name + "` due to exclude rules");
					continue nextEntry;
				}
			}
			linkedList.add(name);
		}
	}

	/**
	 * Extracts a single pathed file to the named parent folder
	 *
	 * @param parent
	 * @param name
	 * @throws MojoExecutionException
	 */
	@Override
	public void extractTo(final String parent, final String name) throws MojoExecutionException {
		assert linkedList.contains(name);
		final File file = new File(parent, name);
		try {
			Files.copyStream(zipFile.getInputStream(zipFile.getEntry(name)), file);
		} catch (IOException ex) {
			throw new MojoExecutionException("name=`" + name + "` file=`" + file + "`", ex);
		}
	}

	/**
	 * Iterates through the remaining entries that will be copied out of this archive
	 *
	 * @return an iterator for the member list
	 */
	@Override
	public Iterator<String> iterator() {
		return new LinkedList<String>(linkedList).iterator();
	}

	public void removeResource(String resourceFile) {
		assert linkedList.contains(resourceFile);
		this.linkedList.remove(resourceFile);
	}

	public String getName() {
		return "Archive `" + this.zipFile.getName() + "`";
	}
}
