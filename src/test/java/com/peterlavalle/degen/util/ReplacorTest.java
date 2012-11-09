/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.util;

import com.peterlavalle.degen.extractors.util.Replacor;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Peter LaValle
 */
public class ReplacorTest extends TestCase {

	public void testBasic() {
		final Replacor replacor = new Replacor("{left}");

		assertEquals("left", replacor.pattern);
		assertEquals("$0", replacor.replacement);
	}

	public void testComplex() {
		final Replacor replacor = new Replacor("{left@right}");

		assertEquals("left", replacor.pattern);
		assertEquals("right", replacor.replacement);
	}

	public void testSpaces() {
		final Replacor replacor = new Replacor(" {le ft@right  }  ");

		assertEquals("le ft", replacor.pattern);
		assertEquals("right  ", replacor.replacement);
	}

	public void testExtracAll() {
		fail("not implemented");

		final String body = "<foo> sakjhfdsakjhas<<bar>> jkhadshj<goo<sad>>";
		final Replacor replacor = new Replacor("{<(.*)>@$0:$1}");

		assertEquals(Arrays.asList("<foo>:foo", "<<bar>>:<bar>", "<bar>:bar", "<goo<sad>>:goo<sad>", "<sad>:sad"), new String[0]);
	}
}
