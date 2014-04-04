package com.gromstudio.treckar.util;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.view.animation.Animation;

import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;
import com.gromstudio.treckar.util.Geolocation.GeolocationListener;

public class GLES20ImmRenderer extends GLES20Renderer implements GeolocationListener, OnCompassChangedListener  {

	static final String TAG = "GLES20ARRenderer";
	Context mContext;
	
	static final float TOP_VIEW_FACTOR = 0.0005f * CoordinateConversions.RADIUS;
	
	float[] mRotation = new float[16];
	float[] mEyec = new float[] {0.0f, 0.0f, 0.0f};
	float[] mLookatc = new float[] {0.0f, 0.0f, 0.0f};
	float[] mUpc = new float[] {0, 1.0f, 0.0f, 1.0f};
	Compass mCompass;
	float mUpDownFactor = 0.0f;
	public static final boolean TEST = true;
	Handler mHandler;
	UpDownRunnable mUpDownRunnable;
	
	public GLES20ImmRenderer(Context context) {
		
		mContext = context;
		
		mCompass = Compass.getInstance(context);
		mCompass.addCompassChangeListener(this);
		
		mHandler = new Handler(context.getMainLooper());
		mUpDownRunnable = null;
		
		Geolocation geolocation = Geolocation.getInstance();
		if ( !geolocation.isInitialized() ) {
			geolocation.initialize(context.getApplicationContext());
		}

		geolocation.addGeolocationListener(context, this);
	}
	
	@Override
	protected GLES20Program instanciateProgram() {
		return new GLES20ImmProgram();
	}

	protected float[] getViewMatrix() {
		
		float[] viewMatrix = new float[16];
		Matrix.setIdentityM(viewMatrix, 0);
		
		mEyec[0] = 0.0f;
		mEyec[1] = 0.0f;
		mEyec[2] = 1.0f;
		
		mLookatc[0] = 0.0f;
		mLookatc[1] = 0.0f;
		mLookatc[2] = 0.0f;
		
		mUpc[0] = 0.0f;
		mUpc[1] = 1.0f;
		mUpc[2] = 0.0f;
		mUpc[3] = 1.0f;

		updateViewVectors();

		computeViewMatrix(getProgram().getViewMatrix());

		return viewMatrix;
	}

	protected float[] getProjectionMatrix(int width, int height) {
		float[] result = new float[16];

		final float ratio = (float) width / height;

		float[] camFOV = CameraUtils.getCameraFOV();
		float camRatio = camFOV[0] / camFOV[1];                          // = 1.2845...

		float displayWidth =mContext.getResources().getDisplayMetrics().widthPixels;     // = 800
		float displayHeight = mContext.getResources().getDisplayMetrics().heightPixels;    // = 480
		float displayRatio = displayWidth / displayHeight;     // = 1.666...
		float trueVerticalFOV = camFOV[0] / displayRatio;  // = 36.30...
		Matrix.perspectiveM(result, 0, trueVerticalFOV, ratio, 500f, 500000.0f);
		return result;
	
	}

	public void cameraUp () {
		if (null!=mUpDownRunnable) {
			mUpDownRunnable.cancel();
		}
		mUpDownRunnable = new UpDownRunnable(1); 
		mHandler.post(mUpDownRunnable);
	}
	
	public void cameraDown () {
		if (null!=mUpDownRunnable) {
			mUpDownRunnable.cancel();
		}
		mUpDownRunnable = new UpDownRunnable(0); 
		mHandler.post(mUpDownRunnable);
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
	
	private void updateViewVectors() {

		CoordinateConversions.getGeocCoords(Geolocation.getInstance().getLastLocation(mContext), mEyec);

		MathUtils.multiply(1,mEyec, 0, 1.0f + 0.01f*mUpDownFactor);

		mUpc[0] = mEyec[0];
		mUpc[1] = mEyec[1];
		mUpc[2] = mEyec[2];
		MathUtils.normalize(3, mUpc, 0);
		
		//get lookat
		float[] tan = new float[] {0, 0, 0};
		MathUtils.cross3f(mUpc, new float[] {0, 1, 0},  mLookatc);
		MathUtils.normalize(3, mLookatc, 0);
	}

	private void computeViewMatrix(float[] viewMatrix) {
		// This works
		Matrix.setLookAtM(mRotation, 0, 
				mEyec[0], mEyec[1], mEyec[2], 
				mLookatc[0], mLookatc[1], mLookatc[2],
				mUpc[0], mUpc[1], mUpc[2]);

		Matrix.multiplyMM(viewMatrix, 0, mCompass.getRotationMatrix(), 0, mRotation, 0);
	}
	
	public class UpDownRunnable implements Runnable {
		int mDirection = 0;
		boolean cancel = false;
		public UpDownRunnable(int direction) {
			mDirection = direction;
			cancel = false;
		}
		
		public void cancel() {
			cancel = true;
		}
		
		public void run() {
			if ( cancel ) {
				return;
			}
			
			mUpDownFactor+=(mDirection==0?+0.1f:-0.1f);
			if ( mUpDownFactor <0.0f || mUpDownFactor >=1.0f ) {
				if ( mUpDownFactor <0.0f ) {
					mUpDownFactor=0.0f;
				}
				if ( mUpDownFactor>1.0f ) {
					mUpDownFactor=1.0f;
				}
			} else {
				mHandler.postDelayed(this, 50);
			}
			updateViewVectors();
			
			
		}
	};
	
}
