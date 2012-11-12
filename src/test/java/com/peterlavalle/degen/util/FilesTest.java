/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.util;

import com.peterlavalle.degen.extractors.util.Files;
import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 *
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

	public void testEncodedGUIDName0() {
		Files.encodedGUIDName("");
	}

	public void testEncodedGUIDName1() {
		Files.encodedGUIDName("1");
	}

	public void testEncodedGUIDName2() {
		Files.encodedGUIDName("12");
	}

	public void testEncodedGUIDName3() {
		Files.encodedGUIDName("123");
	}

	public void testEncodedGUIDName4() {
		Files.encodedGUIDName("1234");
	}
}
