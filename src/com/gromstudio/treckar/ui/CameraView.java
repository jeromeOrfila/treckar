package com.gromstudio.treckar.ui;

import com.gromstudio.treckar.util.CameraUtils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraView extends SurfaceView {

	static final boolean DISPLAY_CAMERA = false;

	static final String TAG = "CameraView";

	Camera mCamera;
	SurfaceHolder mPreviewHolder;

	SurfaceHolder.Callback mSurfaceHolderListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			if ( !DISPLAY_CAMERA ) {
				return;
			}
			mCamera=CameraUtils.openCamera();
			
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			}
			catch (Throwable e){ 
				Log.e(TAG, "Error", e);
			}
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			if ( !DISPLAY_CAMERA ) {
				return;
			}

			Parameters params = mCamera.getParameters();
			
//			for ( Size s :  params.getSupportedPictureSizes() ) {
//				Log.e(TAG, " supported Size : " + s.width + " x " + s.height);
//			}
//			Log.e(TAG, " Asking for size : " + width + " x " + height);
			
			//params.setPreviewSize(width, height);
			params.setPictureFormat(ImageFormat.JPEG);
        	mCamera.setParameters(params);
        	
        	final int rotation = ((WindowManager) getContext()
        			.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
            case Surface.ROTATION_0: // portrait
            	mCamera.setDisplayOrientation(90);
            	break;
            case Surface.ROTATION_90: // landscape
            	mCamera.setDisplayOrientation(0);
            	break;
            case Surface.ROTATION_180: // reverse portrait 
            	mCamera.setDisplayOrientation(270);
            	break;
            case Surface.ROTATION_270: // reverse landscape
            	mCamera.setDisplayOrientation(180);
            	break;
            default:	// undefined
            }			
	        mCamera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder arg0) {
			if (mCamera!=null){
				mCamera.stopPreview();
				mCamera.release();   
			}
		}
	};

	public CameraView(Context context) {
		super(context);
		initialize(context, null, 0);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs, defStyle);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyle) {

		mPreviewHolder = this.getHolder();
		mPreviewHolder.addCallback(mSurfaceHolderListener);

	}



}