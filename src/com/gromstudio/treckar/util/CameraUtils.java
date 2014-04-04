package com.gromstudio.treckar.util;

import android.hardware.Camera;

public class CameraUtils {

	private static Object sAccesLock = new Object();
	private static Camera sOpenedCamera = null;
	private static float[] fov = null;
	
	public static Camera openCamera() {
		synchronized (sAccesLock) {
			if ( sOpenedCamera!=null ) {
				return null;
			}
			sOpenedCamera = Camera.open();
			fov = new float[2];
			fov[0] = sOpenedCamera.getParameters().getHorizontalViewAngle();
			fov[1] = sOpenedCamera.getParameters().getVerticalViewAngle();
			return sOpenedCamera;
		}
	}
	
	public static void releaseCamera() {
		synchronized (sAccesLock) {
			if ( sOpenedCamera!=null ) {
				sOpenedCamera.release();
				sOpenedCamera = null;
			}
		}
	}

	public static float[] getCameraFOV() {
		if ( fov ==null ) {
			synchronized (sAccesLock) {
				if ( sOpenedCamera!=null ) {
					fov = new float[2];
					fov[0] = sOpenedCamera.getParameters().getHorizontalViewAngle();
					fov[1] = sOpenedCamera.getParameters().getVerticalViewAngle();
				} else {
					try{
						Camera camera = Camera.open();
						fov = new float[2];
						fov[0] = camera.getParameters().getHorizontalViewAngle();
						fov[1] = camera.getParameters().getVerticalViewAngle();
						camera.release();
					} catch (RuntimeException e) {
						fov = new float[] {60, 45};
					}
				}
			}
		}
		return fov;
	}
	
}
