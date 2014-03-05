package com.gromstudio.treckar.util;

import javax.microedition.khronos.opengles.GL10;

import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;
import com.gromstudio.treckar.util.Geolocation.GeolocationListener;

import android.content.Context;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class GLES20ImmRenderer extends GLES20Renderer implements GeolocationListener, OnCompassChangedListener  {

	static final String TAG = "GLES20ARRenderer";
	Context mContext;
	
	float[] mRotation = new float[16];
	float[] mEyec = new float[] {0, 0, 1.5f};
	float[] mLookatc = new float[] {0,0,0};
	float[] mUpc = new float[] {0,1.0f,0, 1};
	
	
	static final float TOP_VIEW_FACTOR = 0.002f * CoordinateConversions.RADIUS;
	
	Compass mCompass;
	public static final boolean TEST = true;
	
	public GLES20ImmRenderer(Context context) {
		
		mContext = context;
		
		mCompass = Compass.getInstance(context);
		mCompass.addCompassChangeListener(this);
		
		Geolocation geolocation = Geolocation.getInstance();
		if ( !geolocation.isInitialized() ) {
			geolocation.initialize(context.getApplicationContext());
		}

		geolocation.addGeolocationListener(context, this);
	}

	protected float[] getViewMatrix() {
		
		float[] viewMatrix = new float[16];
		Matrix.setIdentityM(viewMatrix, 0);
		
		//float[] mat = new float[16];
		
		mEyec[0]=0;
		mEyec[1]=0;
		mEyec[2]=1.5f;
		
		mLookatc[0] = 0;
		mLookatc[1] = 0;
		mLookatc[2] = 0;
		
		mUpc[0] = 0;
		mUpc[1] = 1;
		mUpc[2] = 0;
		mUpc[3] = 1;		
		
		float[] view = new float[3];
		if ( TEST ) {
		
			float[] geod = new float[] {45.1555234f, 5.63582604f, 3000.0f} ;
			CoordinateConversions.getGeocCoords(geod, mEyec);

			view[0] = mEyec[0];
			view[1] = mEyec[1];
			view[2] = mEyec[2];
			MathUtils.normalize(3, view, 0);
			
			//get tangent
			float[] tan = new float[] {0, 0, 0};
			MathUtils.cross3f(view, new float[] {0.0f, 1.0f, 0.0f},  tan);
			MathUtils.normalize(3, tan, 0);
			
			MathUtils.cross3f(view, tan, mUpc);

		}
		
		computeViewMatrix(getProgram().getViewMatrix());
		
		return viewMatrix;
	}

	protected float[] getProjectionMatrix(int width, int height) {
		float[] result = new float[16];

//		float diam = TOP_VIEW_FACTOR;
//		//return super.getProjectionMatrix(width, height);
//		// Create a new perspective projection matrix. The height will stay the same
//		// while the width will vary as per aspect ratio.
//		final float ratio = (float) width / height;
//		final float left = -ratio*diam;
//		final float right = ratio*diam;
//		final float bottom = -diam;
//		final float top = diam;
//		final float near = .5f*diam;
//		final float far = 10.0f*diam;
//
//		Matrix.frustumM(result, 0, left, right, bottom, top, near, far);		
		
//		float diam = 10000;
//		//return super.getProjectionMatrix(width, height);
//		float[] result = new float[16];
//		// Create a new perspective projection matrix. The height will stay the same
//		// while the width will vary as per aspect ratio.
//		final float ratio = (float) width / height;
//		final float left = -ratio*diam/10;
//		final float right = ratio*diam/10;
//		final float bottom = -diam/10;
//		final float top = diam/10;
//		final float near = 1f;
//		final float far = 10000.0f;
//
		Matrix.perspectiveM(result, 0, 45.0f, ratio, 0.250f, 1024.0f);
////		Matrix.perspectiveM(result, 0, 180, ratio, 1.0f, 10000.0f);
//		Matrix.frustumM(result, 0, left, right, bottom, top, near, far);		
//		return result;

	
		return result;
	}

	@Override
	public void onLocationChanged(Location location, long waitingTime) {
		
	}

	@Override
	public void onCompassChanged(Compass compass) {
	}
	

	@Override
	public void onDrawFrame(GL10 glUnused) {
		
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        

		computeViewMatrix(getProgram().getViewMatrix());
		
		Matrix.setIdentityM(mModelMatrix, 0);
		if ( mRootMesh != null ) {
			mRootMesh.drawMesh(getProgram(), mModelMatrix);
		}

	}
	
	private void computeViewMatrix(float[] viewMatrix) {
	

//      COMMENTED		
//		float[] view = new float[3];
//		view[0] = mEyec[0];
//		view[1] = mEyec[1];
//		view[2] = mEyec[2];
//		MathUtils.normalize(3, view, 0);
//		
//		//get tangent
//		float[] tan = new float[] {0, 0, 0};
//		MathUtils.cross3f(view, new float[] {0.0f, 1.0f, 0.0f},  tan);
//		MathUtils.normalize(3, tan, 0);
//		
//		MathUtils.cross3f(view, tan, mUpc);
//		
//		float angle = -mCompass.getVerticalRotationAngle();
//		
//		Matrix.setIdentityM(mRotation, 0);
//		Matrix.rotateM(mRotation, 0, mRotation, 0, angle, view[0], view[1], view[2]);
//		Matrix.multiplyMV(mUpc, 0, mRotation, 0, mUpc, 0);
//		
		
		// This works
		Matrix.setLookAtM(mRotation, 0, 
				mEyec[0], mEyec[1], mEyec[2], 
				mLookatc[0], mLookatc[1], mLookatc[2],
				mUpc[0], mUpc[1], mUpc[2]);
		
		//Matrix.rotateM(mRotation, 0, mRotation, 0, 90, 0, 1, 0);

		Matrix.multiplyMM(viewMatrix, 0, mCompass.getRotationMatrix(), 0, mRotation, 0);
		
	}
}
