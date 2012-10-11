/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors;

import com.peterlavalle.degen.RemoteDegen;
import java.io.IOException;
import java.util.Iterator;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author Peter LaValle
 */
public abstract class AExtractionList implements Iterable<String> {

	private final RemoteDegen outer;

	public AExtractionList(final RemoteDegen outer) throws IOException {
		this.outer = outer;
	}

	/**
	 * Extracts a single pathed file to the named parent folder
	 *
	 * @param parent
	 * @param name
	 * @throws MojoExecutionException
	 */
	public abstract void extractTo(final String parent, final String name) throws MojoExecutionException;

	public abstract String getName();

	/**
	 * Iterates through the remaining entries that will be copied out of this archive
	 *
	 * @return an iterator for the member list
	 */
	@Override
	public abstract Iterator<String> iterator();

	/**
	 * Removes any source file with the passed name, or any class file (based on folder paths) likely to have been compiled from that source file.
	 *
	 * @param sourceFile
	 */
	public void removeAny(String sourceFile) {
		final String substring = sourceFile.endsWith(".java") ? sourceFile.substring(0, sourceFile.length() - ".java".length()) : null;
		for (final String resourceFile : this) {
			if (sourceFile.equals(resourceFile)) {
				outer.getLog().debug(getName() + " is dropping `" + resourceFile + "`");
				removeResource(resourceFile);
			}
			if (sourceFile.endsWith(".java") && resourceFile.endsWith(".class")) {
				if (substring == null || !resourceFile.startsWith(substring)) {
					continue;
				}
				final String resourceSubstring = resourceFile.substring(substring.length(), resourceFile.length() - ".class".length());
				assert resourceSubstring.matches("^[a-zA-Z0-9$_]*$");
				outer.getLog().debug(getName() + " is dropping `" + resourceFile + "`");
				removeResource(resourceFile);
			}
		}
	}

	/**
	 * Removes a single file, no special rules are applied it's a match removal
	 * @param resourceFile the file to remove
	 */
	public abstract void removeResource(String resourceFile);
}
