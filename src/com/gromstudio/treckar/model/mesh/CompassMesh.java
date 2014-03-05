package com.gromstudio.treckar.model.mesh;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class CompassMesh extends Mesh {

	SquareMesh mPlaneMesh;
	SquareMesh mXMesh;
	SquareMesh mYMesh;
	
	public CompassMesh() {
		
		mPlaneMesh = new SquareMesh(Mesh.AXIS_Z);
		mXMesh = new SquareMesh(Mesh.AXIS_X);
		mYMesh = new SquareMesh(Mesh.AXIS_Y);
		
		addSubMesh(mXMesh);
		addSubMesh(mYMesh);
		addSubMesh(mPlaneMesh);
	
	}
	
	@Override
	public void loadTextures(GL10 gl, Context context) {
		mPlaneMesh.loadGLTexture(gl, context, com.gromstudio.treckar.R.drawable.compass_ground);
		mXMesh.loadGLTexture(gl, context, com.gromstudio.treckar.R.drawable.compass_axis_x);
		mYMesh.loadGLTexture(gl, context, com.gromstudio.treckar.R.drawable.compass_axis_x);
	}
}
