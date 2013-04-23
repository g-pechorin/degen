/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.util;

import com.peterlavalle.degen.extractors.util.Replacor;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Peter LaValle
 */
public class ReplacorTest extends TestCase {

	public void testBuildReplacors() {
		final List<Replacor> replacors = Replacor.buildReplacors("{.@$0}{das} {423}-{asd} {p} -{o}");

		assertEquals(6, replacors.size());

		assertEquals(new Replacor("{.@$0}"), replacors.get(0));
		assertTrue(replacors.get(0).includes);

		assertEquals(new Replacor("{das}"), replacors.get(1));
		assertTrue(replacors.get(1).includes);

		assertEquals(new Replacor("{423}"), replacors.get(2));
		assertTrue(replacors.get(2).includes);

		assertEquals(new Replacor("-{asd}"), replacors.get(3));
		assertFalse(replacors.get(3).includes);

		assertEquals(new Replacor("{p}"), replacors.get(4));
		assertTrue(replacors.get(4).includes);

		assertEquals(new Replacor("-{o}"), replacors.get(5));
		assertFalse(replacors.get(5).includes);
	}

	public void testFromLibGDX() {
		final Replacor.ReplacorList replacorList = Replacor.buildReplacors("{.*java} -{.*(GdxBuild|GwtModuleGenerator|NumberUtils|GdxRuntimeException).*}");

		// should not match
		assertNull(replacorList.apply("com/badlogic/gdx/utils/GdxBuild.java"));
		assertNull(replacorList.apply("com/badlogic/gdx/utils/GdxRuntimeException.java"));
		assertNull(replacorList.apply("com/badlogic/gdx/utils/GwtModuleGenerator.java"));
		assertNull(replacorList.apply("com/badlogic/gdx/utils/NumberUtils.java"));
		
		// should match
		assertEquals("com/badlogic/gdx/Files.java", replacorList.apply("com/badlogic/gdx/Files.java"));
		assertEquals("com/badlogic/gdx/Game.java", replacorList.apply("com/badlogic/gdx/Game.java"));
		assertEquals("com/badlogic/gdx/utils/Base64Coder.java", replacorList.apply("com/badlogic/gdx/utils/Base64Coder.java"));
		assertEquals("com/badlogic/gdx/audio/Sound.java", replacorList.apply("com/badlogic/gdx/audio/Sound.java"));
	}

	public void testExclude() {

		final Replacor.ReplacorList replacorList = Replacor.buildReplacors("{.*java} -{com.badlogic.gdx.math.*java} -{.*(GdxBuild|GwtModuleGenerator|NumberUtils|GdxRuntimeException).*java}");

		assertNull(replacorList.apply("META-INF/MANIFEST.MF"));
		assertEquals("com/badlogic/gdx/utils/Gdx.java", replacorList.apply("com/badlogic/gdx/utils/Gdx.java"));
		assertNull(replacorList.apply("com/badlogic/gdx/utils/GdxBuild.java"));
	}

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

	public void testExtracAll0() {

		final String body = " sakjhfdsakjhas<ba> ";
		final Replacor replacor = new Replacor("{<([^.]*)>@$1}");

		assertEquals(Arrays.asList("ba"), replacor.extractAll(body));

	}

	public void testExtracAll1() {

		final String body = " sakjhfdsakjhas<ba> ";
		final Replacor replacor = new Replacor("{<([^.]*)>@$0:$1}");

		assertEquals(Arrays.asList("<ba>:ba"), replacor.extractAll(body));

	}

	public void testExtracAll2() {

		final String body = "<foo> sakjhfdsakjhas<ba> jkhadshj<goosad>>";
		final Replacor replacor = new Replacor("{<([^>]*)>@$0:$1}");

		assertEquals(Arrays.asList("<foo>:foo", "<ba>:ba", "<goosad>:goosad"), replacor.extractAll(body));
	}
}
