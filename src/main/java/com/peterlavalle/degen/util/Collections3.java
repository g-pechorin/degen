/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author peter
 */
public class Collections3 {

	public static <T> List<T> filter(final Function<T, Boolean> function, final Iterable<T> input) {

		final List<T> output = Lists.newLinkedList();

		for (final T item : input) {
			if (function.apply(item)) {
				output.add(item);
			}
		}

		return Lists.newArrayList(output);
	}

	public static <T> List<T> filter(final Function<T, Boolean> function, final T[] input) {

		return filter(function, Arrays.asList(input));
	}
}
