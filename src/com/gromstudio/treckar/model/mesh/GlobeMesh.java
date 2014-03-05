package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.GLES20Program;
import com.gromstudio.treckar.util.GLES20Renderer;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class GlobeMesh extends MeshES20 {

	static final String TAG ="WorldMesh";
	
	FloatBuffer mVertices;
	FloatBuffer mColors;
	ShortBuffer mIndices;
	
	int mNbPoints = 0;
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;

	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	

	/** How many elements per vertex. */
	private final int mPositionStrideBytes = mPositionDataSize* BYTES_PER_FLOAT;
	
	/** How many elements per vertex. */
	private final int mColorStrideBytes = mColorDataSize * BYTES_PER_FLOAT;
	
	float[] mModelMatrix = new float[16];
    

    public GlobeMesh() {
		// This triangle is red, green, and blue.

    	mNbPoints = 360*180;
				// Initialize the buffers.
		mVertices = ByteBuffer.allocateDirect( (mNbPoints+1)* mPositionDataSize* BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		mColors = ByteBuffer.allocateDirect((mNbPoints+1)*mColorDataSize* BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		mIndices = ByteBuffer.allocateDirect(mNbPoints * 2 * 2 *  BYTES_PER_SHORT)
		    .order(ByteOrder.nativeOrder())
		    .asShortBuffer();
	
		mIndices.position(0);
		    
//		mIndicesBuffer =  ByteBuffer.allocateDirect(mNbPoints * mBytesPerShort)
//				.order(ByteOrder.nativeOrder())
//				.asShortBuffer();

		mVertices.position(0);
//		mIndicesBuffer.position(0);
		mColors.position(0);
		
		mVertices.put(0.0f);
		mVertices.put(0.0f);
		mVertices.put(0.0f);
		
		mColors.put(0.2f);
		mColors.put(0.2f);
		mColors.put(0.2f);
		mColors.put(0.2f);

		int i = 0;
		float[] geoc = new float[3];
		float[] geod = new float[3];
		float[] hsv = new float[3];
		hsv[1] = 1.0f;
		hsv[2] = 1.0f;
		for ( int lgt = -180; lgt<180; lgt++) {
			for ( int lat=-90; lat<90; lat++) {
				
				geod[0] = (float) lat;
				geod[1] = (float) lgt;
				geod[2] = 0;
				CoordinateConversions.getGeocCoords(geod, geoc);
				
//				mIndicesBuffer.put((short)(i++));
				mVertices.put(geoc[0]);
				mVertices.put(geoc[1]);
				mVertices.put(geoc[2]);
				
				hsv[0] = 2.0f*(lat+90.0f);
				int c = Color.HSVToColor(hsv);
				mColors.put(0.2f);
				mColors.put(0.2f);
				mColors.put(0.2f);
				mColors.put(0.2f);
				
				mIndices.put((short)0);
				mIndices.put((short)i);
				
				i++;
			}
		}
		Matrix.setIdentityM(mModelMatrix, 0);
	}

    @Override
	public void drawMesh(GLES20Program program, final float[] model) {

		GLES20.glEnableVertexAttribArray(program.getPositionHandle());
		checkGlError("glEnableVertexAttribArray");

    	mVertices.position(0);
		GLES20.glVertexAttribPointer(program.getPositionHandle(), 
				mPositionDataSize,
                GLES20.GL_FLOAT, false,
                mPositionStrideBytes,
                mVertices);
		
		checkGlError("glVertexAttribPointer");
		
		GLES20.glEnableVertexAttribArray(program.getColorHandle());
		checkGlError("glEnableVertexAttribArray");
		
		mColors.position(0);
		GLES20.glVertexAttribPointer(program.getColorHandle(), 
        		mColorDataSize, 
        		GLES20.GL_FLOAT, 
        		false, 
        		mColorStrideBytes, 
        		mColors);
		checkGlError("glVertexAttribPointer");
		
		program.glPrepareMVPMatrix(model);

//		mIndicesBuffer.position(0);

		//GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mNbPoints);
		GLES20.glDrawElements(GLES20.GL_LINES, mNbPoints, GLES20.GL_UNSIGNED_SHORT, mIndices);
                
	}

}
