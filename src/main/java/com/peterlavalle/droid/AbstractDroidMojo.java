package com.peterlavalle.droid;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 *
 * @author Peter LaValle
 */
public abstract class AbstractDroidMojo extends AbstractMojo {

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
	/**
	 * @parameter expression="${droid.assetsCriteria}" default-value=".*\.(mp3|ogg|wav)"
	 * @required
	 */
	private String assetsCriteria;
	/**
	 * @parameter expression="${droid.classes}" default-value="classes"
	 * @required
	 */
	private String classesFolder;
	/**
	 * @parameter expression="${droid.assets}" default-value="assets"
	 * @required
	 */
	private String assetsFolder;

	protected MavenProject getProject() {
		return project;
	}

	protected MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}

	protected String getAssetsCriteria() {
		return assetsCriteria;
	}

	protected String getClassesFolder() {
		return classesFolder;
	}

	protected File getAssetsFolder() {
		return new File(getProject().getBuild().getDirectory(), assetsFolder);
	}

	protected File getApkFile() throws MojoExecutionException {
		return new File(getProject().getBuild().getDirectory(), getProject().getBuild().getFinalName() + '.' + getProject().getPackaging());
	}

	protected ZipFile getApkZipFile() throws MojoExecutionException {
		final File apkFile = getApkFile();
		try {
			return new ZipFile(apkFile);
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying top open zip file `" + apkFile + "`", ex);
		}
	}
}
