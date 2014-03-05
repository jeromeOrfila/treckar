package com.gromstudio.treckar.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TriangleMesh;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;


public class GLES20Renderer implements GLSurfaceView.Renderer  {

	/**
	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
	 * of being located at the center of the universe) to world space.
	 */
	protected float[] mModelMatrix = new float[16];

	protected MeshES20 mRootMesh;

	private GLES20Program mProgram;


	/**
	 * Initialize the model data.
	 */
	public GLES20Renderer() {
		mProgram = new GLES20Program();
		mRootMesh = null;
	}

	public void setMesh(MeshES20 mesh) {
		mRootMesh = mesh;
	}

	public GLES20Program getProgram() {
		return mProgram;
	}
	 
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

		// Set the background clear color to gray.
		GLES20.glClearColor(0.f, 0.f, 0.f, 0.f);

		mProgram.setViewMatrix(getViewMatrix());

		mProgram.initialize();

		// Tell OpenGL to use this program when rendering.
		GLES20.glUseProgram(mProgram.getProgramHandle());

	}	


	protected float[] getViewMatrix() {

		float[] result = new float[16];

		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(result, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		return result;

	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		mProgram.setProjectionMatrix(getProjectionMatrix(width, height));

	}

	protected float[] getProjectionMatrix(int width, int height) {

		float[] result = new float[16];
		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = .5f;
		final float far = 10.0f;

		Matrix.frustumM(result, 0, left, right, bottom, top, near, far);		

		return result;
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        

		Matrix.setIdentityM(mModelMatrix, 0);
		if ( mRootMesh != null ) {
			mRootMesh.drawMesh(mProgram, mModelMatrix);
		}

	}
	


}