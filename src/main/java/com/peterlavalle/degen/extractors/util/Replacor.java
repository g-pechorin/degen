/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors.util;

import com.google.common.base.Function;

/**
 *
 * @author Peter LaValle
 */
public class Replacor implements Function<String, String> {

	public Replacor(final String text) {
		if (!text.matches("^\\{(.*)\\}$")) {
			throw new IllegalArgumentException();
		}

		final String last = text.replaceAll("^\\{(.*)\\}$", "$1");

		// TODO : error checking

		if (last.contains("@")) {
			final String[] lastTwo = last.split("@");

			// TODO : error checking

			pattern = lastTwo[0];
			replacement = lastTwo[1];
		} else {
			pattern = last;
			replacement = "$0";

		}
	}
	public final String pattern;
	public final String replacement;

	@Override
	public String apply(final String input) {
		return input.matches(pattern) ? input.replaceAll(pattern, replacement) : null;
	}
}
