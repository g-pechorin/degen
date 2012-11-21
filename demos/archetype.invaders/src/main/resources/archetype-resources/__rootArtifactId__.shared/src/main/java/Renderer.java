#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.simulation.Simulation;

public interface Renderer {
	public void render(Simulation sim, float delta);
	public void dispose();
}
