/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.util;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author Peter LaValle
 */
public class ByYourCommand {

	private OutputStream pipeError;
	private OutputStream pipeOutput;
	private final File directory;
	private final String command;

	public ByYourCommand(final File directory, final String command) {
		this.command = command;
		this.directory = directory;
	}
	private final List<String> args = Lists.newLinkedList();

	public void addArgument(final String arg) {
		args.add(arg);
	}

	public void pipeOutputTo(final OutputStream outputStream) {
		this.pipeOutput = outputStream;
	}

	public void pipeErrorTo(final OutputStream outputStream) {
		this.pipeError = outputStream;
	}

	private OutputStream getPipeErrorTo() {
		return this.pipeError;
	}

	public void pipeInputFrom(final InputStream inputStream) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public int run() {
		
		
		
		try {
			final ProcessBuilder builder = new ProcessBuilder(Lists.asList(command, (String[]) args.toArray()));

			builder.redirectErrorStream(this.pipeError == null && this.pipeOutput != null);
			builder.directory(directory);

			final Process process = builder.start();
			final int waitFor = process.waitFor();

			Util.copyStream(process.getErrorStream(), this.pipeError).close();
			Util.copyStream(process.getInputStream(), this.pipeOutput).close();

			return waitFor;
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
