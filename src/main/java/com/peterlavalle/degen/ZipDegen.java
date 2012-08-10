/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @goal zip
 * @phase generate-sources
 */
public class ZipDegen extends AbstractDegen {

    /**
     * @required
     */
    protected File zippedSources;
    /**
     * @required
     */
    protected File zippedBinaries;

    @Override
    public List<String> getExtractedSourceNames() throws MojoExecutionException {
        try {
            return Util.getFiles(new ZipFile(zippedSources));
        } catch (IOException e) {
            throw new MojoExecutionException("I can't find your sources", e);
        }
    }

    @Override
    public List<String> getExtractedBinaryNames() throws MojoExecutionException {
        try {
            return Util.getFiles(new ZipFile(zippedBinaries));
        } catch (IOException e) {
            throw new MojoExecutionException("I can't find your resources", e);
        }
    }

    @Override
    public void copySource(final String file) throws MojoExecutionException {
        try {

            // now copy the file to the generated sources
            getLog().info("Extracting source `" + file + "` from " + zippedSources);
            Util.copyFile(file, new ZipFile(zippedSources), generatedSources);

        } catch (IOException ex) {
            throw new MojoExecutionException("Problem in copySource(" + file + ")", ex);
        }
    }

    @Override
    public void copyResource(final String file) throws MojoExecutionException {
        try {

            // copy the binary to wherever
            getLog().info("Extracting binary `" + file + "` from " + zippedBinaries);
            Util.copyFile(file, new ZipFile(zippedBinaries), generatedBinaries);

        } catch (IOException ex) {
            throw new MojoExecutionException("Problem in copySource(" + file + ")", ex);
        }
    }
}