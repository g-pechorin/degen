package com.peterlavalle.droid;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 *
 * @author Peter LaValle
 */
public abstract class DroidAbstractMojo extends AbstractMojo {
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

	protected String getAssetsFolder() {
		return assetsFolder;
	}

}
