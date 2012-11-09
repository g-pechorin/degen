/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Peter LaValle
 */
public class Replacor implements Function<String, String> {

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Replacor && toString().equals(obj.toString());
	}

	@Override
	public String toString() {
		return '{' + this.pattern + '@' + this.replacement + '}';
	}

	public Replacor(final String text) {
		if (!text.trim().matches("^*\\{(.*)\\}$")) {
			throw new IllegalArgumentException("text=`" + text + "`");
		}

		final String last = text.trim().replaceAll("^\\{(.*)\\}$", "$1");

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

	public List<String> extractAll(String input) {
		final List<String> extracted = Lists.newLinkedList();
		final Matcher matcher = Pattern.compile(pattern).matcher(input);
		while (matcher.find()) {
			extracted.add(apply(matcher.group()));
		}
		return extracted;
	}
}
