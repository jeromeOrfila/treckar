package com.gromstudio.treckar.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.gromstudio.treckar.model.mesh.CircleMesh;
import com.gromstudio.treckar.model.mesh.CompassMesh;
import com.gromstudio.treckar.model.mesh.CoordinateSystemMesh;
import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.model.mesh.SquareMesh;
import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.Log;

public class CompassView extends GLSurfaceView implements Renderer, OnCompassChangedListener {

	static final String TAG = "CompassView";

	Compass mCompass;
	float [] mSensorRotation;

	private Mesh mMesh;

	public CompassView(Context context) {
		super(context);
		initialize(context, null, 0);
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyle) {

		mCompass = Compass.getInstance(context);
		mCompass.addCompassChangeListener(this);

		mSensorRotation = new float[16];
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setEGLConfigChooser( 8, 8, 8, 8, 16, 0);
		setRenderer(this);

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mCompass.attach();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mCompass.detach();
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -2);

		gl.glMultMatrixf(mCompass.getRotationMatrix(), 0);

		mMesh.draw(gl);

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		gl.glViewport(0, 0, width, height);

		float ratio = ((float) width / height)/2.0f;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -0.5f, 0.5f, 1, 10);

		//		GLU.gluPerspective(gl, 67, ratio, 1, 100);

	}


	@Override	
	public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

		gl.glDisable(GL10.GL_DITHER);

		gl.glClearColor(0,0,0,0);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_ALPHA_TEST );
		gl.glAlphaFunc(GL10.GL_GREATER, 0);
		gl.glClearDepthf(1.0f); 

		/*
		 * create / load the our 3D models here
		 */

		mMesh = new CompassMesh();
		mMesh.loadTextures(gl, getContext());
		
	}

	@Override
	public void onCompassChanged(Compass compass) {

		mSensorRotation = compass.getRotationMatrix();

	}

}
