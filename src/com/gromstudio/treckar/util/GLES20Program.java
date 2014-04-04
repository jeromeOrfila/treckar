package com.gromstudio.treckar.util;

import android.opengl.GLES20;
import android.opengl.Matrix;

public abstract class GLES20Program {

	float [] mMVPMatrix = new float[16];
	
	public abstract void initialize();
	
	public abstract int getProgramHandle();

	public abstract String getVertexShader();
	
	public abstract String getFragmentShader();
	
	public abstract int getPositionHandle();

	public abstract int getColorHandle();
	
	public abstract int getMVMatrixHandle();

	public abstract int getMVPMatrixHandle();
	
	public abstract float[] getViewMatrix();
	
	public abstract float[] getProjectionMatrix();
	
	public abstract void setViewMatrix(final float[] value);

	public abstract void setProjectionMatrix(final float[] value);
	
	public void glPrepareMVPMatrix(float[] modelMatrix) {
		
        Matrix.multiplyMM(mMVPMatrix, 0, getViewMatrix(), 0, modelMatrix, 0);
        
        GLES20.glUniformMatrix4fv(getMVMatrixHandle(), 1, false, mMVPMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, getProjectionMatrix(), 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);

	}

}
