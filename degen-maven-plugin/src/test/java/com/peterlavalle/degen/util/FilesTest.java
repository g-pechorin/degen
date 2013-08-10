/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.util;

import com.peterlavalle.util.Files;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author Peter LaValle
 */
public class FilesTest extends TestCase {

	public void testCharSize() {
		final ByteBuffer buffer = ByteBuffer.allocate(2);

		assertEquals(0, buffer.position());
		buffer.putChar('c');
		assertEquals(2, buffer.position());
	}

	public void testIntSize() {
		final ByteBuffer buffer = ByteBuffer.allocate(4);

		assertEquals(0, buffer.position());
		buffer.putInt(31);
		assertEquals(4, buffer.position());
	}

	public void testEncodedGUIDName0() throws MalformedURLException {
		Files.encodedGUIDName(new URL("http://12"));
	}

	public void testEncodedGUIDName1() throws MalformedURLException {
		Files.encodedGUIDName(new URL("http://1"));
	}

	public void testEncodedGUIDName2() throws MalformedURLException {
		Files.encodedGUIDName(new URL("http://12"));
	}

	public void testEncodedGUIDName3() throws MalformedURLException {
		Files.encodedGUIDName(new URL("http://123"));
	}

	public void testEncodedGUIDName4() throws MalformedURLException {
		Files.encodedGUIDName(new URL("http://1234"));
	}
}
