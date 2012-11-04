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
import java.io.OutputStream;
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

		final LineCommand lineCommand = newLineCommand(new File(androidHome, "platform-tools/"), "aapt");

		lineCommand.addArgument("r");
		lineCommand.addArgument(getBuiltFile().getAbsolutePath());

		for (final ZipEntry entry : Util.toIterable(apkZipFile.entries())) {


			final String name = entry.getName();


			// determin if it's a duplicate
			if (name.matches(getAssetsCriteria()) && apkZipFile.getEntry("assets/" + name) != null) {


				getLog().info("I will remove the duplicate `" + entry.getName() + "`");

				lineCommand.addArgument(name);
			}
		}

		// setup the streams
		lineCommand.pipeErrorTo(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		lineCommand.pipeOutputTo(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		lineCommand.pipeInputFrom(new InputStream() {

			@Override
			public int read() throws IOException {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		
		lineCommand.run();
	}
}
