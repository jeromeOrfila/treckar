package com.gromstudio.treckar.util;

import android.graphics.Color;
import android.util.Log;

public class CoordinateConversions {

	static final String TAG =  "CoordinateConversions";

	static final double DEGtoRAD = (3.1415926535897932384626433832795 / 180.0);
	//WGS84
	static final double RADtoDEG = (180.0 / 3.1415926535897932384626433832795);

	static final float ellipseE= 0.081819191025f; //1/298,257223563
	
	public static final float RADIUS= 6378137.00f; //.5f;//
	
	static final float C1=1.0f-(ellipseE*ellipseE);

	static final int WORLD_MAP_WIDTH = 1024; 
	static final int WORLD_MAP_HEIGHT = 1024;

	static final double MIN_ALTITUDE = 0; // minimum altitude in meters.
	static final float MAX_ALTITUDE = 6000f; // maximum altitude in meters.


	public static void testColor() {

		text(0xC2C2F3);
		text(0xBDFFBA);
		text(0xFFA0D4);
	}

	private static void text(int color) {
		float[] rgb = new float[3];
		rgb[0] = (float)Color.red(color) / 255.0f;
		rgb[1] = (float)Color.green(color) / 255.0f;
		rgb[2] = (float)Color.blue(color) / 255.0f;

		float[] ihs = RGBToIHS(rgb);

		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		float h = 240.0f - hsv[0];
		if (h<0.0f) {
			h += 360.0f;
		}
		if (h>=360.0f) {
			h -= 360.0f;
		}
		Log.e(TAG, String.format(" Color %s: IHS (%.2f %.2f, %.2f),  HSV (%.2f %.2f, %.2f), H: %.2f", 
				Integer.toHexString(color), 
				ihs[0], ihs[1], ihs[2],
				hsv[0], hsv[1], hsv[2], 
				h));
	}


	/** 
	 * getGeocCoords() -- Convertir les coordonnées Géodésique  en geocentrique
	 */
	public static boolean getGeocCoords(float[] geodPos, float[] coords ) {
		// http://www.forumsig.org/archive/index.php/t-9120.html
		// http://geodesie.ign.fr/index.php?page=calculs_sur_un_ellipsoide
	
		double lat = geodPos[0] * DEGtoRAD;
		double lon = geodPos[1] * DEGtoRAD;
		double alt = geodPos[2]; /* metres */

		double sinlat = Math.sin(lat);
		double temp1 = RADIUS /  Math.sqrt(1.0 - (ellipseE * (sinlat * sinlat)));
		double temp2 = temp1 * C1;
		temp1 += alt;
		temp2 += alt;                    /* equ. A-10a */

		/* projection de l'axe d'horizontal sur le plan d'equateur */
		double w = temp1 * Math.cos(lat);

		/* projection de l'axe vertical sur l'axe polaire */
		coords[0] =(float) (w * Math.cos(lon));              /* equ. A-11 */
		coords[1] = (float) (w * Math.sin(lon));              
		coords[2]= (float) (temp2 * sinlat);             /* equ. A-10b */
		
		return true;
	}

	public static float altitudeFromRangeColor(float latitude, float longitude,
			float altMin, float altMax, int color) {

		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		float h = 240.0f - hsv[0];
		if (h<0.0f) { h += 360.0f; }
		if (h>=360.0f) { h -= 360.0f; }
		float c = h/280.0f;
		if ( c<0.0f ) {c=0.0f;}
		if ( c>1.0f ) {c=1.0f;}

		float alt = altMin + c*(altMax-altMin);
		//Log.e(TAG, String.format(" Altitude %.2f",  alt));

		return alt;

	}

	public static int globeColorFromAltitude(float altitude, float altMin, float altMax) {

		float[] hsv = new float[3];
		
		hsv[0] = 240f- altitude/MAX_ALTITUDE*280.0f;
		if ( hsv[0] < 0.0f ) {
			hsv[0]+=360.0f;
		}
		hsv[1] = .5f;
		hsv[2] = .5f;
		float alpha = (altitude-altMin)/(altMax-altMin);
		int res = Color.HSVToColor(hsv);
		return Color.argb((int)(255*alpha), Color.red(res), Color.green(res), Color.blue(res));
		
	}

	public static float[] RGBToIHS (float [] rgb) {

		float[] ihs = new float[3];

		ihs[0] = rgb[0]+rgb[1]+rgb[2];


		if ( rgb[2]< rgb[0] && rgb[2] < rgb[1] ) {
			// b = min (r, g, b);
			ihs[1] = (rgb[1] - rgb[2])/(ihs[0] - 3*rgb[2]);
		} else if ( rgb[0]< rgb[1] && rgb[0] < rgb[2	] ) {
			// r = min (r, g, b);
			ihs[1] = (rgb[2] - rgb[0])/(ihs[0] - 3*rgb[0]) + 1.0f;
		} else {
			// g = min (r, g, b);
			ihs[1] = (rgb[0] - rgb[1])/(ihs[0] - 3*rgb[1]) + 2.0f;
		}

		if ( ihs[1]>1.0f ) {
			ihs[2] = (ihs[0] - 3.0f * rgb[2]) / ihs[0];
		} else if ( ihs[1]>2.0f ) {
			ihs[2] = (ihs[0] - 3.0f * rgb[0]) / ihs[0];
		} else {
			ihs[2] = (ihs[0] - 3.0f * rgb[1]) / ihs[0];
		}
		return ihs;

	}


	public static float[] IHStoRGB (float [] ihs) {

		float[] rgb = new float[3];

		if ( ihs[1]<1.0f ) {

			rgb[0]=ihs[0]*(1.0f+2.0f*ihs[2]-3.0f*ihs[2]*ihs[1])/3.0f;
			rgb[1]=ihs[0]*(1.0f-ihs[2]+3.0f*ihs[2]*ihs[1])/3.0f;
			rgb[2]=ihs[0]*(1.0f-ihs[2])/3.0f;

		} else if ( ihs[1]<2.0f ) {

			rgb[0]=ihs[0]*(1.0f-ihs[2])/3.0f;
			rgb[1]=ihs[0]*(1.0f+2.0f*ihs[2]-3.0f*ihs[2]*(ihs[1]-1.0f))/3.0f;
			rgb[2]=ihs[0]*(1.0f-ihs[2]+3.0f*ihs[2]*(ihs[1]-1.0f))/3.0f;

		} else {

			rgb[0]=ihs[0]*(1.0f-ihs[2]+3.0f*ihs[2]*(ihs[1]-2.0f))/3.0f;
			rgb[1]=ihs[0]*(1.0f-ihs[2])/3.0f;
			rgb[2]=ihs[0]*(1.0f+2.0f*ihs[2]-3.0f*ihs[2]*(ihs[1]-2.0f))/3.0f;

		}
		return rgb;
	}


	public static int colorFromAltitude(float altitude) {

		return 0xFFFFFFFF;

	}


}
