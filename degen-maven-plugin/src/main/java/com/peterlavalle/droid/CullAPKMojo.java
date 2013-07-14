/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.droid;

import com.peterlavalle.util.ByYourCommand;
import com.peterlavalle.util.Callback;
import com.peterlavalle.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Peter LaValle
 * @version $Id$
 * @goal cull
 */
public class CullAPKMojo extends AbstractDroidMojo {

	/**
	 * @parameter expression="${env.ANDROID_HOME}/platform-tools/aapt"
	 * @required
	 */
	private File aapt;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		final ZipFile apkZipFile = getApkZipFile();

		final ByYourCommand lineCommand = new ByYourCommand(aapt.getParentFile(), aapt.getName());

		lineCommand.addArgument("r");
		lineCommand.addArgument(getBuiltFile().getAbsolutePath());

		int count = 0;

		for (final ZipEntry entry : Util.toIterable(apkZipFile.entries())) {

			final String name = entry.getName();

			// determin if it's a duplicate
			if (name.matches(getAssetsCriteria()) && apkZipFile.getEntry("assets/" + name) != null) {

				getLog().info("I will remove the duplicate `" + entry.getName() + "` from the apk");

				lineCommand.addArgument(name);
				count++;
			}
		}

		if (count == 0) {
			getLog().info("There are no duplicates for me to remove");
		} else {
			getLog().info("That's " + count + " files I'm going to remove as duplicates");

			// setup the streams
			lineCommand.pipeErrorTo(new Callback.StringCallbackOutputStream(new Callback<String>() {
				@Override
				public void callback(final String toString) {
					if (!toString.equals("")) {
						getLog().error(toString);
					}
				}
			}));
			lineCommand.pipeOutputTo(new Callback.StringCallbackOutputStream(new Callback<String>() {
				@Override
				public void callback(final String toString) {
					if (!toString.equals("")) {
						getLog().info(toString);
					}
				}
			}));

			final int result = lineCommand.run();

			if (result != 0) {
				throw new MojoExecutionException("Command result=" + result);
			}
		}
	}
}
