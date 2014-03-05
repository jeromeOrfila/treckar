package com.gromstudio.treckar.util;

import android.util.Log;

public class LogUtils {

	public static void logPoints(String tag, float[] geod, float[] geoc) {

		Log.e(tag, String.format(" Point : [%.3f, %.3f, %.3f],  cartesian:[%.3f, %.3f, %.3f]", 
				geod[0], geod[1], geod[2],
				geoc[0], geoc[1], geoc[2]));
		
	}

	public static void logPoints(String tag, float latitude, float longitude, float[] geoc) {

		Log.e(tag, String.format(" Point : [%.3f, %.3f, %.3f],  cartesian:[%.3f, %.3f, %.3f]", 
				latitude, longitude, 0.0f,
				geoc[0], geoc[1], geoc[2]));
		
	}
	

}
