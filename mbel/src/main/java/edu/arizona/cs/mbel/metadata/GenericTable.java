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

package edu.arizona.cs.mbel.metadata;

/**
 * This class is used as a generic construct to hold the data from any arbitrary metadata table.
 * It does this by means of a parsing grammar and a hashtable. The grammar is one of the constant
 * strings defined in the TableConstants.GRAMMAR array.
 *
 * @author Michael Stepp
 */
public class GenericTable {
	private String grammar;
	private java.util.Hashtable data;

	private String name;
	private String[] fieldNames;
	private String[] types;

	/**
	 * Initializes this table with the given grammar.
	 *
	 * @param Grammar the grammar string for this table (one of the constants defined in TableConstants.GRAMMAR)
	 */
	public GenericTable(String Grammar) {
		grammar = Grammar;

		java.util.StringTokenizer outer = new java.util.StringTokenizer(grammar, ":");
		name = outer.nextToken();
		String theRest = outer.nextToken();
		outer = new java.util.StringTokenizer(theRest, ",");

		fieldNames = new String[outer.countTokens()];
		types = new String[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {
			String field = outer.nextToken();
			java.util.StringTokenizer fieldtok = new java.util.StringTokenizer(field, "=");
			fieldNames[i] = fieldtok.nextToken();
			types[i] = fieldtok.nextToken();
		}

		data = new java.util.Hashtable(fieldNames.length);
		for (int i = 0; i < fieldNames.length; i++)
			data.put(fieldNames[i], "");
	}

	/**
	 * Parses a metadata table based on its grammar and the parsing methods defined in TableConstants.
	 * The parsed values all go into an internal hash table, which is keyed according to the names given in the ECMA spec.
	 *
	 * @param in the input stream to read from
	 * @param tc a TableConstants instance to read in heap indexes, table indexes, coded indexes, etc.
	 */
	public void parse(edu.arizona.cs.mbel.MSILInputStream in, TableConstants tc) throws java.io.IOException {
		java.util.StringTokenizer tok = null;

		for (int i = 0; i < fieldNames.length; i++) {
			if (types[i].startsWith("1")) {
				Integer i1 = new Integer(in.readBYTE());
				data.put(fieldNames[i], i1);
			} else if (types[i].startsWith("2")) {
				Integer i2 = new Integer(in.readWORD());
				data.put(fieldNames[i], i2);
			} else if (types[i].startsWith("4")) {
				Long i4 = new Long(in.readDWORD());
				data.put(fieldNames[i], i4);
			} else if (types[i].startsWith("S")) {
				long index = tc.readHeapIndex(in, TableConstants.StringsHeap);
				data.put(fieldNames[i], tc.getString(index));
			} else if (types[i].startsWith("B")) {
				long index = tc.readHeapIndex(in, TableConstants.BlobHeap);
				data.put(fieldNames[i], tc.getBlob(index));
			} else if (types[i].startsWith("G")) {
				long index = tc.readHeapIndex(in, TableConstants.GUIDHeap);
				data.put(fieldNames[i], tc.getGUID(index));
			} else if (types[i].startsWith("T")) {
				tok = new java.util.StringTokenizer(types[i], "|");
				tok.nextToken();
				int table = Integer.parseInt(tok.nextToken());
				Long value = new Long(tc.readTableIndex(in, table));
				data.put(fieldNames[i], value);
			} else if (types[i].startsWith("C")) {
				tok = new java.util.StringTokenizer(types[i], "|");
				tok.nextToken();
				int coded = Integer.parseInt(tok.nextToken());
				Long value = new Long(tc.readCodedIndex(in, coded));
				data.put(fieldNames[i], value);
			}
		}
	}

	/**
	 * Returns the type of table this is (i.e. "TypeDef")
	 */
	public String getTableType() {
		return name;
	}

	/**
	 * Returns a string field with the given name
	 *
	 * @param fieldName the name of the field (i.e. "Name" or "Namespace")
	 * @return the string field, or null if invalid
	 */
	public String getString(String fieldName) {
		if (data == null || fieldName == null)
			return null;

		Object obj = data.get(fieldName);
		if (obj == null || !(obj instanceof String))
			return null;
		else
			return (String) obj;
	}

	/**
	 * Returns a blob field of this table with the given name
	 *
	 * @param fieldName the name of the field (i.e. "Signature")
	 * @return a byte array blob
	 */
	public byte[] getBlob(String fieldName) {
		if (data == null || fieldName == null)
			return null;

		Object obj = data.get(fieldName);
		if (obj == null || !(obj instanceof byte[]))
			return null;
		else
			return (byte[]) obj;
	}

	/**
	 * Returns a GUID field of this table
	 *
	 * @param fieldName the name of the field (i.e. "Mvid")
	 * @return a byte array GUID
	 */
	public byte[] getGUID(String fieldName) {
		return getBlob(fieldName);
	}

	/**
	 * Returns an integer valued field from this table
	 *
	 * @param fieldName the name of this field (i.e. "BuildNumber" or "MajorVersion")
	 * @return a Number containing the constant value (will either be an Integer or a Long)
	 */
	public Number getConstant(String fieldName) {
		if (data == null || fieldName == null)
			return null;

		Object obj = data.get(fieldName);
		if (obj == null || !(obj instanceof Number))
			return null;
		else
			return (Number) obj;
	}

	/**
	 * Returns a table index field from this table
	 *
	 * @param fieldName the name of the field (i.e. "MethodList" or "Parent")
	 * @return a Long containing a RID
	 */
	public Long getTableIndex(String fieldName) {
		Number temp = getConstant(fieldName);
		if (temp == null || !(temp instanceof Long))
			return null;
		else
			return (Long) temp;
	}

	/**
	 * Returns a coded index field of this table
	 *
	 * @param fieldName the name of the field
	 * @return a Long containing a coded index
	 */
	public Long getCodedIndex(String fieldName) {
		return getTableIndex(fieldName);
	}

	/**
	 * Sets a given field in the internal hashtable to the given value
	 *
	 * @param fieldName the field to set
	 * @param obj       the value to set it to
	 * @return true if the fieldName is a valid key, false otherwise
	 */
	public boolean setFieldValue(String fieldName, Object obj) {
		if (data == null || fieldName == null || obj == null)
			return false;

		boolean found = false;
		for (int i = 0; i < fieldNames.length; i++) {
			if (fieldName.equals(fieldNames[i])) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;

		data.put(fieldName, obj);
		return true;
	}
   
   /*
   public void output(){
      System.out.print(name + "Table:{");
      if (data!=null)
      for (int i=0;i<fieldNames.length;i++){
         System.out.print("\n  "+fieldNames[i] + " = ");
         if (data==null)
            System.out.print(types[i]);
         else{
            Object obj = data.get(fieldNames[i]);
            if (obj instanceof byte[])
               System.out.print(BlobStream.blobToString((byte[])obj));
            else if (obj instanceof String)
               System.out.print("\"" + obj + "\"");
            else 
               System.out.print(obj);
         }
      }
      System.out.print("\n}");
   }
   */

	public void emit(edu.arizona.cs.mbel.ByteBuffer buffer, edu.arizona.cs.mbel.emit.ClassEmitter emitter) {
		for (int i = 0; i < fieldNames.length; i++) {
			if (types[i].startsWith("1")) {
				Number n = (Number) data.get(fieldNames[i]);
				buffer.put(n.intValue());
			} else if (types[i].startsWith("2")) {
				Number n = (Number) data.get(fieldNames[i]);
				buffer.putWORD(n.intValue());
			} else if (types[i].startsWith("4")) {
				Number n = (Number) data.get(fieldNames[i]);
				buffer.putDWORD(n.longValue());
			} else if (types[i].startsWith("S")) {
				Number n = (Number) data.get(fieldNames[i]);
				emitter.putStringsToken(buffer, n);
			} else if (types[i].startsWith("B")) {
				Number n = (Number) data.get(fieldNames[i]);
				emitter.putBlobToken(buffer, n);
			} else if (types[i].startsWith("G")) {
				Number n = (Number) data.get(fieldNames[i]);
				emitter.putGUIDToken(buffer, n);
			} else if (types[i].startsWith("T")) {
				int type = Integer.parseInt(types[i].substring(2));
				Number n = (Number) data.get(fieldNames[i]);
				emitter.putTableIndex(buffer, type, n);
			} else if (types[i].startsWith("C")) {
				int type = Integer.parseInt(types[i].substring(2));
				Number n = (Number) data.get(fieldNames[i]);
				emitter.putCodedIndex(buffer, type, n);
			}
		}
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof GenericTable))
			return false;

		GenericTable table = (GenericTable) o;
		if (!grammar.equals(table.grammar))
			return false;
		for (int i = 0; i < fieldNames.length; i++) {
			Object obj1 = data.get(fieldNames[i]);
			Object obj2 = table.data.get(fieldNames[i]);
			if (obj1 instanceof byte[]) {
				byte[] b1 = (byte[]) obj1;
				byte[] b2 = (byte[]) obj2;
				if (b1.length != b2.length)
					return false;
				for (int j = 0; j < b1.length; j++)
					if (b1[j] != b2[j])
						return false;
			} else if (!obj1.equals(obj2))
				return false;
		}
		return true;
	}
}
