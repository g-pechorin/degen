package com.peterlavalle.degen;

import com.google.inject.Inject;
import com.peterlavalle.degen.cmake.CScanner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @author Peter LaValle
 * @version $Id$
 * @goal com.peterlavalle.degen.cmake-generate
 * @phase generate-sources
 */
public class CMakeGenerateMojo extends AbstractDegenMojo {


	/**
	 * @parameter expression="${project.build.directory}/com.peterlavalle.degen.cmake"
	 * @required
	 */
	private File cmake;
	/**
	 * @parameter expression=".+\.(c|cpp)$"
	 * @required
	 */
	private String sourcePattern;
	@Inject
	private CScanner scanner = null;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getInjector();

		scanner.apply(new File(cmake, "CMakeLists.txt"), sourcePattern);
	}
}
