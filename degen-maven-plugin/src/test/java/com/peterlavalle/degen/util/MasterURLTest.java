package com.peterlavalle.degen.util;

import com.peterlavalle.degen.extractors.util.MasterURL;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Peter LaValle
 */
public class MasterURLTest extends TestCase {

	public void testMultipleReplacorsNegating() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {opp.jo\\d.Pot.java} -{.*0.*}");

		assertEquals("opp/jo9/Pot/java", masterURL.applyReplacors("opp/jo9/Pot/java"));
		assertEquals("opp/jo3/Pot/java", masterURL.applyReplacors("opp/jo3/Pot/java"));
		assertNull(masterURL.applyReplacors("opp/jo0/Pot/java"));
	}

	public void testMultipleReplacorsConstruction() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {opp.jo\\d.Pot.java@src/$0} -{.*0.*}");

		assertTrue(masterURL.replacors.get(0).includes);
		assertEquals("opp.jo\\d.Pot.java", masterURL.replacors.get(0).pattern);
		assertEquals("src/$0", masterURL.replacors.get(0).replacement);

		assertFalse(masterURL.replacors.get(1).includes);
		assertEquals(".*0.*", masterURL.replacors.get(1).pattern);
		assertNull("$0", masterURL.replacors.get(1).replacement);
	}

	public void testMultipleReplacorsFiltering() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {opp.jo\\d.Pot.java} {.*0.*}");

		assertNull(masterURL.applyReplacors("opp/jo9/Pot/java"));
		assertNull(masterURL.applyReplacors("opp/jo3/Pot/java"));
		assertEquals("opp/jo0/Pot/java", masterURL.applyReplacors("opp/jo0/Pot/java"));
	}

	public void testBasic() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertNull(masterURL.replacors);
		assertEquals(0, masterURL.zips.size());
	}

	public void testPattern() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {ab.class}");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertEquals("ab.class", masterURL.replacors.get(0).pattern);
		assertEquals("$0", masterURL.replacors.get(0).replacement);
		assertEquals(0, masterURL.zips.size());
	}

	public void testReplacement() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {ab.class@src/$0}");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertEquals("ab.class", masterURL.replacors.get(0).pattern);
		assertEquals("src/$0", masterURL.replacors.get(0).replacement);
		assertEquals(Arrays.asList(), masterURL.zips);
	}

	public void testZip() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip @ src/sources.zip");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertNull(masterURL.replacors);
		assertEquals(Arrays.asList("src/sources.zip"), masterURL.zips);
	}

	public void testZipZip() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip @ src/sources.zip @inner-crazy.jar");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertNull(masterURL.replacors);
		assertEquals(Arrays.asList("src/sources.zip", "inner-crazy.jar"), masterURL.zips);
	}

	public void testBig() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip  {pattern@replacem ent}");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertEquals("pattern", masterURL.replacors.get(0).pattern);
		assertEquals("replacem ent", masterURL.replacors.get(0).replacement);
		assertEquals(Arrays.asList(), masterURL.zips);
	}
}
