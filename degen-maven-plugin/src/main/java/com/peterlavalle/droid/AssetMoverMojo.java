package com.peterlavalle.droid;

import com.peterlavalle.droid.files.DemiFile;
import com.peterlavalle.util.Util;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * Copies all "things" from classes/ and "compile" dependencies to a target/assets/ folder for Android builds.
 *
 * @version $Id$
 * @goal assets
 */
public class AssetMoverMojo extends AbstractDroidMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// start with the classes directory
		final List<DemiFile> files = Util.asList(DemiFile.listFiles(getProject().getBuild().getDirectory() + '/' + getClassesFolder()));

		// add in dependency jars
		for (final DefaultArtifact dependency : (Iterable<DefaultArtifact>) getProject().getDependencyArtifacts()) {

			if (dependency == null) {
				getLog().error("A null dependency worked its way in");
			}

			// we only care about compile jars
			final String scope = dependency.getScope();
			if (scope != null && !scope.equals("compile")) {
				continue;
			}

			// add the guts of the zip
			files.addAll(Util.asList(DemiFile.listFiles(dependency.getFile())));
		}

		// process all demi files
		while (!files.isEmpty()) {
			final DemiFile file = files.remove(0);

			if (file == null) {
				getLog().error("A null file worked its way into an array");
			}

			// scan directories and moveon
			{
				final Iterable<DemiFile> listFiles = file.listFiles();

				if (listFiles != null) {
					files.addAll(Util.asList(listFiles));
					continue;
				}
			}

			// skip non-asset files
			if (!file.getName().matches(getAssetsCriteria())) {
				continue;
			}

			// talk about any file that we add
			getLog().info("Copying asset " + file.getName());

			file.copyTo(getAssetsFolder());
		}

		getLog().debug("assets copies are done");
	}
}
