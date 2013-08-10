package com.peterlavalle.droid;

import com.peterlavalle.degen.AbstractDegenMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * @author Peter LaValle
 */
public abstract class AbstractDroidMojo extends AbstractDegenMojo {


	protected ZipFile getApkZipFile() throws MojoExecutionException {
		final File apkFile = getBuiltFile();
		try {
			return new ZipFile(apkFile);
		} catch (IOException ex) {
			throw new MojoExecutionException("While trying top open zip file `" + apkFile + "`", ex);
		}
	}
}
