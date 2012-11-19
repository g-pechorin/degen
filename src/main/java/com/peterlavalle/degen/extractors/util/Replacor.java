/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.LinkedList;
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
		return (this.includes ? "{" : "-{") + this.pattern + '@' + this.replacement + '}';
	}

	public static class ReplacorList extends LinkedList<Replacor> implements Function<String, String> {

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();

			for (final Replacor replacor : this) {
				if (builder.toString().equals("")) {
					builder.append(' ');
				}

				builder.append(replacor);
			}

			return builder.toString();
		}

		@Override
		public String apply(String input) {
			String output = input;

			for (final Replacor replacor : this) {

				if (replacor.includes) {
					output = replacor.apply(output);
				} else if (output.matches(replacor.pattern)) {
					return null;
				}

				if (output == null) {
					return null;
				}
			}

			return output;
		}
	}

	public static ReplacorList buildReplacors(String replaceors) {

		final ReplacorList replacors = new ReplacorList();
		final String extractor = "^\\s*(\\-?\\{[^\\}]+\\})(.*)$";

		while (replaceors.matches(extractor)) {
			final String head = replaceors.replaceAll(extractor, "$1").trim();
			replaceors = replaceors.replaceAll(extractor, "$2").trim();

			replacors.add(new Replacor(head));
		}

		return replacors;
	}
	public final boolean includes; // HACK but whatever ..

	public Replacor(String text) {

		this.includes = !text.trim().startsWith("-");

		if (!includes && text.contains("@")) {
			throw new IllegalArgumentException("Exclusion replacors cannot perform replacement");
		}

		if (text.replaceAll("[^@]", "").length() > 1) {
			throw new IllegalArgumentException("Replacors can only use the @ character to separate the pattern and the replacement");
		}
		
		if (!text.trim().matches("^\\-?\\{(.*)\\}$")) {
			throw new IllegalArgumentException("text=`" + text + "` is not a Replacor");
		}

		// strip off the outer symbols
		final String last = text.replaceAll("^\\s*\\-?\\{(.*)\\}\\s*$", "$1");

		if (last.contains("@")) {


			final String[] lastTwo = last.split("@");

			pattern = lastTwo[0];
			replacement = lastTwo[1];
		} else {
			pattern = last;
			replacement = includes ? "$0" : null;
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
