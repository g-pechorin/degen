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
 * This class represents a .NET Module. The Module is the root of the tree as far as MBEL objects are concerned.
 * Modules contain the list of TypeDefs defined in this Module, and this list must be kept up to date by the user
 * (using Module.addTypeDef() and Module.removeTypeDef(), etc). A Module also contains the global methods and fields,
 * as well as some PE/COFF file information needed to re-emit the module back to disk. A Module object is all that is
 * needed to re-emit the input file back to disk (using the Emitter class). If this Module is the prime module in its Assembly,
 * it will contain an AssemblyInfo object giving information about the defining Assembly. The Module also keeps track of the
 * EntryPoint, the VTable, the ManifestResources, and the File references.
 *
 * @author Michael Stepp
 */
public class Module {
	private edu.arizona.cs.mbel.parse.PEModule pe_module;
	//////////////////////////////////////////////////////
	private int Generation;
	private String Name;
	private byte[] Mvid;                // GUID
	private byte[] EncID;               // GUID
	private byte[] EncBaseID;           // GUID
	//////////////////////////////////////////////////////
	private java.util.Vector typeDefs;
	private java.util.Vector fileReferences;
	private java.util.Vector manifestResources;
	private java.util.Vector vtableFixups;
	private AssemblyInfo assemblyInfo;
	private EntryPoint entryPoint;

	private java.util.Vector moduleAttributes;

	/**
	 * Constructs a new Module "from scratch".
	 * This makes a new Module that has only the global "<Module>" class in it and nothing else.
	 * This module will be a pure IL only module. The Mvid GUID must be supplied by the user.
	 *
	 * @param name the logical name of this Module
	 * @param mvid the Mvid GUID for this module
	 * @param sub  the subsystem ID for the target subsystem of this module (constants defined in PE_Header.PE_SUBSYSTEM_?)
	 */
	public Module(String name, byte[] mvid, int sub) {
		pe_module = new edu.arizona.cs.mbel.parse.PEModule(sub);
		Generation = 0;
		Name = name;
		Mvid = mvid;
		EncID = new byte[0];
		EncBaseID = new byte[0];
		typeDefs = new java.util.Vector(10);
		fileReferences = new java.util.Vector(10);
		manifestResources = new java.util.Vector(10);
		vtableFixups = new java.util.Vector(10);
		moduleAttributes = new java.util.Vector(10);

		assemblyInfo = null;
		entryPoint = null;


		TypeDef mod = new TypeDef("", "<Module>", 0);
		addTypeDef(mod);
	}


	/**
	 * Constructs a Module with the given name and PEModule underlying file structure.
	 *
	 * @param pe   the PE/COFF file header structure of the file this Module was read from.
	 * @param name the logical name of this Module (usually == the filename)
	 */
	protected Module(edu.arizona.cs.mbel.parse.PEModule pe, String name) {
		pe_module = pe;
		Name = name;
		typeDefs = new java.util.Vector(20);
		fileReferences = new java.util.Vector(10);
		manifestResources = new java.util.Vector(5);
		vtableFixups = new java.util.Vector(10);
		assemblyInfo = null;
		entryPoint = null;

		moduleAttributes = new java.util.Vector(10);
	}

	/**
	 * Adds a CustomAttribute to this Module
	 *
	 * @param ca the CustomAttribute to add
	 */
	public void addModuleAttribute(CustomAttribute ca) {
		if (ca != null)
			moduleAttributes.add(ca);
	}

	/**
	 * Returns a list of the CustomAttributes on this Module
	 *
	 * @return a non-null array of CustomAttributes
	 */
	public CustomAttribute[] getModuleAttributes() {
		CustomAttribute[] cas = new CustomAttribute[moduleAttributes.size()];
		for (int i = 0; i < cas.length; i++)
			cas[i] = (CustomAttribute) moduleAttributes.get(i);
		return cas;
	}

