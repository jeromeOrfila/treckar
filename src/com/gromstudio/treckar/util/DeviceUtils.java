package com.gromstudio.treckar.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

public class DeviceUtils {

	private static boolean hasGLES20(Context context) {
	    ActivityManager am = (ActivityManager)
	                context.getSystemService(Context.ACTIVITY_SERVICE);
	    ConfigurationInfo info = am.getDeviceConfigurationInfo();
	    return info.reqGlEsVersion >= 0x20000;
	}
	
}
