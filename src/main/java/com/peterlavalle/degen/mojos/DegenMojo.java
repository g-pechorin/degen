/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;

import com.google.common.collect.Sets;
import com.peterlavalle.degen.extractors.util.FileHook;
import com.peterlavalle.degen.extractors.util.Files;
import com.peterlavalle.degen.extractors.util.MasterURL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
	 * @parameter expression="${source_filter}" default-value=".*\.java$"
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


		final Map<String, FileHook> hooks = new TreeMap<String, FileHook>();

		// find all hooks
		for (final String source : sources) {
			final MasterURL masterURL;
			try {
				masterURL = new MasterURL(source);
			} catch (final MalformedURLException ex) {
				throw new MojoExecutionException("MasterURL(`" + source + "`)", ex);
			}
			try {
				for (final FileHook hook : masterURL.listFiles(getCacheDir(project))) {
					final String name = hook.getName();

					// if we've already got this one, skip it
					if (hooks.containsKey(name)) {
						continue;
					}

					// if this is a .class file and we've already got the .java file - skip this one
					if (name.endsWith(".class") && hooks.containsKey(name.replaceAll("\\.class$", "\\.java"))) {
						continue;
					}

					// save it
					hooks.put(name, hook);

					// if this was a .java file, we may need to remove keys (sorry)
					if (name.endsWith(".java")) {
						final String replaceAll = name.replaceAll("\\.java", "");

						for (final String hookName : hooks.keySet()) {
							if (!hookName.endsWith(".class")) {
								continue;
							}
							if (!hookName.startsWith(replaceAll)) {
								continue;
							}
							if (hookName.substring(replaceAll.length()).matches("^(\\$|\\.).*class$")) {
								hooks.remove(hookName);
							}
						}
					}
				}
			} catch (final IOException ex) {
				throw new MojoExecutionException("MasterURL(`" + source + "`).listFiles()", ex);
			}
		}

		final Set<String> activeSources = Sets.newHashSet();
		final Set<String> activeResources = Sets.newHashSet();
		for (final FileHook hook : hooks.values()) {

			final String name = hook.getName();
			(hook.getName().matches(source_filter) ? activeSources : activeResources).add(name);

			try {
				pullHook(hook, hook.getName().matches(source_filter));
			} catch (final IOException ex) {
				throw new MojoExecutionException("Problem with `" + name + "`", ex);
			}
		}

		// TODO : remove anything in sources or resources that isn't relevant
		getLog().info("TODO : remove anything in sources or resources that isn't relevant");
		
		project.addCompileSourceRoot(gensource_folder);
		projectHelper.addResource(project, genresource_folder, new ArrayList(), new ArrayList());
	}
	/**
	 * Where to put the generated source
	 *
	 * @parameter expression="${gensource_folder}" default-value="${project.build.directory}/degen/generated-source"
	 * @required
	 */
	private String gensource_folder;

	public File getGeneratedSources() {
		
		return new File(gensource_folder);
	}
	
	/**
	 * Where to put the generated resource
	 *
	 * @parameter expression="${genresource_folder}" default-value="${project.build.directory}/degen/generated-resource"
	 * @required
	 */
	private String genresource_folder;

	public File getGeneratedResources() {
		return new File(genresource_folder);
	}

	private void pullHook(FileHook hook, final boolean isSource) throws IOException {
		final File finalName = new File(isSource ? getGeneratedSources() : getGeneratedResources(), hook.getName());

		if (finalName.lastModified() < hook.lastModified()) {
			Files.copyStream(hook.openInputStream(), finalName);
		}
	}
}
