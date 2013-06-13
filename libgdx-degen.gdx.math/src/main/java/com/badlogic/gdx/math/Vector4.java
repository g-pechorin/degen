/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.utils.NumberUtils;

/** Encapsulates a 4D vector.
 * Allows chaining operations by returning a reference to itself in all modification methods.
 * Based on the original com.badlogic.gdx.math.Vector3
 * @author badlogicgames@gmail.com */
public class Vector4 implements Serializable {
	private static final long serialVersionUID = 3848746241567372522L;

	/** the x-component of this vector **/
	public float x;
	/** the x-component of this vector **/
	public float y;
	/** the x-component of this vector **/
	public float z;
	/** the w-component of this vector **/
	public float w;

	/** Constructs a vector at (0,0,0,0) */
	public Vector4 () {
	}

	/** Creates a vector from the given vector
	 * @param vector The vector */
	public Vector4 (Vector4 vector) {
		this.set(vector);
	}

	/** Creates a vector from the given array. The array must have at least 3 elements.
	 * 
	 * @param values The array */
	public Vector4 (float... values) {
		assert values.length == 4;
		this.set(values);
	}

	/** Sets the components of the given vector
	 * 
	 * @param vector The vector
	 * @return This vector for chaining */
	public Vector4 set (Vector4 vector) {
		return this.set(vector.x, vector.y, vector.z, vector.w);
	}

	/** Sets the components from the array. The array must have at least 3 elements
	 * 
	 * @param values The array
	 * @return this vector for chaining */
	public Vector4 set (float... values) {
		assert values.length == 4;
		return this.set(values[0], values[1], values[2], values[3]);
	}

	/** @return a copy of this vector */
	public Vector4 cpy () {
		return new Vector4(this);
	}

	/** {@inheritDoc} */
	public String toString () {
		return x + "," + y + "," + z + "," + w;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + NumberUtils.floatToIntBits(x);
		result = prime * result + NumberUtils.floatToIntBits(y);
		result = prime * result + NumberUtils.floatToIntBits(z);
		result = prime * result + NumberUtils.floatToIntBits(w);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vector4 other = (Vector4)obj;
		if (NumberUtils.floatToIntBits(x) != NumberUtils.floatToIntBits(other.x)) return false;
		if (NumberUtils.floatToIntBits(y) != NumberUtils.floatToIntBits(other.y)) return false;
		if (NumberUtils.floatToIntBits(z) != NumberUtils.floatToIntBits(other.z)) return false;
		if (NumberUtils.floatToIntBits(w) != NumberUtils.floatToIntBits(other.w)) return false;
		return true;
	}

	/**
	 * Compares this vector with the other vector, using the supplied
	 * epsilon for fuzzy equality testing.
	 * @param obj
	 * @param epsilon
	 * @return whether the vectors are the same.
	 */
	public boolean epsilonEquals(Vector4 obj, float epsilon) {
		if(obj == null) return false;
		if(Math.abs(obj.x - x) > epsilon) return false;
		if(Math.abs(obj.y - y) > epsilon) return false;
		if(Math.abs(obj.z - z) > epsilon) return false;
		if(Math.abs(obj.w - w) > epsilon) return false;
		return true;
	}
	
	/**
	 * Compares this vector with the other vector, using the supplied
	 * epsilon for fuzzy equality testing.
	 * @return whether the vectors are the same.
	 */
	public boolean epsilonEquals(float x, float y, float z, float w, float epsilon) {
		if(Math.abs(x - this.x) > epsilon) return false;
		if(Math.abs(y - this.y) > epsilon) return false;
		if(Math.abs(z - this.z) > epsilon) return false;
		if(Math.abs(w - this.w) > epsilon) return false;
		return true;
	}
	
	/** Scales the vector components by the given scalars.
	 * 
	 * @param scalarX
	 * @param scalarY
	 * @param scalarZ */
	public Vector4 scale (float scalarX, float scalarY, float scalarZ, float scalarW) {
		x *= scalarX;
		y *= scalarY;
		z *= scalarZ;
		w *= scalarW;
		return this;
	}
}
