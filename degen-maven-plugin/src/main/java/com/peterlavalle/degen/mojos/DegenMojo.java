/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;


import com.peterlavalle.degen.extractors.util.FileHook;
import com.peterlavalle.degen.extractors.util.MasterURL;
import com.peterlavalle.util.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @author Peter LaValle
 * @version $Id$
 * @goal degenerate
 * @phase generate-sources
 */
public class DegenMojo extends AMojo {

	/**
	 * Which files are sources?
	 *
	 * @parameter expression="${source_filter}" default-value=".*\.(c|cpp|h|hpp|java|scala|m)$"
	 * @required
	 */
	private String source_filter;
	/**
	 * Where do we look for things? Each of these strings should be "URL of a zip file [
	 *
	 * @some zip file inside of the URL] a regular expression of what to extract"
	 * @parameter expression="${sources}"
	 * @required
	 */
	private String[] sources;
	/**
	 * Which files are sources?
	 *
	 * @parameter default-value="degen-cache"
	 * @required
	 */
	private String degenCacheSubdir;

	public String getSourceFilter() {
		return source_filter;
	}

	public File getDegenCacheDir() {

		MavenProject project = getProject();

		assert project != null;

		// crawl up to the "most parent" project
		while (project.getParent() != null && project.getParent().getBasedir() != null) {
			project = project.getParent();
		}

		// make a file object
		File degenCache = new File(project.getBuild().getDirectory(), degenCacheSubdir);

		if (!degenCache.exists()) degenCache.mkdirs();

		return degenCache;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		assert MasterURL.LOG == null;
		MasterURL.LOG = getLog();

		final Map<String, FileHook> hooks = new TreeMap<String, FileHook>();

		// collect all "normal" source files
		for (final String src : (List<String>) getProject().getCompileSourceRoots()) {

			if (src == null) {
				continue;
			}

			for (final String sourceFile : scanFolderForSourceFiles(src, "")) {
				hooks.put(sourceFile, null);
			}
		}

		for (final String src : hooks.keySet()) {
			getLog().debug("src=\t" + src);
		}


		// find all hooks. uses MasterURL to determine which files from jars to include or exclude
		for (final String source : sources) {

			final MasterURL masterURL;
			try {
				masterURL = new MasterURL(source);
			} catch (final MalformedURLException ex) {
				throw new MojoExecutionException("MasterURL(`" + source + "`)", ex);
			}
			try {
				final File cacheDir = getDegenCacheDir();
				for (final FileHook hook : masterURL.listFiles(cacheDir)) {
					final String name = hook.getName();

					getLog().debug("masterURL=" + source + "\n\thook.getName()=" + hook.getName());

					// if we've already got this one, skip it
					if (hooks.containsKey(name)) {
						getLog().info("skip: masterURL (" + source + ") contians an extra copy of `" + hook.getName() + "`");
						continue;
					}

					// if this is a .class file and we've already got the .java file - skip this one
					if (name.endsWith(".class") && hooks.containsKey(name.replaceAll("\\.class$", "\\.java"))) {
						getLog().debug("skip: class `" + hook.getName() + "` already has a source file");
						continue;
					}

					// save it
					hooks.put(name, hook);

					// if this was a .java file, we may need to remove keys (sorry)
					// ... so if we've already grabbed a .class file, this removes it when we find a .java file
					if (name.endsWith(".java")) {
						final String replaceAll = name.replaceAll("\\.java", "");

						for (final String hookName : new HashSet<String>(hooks.keySet())) {
							if (!hookName.endsWith(".class")) {
								continue;
							}
							if (!hookName.startsWith(replaceAll)) {
								continue;
							}
							if (hookName.substring(replaceAll.length()).matches("^(\\$|\\.).*class$")) {
								getLog().debug("skip: removing class  `" + hook.getName() + "` becasuse of `" + name + "`");
								hooks.remove(hookName);
							}
						}
					}
				}
			} catch (final IOException ex) {
				throw new MojoExecutionException("MasterURL(`" + source + "`).listFiles()", ex);
			}
		}

		final Set<String> activeSources = new HashSet<String>();
		final Set<String> activeResources = new HashSet<String>();
		for (final FileHook hook : hooks.values()) {

			// if it was a "normal" one - ignore
			if (hook == null) {
				continue;
			}

			final String name = hook.getName();
			(hook.getName().matches(getSourceFilter()) ? activeSources : activeResources).add(name);

			try {
				pullHook(hook, hook.getName().matches(getSourceFilter()));
			} catch (final IOException ex) {
				throw new MojoExecutionException("Problem with `" + name + "`", ex);
			}
		}

		getProject().addCompileSourceRoot(getGeneratedSourcesDirectory().getAbsolutePath());
		getProjectHelper().addResource(getProject(), getGeneratedResourcesDirectory().getAbsolutePath(), new ArrayList(), Collections.singletonList("**/**.java"));

		assert MasterURL.LOG == getLog();
		MasterURL.LOG = null;
	}

	private void pullHook(FileHook hook, final boolean isSource) throws IOException {
		final File finalName = new File(isSource ? getGeneratedSourcesDirectory() : getGeneratedResourcesDirectory(), hook.getName());

		if (finalName.lastModified() < hook.lastModified()) {
			Files.copyStream(hook.openInputStream(), finalName);
		}
	}

	/**
	 * Scans the folder for files matching the configured regular expression.
	 *
	 * @param folder the folder to scan, relative to the project's basedir
	 */
	public List<String> scanFolderForSourceFiles(String root, String folder) {

		final File folderFile = new File(root, folder);
		final List<String> strings = new LinkedList<String>();

		if (folderFile.exists()) {
			for (final File file : folderFile.listFiles()) {

				if (file.isDirectory()) {
					strings.addAll(scanFolderForSourceFiles(root, folder + file.getName() + '/'));
				} else {
					strings.add(folder + file.getName());
				}
			}
		}

		return strings;
	}
}
