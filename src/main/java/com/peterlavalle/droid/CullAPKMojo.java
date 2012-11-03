/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.droid;

import com.peterlavalle.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 *
 * @author Peter LaValle
 * @goal cull
 * @phase package
 * @version $Id$
 */
public class CullAPKMojo extends AbstractDroidMojo {

	/**
	 * @parameter expression="${env.ANDROID_HOME}"
	 * @required
	 */
	private File androidHome;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		final ZipFile apkZipFile = getApkZipFile();

		final File aapt = new File(androidHome, "platform-tools/aapt.exe");

		final LinkedList<String> removeables = new LinkedList<String>();

		for (final ZipEntry entry : Util.toIterable(apkZipFile.entries())) {


			final String name = entry.getName();


			// determin if it's a duplicate
			if (name.matches(getAssetsCriteria()) && apkZipFile.getEntry("assets/" + name) != null) {


				getLog().info("I will remove the duplicate `" + entry.getName() + "`");

				removeables.add(name);
			}
		}
		
		throw new MojoExecutionException("You must run the command: \n\n\taapt r \"" + getBuiltFile().getAbsolutePath() + " " + removeables.toString().replaceAll("[\\[\\],]", "")+"\n");
	}
}
