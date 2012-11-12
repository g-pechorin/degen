/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.mojos;

import com.peterlavalle.degen.extractors.util.Files;
import com.peterlavalle.degen.extractors.util.Replacor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 *
 * @author Peter LaValle
 * @goal hgscrape
 * @phase generate-sources
 * @version $Id$
 */
public class HgScrapeMojo extends AbstractMojo {

	// TODO : configure this from POM
	private String url = "http://hg.icculus.org/icculus/mojoshader/file/4363cfdb85b9";

	public URL getURL() throws MojoExecutionException {
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			throw new MojoExecutionException(url, ex);
		}
	}
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
	//
	// TODO : configure this from POM
	private Replacor SUB_DIR = new Replacor("{<a href=\"(.+/([^/]+))\">files</a>@$2}");
	//
	// TODO : configure this from POM
	private Replacor SUB_FILE = new Replacor("{<a class=\"list\" href=\"(.+)\">([^<]+)</a>@$2|$1}");
	//
	// TODO : configure this from POM
	private String filter_source = "{.*\\.java@$0}";

	public Replacor getFilterSource() {
		return new Replacor(filter_source);
	}
	/**
	 * Where to put the generated source
	 *
	 * @parameter expression="${gensource_folder}" default-value="${project.build.directory}/hgscrape/gensource_folder"
	 * @required
	 */
	public File gensource_folder;

	private void execute(final File cacheDir, final URL url, final String sub_path) throws MojoExecutionException {
		final URL rootUrl = getURL();

		// cache the directory's file into a string
		final String fileString;
		try {
			fileString = new String(Files.copyStream(url.openStream(), new ByteArrayOutputStream()).toByteArray());
		} catch (IOException ex) {
			throw new MojoExecutionException("", ex);
		}

		// download all files
		for (final String subFile : SUB_FILE.extractAll(fileString)) {
			try {

				// get the URL to download from
				final URL downloaded_file = new URL(rootUrl.toString().replace(rootUrl.getPath(), subFile.split("\\|")[1]));

				// get the name that we'll download to
				final String name = sub_path + subFile.split("\\|")[0];

				// TODO : check if it's a resource or source
				final File parent = null;

				// perform the download
				Files.copyStream(downloaded_file.openStream(), new File(parent, name));
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}
		}



		// recur
		for (final String subDir : SUB_DIR.extractAll(fileString)) {
			try {

				execute(cacheDir, new URL(url.toString() + '/' + subDir), sub_path + subDir + File.pathSeparator);
				
			} catch (IOException ex) {
				throw new MojoExecutionException("", ex);
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException {

		execute(new File(project.getBuild().getDirectory()), getURL(), "");

		if (true) {
			return;
		}

		final File file;
		try {
			file = Files.cacheFile(new File(project.getBuild().getDirectory()), getURL());
			file.deleteOnExit();
		} catch (IOException ex) {
			throw new MojoExecutionException("", ex);
		}



		// get a string of the file
		final String fileString;
		try {
			fileString = Files.openFileAsString(file);
		} catch (IOException ex) {
			throw new MojoExecutionException("", ex);
		}

		// recursion will be needed from here ...
		for (final String subDir : SUB_DIR.extractAll(fileString)) {
			getLog().info(">> subDir=`" + subDir + "`");
			getLog().info(">> url=`" + getURL().toString().replace(getURL().getPath(), subDir.split("\\|")[1]) + "`");

		}

		for (final String subFile : SUB_FILE.extractAll(fileString)) {
			getLog().info(">> subFile=`" + subFile + "`");
			getLog().info(">> url=`" + getURL().toString().replace(getURL().getPath(), subFile.split("\\|")[1]) + "`");
		}

	}
}
