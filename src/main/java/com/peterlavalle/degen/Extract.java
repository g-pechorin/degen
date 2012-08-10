package com.peterlavalle.degen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @goal extract
 * @phase generate-sources
 */
public class Extract extends AbstractMojo {

    /**
     * @parameter expression="target/degen-extracted/bin"
     * @required
     */
    protected File extractedBinaries;
    /**
     * @parameter expression="target/degen-extracted/src"
     * @required
     */
    protected File extractedSources;
    /**
     * @required
     */
    protected File zippedSources;
    /**
     * @required
     */
    protected File zippedBinaries;

    @Override
    public void execute() throws MojoExecutionException {

        if (zippedSources.exists()) {
            extractedSources.delete();
            extractedSources.mkdirs();
            
            Util.extractZip(zippedSources, extractedSources);
        }

        if (zippedBinaries.exists()) {
            extractedBinaries.delete();
            extractedBinaries.mkdirs();
            
            Util.extractZip(zippedBinaries, extractedBinaries);
        }
    }
}
