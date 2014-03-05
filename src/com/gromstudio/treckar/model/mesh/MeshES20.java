package com.gromstudio.treckar.model.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.gromstudio.treckar.util.GLES20Program;

public class MeshES20 {

	/** How many bytes per float. */
	protected static final int BYTES_PER_FLOAT = 4;

	/** How many bytes per short. */
	protected static final int BYTES_PER_SHORT = 2;

	/** Size of the position data in elements. */
	protected static final int POSITION_DATA_SIZE = 3;

	/** How many elements per vertex. */
	protected static final int POSITION_STRIDE_BYTES = POSITION_DATA_SIZE * BYTES_PER_FLOAT;
	
	/** Size of the position data in elements. */
	protected static final int COLOR_DATA_SIZE = 4;

	/** How many elements per vertex. */
	protected static final int COLOR_STRIDE_BYTES = COLOR_DATA_SIZE * BYTES_PER_FLOAT;
	
	
	static final float[]  IDENTITY_MATRIX  = new float[] {1, 0, 0, 0,  
		0, 1, 0, 0, 
		0, 0, 1, 0, 
		0, 0, 0, 1};

	public static class SubMeshES20 {
		MeshES20 mesh;
		float[] matrix;
		public SubMeshES20(MeshES20 msh, final float[] mtx) {
			mesh = msh;
			matrix = Arrays.copyOf(mtx, mtx.length);
		}
	}
	
	List<SubMeshES20> mElements;
	
	private CoordinateSystemMesh mCoordsMesh;
	
	public MeshES20() {
		mElements = null;
		mCoordsMesh = null;
	}
	
	public void setCoordinatesVisibility(boolean visible) {
		if ( visible && null==mCoordsMesh ) {
			mCoordsMesh = new CoordinateSystemMesh();
		} else {			
			if ( mCoordsMesh!=null ) {
				mCoordsMesh.destroyMesh();
			}
			mCoordsMesh = null;
		}
	}
	
	public void loadSubMeshesTextures(GL10 gl, Context context) {
		if ( null!=mElements ) {
			for ( SubMeshES20 sm : mElements) {
				sm.mesh.loadTextures(gl, context);
			}
		}
	}
	
	public void loadTextures(GL10 gl, Context context) {
		
		loadSubMeshesTextures(gl, context);
		
	}
	
	public void addSubMesh(MeshES20 mesh, float[] matrix) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMeshES20>();
		}
		mElements.add(new SubMeshES20(mesh, matrix));
	}
	
	public void addSubMesh(MeshES20 mesh) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMeshES20>();
		}
		mElements.add(new SubMeshES20(mesh, IDENTITY_MATRIX));
	}
	
	public void addSubMesh(SubMeshES20 mesh) {
		if ( null==mElements ) {
			mElements = new ArrayList<SubMeshES20>();
		}
		mElements.add(mesh);
	}
	
	public List<SubMeshES20> getSubMeshes() {
		return mElements;
	}
	
	public void removeSubMesh(MeshES20 mesh) {
		if ( null!=mElements ) {
			
			Iterator<SubMeshES20> it = mElements.iterator();
			while ( it.hasNext() ) {
				SubMeshES20 sm = it.next();
				if ( sm.mesh == mesh ) {
					it.remove();
				}
			}
		}
	}
	
	protected void drawSubMeshes(GLES20Program program, float[] modelMatrix) {
		
		float[] model = new float[16];
		if ( null!=mElements) {
			for (SubMeshES20 sm : mElements) {
				
				if ( sm.mesh!=null ) {
					Matrix.multiplyMM(model, 0, sm.matrix, 0, modelMatrix, 0);
					sm.mesh.drawMesh(program, model);
				}
			}
		}
		
	}

	public void destroySubMeshes() {
		for (SubMeshES20 sm : mElements) {
			if ( null!=sm.mesh ) {
                sm.mesh.destroyMesh();
			}
		}
		mElements.clear();
	}
	
	public void destroyMesh() {
		destroySubMeshes();

		if ( mCoordsMesh!=null ) {
			mCoordsMesh.destroyMesh();
			mCoordsMesh = null;
		}
		
	}
	
	public void drawMesh(GLES20Program program, float[] modelMatrix) {
		
		if (null!=mCoordsMesh) {
			mCoordsMesh.drawMesh(program, modelMatrix);
		}
		drawSubMeshes(program, modelMatrix);
		
	}
	
	protected void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("opengl", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
	
	
}
