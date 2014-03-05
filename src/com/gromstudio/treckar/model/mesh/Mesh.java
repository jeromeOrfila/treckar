package com.gromstudio.treckar.model.mesh;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.gromstudio.treckar.util.GLES20Renderer;

import android.content.Context;
import android.opengl.Matrix;

public class Mesh {

	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_Z = 2;
	
	static final float[]  IDENTITY_MATRIX  = new float[] {1, 0, 0, 0,  
											0, 1, 0, 0, 
											0, 0, 1, 0, 
											0, 0, 0, 1};
	
	public static class SubMesh {
		Mesh mesh;
		float[] matrix;
		public SubMesh(Mesh msh, float[] mtx) {
			mesh = msh;
			matrix = Arrays.copyOf(mtx, mtx.length);
		}
	}
	
	List<SubMesh> mElements;
	
	public Mesh() {
		mElements = null;
	}
	
	
	public void loadSubMeshesTextures(GL10 gl, Context context) {
		if ( null!=mElements ) {
			for ( SubMesh sm : mElements) {
				sm.mesh.loadTextures(gl, context);
			}
		}
	}
	
	public void loadTextures(GL10 gl, Context context) {
		
		loadSubMeshesTextures(gl, context);
		
	}
	
	public void addSubMesh(Mesh mesh, float[] matrix) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMesh>();
		}
		mElements.add(new SubMesh(mesh, matrix));
	}
	
	public void addSubMesh(Mesh mesh) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMesh>();
		}
		mElements.add(new SubMesh(mesh, IDENTITY_MATRIX));
	}
	
	public void addSubMesh(SubMesh mesh) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMesh>();
		}
		mElements.add(mesh);
	}
	
	public List<SubMesh> getSubMeshes() {
		return mElements;
	}
	
	public void removeSubMesh(Mesh mesh) {
		if ( null!=mElements ) {
			
			Iterator<SubMesh> it = mElements.iterator();
			while ( it.hasNext() ) {
				SubMesh sm = it.next();
				if ( sm.mesh == mesh ) {
					it.remove();
				}
			}
		}
	}
	
	public void clearSubMeshes() {
		mElements.clear();
	}
	
	
	protected void drawSubMeshes(GL10 gl) {
		if ( null!=mElements) {
			for (SubMesh sm : mElements) {
				gl.glPushMatrix();
				gl.glMultMatrixf( FloatBuffer.wrap(sm.matrix));
				sm.mesh.draw(gl);
				gl.glPopMatrix();
			}
		}
	}
	
	protected void drawSubMeshesES20(GLES20Renderer renderer, final float[] modelMtx) {
		
		float[] modelMatrix = new float[16];
		
		if ( null!=mElements) {
			for (SubMesh sm : mElements) {
				
				Matrix.multiplyMM(modelMatrix, 0, modelMtx, 0, sm.matrix, 0);
				sm.mesh.drawGLES20(renderer, modelMatrix);

			}
		}
	}
	
	public void draw(GL10 gl) {
		
		drawSubMeshes(gl);
		
	}
		
	public void drawGLES20(GLES20Renderer renderer, final float[] modelMtx) {
		
		drawSubMeshesES20(renderer, modelMtx);
		
	}
}
