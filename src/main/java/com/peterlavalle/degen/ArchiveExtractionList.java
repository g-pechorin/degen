/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

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
public class ArchiveExtractionList implements Iterable<String> {
	// a list of all files that we might extract
	final List<String> linkedList = new LinkedList<String>();
	// handle to the actual zipFile
	private final ZipFile zipFile;
	private final RemoteDegen outer;

	/**
	 * Constructs a new object to represent the named zipfile.
	 *
	 * @param file
	 * @throws IOException
	 */
	/**
	 * Constructs a new object to represent the named zipfile.
	 *
	 * @param file
	 * @throws IOException
	 */
	public ArchiveExtractionList(final RemoteDegen outer,final File file, final String regex, final String replacement) throws IOException {
		this.outer = outer;
		if (!regex.equals("^.*$") || !replacement.equals("$0")) {
			throw new UnsupportedOperationException("TODO : Not ready");
		}
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
	 * Constructs a new object to represent the named zipfile.
	 *
	 * @param file
	 * @throws IOException
	 */
	/**
	 * Constructs a new object to represent the named zipfile.
	 *
	 * @param file
	 * @throws IOException
	 */
	public ArchiveExtractionList(final RemoteDegen outer,final File file) throws IOException {
		this(outer, file, "^.*$", "$0");
	}

	/**
	 * Extracts all files remaining in the list into the passed directory
	 *
	 * @param outputFolder
	 * @throws MojoExecutionException
	 */
	public void extractTo(String outputFolder) throws MojoExecutionException {
		for (final String resourceFile : this) {
			extractTo(outputFolder, resourceFile);
		}
	}

	/**
	 * Extracts a single pathed file to the named parent folder
	 *
	 * @param parent
	 * @param name
	 * @throws MojoExecutionException
	 */
	public void extractTo(final String parent, final String name) throws MojoExecutionException {
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

	/**
	 * Removes any source file with the passed name, or any class file (based on folder paths) likely to have been compiled from that source file.
	 *
	 * @param sourceFile
	 */
	public void removeAny(String sourceFile) {
		final String substring = sourceFile.endsWith(".java") ? sourceFile.substring(0, sourceFile.length() - ".java".length()) : null;
		for (final String resourceFile : this) {
			if (sourceFile.equals(resourceFile)) {
				outer.getLog().debug("Archive `" + this.zipFile.getName() + "` is dropping `" + resourceFile + "`");
				this.linkedList.remove(resourceFile);
			}
			if (sourceFile.endsWith(".java") && resourceFile.endsWith(".class")) {
				if (substring == null || !resourceFile.startsWith(substring)) {
					continue;
				}
				final String resourceSubstring = resourceFile.substring(substring.length(), resourceFile.length() - ".class".length());
				assert resourceSubstring.matches("^[a-zA-Z0-9$_]*$");
				outer.getLog().debug("Archive `" + this.zipFile.getName() + "` is dropping `" + resourceFile + "`");
				this.linkedList.remove(resourceFile);
			}
		}
	}

	public void setFileRename(String regex, String replacement) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
}
