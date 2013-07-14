/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peterlavalle.degen.extractors.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Peter LaValle
 */
public interface FileHook {

	String getName();

	InputStream openInputStream() throws IOException;

	long lastModified();
}
