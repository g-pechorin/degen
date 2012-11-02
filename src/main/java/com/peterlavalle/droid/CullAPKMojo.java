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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author Peter LaValle
 */
public class CullAPKMojo extends AbstractDroidMojo {
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final ZipFile apkFile = getApkZipFile();



		// start a new zip output file (on max compression)
		final File tempFile;
		final ZipOutputStream zipOutputStream;
		try {
			tempFile = File.createTempFile("temp-droid-cull", ".zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(tempFile));
			zipOutputStream.setLevel(9);
		} catch (IOException ex) {
			throw new MojoExecutionException("Problem while trying to open temporary file", ex);
		}
		
		for (final ZipEntry entry : Util.toIterable(apkFile.entries())) {
			if (!new File(getAssetsFolder(), entry.getName()).exists() && entry.getName().matches(getAssetsCriteria()) && apkFile.getEntry("assets/" + entry.getName()) != null) {
				try {
					// put the entry
					zipOutputStream.putNextEntry(new ZipEntry(entry.getName()));
				} catch (IOException ex) {
					throw new MojoExecutionException("Problem putting entry for `" + entry.getName() + "`", ex);
				}

				// open the input stream
				final InputStream inputStream;
				try {
					inputStream = apkFile.getInputStream(entry);
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
				
				getLog().info("I removed the duplicate `" + entry.getName() + "`");
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
