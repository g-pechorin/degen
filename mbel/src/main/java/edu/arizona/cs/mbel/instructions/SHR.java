/* MBEL: The Microsoft Bytecode Engineering Library
 * Copyright (C) 2003 The University of Arizona
 * http://www.cs.arizona.edu/mbel/license.html
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package edu.arizona.cs.mbel.instructions;

/**
 * Shift right (shr preserves the top bit, shr.un zeroes the top bit).<br>
 * Stack transition:<br>
 * ..., value, shiftAmount --> ..., result
 *
 * @author Michael Stepp
 */
public class SHR extends Instruction {
	public static final int SHR = 0x63;
	public static final int SHR_UN = 0x64;
	protected static final int OPCODE_LIST[] = {SHR, SHR_UN};

	public SHR(boolean un) throws InstructionInitException {
		super((un ? SHR_UN : SHR), OPCODE_LIST);
	}

	public boolean isUnsigned() {
		return (getOpcode() == SHR_UN);
	}

	public String getName() {
		return (isUnsigned() ? "shr.un" : "shr");
	}

	public SHR(int opcode, edu.arizona.cs.mbel.mbel.ClassParser parse) throws java.io.IOException, InstructionInitException {
		super(opcode, OPCODE_LIST);
	}

	public boolean equals(Object o) {
		return (super.equals(o) && (o instanceof SHR));
	}
}
