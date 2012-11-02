package com.peterlavalle.droid;

import com.google.common.collect.Lists;
import com.peterlavalle.droid.files.DemiFile;
import java.io.File;
import java.util.LinkedList;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Copies all "things" from classes/ and "compile" dependencies to a target/assets/ folder for Android builds.
 * 
 * @goal assets
 * @phase compile
 * @version $Id$
 */
public class AssetMoverMojo extends AbstractDroidMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// start with the classes directory
		final LinkedList<DemiFile> files = Lists.newLinkedList(DemiFile.listFiles(getProject().getBuild().getDirectory() + '/' + getClassesFolder()));

		// add in dependency jars
		for (final DefaultArtifact dependency : (Iterable<DefaultArtifact>) getProject().getDependencyArtifacts()) {

			// we only care about compile jars
			if (!dependency.getScope().equals("compile")) {
				continue;
			}

			// add the guts of the zip
			files.addAll(Lists.newArrayList(DemiFile.listFiles(dependency.getFile())));
		}

		// process all demi files
		while (!files.isEmpty()) {
			final DemiFile file = files.removeFirst();

			// scan directories and moveon
			{
				final Iterable<DemiFile> listFiles = file.listFiles();

				if (listFiles != null) {
					files.addAll(Lists.newArrayList(listFiles));
					continue;
				}
			}

			// skip non-asset files
			if ( !file.getName().matches(getAssetsCriteria()) ) {
				continue;
			}
			
			// talk about any file that we add
			getLog().info("Copying asset " + file.getName());;
			
			file.copyTo( getAssetsFolder());
		}
	}
}
