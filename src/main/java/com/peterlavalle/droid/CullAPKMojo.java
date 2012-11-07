/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.droid;

import com.peterlavalle.util.ByYourCommand;
import com.peterlavalle.util.Callback;
import com.peterlavalle.util.Util;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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

		final ByYourCommand lineCommand = new ByYourCommand(new File(androidHome, "platform-tools/"), "aapt");

		lineCommand.addArgument("r");
		lineCommand.addArgument(getBuiltFile().getAbsolutePath());

		for (final ZipEntry entry : Util.toIterable(apkZipFile.entries())) {


			final String name = entry.getName();


			// determin if it's a duplicate
			if (name.matches(getAssetsCriteria()) && apkZipFile.getEntry("assets/" + name) != null) {

				getLog().info("I will remove the duplicate `" + entry.getName() + "` from the apk");

				lineCommand.addArgument(name);
			}
		}

		// setup the streams
		lineCommand.pipeErrorTo(new Callback.StringCallbackOutputStream(new Callback<String>() {
			@Override
			public void callback(final String toString) {
				getLog().error(toString);
			}
		}));
		lineCommand.pipeOutputTo(new Callback.StringCallbackOutputStream(new Callback<String>() {
			@Override
			public void callback(final String toString) {
				getLog().info(toString);
			}
		}));

		final int result = lineCommand.run();

		if (result != 0) {
			throw new MojoExecutionException("Command result=" + result);
		}
	}
}
