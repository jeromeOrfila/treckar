package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.gromstudio.treckar.util.GLES20Program;

public class CoordinateSystemMesh extends MeshES20 {
	
	private FloatBuffer mColorBuffer;
	private FloatBuffer mVertexBuffer;
	private ByteBuffer mIndexBuffer;
	int nbPoints = 4;

	public CoordinateSystemMesh() {
		
		float vertices[] = {
				0,0,0,
				1,0,0,
				0,1,0,
				0,0,1
		};
		float colors[] = {
				0,0,0,0,
				1,0,0,1,
				0,1,0,1,
				0,0,1,1
		};
		byte indices[] = { 0, 1, 0, 2, 0, 3 };

		
		ByteBuffer vbb;
		vbb = ByteBuffer.allocateDirect(nbPoints*POSITION_STRIDE_BYTES);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		vbb = ByteBuffer.allocateDirect(nbPoints*COLOR_STRIDE_BYTES);
		vbb.order(ByteOrder.nativeOrder());
		mColorBuffer = vbb.asFloatBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	@Override
	public void destroyMesh() {
		super.destroyMesh();
		
		mVertexBuffer.clear();
		mColorBuffer.clear();
		mIndexBuffer.clear();
		
		mVertexBuffer = null;
		mColorBuffer = null;
		mIndexBuffer = null;
	}
	

	@Override
	public void drawMesh(GLES20Program program, final float[] model) {
		
		if ( mVertexBuffer==null||mColorBuffer==null||mIndexBuffer==null) {
			return;
		}
		
		GLES20.glEnableVertexAttribArray(program.getPositionHandle());
		checkGlError("glEnableVertexAttribArray");

		GLES20.glVertexAttribPointer(program.getPositionHandle(), 
				POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                POSITION_STRIDE_BYTES,
                mVertexBuffer);
		
		checkGlError("glVertexAttribPointer");
		
		GLES20.glEnableVertexAttribArray(program.getColorHandle());
		checkGlError("glEnableVertexAttribArray");
		
		
		GLES20.glVertexAttribPointer(program.getColorHandle(), 
        		COLOR_DATA_SIZE, 
        		GLES20.GL_FLOAT, 
        		false, 
        		COLOR_STRIDE_BYTES, 
        		mColorBuffer);
		checkGlError("glVertexAttribPointer");
		
		program.glPrepareMVPMatrix(model);
        
		mVertexBuffer.position(0);
		mColorBuffer.position(0);
		mIndexBuffer.position(0);

		GLES20.glDrawElements(GL10.GL_LINES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        checkGlError("glDrawElements");
//        
        mVertexBuffer.position(0);
        mColorBuffer.position(0);
		//GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPointsCount);

        GLES20.glDisableVertexAttribArray(program.getPositionHandle());
        GLES20.glDisableVertexAttribArray(program.getColorHandle());
        
	}
	
}
