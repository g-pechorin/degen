/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author peterlavalle
 * @phase generate-sources
 */
public abstract class AbstractDegen extends AbstractMojo {

	/**
	 * Where should we copy the "real" source files from?
	 *
	 * @parameter expression="${degen.sources}" default-value="src/main/java"
	 * @required
	 */
	protected File sources;
	/**
	 * What files (from the binaries and the sources, but not the real sources or real resources) should we always skip
	 *
	 * @parameter expression="${degen.skipRegex}" default-value=""
	 */
	private String skipRegex;
	/**
	 * Controls where I place the generated (well, extracted) source files
	 *
	 * @parameter expression="${degen.generatedSources}" default-value="target/generated-sources"
	 * @required
	 */
	protected File generatedSources;
	/**
	 * Controls where I place the generated (well, extracted) resources
	 *
	 * @parameter expression="${degen.generatedBinaries}" default-value="target/generated-sources"
	 * @required
	 */
	protected File generatedBinaries;

	@Override
	public void execute() throws MojoExecutionException {

		generatedSources.delete();
		generatedSources.mkdirs();

		generatedBinaries.delete();
		generatedBinaries.mkdirs();

		// get the list of "real" sources
		final List<String> sourceFiles = getSourceFileNames();

		// get the list of "extracted" sources
		final List<String> extractedSourceFiles = getExtractedSourceNames();

		// get the list of binaries and resources
		final List<String> extractedBinaryFiles = getExtractedBinaryNames();

		// if there's a .java file in sourceFiles then
		for (final String file : sourceFiles) {

			// remove it from extractedSourceFiles
			extractedSourceFiles.remove(file);

			// remove any pre-generated classes
			for (final String binary : new LinkedList<String>(extractedBinaryFiles)) {
				if (binary.startsWith(file.replaceAll("\\.java$", "")) && binary.endsWith(".class")) {
					extractedBinaryFiles.remove(binary);
				}
			}

			getLog().info("`" + file + "` will be compiled as normal");
		}


		// if there's a .java file in extractedSourceFiles then
		for (final String file : extractedSourceFiles) {
			final String folderClassName = file.replaceAll("\\.java$", "");

			// if we should skip this one
			if (skipRegex != null && !skipRegex.equals("") && file.matches(skipRegex)) {
				continue;
			}

			// remove any pre-generated classes
			for (final String binary : new LinkedList<String>(extractedBinaryFiles)) {

				// check that the prefix is present
				if (!binary.startsWith(folderClassName)) {
					continue;
				}

				// check if this is a class file that came out of the java file
				if (binary.replaceFirst(folderClassName, "").matches("^[^\\/]\\.class$")) {
					extractedBinaryFiles.remove(binary);
				}
			}

			// copy this source file
			copySource(file);
		}

		// copy any remaining .class files and resources
		for (final String file : extractedBinaryFiles) {

			// if we should skip this one
			if (skipRegex != null && !skipRegex.equals("") && file.matches(skipRegex)) {
				continue;
			}

			// copy this resource
			copyResource(file);
		}
	}

	public List<String> getSourceFileNames() {
		return Util.getFiles(sources);
	}

	protected abstract List<String> getExtractedSourceNames() throws MojoExecutionException;

	protected abstract List<String> getExtractedBinaryNames() throws MojoExecutionException;

	protected abstract void copySource(final String file) throws MojoExecutionException;

	protected abstract void copyResource(final String file) throws MojoExecutionException;
}
