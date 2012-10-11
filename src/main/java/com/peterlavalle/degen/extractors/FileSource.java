/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.peterlavalle.degen.RemoteDegen;
import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Uses a uggly string to determine a list of files that should be extracted and replaced
 * 
 * @author peter
 */
public class FileSource {

	private final Recipe recipe;

	FileSource(Recipe recipe) {
		this.recipe = recipe;
	}

	public FileSource(String line) throws MalformedURLException {
		this(new Recipe(line));
	}

	public List<String> getOriginalNames() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getFinalName(final String originalName) {
		return originalName.matches(recipe.expression) ? originalName.replaceAll(recipe.expression, recipe.replacement) : null;
	}

	public byte[] getBytes(final String originalName) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	static class Recipe {

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder(getUrl().toString());

			for (final String zip : getZipList()) {
				builder.append(" @").append(zip);
			}

			if (!expression.equals("^.*$")) {
				builder.append(" ~").append(expression);
			}

			if (!replacement.equals("$0")) {
				builder.append(" =").append(replacement);
			}

			return builder.toString();
		}
		private final URL url;
		private final List<String> zipList;

		Recipe(String test) throws MalformedURLException {
			final String[] split = test.trim().split("\\s+");

			if (split.length == 0) {

				throw new IllegalArgumentException("You need at least a URL");
			}

			this.url = new URL(split[0].trim());


			String expressionTmp = ".*";
			String replacementTmp = "$0";
			LinkedList<String> zipListTmp = new LinkedList<String>();

			int i = 1;

			// get any zipListEntries (if present)
			for (i = 1; i < split.length && split[i].startsWith("@"); i++) {
				zipListTmp.add(split[i].substring(1));
			}

			// get the pattern (if present)
			if (i < split.length && split[i].startsWith("~")) {
				expressionTmp = split[i++].substring(1);
			}

			// get the replacement (if present)
			if (i < split.length && split[i].startsWith("=")) {
				replacementTmp = split[i++].substring(1);
			}

			if (i < split.length) {
				throw new RuntimeException("Your zips and replacements are done worng");
			}

			expressionTmp = !expressionTmp.startsWith("^") ? "^" + expressionTmp : expressionTmp;
			expressionTmp = !expressionTmp.endsWith("$") ? expressionTmp + "$" : expressionTmp;
			this.expression = expressionTmp;
			this.replacement = replacementTmp;
			this.zipList = ImmutableList.copyOf(zipListTmp);
		}

		public List<String> getZipList() {
			return this.zipList;
		}

		public URL getUrl() {
			return this.url;
		}
		private final String expression;
		private final String replacement;

		public String getExpression() {
			return expression;
		}

		public String getReplacement() {
			return replacement;
		}
	}

	public AExtractionList getExtractionList(RemoteDegen outer) throws IOException {
		return new ExtractionList(outer);
	}

	public class ExtractionList extends AExtractionList {

		public ExtractionList(RemoteDegen outer) throws IOException {
			super(outer);

			class NameTransformer implements Iterable<String> {

				@Override
				public Iterator<String> iterator() {
					final Iterator<String> coreIterator = FileSource.this.getOriginalNames().iterator();

					return new Iterator<String>() {

						@Override
						public boolean hasNext() {
							return coreIterator.hasNext();
						}

						@Override
						public String next() {
							return FileSource.this.getFinalName(coreIterator.next());
						}

						@Override
						public void remove() {
							coreIterator.remove();
						}
					};
				}
			}

			names = Lists.newLinkedList(new NameTransformer());
		}
		private final List<String> names;

		@Override
		public void extractTo(String parent, String name) throws MojoExecutionException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getName() {
			return recipe.toString();
		}

		@Override
		public Iterator<String> iterator() {
			return names.iterator();
		}

		@Override
		public void removeResource(String resourceFile) {
			names.remove(resourceFile);
		}
	}
}
