package com.gromstudio.treckar.util;

import android.util.Log;

public class MathUtils {

	static final String TAG = "MathUtils";

	/**
	 * Converts this vector into a normalized (unit length) vector
	 * <b>Modifies the input parameter</b>
	 * @param vector The vector to normalize
	 **/
	public static void normalize(int size, float[] vector, int offset) {
		multiply(size, vector, offset, 1/magnitude(size, vector, offset));
	}

	/**
	 * Copy a vector from <code>from</code> into <code>to</code>
	 * @param from The source
	 * @param to The destination
	 **/
	public static void copy(int size, float[] from, int offsetFrom, float[] to, int offsetTo) {
		for (int i=0;i<from.length;i++) {
			to[i] = from[i];
		}
	}

	/**
	 * Multiply a vector by a scalar.  <b>Modifies the input vector</b>
	 * @param vector The vector 
	 * @param scalar The scalar
	 **/
	public static void multiply(int size, float[] vector, int offset, float scalar) {
		for (int i=0;i<size;i++)
			vector[offset+i] *= scalar;
	}

	/**
	 * Compute the dot product of two vectors
	 * @param v1 The first vector
	 * @param v2 The second vector
	 * @return v1 dot v2
	 **/
	public static float dot(int size, float[] v1, int offsetV1, float[] v2, int offsetV2) {
		float res = 0;
		for (int i=0;i<size;i++)
			res += v1[offsetV1+i]*v2[offsetV2+i];
		return res;
	}

	/**
	 * Compute the cross product of two vectors
	 * @param v1 The first vector
	 * @param v2 The second vector
	 * @param result Where to store the cross product
	 **/
	public static void cross3f(float[] p1, float[] p2, float[] result) {
		result[0] = p1[1]*p2[2]-p2[1]*p1[2];
		result[1] = p1[2]*p2[0]-p2[2]*p1[0];
		result[2] = p1[0]*p2[1]-p2[0]*p1[1];
	}

	/**
	 * Compute the magnitude (length) of a vector
	 * @param vector The vector
	 * @return The magnitude of the vector
	 **/
	public static float magnitude(int size, float[] vector, int offset) {
		float tmp = 0.0f;
		for ( int i=offset; i <(offset+size); i++ ) {
			tmp += vector[i]*vector[i];
		}
		return (float)Math.sqrt(tmp);
	}

	/**
	 * Homogenize a point (divide by its last element)
	 * @param pt The point <b>Modified</b>
	 **/
	public static void homogenize(int size, float[] pt, int offset)
	{
		multiply(size, pt, offset, 1/pt[3]);
	}

	/**
	 * Pretty print a vector
	 * @param vec The vector to print
	 **/
	public static void printVector(int size, float[] vec, int offset) {
		for (int i=offset;i<size+offset;i++) {
			Log.e(TAG, ""+vec[i]);
		}
	}

	/**
	 * Subtracts two vectors (a-b).
	 * @param a The first vector
	 * @param b The second vector
	 * @param result Storage for the result, if null, do nothing
	 **/
	public static void sub(int size, float[] a, int offsetA,float[] b, int offsetB, float[] result, int offsetResult) {
		if ( result==null ) {
			return;
		}
		for (int i=0;i<size;i++)
			result[offsetResult+i] = a[offsetA+i]-b[offsetB+i];
	}

	/**
	 * Adds two vectors (a+b).
	 * @param a The first vector
	 * @param b The second vector
	 * @param result Storage for the result, if null, do nothing.
	 **/
	public static void add(int size, float[] a, int offsetA, float[] b, int offsetB, float[] result, int offsetResult) {
		if ( result==null ) {
			return;
		}
		for (int i=0;i<size;i++)
			result[offsetResult+i] = a[offsetA+i]+b[offsetB+i];
	}
	
}

