package com.gromstudio.treckar.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.gromstudio.treckar.model.mesh.GyroMesh;
import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TriangleMesh;
import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;
import com.gromstudio.treckar.util.GLES20ImmRenderer;
import com.gromstudio.treckar.util.GLES20TopRenderer;
import com.gromstudio.treckar.util.GLES20Renderer;
import com.gromstudio.treckar.util.GLRenderer;

public class WorldMapView extends GLSurfaceView implements OnCompassChangedListener {

	static final boolean DEBUG = true;
	static final String TAG = "WorldMapView";
    
	float [] mSensorRotation;
	
	private MeshES20 mMesh;
	private GLES20ImmRenderer mRenderer;
	private boolean mIsCameraDown = true;
	private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    
	public WorldMapView(Context context) {
		super(context);
		initialize(context, null, 0);
	}

	public WorldMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyle) {

		//mRenderer.setMesh(new TriangleMesh());
		
		mRenderer = new GLES20ImmRenderer(context);

		mSensorRotation = new float[16];
		getHolder().setFormat(PixelFormat.TRANSLUCENT);

		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		setEGLConfigChooser( 8, 8, 8, 8, 16, 0);
		setRenderer(mRenderer);

	}

	public void setMesh(MeshES20 mesh) {
		
		mMesh = new GyroMesh(getContext());
		mMesh.addSubMesh(mesh);
		//mMesh = mesh;
		mRenderer.setMesh(mesh);

	}
	
	public void switchCameraUpDown () {
		if ( mIsCameraDown ) {
			mRenderer.cameraUp();
		} else {
			mRenderer.cameraDown();
		}
		mIsCameraDown = !mIsCameraDown;
	}
	public void animateCameraUp () {
			mRenderer.cameraUp();
		mIsCameraDown = false;
	}
	
	public void animateCameraDown() {
		mRenderer.cameraDown();
		mIsCameraDown = true;	
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void onCompassChanged(Compass compass) {

		mSensorRotation = compass.getRotationMatrix();

	}

	
	
}
