/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Peter LaValle
 */
public interface Callback<T> {

	public void callback(String toString);

	public class StringCallbackOutputStream extends OutputStream {

		public StringCallbackOutputStream(final Callback<String> callback) {
			this.callback = callback;
		}

		public final Callback<String> callback;
		private StringBuilder builder = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte) b});
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			builder.append(new String(b, off, len).replace('\r', '\n'));

			while (builder.toString().contains("\n")) {
				final String first = builder.toString().split("\n")[0];

				builder = new StringBuilder(builder.toString().substring(first.length() + 1));

				callback.callback(first);
			}
		}

		@Override
		public void close() throws IOException {
			callback.callback(builder.toString());
			builder = new StringBuilder();
		}
	}

	;
}
