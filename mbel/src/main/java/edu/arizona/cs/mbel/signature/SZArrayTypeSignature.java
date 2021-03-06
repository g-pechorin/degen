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
 * This class represents a type signature for an SZARRAY (Single-dimensional, Zero-based array)
 *
 * @author Michael Stepp
 */
public class SZArrayTypeSignature extends TypeSpecSignature {
	private java.util.Vector customMods;   // CustomModifierSignatures
	private TypeSignature elementTypeSignature;

	/**
	 * Makes an SZArrayTypeSignature with the given element type
	 *
	 * @param type the TypeSignature of the elements of this array
	 */
	public SZArrayTypeSignature(TypeSignature type) throws SignatureException {
		this(null, type);
	}

	/**
	 * Constructor which allows CustomModifiers to be added to this signature
	 *
	 * @param mods an array of CustomModifiers to be applied to this signature
	 * @param type the TypeSignature of the elements of this array
	 */
	public SZArrayTypeSignature(CustomModifierSignature[] mods, TypeSignature type) throws SignatureException {
		super(ELEMENT_TYPE_SZARRAY);
		if (type == null)
			throw new SignatureException("SZArrayTypeSignature: null element type given");
		elementTypeSignature = type;

		customMods = new java.util.Vector(10);
		if (mods != null) {
			for (int i = 0; i < mods.length; i++)
				if (mods[i] != null)
					customMods.add(mods[i]);
		}
	}

	private SZArrayTypeSignature() {
		super(ELEMENT_TYPE_SZARRAY);
	}

	/**
	 * Factory method for parsing SZArrayTypeSignatures from raw binary blobs
	 *
	 * @param buffer the wrapper around the binary blob
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return an SZArrayTypeSignature representing this binary blob, or null if there was a parse error
	 */
	public static TypeSignature parse(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.mbel.TypeGroup group) {
		SZArrayTypeSignature blob = new SZArrayTypeSignature();
		byte data = buffer.get();
		if (data != ELEMENT_TYPE_SZARRAY)
			return null;

		blob.customMods = new java.util.Vector(10);
		int pos = buffer.getPosition();
		CustomModifierSignature temp = CustomModifierSignature.parse(buffer, group);
		while (temp != null) {
			blob.customMods.add(temp);
			pos = buffer.getPosition();
			temp = CustomModifierSignature.parse(buffer, group);
		}
		buffer.setPosition(pos);

		blob.elementTypeSignature = TypeSignature.parse(buffer, group);
		if (blob.elementTypeSignature == null)
			return null;

		return blob;
	}

	/**
	 * Getter method for the CustomModifiers applied to this signature
	 */
	public CustomModifierSignature[] getCustomMods() {
		CustomModifierSignature[] sigs = new CustomModifierSignature[customMods.size()];
		for (int i = 0; i < sigs.length; i++)
			sigs[i] = (CustomModifierSignature) customMods.get(i);
		return sigs;
	}

	/**
	 * Getter method for the single-byte code type value
	 */
	public TypeSignature getElementType() {
		return elementTypeSignature;
	}

	public void emit(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.emit.ClassEmitter emitter) {
		buffer.put(ELEMENT_TYPE_SZARRAY);
		for (int i = 0; i < customMods.size(); i++)
			((CustomModifierSignature) customMods.get(i)).emit(buffer, emitter);
		elementTypeSignature.emit(buffer, emitter);
	}
   
/*
   public void output(){
      System.out.print("SZArrayTypeSignature[");
      for (int i=0;i<customMods.size();i++){
         ((CustomModifierSignature)customMods.get(i)).output();
         System.out.print(',');
      }
      elementTypeSignature.output();
      System.out.print("]");
   }
*/
}
