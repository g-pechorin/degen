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


package edu.arizona.cs.mbel.signature;

/**
 * This class describes a class type signature
 *
 * @author Michael Stepp
 */
public class ClassTypeSignature extends TypeSignature {
	private edu.arizona.cs.mbel.mbel.AbstractTypeReference classType;

	/**
	 * Makes a class signature representing the given type
	 *
	 * @param clazz an mbel reference to the type this signature describes
	 */
	public ClassTypeSignature(edu.arizona.cs.mbel.mbel.AbstractTypeReference clazz) throws SignatureException {
		this();
		if (clazz == null)
			throw new SignatureException("ClassTypeSignature: null class given");
		classType = clazz;
	}

	private ClassTypeSignature() {
		super(ELEMENT_TYPE_CLASS);
	}

	/**
	 * Factory method for parsing a class signature from a raw binary blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a ClassTypeSignature representing the given blob, or null if there was a parse error
	 */
	public static TypeSignature parse(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.mbel.TypeGroup group) {
		ClassTypeSignature blob = new ClassTypeSignature();
		byte data = buffer.get();
		if (data != ELEMENT_TYPE_CLASS)
			return null;

		int token[] = parseTypeDefOrRefEncoded(buffer);
		if (token[0] == edu.arizona.cs.mbel.metadata.TableConstants.TypeDef) {
			blob.classType = group.getTypeDefs()[token[1] - 1];
		} else if (token[0] == edu.arizona.cs.mbel.metadata.TableConstants.TypeRef) {
			blob.classType = group.getTypeRefs()[token[1] - 1];
		} else if (token[0] == edu.arizona.cs.mbel.metadata.TableConstants.TypeSpec) {
			blob.classType = group.getTypeSpecs()[token[1] - 1];
		} else
			return null;
		return blob;
	}

	/**
	 * Returns a reference to the type this type signature describes
	 */
	public edu.arizona.cs.mbel.mbel.AbstractTypeReference getClassType() {
		return classType;
	}

	public void emit(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.emit.ClassEmitter emitter) {
		buffer.put(ELEMENT_TYPE_CLASS);
		long token = emitter.getTypeToken(classType);
		byte[] data = makeTypeDefOrRefEncoded((int) ((token >> 24) & 0xFF), (int) (token & 0xFFFFFF));
		buffer.put(data);
	}
   
/*
   public void output(){
      System.out.print("ClassTypeSignature[");
      classType.output();
      System.out.print("]");
   }
*/
}