	/**
	 * Removes a CustomAttribute from this Module
	 */
	public void removeModuleAttribute(CustomAttribute ca) {
		if (ca != null)
			moduleAttributes.remove(ca);
	}


	/**
	 * Returns the PEModule header data from the input file of this Module (used by emitter only)
	 */
	public edu.arizona.cs.mbel.parse.PEModule getPEModule() {
		return pe_module;
	}

	/**
	 * Returns the EntryPoint object for this Module (may be null)
	 */
	public EntryPoint getEntryPoint() {
		return entryPoint;
	}

	/**
	 * Sets the EntryPoint for this Module.
	 * (note: DLLs must have a null EntryPoint)
	 *
	 * @param e the EntryPoint (may be null)
	 */
	public void setEntryPoint(EntryPoint e) {
		entryPoint = e;
	}

	/**
	 * Returns the ManifestResources defined in this Module.
	 *
	 * @return a non-null array of ManifestResources
	 */
	public ManifestResource[] getManifestResources() {
		ManifestResource[] res = new ManifestResource[manifestResources.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = (ManifestResource) manifestResources.get(i);
		}
		return res;
	}

	/**
	 * Adds a ManifestResource to this Module.
	 *
	 * @param res the resource to add
	 */
	public void addManifestResource(ManifestResource res) {
		manifestResources.add(res);
	}

	/**
	 * Removes a ManifestResource from this Module
	 *
	 * @param res the resource to remove (using ManifestResource.equals())
	 */
	public void removeManifestResource(ManifestResource res) {
		manifestResources.remove(res);
	}

	/**
	 * Returns true iff this is the prime Module in the Assembly
	 * (iff this module has an AssemblyInfo object).
	 */
	public boolean isPrimeModule() {
		return (assemblyInfo != null);
	}

	/**
	 * Returns the AssemblyInfo information from this Module (may be null)
	 */
	public AssemblyInfo getAssemblyInfo() {
		return assemblyInfo;
	}

	/**
	 * Sets the AssemblyInfo of this Module (can be null, see isPrimeModule())
	 */
	public void setAssemblyInfo(AssemblyInfo info) {
		assemblyInfo = info;
	}

	/**
	 * Returns the VTableFixups for this Module.
	 *
	 * @return a non-null array of VTableFixups
	 */
	public VTableFixup[] getVTableFixups() {
		VTableFixup[] vt = new VTableFixup[vtableFixups.size()];
		for (int i = 0; i < vt.length; i++) {
			vt[i] = (VTableFixup) vtableFixups.get(i);
		}
		return vt;
	}

	/**
	 * Adds a VTableFixups to this Module (used by parser only)
	 */
	protected void addVTableFixup(VTableFixup vt) {
		if (vt == null)
			return;
		vtableFixups.add(vt);
	}

	/**
	 * Returns the global field whose name is 'name', or null if not found.
	 *
	 * @param name the name of the field, to be compared with Field.getName()
	 * @return the given field, or null if not found
	 */
	public Field getGlobalField(String name) {
		TypeDef mod = getTypeDefByName("", "<Module>");
		if (mod == null)
			return null;
		return mod.getFieldByName(name);
	}

	/**
	 * Returns the global method whose name is 'name', or null if not found.
	 *
	 * @param name the name of the method, to be compared with Method.getName()
	 * @return the given method, or null if not found
	 */
	public Method getGlobalMethod(String name) {
		TypeDef mod = getTypeDefByName("", "<Module>");
		if (mod == null)
			return null;
		return mod.getMethodByName(name);
	}

	/**
	 * Adds a global field to this Module.
	 * Global fields and methods are achieved in .NET by adding them to a
	 * hidden TypeDef named "<Module>" in namespace "". If such a TypeDef
	 * does not exist, this method (and addGlobalMethod) will create it.
	 */
	public void addGlobalField(Field field) {
		TypeDef module = getTypeDefByName("", "<Module>");
		if (module == null) {
			module = new TypeDef("", "<Module>", 0);
			module.setParent(this);
		}
		module.addField(field);
	}

