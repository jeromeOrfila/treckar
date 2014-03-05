package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import com.gromstudio.treckar.util.GLES20Program;
import com.gromstudio.treckar.util.GLES20Renderer;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class PointMesh extends MeshES20 {

	FloatBuffer mVertices;
	FloatBuffer mColors;

    public PointMesh(float x, float y, float z, int color) {
		
		// Initialize the buffers.
    	mVertices = ByteBuffer.allocateDirect(POSITION_STRIDE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
    	
    	mColors = ByteBuffer.allocateDirect(COLOR_STRIDE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

    	mVertices.put(x);
    	mVertices.put(y);
    	mVertices.put(z);
    	mVertices.position(0);

    	mColors.put((float)Color.red(color)/255.0f);
    	mColors.put((float)Color.green(color)/255.0f);
    	mColors.put((float)Color.blue(color)/255.0f);
    	mColors.put(1.0f);
    	mColors.position(0);

	}

    @Override
	public void drawMesh(GLES20Program program, final float[] model) {
		
		GLES20.glEnableVertexAttribArray(program.getPositionHandle());
		checkGlError("glEnableVertexAttribArray");

    	mVertices.position(0);
		GLES20.glVertexAttribPointer(program.getPositionHandle(), 
				POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                POSITION_STRIDE_BYTES,
                mVertices);
		
		checkGlError("glVertexAttribPointer");
		
		GLES20.glEnableVertexAttribArray(program.getColorHandle());
		checkGlError("glEnableVertexAttribArray");
		
		mColors.position(0);        
		GLES20.glVertexAttribPointer(program.getColorHandle(), 
        		COLOR_DATA_SIZE, 
        		GLES20.GL_FLOAT, 
        		false, 
        		COLOR_STRIDE_BYTES, 
        		mColors);
		checkGlError("glVertexAttribPointer");
		
		program.glPrepareMVPMatrix(model);

		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
                        
	}

}
