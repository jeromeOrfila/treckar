package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import com.gromstudio.treckar.util.GLES20Program;
import com.gromstudio.treckar.util.GLES20Renderer;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class LineMesh extends MeshES20 {

	FloatBuffer mVertices;
	FloatBuffer mColors;
	ByteBuffer mIndexBuffer;

    public LineMesh(float x1, float y1, float z1, float x2, float y2, float z2, int color, int color2) {
		
		// Initialize the buffers.
    	mVertices = ByteBuffer.allocateDirect(2*POSITION_STRIDE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
    	
    	mColors = ByteBuffer.allocateDirect(2*COLOR_STRIDE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

    	mVertices.put(x1);
    	mVertices.put(y1);
    	mVertices.put(z1);
    	mVertices.put(x2);
    	mVertices.put(y2);
    	mVertices.put(z2);
    	mVertices.position(0);

    	mColors.put((float)Color.red(color)/255.0f);
    	mColors.put((float)Color.green(color)/255.0f);
    	mColors.put((float)Color.blue(color)/255.0f);
    	mColors.put(1.0f);
    	mColors.put((float)Color.red(color2)/255.0f);
    	mColors.put((float)Color.green(color2)/255.0f);
    	mColors.put((float)Color.blue(color2)/255.0f);
    	mColors.put(1.0f);
    	mColors.position(0);
    	
    	mIndexBuffer = ByteBuffer.allocateDirect(2);
		mIndexBuffer.put((byte)0);
		mIndexBuffer.put((byte)1);
		mIndexBuffer.position(0);

	}

    @Override
	public void destroyMesh() {
		super.destroyMesh();
		
		mVertices.clear();
		mColors.clear();
		mIndexBuffer.clear();
		
		mVertices= null;
		mColors = null;
		mIndexBuffer = null;
	}
    
    @Override
	public void drawMesh(GLES20Program program, final float[] model) {

		if ( mVertices==null||mColors==null||mIndexBuffer==null) {
			return;
		}
		
		mVertices.position(0);
		mColors.position(0);
		mIndexBuffer.position(0);

		GLES20.glEnableVertexAttribArray(program.getPositionHandle());
		checkGlError("glEnableVertexAttribArray");

		GLES20.glVertexAttribPointer(program.getPositionHandle(), 
				POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                POSITION_STRIDE_BYTES,
                mVertices);
		
		checkGlError("glVertexAttribPointer");
		
		GLES20.glEnableVertexAttribArray(program.getColorHandle());
		checkGlError("glEnableVertexAttribArray");
		
		
		GLES20.glVertexAttribPointer(program.getColorHandle(), 
        		COLOR_DATA_SIZE, 
        		GLES20.GL_FLOAT, 
        		false, 
        		COLOR_STRIDE_BYTES, 
        		mColors);
		checkGlError("glVertexAttribPointer");
		
		program.glPrepareMVPMatrix(model);
        

		GLES20.glDrawElements(GL10.GL_LINES, 2, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        checkGlError("glDrawElements");
        
        mVertices.position(0);
        mColors.position(0);

        GLES20.glDisableVertexAttribArray(program.getPositionHandle());
        GLES20.glDisableVertexAttribArray(program.getColorHandle());
                        
	}

}
