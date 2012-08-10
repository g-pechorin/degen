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
 * @goal remote
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class RemoteDegen extends AbstractDegen {

    /**
     * URL (possibly HTTP://) to the distribution's zip file
	 * 
     * @parameter expression="${degen.distributionZipURL}"
     * @required
     */
    protected String distributionZipURL;
    /**
     * Path within the distribution's zip file to the source zip file we want
     *
     * @parameter expression="${degen.sourcesPath}"
     * @required
     */
    protected String sourcesPath;
    /**
     * Path within the distribution's zip file to the binary zip file we want
     *
     * @parameter expression="${degen.binariesPath}"
     * @required
     */
    protected String binariesPath;
    private ZipFile distributionZipFile;
    private ZipFile binariesZipFile;
    private ZipFile sourcesZipFile;

    protected ZipFile getDistributionFile() throws MojoExecutionException {
		
		if ( distributionZipURL == null ||distributionZipURL.equals("")) {
			throw new IllegalArgumentException();
		}
		
        if (this.distributionZipFile == null) {
            final File temporaryFileFromUrl;
            try {
                temporaryFileFromUrl = Util.getTemporaryFileFromUrl(distributionZipURL);
            } catch (IOException e) {
                throw new MojoExecutionException("couldn't download the file `" + distributionZipURL + "`", e);
            }
            try {
                this.distributionZipFile = new ZipFile(temporaryFileFromUrl);
            } catch (IOException e) {
                throw new MojoExecutionException("couldn't read the file from `" + distributionZipURL + "`", e);
            }
        }

        return this.distributionZipFile;
    }

    @Override
    public void execute() throws MojoExecutionException {
        distributionZipFile = null;
        binariesZipFile = null;
        sourcesZipFile = null;
		
        super.execute();
    }

    public ZipFile getSourcesZipFile() throws MojoExecutionException, IOException {
        return sourcesZipFile == null ? sourcesZipFile = new ZipFile(Util.getTemporaryFileFromZip(getDistributionFile(), sourcesPath)) : sourcesZipFile;
    }

    public ZipFile getBinariesZipFile() throws MojoExecutionException, IOException {
        return binariesZipFile == null ? binariesZipFile = new ZipFile(Util.getTemporaryFileFromZip(getDistributionFile(), binariesPath)) : binariesZipFile;
    }

    @Override
    protected List<String> getExtractedSourceNames() throws MojoExecutionException {
        try {
            return Util.getFiles(getSourcesZipFile());
        } catch (IOException e) {
            throw new MojoExecutionException("I can't find your sources", e);
        }
    }

    @Override
    protected List<String> getExtractedBinaryNames() throws MojoExecutionException {
        try {
            return Util.getFiles(getBinariesZipFile());
        } catch (IOException e) {
            throw new MojoExecutionException("I can't find your resources", e);
        }
    }

    @Override
    protected void copySource(String file) throws MojoExecutionException {
        try {
            Util.copyFile(file, getSourcesZipFile(), this.generatedSources);
        } catch (IOException ex) {
            throw new MojoExecutionException("copySource(" + file + ")", ex);
        }
    }

    @Override
    protected void copyResource(String file) throws MojoExecutionException {
        try {
            Util.copyFile(file, getBinariesZipFile(), this.generatedBinaries);
        } catch (IOException ex) {
            throw new MojoExecutionException("copySource(" + file + ")", ex);
        }
    }
}
