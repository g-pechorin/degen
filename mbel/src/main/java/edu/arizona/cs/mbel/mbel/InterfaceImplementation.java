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
 * This class encapsulates the implementation of an interface by a TypeDef.
 * A TypeDef will have a vector of these, one for each interface it implements.
 * This class exists because it mirrors an InterfaceImpl metadata table,
 * which may have CustomAttributes on it, so those have to go somewhere.
 *
 * @author Michael Stepp
 */
public class InterfaceImplementation {
	private TypeRef interfaceType;
	private java.util.Vector interfaceImplAttributes;

	/**
	 * Makes a new InterfaceImplementation object for the given interface
	 *
	 * @param ref a TypeRef that represents an interface
	 */
	public InterfaceImplementation(TypeRef ref) {
		interfaceType = ref;
		interfaceImplAttributes = new java.util.Vector(10);
	}

	/**
	 * Returns the TypeRef of the interface.
	 */
	public TypeRef getInterface() {
		return interfaceType;
	}

	/**
	 * Adds a CustomAttribute to this InterfaceImpl
	 */
	public void addInterfaceImplAttribute(CustomAttribute ca) {
		if (ca != null)
			interfaceImplAttributes.add(ca);
	}

	/**
	 * Returns a non-null array of CustomAttributes on this InterfaceImplementation (InterfaceImpl)
	 */
	public CustomAttribute[] getInterfaceImplAttributes() {
		CustomAttribute[] cas = new CustomAttribute[interfaceImplAttributes.size()];
		for (int i = 0; i < cas.length; i++)
			cas[i] = (CustomAttribute) interfaceImplAttributes.get(i);
		return cas;
	}

	/**
	 * Removes a CustomAttribute from this InterfaceImpl
	 */
	public void removeInterfaceImplAttribute(CustomAttribute ca) {
		if (ca != null)
			interfaceImplAttributes.remove(ca);
	}
}
