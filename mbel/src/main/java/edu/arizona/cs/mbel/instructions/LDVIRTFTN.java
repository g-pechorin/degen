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
 * Load virtual method pointer.<br>
 * Stack transition:<br>
 * ..., object --> ..., ftn
 *
 * @author Michael Stepp
 */
public class LDVIRTFTN extends Instruction {
	public static final int LDVIRTFTN = 0x07FE;
	protected static final int OPCODE_LIST[] = {LDVIRTFTN};
	private edu.arizona.cs.mbel.mbel.MethodDefOrRef method;

	/**
	 * Makes a LDVIRTFTN object for the given method reference.
	 *
	 * @param ref the method reference
	 */
	public LDVIRTFTN(edu.arizona.cs.mbel.mbel.MethodDefOrRef ref) throws InstructionInitException {
		super(LDVIRTFTN, OPCODE_LIST);
		method = ref;
	}

	/**
	 * Returns the method reference for this instruction.
	 */
	public edu.arizona.cs.mbel.mbel.MethodDefOrRef getMethod() {
		return method;
	}

	public String getName() {
		return "ldvirtftn";
	}

	public int getLength() {
		return (super.getLength() + 4);
	}

	protected void emit(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.emit.ClassEmitter emitter) {
		super.emit(buffer, emitter);
		long token = emitter.getMethodRefToken(method);
		buffer.putTOKEN(token);
	}

	public LDVIRTFTN(int opcode, edu.arizona.cs.mbel.mbel.ClassParser parse) throws java.io.IOException, InstructionInitException {
		super(opcode, OPCODE_LIST);
		long methodToken = parse.getMSILInputStream().readTOKEN();
		method = parse.getMethodDefOrRef(methodToken);
	}

	public boolean equals(Object o) {
		if (!(super.equals(o) && (o instanceof LDVIRTFTN)))
			return false;
		LDVIRTFTN ldvirtftn = (LDVIRTFTN) o;
		return (method == ldvirtftn.method);
	}

/*
   public void output(){
      System.out.print(getName()+" ");
      method.output();
   }
*/
}