	/**
	 * Adds a global method to this Module (see addGlobalField).
	 * Global methods must be static and public, and cannot have HASTHIS or EXPLICITTHIS
	 * in their calling convention.
	 */
	public void addGlobalMethod(Method method) {
		TypeDef module = getTypeDefByName("", "<Module>");
		if (module == null) {
			module = new TypeDef("", "<Module>", 0);
			module.setParent(this);
		}
		module.addMethod(method);
	}

	/**
	 * Returns the TypeDef with the given name and namespace
	 *
	 * @param ns   the namespace of the target TypeDef
	 * @param name the name of the target TypeDef
	 * @return the target TypeDef, or null if not found
	 */
	public TypeDef getTypeDefByName(String ns, String name) {
		for (int i = 0; i < typeDefs.size(); i++) {
			TypeDef def = (TypeDef) (typeDefs.get(i));
			if (ns.equals(def.getNamespace()) && name.equals(def.getName()))
				return def;
		}
		return null;
	}

	/**
	 * Returns all TypeDefs defined in this Module
	 *
	 * @return a non-null array of TypeDefs
	 */
	public TypeDef[] getTypeDefs() {
		TypeDef[] classes = new TypeDef[typeDefs.size()];
		for (int i = 0; i < classes.length; i++)
			classes[i] = (TypeDef) typeDefs.get(i);
		return classes;
	}

	/**
	 * Returns the File references in this Module
	 *
	 * @return a non-null array of FileReferences
	 */
	public FileReference[] getFileReferences() {
		FileReference[] refs = new FileReference[fileReferences.size()];
		for (int i = 0; i < refs.length; i++) {
			refs[i] = (FileReference) fileReferences.get(i);
		}
		return refs;
	}

	/**
	 * Adds a FileReference to this Module
	 */
	public void addFileReference(FileReference ref) {
		fileReferences.add(ref);
	}

	/**
	 * Returns the generation number of this Module
	 */
	public int getGeneration() {
		return Generation;
	}

	/**
	 * Sets the generation number of this Module
	 */
	public void setGeneration(int gen) {
		Generation = gen;
	}

	/**
	 * Returns the logical name of this Module (usually == the filename)
	 */
	public String getName() {
		return Name;
	}

	/**
	 * Sets the logical name of this Module (unsafe?)
	 */
	public void setName(String name) {
		Name = name;
	}

	/**
	 * Returns the Mvid GUID of this Module
	 */
	public byte[] getMvidGUID() {
		return Mvid;
	}

	/**
	 * Sets the Mvid GUID value
	 */
	public void setMvidGUID(byte[] guid) {
		Mvid = guid;
	}

	/**
	 * Returns the EncId GUID value
	 */
	public byte[] getEncIdGUID() {
		return EncID;
	}

	/**
	 * Sets the EncId GUID value
	 */
	public void setEncIdGUID(byte[] guid) {
		EncID = guid;
	}

	/**
	 * Returns the EncBaseId GUID value
	 */
	public byte[] getEncBaseIdGUID() {
		return EncBaseID;
	}

	/**
	 * Sets the EncBaseId GUID value
	 */
	public void setEncBaseIdGUID(byte[] guid) {
		EncBaseID = guid;
	}

	/**
	 * Add a TypeDef definition to this Module
	 */
	public void addTypeDef(TypeDef def) {
		typeDefs.add(def);
		def.setParent(this);
	}

	/**
	 * Removes a TypeDef from this Module (based on TypeDef.equals())
	 */
	public void removeTypeDef(TypeDef def) {
		if (typeDefs.remove(def))
			def.setParent(null);
	}
   
/*
   public void output(){
      System.out.print("Module[Name=\""+Name+"\", Generation="+Generation);
      if (isPrimeModule())
         System.out.print(", IsPrimeModule");
      if (entryPoint!=null){
         System.out.print(", EntryPoint=");
         entryPoint.output();
      }
      System.out.print("]");
   }
*/
}
