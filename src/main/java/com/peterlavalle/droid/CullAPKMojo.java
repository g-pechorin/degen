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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		final ZipFile apkZipFile = getApkZipFile();

		// start a new zip output file (on max compression)
		final File tempFile;
		final ZipOutputStream zipOutputStream;
		try {
			tempFile = File.createTempFile("temp-droid-cull", ".zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(tempFile));
		} catch (IOException ex) {
			throw new MojoExecutionException("Problem while trying to open temporary file", ex);
		}

		for (final ZipEntry entry : Util.toIterable(apkZipFile.entries())) {


			final String name = entry.getName();
			final boolean isAsset = name.startsWith("assets/") && name.substring("assets/".length()).matches(getAssetsCriteria());

			// determin if it's a duplicate
			if (name.matches(getAssetsCriteria()) && apkZipFile.getEntry("assets/" + name) != null) {
				getLog().info("I removed the duplicate `" + entry.getName() + "`");
				continue;
			}

			// set the compression level (assets can't be compressed see - http://ponystyle.com/blog/2010/03/26/dealing-with-asset-compression-in-android-apps/)
			zipOutputStream.setLevel(isAsset ? 0 : 9);

			// put the entry
			try {
				zipOutputStream.putNextEntry(new ZipEntry(entry.getName()));
			} catch (IOException ex) {
				throw new MojoExecutionException("Problem putting entry for `" + entry.getName() + "`", ex);
			}

			// open the input stream
			final InputStream inputStream;
			try {
				inputStream = apkZipFile.getInputStream(entry);
			} catch (IOException ex) {
				throw new MojoExecutionException("Problem opening entry `" + entry.getName() + "`", ex);
			}
			try {
				// copy the streams
				Util.copyStream(inputStream, zipOutputStream);
			} catch (IOException ex) {
				throw new MojoExecutionException("Problem copying entry `" + entry.getName() + "`", ex);
			}

			// close the entry
			try {
				zipOutputStream.closeEntry();
			} catch (IOException ex) {
				throw new MojoExecutionException("Problem closing entry `" + entry.getName() + "`", ex);
			}
		}
		try {
			zipOutputStream.close();
		} catch (IOException ex) {
			throw new MojoExecutionException("IOException while trying to close the zipfile", ex);
		}

		// copy the old apk file to "original"
		getLog().warn("Forgot to copy original...");

		// copy the new zip file as the apk file
		try {
			Util.copyStream(new FileInputStream(tempFile), new FileOutputStream(getApkFile())).close();
		} catch (FileNotFoundException ex) {
			throw new MojoExecutionException("Inexplicable FNF exception", ex);
		} catch (IOException ex) {
			throw new MojoExecutionException("IOException while trying to finish the droid:cull", ex);
		}
	}
}
