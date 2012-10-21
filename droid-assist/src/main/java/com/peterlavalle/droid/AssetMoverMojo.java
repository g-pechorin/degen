package com.peterlavalle.droid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Copies all "things" from "selected" "provided" dependencies to a resources directory (attaching them to the build) except for things matching an "assets creteria" which are copies to an "assets directory"
 * 
 * Certain things (in an android build) have to be packaged as assets to work "everywhere" (so far - sounds are the only example I know of this)
 * 
 * The easiest way I can think of to perform cross-platform builds which accomodate this quirk is to (1) declare the shared code as "provided" and (2) rely on this plugin to split apart shared code into assets and not-assets.
 * 
 * This plugin will ignore "some" managemtn keys (by default com\.google\.android::.*)
 * 
 * 
 * @goal assets
 * @phase compile
 * @version $Id$
 */
public class AssetMoverMojo extends AbstractMojo {

	// =============================
	// -- Maven stuff
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
	//
	// ===========================================
	// -- Config stuff
	/**
	 * the directory to output the copied resources to
	 *
	 * @parameter expression="${droid.resourcesDirectory}" default-value="${project.build.directory}/droid/copied-resources"
	 * @required
	 */
	private String resourcesDirectory;
	/**
	 * the directory to output the copied assets to
	 *
	 * @parameter expression="${droid.assetsDirectory}" default-value="${project.build.directory}/droid/copied-assets"
	 * @required
	 */
	private String assetsDirectory;
	/**
	 * @parameter expression="${droid.assetsCriteria}" default-value=".*\.(mp3|ogg|wav)"
	 * @required
	 */
	private String assetsCriteria;
	
	/**
	 * @parameter expression="${droid.skipCriteria}" default-value="com\.google\.android::.*"
	 * @required
	 */
	private String skipCriteria;
	
	/**
	 * @parameter expression="${droid.includeCriteria}" default-value=.*::compile"
	 * @required
	 */
	private String includeCriteria;
	
	
	//
	// ======================
	// -- Accessors

	public File getAssetsDirectory() {
		return new File(assetsDirectory);
	}

	public File getResourcesDirectory() {
		return new File(resourcesDirectory);
	}

	public String getSkipCriteria() {
		return skipCriteria;
	}

	public String getAssetsCriteria() {
		return assetsCriteria;
	}

	//
	// ================================
	// -- Actual body methods
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// setup the folders
		projectHelper.addResource(project, getResourcesDirectory().getPath(), null, null);


		// all keys that have been visited
		final HashSet<String> visitedKeys = new HashSet<String>();

		// for each dependency
		for (final Dependency dependency : (List<Dependency>) this.project.getDependencies()) {

			// - execute on it
			execute(visitedKeys, dependency);
		}
	}

	public static String calcDependencyKey(Dependency dependency) {
		return new StringBuilder(dependency.getGroupId())//
				.append("::").append(dependency.getArtifactId())//
				.append("::").append(dependency.getVersion())//
				.append("::").append(dependency.getClassifier())//
				.append("::").append(dependency.getScope() == null || dependency.getScope().equals("") ? "compile" : dependency.getScope())//
				.toString();
	}

	private void execute(Set<String> visitedKeys, Dependency dependency) throws MojoExecutionException {

		// check to see if we should skipit
		{
			final String managementKey = calcDependencyKey(dependency);

			// check if we should ship it
			if (managementKey.matches(getSkipCriteria())) {
				getLog().info("Skipping " + managementKey + "` since it matches the skip criteria");
				return;
			}
			
			// be sure we should add it
			if (!managementKey.matches(includeCriteria)) {
				getLog().info("Skipping " + managementKey + "` since it does not match the include criteria");
				return;
			}
			
			// skip ones that we've already done
			if (!visitedKeys.add(managementKey)) {
				return;
			}

			getLog().info(getClass().getSimpleName() + " is processing " + managementKey);
		}

		// write out the files
		{
			final File dependencyFile = getDependencyFile(dependency);
			final ZipFile dependencyZipFile = getDependencyZipFile(dependencyFile);
			for (final ZipEntry entry : Collections.list(dependencyZipFile.entries())) {

				// get the file to write out
				final File output = new File(entry.getName().matches(getAssetsCriteria()) ? getAssetsDirectory() : getResourcesDirectory(), entry.getName());

				// check ALL the things
				if ( //
						entry.getTime() != -1 //
						&& output.exists() //
						&& output.length() == entry.getSize()
						&& output.lastModified() > entry.getTime() //
						&& output.lastModified() > dependencyFile.lastModified()) {
					continue;
				}

				// copy the file
				extractFile(output, dependencyZipFile, entry);

			}
		}

		// do all of the dependency's depedencies
		throw new UnsupportedOperationException("Not yet implemented - do all of the dependency's depedencies");
	}

	public File getDependencyFile(Dependency dependency) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public ZipFile getDependencyZipFile(File dependencyFile) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void extractFile(File output, ZipFile zipFile, ZipEntry entry) throws MojoExecutionException {

		output.getParentFile().mkdirs();

		final FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(output);
		} catch (FileNotFoundException ex) {
			throw new MojoExecutionException("Couldn't write-open the file `" + output + "`", ex);
		}

		final InputStream inputStream;
		try {
			inputStream = zipFile.getInputStream(entry);
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to read-open `" + entry + "` @ `" + zipFile + "`", ex);
		}

		final byte[] buffer = new byte[128];
		try {
			for (int i = 0; inputStream.read(buffer) != -1;) {
				try {
					outputStream.write(buffer, 0, i);
				} catch (IOException ex) {
					throw new MojoExecutionException("While trying to write from `" + entry + "` @ `" + zipFile + "` to `" + output + "`", ex);
				}
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to read from `" + entry + "` @ `" + zipFile + "`", ex);
		}

		try {
			inputStream.close();
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to close my handle to `" + entry + "` @ `" + zipFile + "`", ex);
		}
		try {
			outputStream.close();
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying to close my handle to the file `" + output + "`", ex);
		}
	}
}
