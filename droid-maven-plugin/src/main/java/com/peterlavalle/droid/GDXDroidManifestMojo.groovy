package com.peterlavalle.droid

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper;
/**
 *
 * @author Peter LaValle
 * @goal gdx-mainfest
 * @version $Id$
 */
class GDXDroidManifestMojo extends AbstractMojo {
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
     * @parameter expression="${gdx.manifest}" default-value="${project.build.directory}/AndroidManifest.xml"
     * @required
     */
    private String assetsFolder;

    /**
     * @parameter expression="${gdx.wakelock}" default-value="false"
     * @required
     */
    private boolean requestWakeLock;

    /**
     * @parameter expression="${gdx.listener}"
     * @required
     */
    private String appListener;

    bits to generate the res directory

    execute which fires all of the templates
}
