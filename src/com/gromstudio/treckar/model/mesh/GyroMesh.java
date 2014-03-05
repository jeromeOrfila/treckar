package com.gromstudio.treckar.model.mesh;

import java.util.Arrays;

import android.content.Context;
import android.opengl.Matrix;

import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.GLES20Program;
import com.gromstudio.treckar.util.GLES20Renderer;
import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;

public class GyroMesh extends MeshES20 implements OnCompassChangedListener {

	private Compass mCompass;
	private float[] mGyroMatrix = new float[16];
	//private float[] mLocalizedMatrix = new float[16];
	private float[] mModelMatrix = new float[16];

	public GyroMesh (Context context) {

		mCompass = Compass.getInstance(context);
		mCompass.addCompassChangeListener(this);
		
		Matrix.setIdentityM(mGyroMatrix, 0);

//		float[] mm = new float[16];
//		Matrix.scaleM(mm, 0, 0.2f, 0.5f, 0.2f);
//		
//		MeshES20 m = new GyroMesh(mCompass);
//		m.addSubMesh(
//		mCoordsMesh = new CoordinateSystemMesh();
//		
//		Matrix.scaleM(mm, 0, 0.5f, 0.5f, 0.5f);
//		addSubMesh(m, mm);
//		
//
//		float[] geod = {45.5f, 5.5f, 0.0f};
//		float[] geoc = new float[3];
//		CoordinateConversions.getGeocCoords(geod, geoc);
//
//		Matrix.setIdentityM(mLocalizedMatrix, 0);
//		Matrix.translateM(mLocalizedMatrix, 0, geoc[0], geoc[1], geoc[2]);
	
	}
	
	@Override
	public void onCompassChanged(Compass compass) {

		mGyroMatrix = compass.getRotationMatrix();

//		float[] geod = {45.5f, 5.5f, 0.0f};
//		float[] geoc = new float[3];
//		CoordinateConversions.getGeocCoords(geod, geoc);
//
//		Matrix.setIdentityM(mLocalizedMatrix, 0);
//		Matrix.translateM(mLocalizedMatrix, 0, geod[0], geod[1], geoc[2]);

	}

	@Override
	public void drawMesh(GLES20Program program, final float[] modelMtx) {
		
		//mModelMatrix = Arrays.copyOf(modelMtx, modelMtx.length);
		
		//Matrix.multiplyMV(mModelMatrix, 0, mGyroMatrix, 0, mLocalizedMatrix, 0);
		//mCoordsMesh.drawMesh(program, mGyroMatrix);
		
		
//		Matrix.multiplyMM(mModelMatrix, 0, modelMtx, 0, mGyroMatrix, 0);
//		Matrix.multiplyMM(mModelMatrix, 0, mLocalizedMatrix, 0, mModelMatrix, 0);		
//		Matrix.scaleM(mModelMatrix, 0, mModelMatrix, 0, 0.2f, 0.5f, 0.2f);		
		
		//mCoordsMesh.draw(program, mModelMatrix);

		super.drawMesh(program, mGyroMatrix);

	}

}
