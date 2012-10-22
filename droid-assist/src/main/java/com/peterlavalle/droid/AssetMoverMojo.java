package com.peterlavalle.droid;

import com.google.common.collect.Lists;
import com.peterlavalle.droid.files.DemiFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.DefaultArtifact;
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
	/**
	 * @parameter expression="${droid.assetsCriteria}" default-value=".*\.(mp3|ogg|wav)"
	 * @required
	 */
	private String assetsCriteria;
	/**
	 * @parameter expression="${droid.target}" default-value="classes"
	 * @required
	 */
	private String classesFolder;

	//
	// ================================
	// -- Actual body methods
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// start with the classes directory
		final LinkedList<DemiFile> files = Lists.newLinkedList(DemiFile.listFiles(project.getBuild().getDirectory() + '/' + classesFolder));

		
		// add in dependency jars
		for (final DefaultArtifact dependency : (Iterable<DefaultArtifact>) project.getDependencyArtifacts()) {

			// we only care about compile jars
			if (!dependency.getScope().equals("compile")) {
				continue;
			}

			// add the guts of the zip
			files.addAll(Lists.newArrayList(DemiFile.listFiles(dependency.getFile())));
			
			getLog().error("TODO : Can I remove the dependency?");
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
			if ( !file.getName().matches(assetsCriteria) ) {
				continue;
			}
			
			// talk about any file that we add
			getLog().info("OTTHNOI : assetize " + file.getName());;

			getLog().error("TODO : You need to actually copy the file!");
		}
	}
}
