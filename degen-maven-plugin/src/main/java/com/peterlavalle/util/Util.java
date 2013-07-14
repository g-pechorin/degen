/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Peter LaValle
 */
public final class Util {

	private Util() {
	}

	public static <T extends OutputStream> T copyStream(InputStream inputStream, T outputStream) throws IOException {

		final byte[] buffer = new byte[128];
		int read;

		while ((read = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}

		inputStream.close();

		return outputStream;
	}

	public static <T> List<T> asList(Iterable<T> tail) {
		final LinkedList<T> result = new LinkedList<T>();

		for (T val : tail) {
			result.add(val);
		}

		return result;
	}

	public static <T> List<T> asList(T head, Iterable<T> tail) {


		Collection<T> _tail = (Collection<T>) (tail instanceof Collection ? (Collection) tail : asList(tail));

		final ArrayList<T> result = new ArrayList<T>(_tail.size() + 1);

		result.add(head);
		result.addAll(_tail);

		return result;
	}

	public static <T> Iterable<T> toIterable(final Enumeration<T> enumeration) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return toIterator(enumeration);
			}
		};
	}

	public static <T> Iterator<T> toIterator(final Enumeration<T> enumeration) {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return enumeration.hasMoreElements();
			}

			@Override
			public T next() {
				return enumeration.nextElement();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
