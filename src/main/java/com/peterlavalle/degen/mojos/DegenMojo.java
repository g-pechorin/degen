/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;

import com.peterlavalle.degen.extractors.util.MasterURL;
import com.peterlavalle.degen.extractors.util.Files;
import com.peterlavalle.degen.extractors.util.FileHook;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 *
 * @author Peter LaValle
 * @goal degenerate
 * @phase generate-sources
 * @version $Id$
 */
public class DegenMojo extends AbstractMojo {

	/**
	 * Which files are sources?
	 *
	 * @parameter expression="${source_filter}" default-value=".*\.java"
	 * @required
	 */
	private String source_filter;
	/**
	 * Whher do we look for things
	 *
	 * @parameter expression="${sources}"
	 * @required
	 */
	private String[] sources;
	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;
	/**
	 * the maven project helper class for adding resources
	 *
	 * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
	 */
	private MavenProjectHelper projectHelper;

	public File getCacheDir(final MavenProject project) {
		return project.getParent() != null ? getCacheDir(project.getParent()) : new File(project.getBuild().getDirectory());
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Set<FileHook> hooks = null;

		// find all hooks
		for (final String source : sources) {
			final MasterURL masterURL;
			try {
				masterURL = new MasterURL(source);
			} catch (MalformedURLException ex) {
				throw new MojoExecutionException("source=`" + source + "`", ex);
			}

			for (final FileHook hook : masterURL.listFiles(getCacheDir(project))) {
				// check that the hook's name isn't already covered, then add it
				throw new UnsupportedOperationException("check that the hook's name isn't already covered");
			}
		}

		final Set<String> activeSources = null;
		final Set<String> activeResources = null;
		for (final FileHook hook : hooks) {
			final String name = hook.getName();
			try {

				(hook.getName().matches(source_filter) ? activeSources : activeResources).add(name);

				pullHook(hook, hook.getName().matches(source_filter));
			} catch (final IOException ex) {
				throw new MojoExecutionException("Problem with `" + name + "`", ex);
			}
		}

		// remove anything in sources or resources that isn't relevant
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public File getGeneratedSources() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public File getGeneratedResources() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void pullHook(FileHook hook, final boolean isSource) throws IOException {
		final File finalName = new File(isSource ? getGeneratedSources() : getGeneratedResources(), hook.getName());

		if (finalName.lastModified() < hook.lastModified()) {


			Files.copyStream(hook.openInputStream(), finalName);
		}
	}
}
