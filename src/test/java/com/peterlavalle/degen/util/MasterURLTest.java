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

	public void testBasic() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertEquals(".*", masterURL.replacor.pattern);
		assertEquals("$0", masterURL.replacor.replacement);
		assertEquals(0, masterURL.zips.size());
	}

	public void testPattern() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {ab.class}");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertEquals("ab.class", masterURL.replacor.pattern);
		assertEquals("$0", masterURL.replacor.replacement);
		assertEquals(0, masterURL.zips.size());
	}

	public void testReplacement() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://www.example.com/archive.zip {ab.class@src/$0}");

		assertEquals(new URL("http://www.example.com/archive.zip"), masterURL.url);
		assertEquals("ab.class", masterURL.replacor.pattern);
		assertEquals("src/$0", masterURL.replacor.replacement);
		assertEquals(Arrays.asList(), masterURL.zips);
	}

	public void testZip() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip @ src/sources.zip");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertEquals(".*", masterURL.replacor.pattern);
		assertEquals("$0", masterURL.replacor.replacement);
		assertEquals(Arrays.asList("src/sources.zip"), masterURL.zips);
	}

	public void testZipZip() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip @ src/sources.zip @inner-crazy.jar");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertEquals(".*", masterURL.replacor.pattern);
		assertEquals("$0", masterURL.replacor.replacement);
		assertEquals(Arrays.asList("src/sources.zip","inner-crazy.jar"), masterURL.zips);
	}

	public void testBig() throws MalformedURLException {
		final MasterURL masterURL = new MasterURL("http://blah-blah.url/zome.zip  {pattern@replacement}");

		assertEquals(new URL("http://blah-blah.url/zome.zip"), masterURL.url);
		assertEquals("pattern", masterURL.replacor.pattern);
		assertEquals("replacement", masterURL.replacor.replacement);
		assertEquals(Arrays.asList(), masterURL.zips);
	}
}
