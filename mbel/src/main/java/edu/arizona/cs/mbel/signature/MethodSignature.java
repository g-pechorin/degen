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
 * This class describes a method signature
 *
 * @author Michael Stepp
 */
public class MethodSignature extends StandAloneSignature implements CallingConvention {
	private byte flags;
	private int requiredParamCount;
	// number of params before SENTINEL
	private ReturnTypeSignature returnType;
	private java.util.Vector params;

	private MethodSignature() {
	}

	/**
	 * This constructor is for methods with the VARARG calling convention.
	 *
	 * @param hasthis        true iff HASTHIS flag is set
	 * @param explicitthis   true iff EXPLICITTHIS flag is set
	 * @param rType          the return type of this method
	 * @param requiredParams the required params of this method
	 * @param extraParams    the extra "varargs" params of this method
	 */
	public MethodSignature(boolean hasthis, boolean explicitthis, ReturnTypeSignature rType, ParameterSignature[] requiredParams, ParameterSignature[] extraParams) throws SignatureException {
		flags = (byte) ((hasthis ? HASTHIS : 0) | (explicitthis ? EXPLICITTHIS : 0) | VARARG);
		if (requiredParams == null) {
			params = new java.util.Vector(10);
			requiredParamCount = 0;
		} else {
			params = new java.util.Vector(requiredParams.length + 10);
			for (int i = 0; i < requiredParams.length; i++) {
				if (requiredParams[i] == null)
					throw new SignatureException("MethodSignature: Null required parameter given");
				params.add(requiredParams[i]);
			}
			requiredParamCount = params.size();
		}

		if (extraParams != null) {
			for (int i = 0; i < extraParams.length; i++) {
				if (extraParams[i] == null)
					throw new SignatureException("MethodSignature: Null extra parameter given");
				params.add(extraParams[i]);
			}
		}
		returnType = rType;

		if (returnType == null)
			throw new SignatureException("MethodSignature: null return type");
	}

	/**
	 * Convenience constructor for a method signature with the HASTHIS flag, and DEFAULT calling convention.
	 * (note: this combination of flags is used for a standard non-static C# method)
	 *
	 * @param rType  the return type of this method
	 * @param params the parameter list for this method
	 */
	public MethodSignature(ReturnTypeSignature rType, ParameterSignature[] Params) throws SignatureException {
		this(true, false, DEFAULT, rType, Params);
	}

	/**
	 * Constructor for specifying the calling convention flags, return type, and
	 * parameter signatures of this method signature.
	 *
	 * @param hasthis     true iff this method has a 'this' pointer
	 * @param explictthis true iff this method has an explicit 'this' pointer
	 * @param convention  the calling convention of this method (defined in CallingConvention)
	 * @param rType       return type signature for this method
	 * @param Params      array of parameter signatures for this method
	 */
	public MethodSignature(boolean hasthis, boolean explicitthis, byte convention, ReturnTypeSignature rType, ParameterSignature[] Params) throws SignatureException {
		flags = (byte) (((hasthis || explicitthis) ? HASTHIS : 0) | (explicitthis ? EXPLICITTHIS : 0) | (convention & 0x0F));
		requiredParamCount = -1;
		if (Params == null) {
			params = new java.util.Vector(10);
		} else {
			params = new java.util.Vector(Params.length + 10);
			for (int i = 0; i < Params.length; i++) {
				if (Params[i] == null)
					throw new SignatureException("MethodSignature: Null param given");
				params.add(Params[i]);
			}
		}
		returnType = rType;

		if (returnType == null)
			throw new SignatureException("MethodSignature: null return type");
	}

	/**
	 * Factory method for parsing a MethodSignature from a binary blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a MethodSignature representing the given blob, or null if there was a parse error
	 */
	public static MethodSignature parse(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.mbel.TypeGroup group) {
		MethodSignature blob = new MethodSignature();

		blob.flags = buffer.get();
		int paramCount = readCodedInteger(buffer);
		blob.returnType = ReturnTypeSignature.parse(buffer, group);
		if (blob.returnType == null)
			return null;

		blob.params = new java.util.Vector(paramCount + 10);
		ParameterSignature temp = null;
		blob.requiredParamCount = -1;
		for (int i = 0; i < paramCount; i++) {
			if (buffer.peek() == ELEMENT_TYPE_SENTINEL) {
				blob.requiredParamCount = i;
				buffer.get();
			}
			temp = ParameterSignature.parse(buffer, group);
			if (temp == null)
				return null;
			blob.params.add(temp);
		}
		return blob;
	}

	/**
	 * Convenience method for testing HASTHIS calling convention flag
	 *
	 * @return true if this method's calling convention has a 'this' pointer, false otherwise
	 */
	public boolean HasThis() {
		return (flags & HASTHIS) != 0;
	}

