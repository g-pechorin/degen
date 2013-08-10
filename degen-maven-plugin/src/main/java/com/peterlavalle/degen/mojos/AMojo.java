/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;

import com.peterlavalle.degen.AbstractDegenMojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Map;

/**
 * @author Peter LaValle
 */
public abstract class AMojo extends AbstractDegenMojo {

	/**
	 * Where to put the generated source
	 *
	 * @parameter default-value="${project.build.directory}/generated-sources/degen-sources"
	 * @required
	 */
	private File generatedSourcesDirectory;
	/**
	 * Where to put the generated resource
	 *
	 * @parameter default-value="${project.build.directory}/generated-sources/degen-resources"
	 * @required
	 */
	private File generatedResourcesDirectory;

	static boolean projectHasPluginKey(final MavenProject project, final String name) {

		final Map pluginArtifactMap = project.getPluginArtifactMap();

		for (Object key : pluginArtifactMap.keySet()) {

			if (key.toString().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public File getGeneratedSourcesDirectory() {
		if (!generatedSourcesDirectory.exists()) generatedSourcesDirectory.mkdirs();
		return generatedSourcesDirectory;
	}

	public File getGeneratedResourcesDirectory() {
		if (!generatedResourcesDirectory.exists()) generatedResourcesDirectory.mkdirs();
		return generatedResourcesDirectory;
	}
}
