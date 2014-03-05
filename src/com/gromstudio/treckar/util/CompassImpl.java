package com.gromstudio.treckar.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class CompassImpl extends Compass implements SensorEventListener {

	static final String TAG = "CompassImpl";

	private SensorManager mSensorManager;

	private List<Sensor> mSensors;

	int mCount = 0;

	private float[] gravity = new float[3];
	private float[] geomag = new float[3];
	private float[] mRotationMatrix = new float[16];

	protected CompassImpl() {
		
		mListeners = new ArrayList<WeakReference<OnCompassChangedListener>>();
		mSensors = new ArrayList<Sensor>();
	}
	
	public void initialize(Context context) {
		super.initialize(context);

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		updateSensors();
		
	}
	
	@Override
	public void updateSensors() {
		
		if ( mSensorManager==null ) {
			// not initialized yet
			return;
		}
		boolean attached = isAttached();
		if ( attached ) {
			detach();
		}
		mSensors.clear();
		List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for ( Sensor sensor : deviceSensors ) {
			Log.e(TAG, "Sensor : " + sensor.getName());

			if ( mFlags.isFlagSet(TYPE_LIGHT) && sensor.getType() == Sensor.TYPE_LIGHT ) {
				mSensors.add(sensor);
			}
			if ( mFlags.isFlagSet(TYPE_ORIENTATION) && sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
				mSensors.add(sensor);
			}
			if ( mFlags.isFlagSet(TYPE_ORIENTATION) && sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ) {
				mSensors.add(sensor);
			}
		}
		if ( attached ) {
			attach();
		}
	}

	public void attach() {
		super.attach();
		if ( mSensorManager!=null ) {
			for ( Sensor s : mSensors ) {
				mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
			}
		}
	}

	public void detach() {
		super.detach();
		if ( mSensorManager!=null ) {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		int type=event.sensor.getType();

		//Smoothing the sensor data a bit
		if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			geomag[0]=(geomag[0]*1+event.values[0])*0.5f;
			geomag[1]=(geomag[1]*1+event.values[1])*0.5f;
			geomag[2]=(geomag[2]*1+event.values[2])*0.5f;
		} else if (type == Sensor.TYPE_ACCELEROMETER) {
			gravity[0]=(gravity[0]*2+event.values[0])*0.33334f;
			gravity[1]=(gravity[1]*2+event.values[1])*0.33334f;
			gravity[2]=(gravity[2]*2+event.values[2])*0.33334f;
		}

		if ((type==Sensor.TYPE_MAGNETIC_FIELD) || (type==Sensor.TYPE_ACCELEROMETER)) {
			mRotationMatrix = new float[16];
			SensorManager.getRotationMatrix(mRotationMatrix, null, gravity, geomag);
			
			final int rotation = ((WindowManager) mContext
        			.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
            case Surface.ROTATION_0: // portrait
            	// NO coordinate re mapping necessary
            	break;
            case Surface.ROTATION_90: // landscape
            	SensorManager.remapCoordinateSystem( 
    					mRotationMatrix,
    					SensorManager.AXIS_Y,
    					SensorManager.AXIS_MINUS_X,
    					mRotationMatrix );
            	break;
            case Surface.ROTATION_180: // reverse portrait 
            	SensorManager.remapCoordinateSystem( 
    					mRotationMatrix, 
    					SensorManager.AXIS_X, 
    					SensorManager.AXIS_MINUS_Y, 
    					mRotationMatrix );
            	break;
            case Surface.ROTATION_270: // reverse landscape
            	SensorManager.remapCoordinateSystem( 
    					mRotationMatrix, 
    					SensorManager.AXIS_MINUS_Y, 
    					SensorManager.AXIS_X, 
    					mRotationMatrix );
            	break;
            default:	// undefined
            }	
            
            //Log.e(TAG, String.format("Oriantation %.2fdeg (%.2frad) ", azimuthInDegress, azimuthInRadians));
            dispatchCompasChanged();
		}
	}

	@Override
	public float[] getOrientation() {

		float orientation[] = new float[3];
		orientation[0] = 0;
		orientation[1] = 0;
		orientation[2] = 0;

		if ( mRotationMatrix != null) {
			SensorManager.getOrientation(mRotationMatrix, orientation);
		}
		return orientation;
	}

	@Override
	public float[] getRotationMatrix() {

		return mRotationMatrix;
		
	}

}
