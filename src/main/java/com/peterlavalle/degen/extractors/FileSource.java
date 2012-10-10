/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors;

import com.google.common.collect.Lists;
import com.peterlavalle.degen.RemoteDegen;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Uses a uggly string to determine a list of files that should be extracted and replaced
 * 
 * @author peter
 */
public class FileSource {

	public FileSource(Recipe recipe) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	void inTest() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public List<String> getOriginalNames() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getFinalName(String string) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public byte[] getBytes(String get) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	static class Recipe {

		Recipe(String test) {
			throw new UnsupportedOperationException("Not yet implemented");
		}

		List<String> getZipList() {
			throw new UnsupportedOperationException("Not yet implemented");
		}

		URL getUrl() {
			throw new UnsupportedOperationException("Not yet implemented");
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

			names = Lists.newLinkedList(new Iterable<String>() {

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
			});
		}
		private final List<String> names;

		@Override
		public void extractTo(String parent, String name) throws MojoExecutionException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
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
