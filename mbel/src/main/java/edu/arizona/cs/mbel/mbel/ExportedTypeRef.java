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

package edu.arizona.cs.mbel.mbel;

/**
 * This class is a TypeRef that refers a type defined in another module of this assembly,
 * as referenced by the ExportedType table. Thus, it is a public class in another module.
 * (prime module only?)
 *
 * @author Michael Stepp
 */
public class ExportedTypeRef extends TypeRef implements edu.arizona.cs.mbel.signature.TypeAttributes {
	private long ExportedTypeRID = -1L;

	private long Flags;
	private FileReference file;
	private ExportedTypeRef exportedType;
	// one or the other will be nonnull

	private java.util.Vector exportedTypeAttributes;

	/**
	 * Makes an ExportedTypeRef with the given namespace, name, and flags
	 *
	 * @param ns    the namespace of the type
	 * @param name  the name of the type
	 * @param flags a bit vector of flags (defined in TypeAttributes)
	 */
	protected ExportedTypeRef(String ns, String name, long flags) {
		super(ns, name);
		Flags = flags;

		exportedTypeAttributes = new java.util.Vector(10);
	}

	/**
	 * Makes an ExportedTypeRef with the given namespace, name, flags, and file reference.
	 *
	 * @param ns    the namespace of this type
	 * @param name  the name of this type
	 * @param flags a bit vector of flags (defined in TypeAttributes)
	 * @param ref   the file reference in which this type is defined
	 */
	public ExportedTypeRef(String ns, String name, long flags, FileReference ref) {
		super(ns, name);
		Flags = flags;
		file = ref;
		exportedType = null;
	}

	/**
	 * Makes a nested ExportedTypeRef with the given namespace, name, flags, and parent type
	 *
	 * @param ns     the namespace of this type
	 * @param name   the name of this type
	 * @param flags  a bit vector of flags (defined in TypeAttributes)
	 * @param parent an ExportedTypeRef of the parent of this nested type
	 */
	public ExportedTypeRef(String ns, String name, long flags, ExportedTypeRef parent) {
		super(ns, name);
		Flags = flags;
		exportedType = parent;
		file = null;
	}


	/**
	 * Adds a CustomAttribute to this ExportedTypeRef
	 */
	public void addExportedTypeAttribute(CustomAttribute ca) {
		if (ca != null)
			exportedTypeAttributes.add(ca);
	}

	/**
	 * Returns a non-null array of CustomAttributes on this ExportedTypeRef (ExportedType)
	 */
	public CustomAttribute[] getExportedTypeAttributes() {
		CustomAttribute[] cas = new CustomAttribute[exportedTypeAttributes.size()];
		for (int i = 0; i < cas.length; i++)
			cas[i] = (CustomAttribute) exportedTypeAttributes.get(i);
		return cas;
	}

	/**
	 * Removes a CustomAttribute from this ExportedType
	 */
	public void removeExportedTypeAttribute(CustomAttribute ca) {
		if (ca != null)
			exportedTypeAttributes.remove(ca);
	}

	/**
	 * Returns the ExportedType RID of this ExportedTypeRef (used by emitter)
	 */
	public long getExportedTypeRID() {
		return ExportedTypeRID;
	}

	/**
	 * Sets the ExportedType RID of this ExportedTypeRef (used by emitter).
	 * This method can only be called once.
	 */
	public void setExportedTypeRID(long rid) {
		if (ExportedTypeRID == -1L)
			ExportedTypeRID = rid;
	}

	/**
	 * Returns a bit vector of flags for this ExportedTypeRef (defined in TypeAttributes)
	 */
	public long getFlags() {
		return Flags;
	}

	/**
	 * Sets the flags for this ExportedTypeRef
	 */
	public void setFlags(long flags) {
		Flags = flags;
	}

	/**
	 * Sets the File reference for this ExportedTypeRef (non-nested only)
	 */
	public void setFileReference(FileReference ref) {
		exportedType = null;
		file = ref;
	}

	/**
	 * Sets the parent ExportedTypeRef of this ExportedTypeRef
	 */
	public void setExportedTypeRef(ExportedTypeRef ref) {
		file = null;
		exportedType = ref;
	}

	/**
	 * Returns the file reference for this ExportedTypeRef (non-nested only, may be null)
	 */
	public FileReference getFileReference() {
		return file;
	}

	/**
	 * Returns the parent ExportedTypeRef of this ExportedTypeRef (nested only, may be null)
	 */
	public ExportedTypeRef getExportedTypeRef() {
		return exportedType;
	}

	/**
	 * Compares 2 ExportedTypeRefs
	 * Returns true iff the names are equals and the files/parents are equal
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExportedTypeRef))
			return false;

		ExportedTypeRef ref = (ExportedTypeRef) o;
		if (!(getName().equals(ref.getName()) || getNamespace().equals(ref.getNamespace())))
			return false;
		if (file != null) {
			return file.equals(ref.file);
		} else {
			return exportedType.equals(ref.exportedType);
		}
	}

/*
   public void output(){
      System.out.print("ExportedTypeRef[Name=\""+getName()+"\", Namespace=\""+getNamespace()+"\"");
      if (file!=null){
         System.out.print(", File=");
         file.output();
      }else{
         System.out.print(", ExportedType=");
         exportedType.output();
      }
      System.out.print("]");
   }
*/
}
