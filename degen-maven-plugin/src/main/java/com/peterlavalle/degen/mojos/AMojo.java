/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.util.Map;

/**
 * @author Peter LaValle
 */
public abstract class AMojo extends AbstractMojo {

	static boolean projectHasPluginKey(final MavenProject project, final String name) {

		final Map pluginArtifactMap = project.getPluginArtifactMap();

		for (Object key : pluginArtifactMap.keySet()) {

			if (key.toString().equals(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Which files are sources?
	 *
	 * @parameter expression="${source_filter}" default-value=".*\.java$"
	 * @required
	 */
	private String source_filter;

	public String getSourceFilter() {
		return source_filter;
	}

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
	 * Where to put the generated source
	 *
	 * @parameter expression="${gensource_folder}" default-value="${project.build.directory}/generated-sources/degen-sources"
	 * @required
	 */
	private String gensource_folder;

	public String getGeneratedSources() {
		return gensource_folder;
	}

	public File getGeneratedSourcesFile() {
		return new File(gensource_folder);
	}

	/**
	 * Where to put the generated resource
	 *
	 * @parameter expression="${genresource_folder}" default-value="${project.build.directory}/generated-sources/degen-resources"
	 * @required
	 */
	private String genresource_folder;

	public String getGeneratedResources() {
		return genresource_folder;
	}

	public File getGeneratedResourcesFile() {
		return new File(genresource_folder);
	}

	/**
	 * @return the project
	 */
	public MavenProject getProject() {
		return project;
	}

	/**
	 * @return the projectHelper
	 */
	public MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}
}