	/**
	 * Sets whether this method has a 'this' pointer or not (i.e. static vs. non-static).
	 * Since EXPLICITTHIS cannot be set without HASTHIS, passing false also turns off EXPLICITTHIS.
	 */
	public void setHasThis(boolean hasthis) {
		if (hasthis)
			flags = (byte) (flags | HASTHIS);
		else {
			flags = (byte) (flags & ~HASTHIS);
			setExplicitThis(false);
		}
	}

	/**
	 * Convenience method for testing EXPLICITTHIS calling convention flag
	 *
	 * @return true if this method's calling convention has 'this' as an explicit argument
	 */
	public boolean ExplicitThis() {
		return (flags & EXPLICITTHIS) != 0;
	}

	/**
	 * Sets whether this this method has an explicit 'this' pointer.
	 * Since EXPLICITTHIS cannot be set without HASTHIS, passing true also turns on HASTHIS.
	 */
	public void setExplicitThis(boolean exthis) {
		if (exthis) {
			setHasThis(true);
			flags = (byte) (flags | EXPLICITTHIS);
		} else
			flags = (byte) (flags & ~EXPLICITTHIS);
	}

	/**
	 * Returns a byte of flags representing the calling convention of this method
	 */
	public byte getCallingConvention() {
		return (byte) (flags & CALL_CONV_MASK);
	}

	/**
	 * Sets the calling convention of this method.
	 *
	 * @param conv a byte code for the calling convention (defined in CallingConvention)
	 */
	public void setCallingConvention(byte conv) {
		flags = (byte) ((flags & 0xF0) | (conv & 0x0F));
	}

	/**
	 * Sets this methods calling convention flags
	 * (this method is user-proof, i.e. ignores invalid input)
	 */
	public void setFlags(byte Flags) {
		if ((Flags & (~HASTHIS) & (~EXPLICITTHIS)) != 0)
			return;
		flags = Flags;
	}

	/**
	 * Getter method for the parameter signatures in this method
	 *
	 * @return an array of parameter signature, in order (will never be null, but may have 0 length)
	 */
	public ParameterSignature[] getParameters() {
		ParameterSignature[] arr = new ParameterSignature[params.size()];
		for (int i = 0; i < arr.length; i++)
			arr[i] = (ParameterSignature) params.get(i);
		return arr;
	}

	/**
	 * Inserts a parameter in the parameter list of this method
	 * (if index<0, parameter is inserted at the front of the list.
	 * if index>length, the parameter is inserted at the end)
	 *
	 * @param sig   the parameter signature to insert
	 * @param index the position at which to insert the parameter (0-based)
	 */
	public void insertParameter(ParameterSignature sig, int index) {
		index = Math.max(0, index);
		index = Math.min(index, params.size());
		params.insertElementAt(sig, index);
	}

	/**
	 * Removes a parameter from this method signature (comparison by reference)
	 */
	public void removeParameter(ParameterSignature sig) {
		params.remove(sig);
	}

	/**
	 * Getter method for the return type of this method
	 */
	public ReturnTypeSignature getReturnType() {
		return returnType;
	}

	/**
	 * Setter method for the return type of this method
	 */
	public void setReturnType(ReturnTypeSignature rType) {
		returnType = rType;
	}

	/**
	 * Write this signature out to a buffer in raw binary form
	 *
	 * @param buffer the buffer to write to
	 */
	public void emit(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.emit.ClassEmitter emitter) {
		buffer.put(flags);
		byte[] count = encodeInteger(params.size());
		buffer.put(count);
		returnType.emit(buffer, emitter);
		for (int i = 0; i < params.size(); i++) {
			if (i == requiredParamCount)
				buffer.put(ELEMENT_TYPE_SENTINEL);
			((ParameterSignature) params.get(i)).emit(buffer, emitter);
		}
	}
   
/*
   public void output(){
      System.out.print("MethodSignature[");
      if ((flags & HASTHIS)!=0){
         System.out.print("HASTHIS,");
         if ((flags&EXPLICITTHIS)!=0)
            System.out.print("EXPLICITTHIS,");
      }

      String[] CALLCONV = {"DEFAULT,", "C,", "STDCALL,", "THISCALL,", "FASTCALL,", "VARARG,"};
      System.out.print(CALLCONV[flags&0x7]);
      System.out.print(params.size() + ",");

      returnType.output();
      for (int i=0;i<params.size();i++){
         if (i==requiredParamCount)
            System.out.print(",SENTINEL");
         System.out.print(",");
         ((ParameterSignature)params.get(i)).output();
      }
      System.out.print("]");
   }
*/
}
