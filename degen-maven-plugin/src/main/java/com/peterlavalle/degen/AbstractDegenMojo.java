package com.peterlavalle.degen;

import com.google.inject.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Created with IntelliJ IDEA.
 * User: peter
 * Date: 13/07/13
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractDegenMojo extends AbstractMojo implements Module {

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
	private Injector injector = null;

	@Override
	public void configure(Binder binder) {
		binder.bind(MavenProject.class).toProvider(new Provider<MavenProject>() {
			@Override
			public MavenProject get() {
				return project;
			}
		});
		binder.bind(MavenProjectHelper.class).toProvider(new Provider<MavenProjectHelper>() {
			@Override
			public MavenProjectHelper get() {
				return projectHelper;
			}
		});

		binder.requestInjection(this);
	}

	protected Injector getInjector() {
		if (injector == null) {
			Guice.createInjector(this);
		}

		return injector;
	}

}
