package com.peterlavalle.degen;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @goal folder
 * @phase generate-sources
 */
public class FolderDegen extends AbstractDegen {

    /**
     * @parameter expression="target/degen-extracted/src"
     * @required
     */
    protected File extractedSources;
    /**
     * @parameter expression="target/degen-extracted/bin"
     * @required
     */
    protected File extractedBinaries;


    @Override
    public List<String> getExtractedSourceNames() {
        return Util.getFiles(extractedSources);
    }

    @Override
    public List<String> getExtractedBinaryNames() {
        return Util.getFiles(extractedBinaries);
    }

    @Override
    public void copySource(final String file) throws MojoExecutionException {
        // now copy the file to the generated sources
        getLog().info("Copying `" + file + "` from extractedSources");
        Util.copyFile(file, extractedSources, generatedSources);
    }

    @Override
    public void copyResource(final String file) throws MojoExecutionException {
        getLog().info("Copying `" + file + "` from extractedBinaries");
        Util.copyFile(file, extractedBinaries, generatedBinaries);
    }
}