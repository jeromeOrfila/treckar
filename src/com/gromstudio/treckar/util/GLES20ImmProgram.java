package com.gromstudio.treckar.util;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class GLES20ImmProgram extends GLES20DefaultProgram {


	public String getVertexShader() {

		final String vertexShader =
			"uniform mat4 u_MVPMatrix;           \n"		// A constant representing the combined model/view/projection matrix.
		  + "uniform mat4 u_MVMatrix;            \n"		// A constant representing the combined model/view matrix.

		  + "attribute vec4 a_Position;          \n"		// Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;             \n"		// Per-vertex color information we will pass in.			  
		  + "attribute vec4 a_Normal;            \n"
		  
		  + "varying vec4 v_Position;            \n"		// This will be passed into the fragment shader.
		  + "varying vec4 v_Color;               \n"		// This will be passed into the fragment shader.
		  + "varying vec4 v_Normal;              \n"		// This will be passed into the fragment shader.
		  + "varying vec4 v_EyeVector;           \n"		// This will be passed into the fragment shader.
		  
		  + "void main()                         \n"		// The entry point for our vertex shader.
		  + "{                                   \n"
		  + "   v_Position = vec4(u_MVMatrix       "
		  + "                 * a_Position);     \n"
		  + "   v_Color = a_Color;               \n"		// Pass the color through to the fragment shader. 
		  + "   v_Normal = vec4(u_MVMatrix         "
		  + "                 * a_Normal);\n"		// Pass the color through to the fragment shader.
		  + "   v_EyeVector = vec4(u_MVMatrix *    "
		  + "        vec4(a_Position));\n"
		  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
		  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
		  + "}                              \n";    // normalized screen coordinates.
		return vertexShader;
	}
		
	public String getFragmentShader() {
		
		
		final String fragmentShader =
				"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
			  										// precision in the fragment shader.				
			  + "varying vec4 v_Position;          \n"		// This is the color from the vertex shader interpolated across the 
			  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
			  + "varying vec4 v_Normal;          \n"		// This is the color from the vertex shader interpolated across the 
			  + "varying vec4 v_EyeVector;          \n"		// This is the color from the vertex shader interpolated across the 
			  											// triangle per fragment.			  
			  + "void main()                                                                     \n"// The entry point for our fragment shader.
			  + "{                                                                               \n"
			  + "                                                                                \n"
			  + "   gl_FragColor = v_Color * vec4(1.0, 1.0, 1.0, 0.50);     \n"		// Pass the color directly through the pipeline.	
			  + "}                              \n";
		return fragmentShader;
	}

	protected void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("opengl", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}
