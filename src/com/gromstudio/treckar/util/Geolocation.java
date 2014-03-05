package com.gromstudio.treckar.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;


/**
 * Implementation of Geolocation service
 * 
 * @author grom
 * @version $Revision: 1.0 $
 */
public class Geolocation {



	static class AltitudeBuffer {

		int mIndex;
		double [] mAltitudes;
		public AltitudeBuffer(int size) {
			mAltitudes = new double[size];
			Arrays.fill(mAltitudes, 0);
			mIndex = 0;
		}

		public void put(double value) {
			mIndex++;
			if (mIndex>=mAltitudes.length) {
				mIndex = 0;
			}
			mAltitudes[mIndex] = value;
		}

		public double getAverage() {
			int count = 0;
			double sum = 0.0;
			for ( double val : mAltitudes ) {
				if ( val!=0 ) {
					count++;
					sum += val;
				}
			}
			if ( count==0 ) {
				return 0;
			}
			return sum/(double)count;
		}
	}

	/**
	 * <b>Thrown when an error occurres during service exception</b>
	 * 
	 * @author gromstudio
	 * @version $Revision: 1.0 $
	 */
	public class GeolocationServiceException extends Exception {
		private static final long serialVersionUID = 1787550163839643652L;
		/**
		 * 	Default {@link ServiceException} constructor
		 * @param displayMessage String
		 */
		public GeolocationServiceException(String displayMessage) {
			super(displayMessage);
		}
	}

	/**
	 * The interface of a geographic listener. Implemented objects should be added
	 * to the list of observers addGeolocationListener that will be inform when the
	 * location changes.
	 * @author gromstudio
	 * @version $Revision: 1.0 $
	 */
	public interface GeolocationListener {
		/**
		 * <b>The location has been updated.</b>
		 * <p>
		 * This callback is called when the position changed. In case the listener is 
		 * associated to a timeout, the location of this method will be null.
		 * </p> 
		 * @param location the updated location or null if the timeout expired
		 * @param waitingTime the time elapsed  since the listener is waiting
		 */
		void onLocationChanged(Location location, long waitingTime);
	}


	public static final boolean DEBUG = true;

	public static  boolean GEOLOCATION_FAIL = false;

	private static final String TAG = "GeolocationServiceV2Impl";

	private static final boolean CONTINUOUS_GEOLOC = false;

	/**
	 * Private shared preferences names
	 */
	private static final String SP_NAME = "com.sncf.ter.nfc.geolocationservice.name";

	/**
	 * Minimum time to refresh position 
	 */
	private static final int MIN_TIME = 1000 * 60; // 1 minutes

	/**
	 * An empty location will be sent to listeners waiting for
	 * more than TIMEOUT ms
	 */
	static final int TIMEOUT = 5000; //5 seconds

	/**
	 * The timeout delay will be checked each TIMEOUT_CHECK_FREQUENCE ms
	 */
	static final int TIMEOUT_CHECK_FREQUENCE = 500;

	/**
	 * Minimum distance to throw a location update
	 */
	private static final int MIN_DISTANCE = 1; // 1 meters

	/**
	 */
	static final int ALTITUDE_BUFFER_SIZE = 5; //5 last entries



	/**
	 * private implementation of the LocationListener. Update the service when
	 * the providers state change; and dispatch the location change to all the 
	 * listeners.
	 */
	private final LocationListener mLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			if ( DEBUG ) Log.e(TAG, "Location updated");

			if ( GEOLOCATION_FAIL ) return;

			if ( isBetterLocation(location, mLastLocation) ) {
				mLastLocation = location;

				if ( location.getAltitude()!=0.0 ) {
					mLastAltitudes.put(location.getAltitude());
				}
			}

			notifyLocationChanged(mContext, location);

			// Stops location updates if no more listener is waiting for location
			// and the continuous geolocation is deactivated
			if ( mListeners.size() == 0 &&
					mIsRunning &&
					!CONTINUOUS_GEOLOC ) {
				stopUpdates();
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if ( DEBUG ) Log.e(TAG, "Status changed");
		}

		public void onProviderEnabled(String provider) {
			if ( DEBUG ) Log.e(TAG, "Provider enabled");
			Location location = mLocationManager.getLastKnownLocation(provider);
			if ( !GEOLOCATION_FAIL &&
					isBetterLocation(location, mLastLocation) ) {
				mLastLocation = location;
				notifyLocationChanged(mContext, location);
			}
		}

		public void onProviderDisabled(String provider) {
		}
	};

	private Runnable mCheckTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			checkNotifyTimeout(mContext);
			if ( mListeners.size()>0 ) {
				mHandler.postDelayed(this, TIMEOUT_CHECK_FREQUENCE);
			}
		}
	};

	/**
	 */
	static class GeolocationTimeoutListener {
		long startListening;
		WeakReference<GeolocationListener> wrListener;
		/**
		 * Constructor for GeolocationTimeoutListener.
		 * @param timestamp long
		 * @param listener GeolocationListener
		 */
		public GeolocationTimeoutListener(long timestamp, GeolocationListener listener) {
			startListening = timestamp;
			wrListener = new WeakReference<GeolocationListener>(listener);
		}
	}

	/**
	 * List of objects listening changes of location
	 */
	private ArrayList<GeolocationTimeoutListener> mListeners;

	/**
	 * Keep a handler runing on the application context's main loop.
	 */
	Handler mHandler;

	/**
	 * Context
	 */
	Context mContext;

	/**
	 * Location manager
	 */
	LocationManager mLocationManager;

	/**
	 * Is the geolocation service is currently running?
	 */
	boolean mIsRunning = false;

	/**
	 * Keeps a weak reference to the application context
	 */
	private WeakReference<Context> mAppContext;

	private boolean mIsInitialized = false;
	/**
	 * Last known position
	 */
	private Location mLastLocation;

	private AltitudeBuffer mLastAltitudes;

	private static Geolocation sInstance=null;
	
	public static Geolocation getInstance() {
		if ( null==sInstance ) {
			sInstance = new Geolocation();
		}
		return sInstance;
	}

	/**
	 * Constructor: 
	 * Keeps a weak reference to the application context. The weak reference should never 
	 * point to a null object. If it occurs, the methods will throw a GeolocationServiceException.
	 */
	private Geolocation() {

		mListeners = new ArrayList<GeolocationTimeoutListener>();
		mIsInitialized = false;
	}

	/**
	 * Method initialize.
	 * @param applicationContext SNCFTerNFCApplication
	 */
	public synchronized void initialize(Context applicationContext) {

		Log.e(TAG, "initialize");
		mContext = applicationContext;

		mLastAltitudes = new AltitudeBuffer(ALTITUDE_BUFFER_SIZE);
		
		mHandler = new Handler(applicationContext.getMainLooper());

		mLocationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);

		if ( CONTINUOUS_GEOLOC ) {
			startUpdates();
		}

		if ( !GEOLOCATION_FAIL ) {

			if ( mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
				mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			} else if ( mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER )) {
				mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

		}
		mIsInitialized = true;
	}
	
	public synchronized boolean isInitialized() {
		return mIsInitialized;
	}

	/**
	 * Notifies all the listeners that the position has changed.
	 * @param context Context
	 * @param location Location
	 */
	protected synchronized void notifyLocationChanged(Context context, Location location) {

		try {
			if ( !isEnabled(context) ) {
				return;
			}
			Iterator<GeolocationTimeoutListener> it = mListeners.iterator();
			while (it.hasNext()) {
				GeolocationTimeoutListener tl = it.next();
				WeakReference<GeolocationListener> wrListener = tl.wrListener;
				if ( null!= wrListener ) {
					GeolocationListener l = wrListener.get(); 
					if ( l == null ) {
						it.remove();
					} else {
						long time =   System.currentTimeMillis()-tl.startListening;
						tl.startListening = System.currentTimeMillis();
						l.onLocationChanged(location, time);
					}
				}
			}

		} catch (GeolocationServiceException e) {
			// Silent catch
			if ( DEBUG ) {
				Log.e("Location", "isEnabled threw GeolocationServiceException " + e.getMessage());
			}
		}

	}

	/**
	 * Notifies all the listeners that the position has changed.
	 * @param context Context
	 */
	protected synchronized void checkNotifyTimeout(Context context) {

		try {
			if ( !isEnabled(context) ) {
				return;
			}

			long endTime = System.currentTimeMillis();

			Iterator<GeolocationTimeoutListener> it = mListeners.iterator();
			while (it.hasNext()) {

				GeolocationTimeoutListener tl = it.next();
				GeolocationListener l = tl.wrListener.get(); 

				if ( l==null || (tl.startListening+TIMEOUT )<endTime ) {

					if ( l != null ) {
						l.onLocationChanged(null, 0);						
					}
				}
			}
		} catch (GeolocationServiceException e) {
			if ( DEBUG ) Log.e(TAG, "isEnabled threw GeolocationServiceException " + e.getMessage());
		}
	}

	/**
	 * Method isEnabled.
	 * @param context Context
	 * @return boolean
	 * @throws GeolocationServiceException
	 */
	public boolean isEnabled(Context context) throws GeolocationServiceException {

		if ( null == context ) {
			throw new GeolocationServiceException("Undefined context");
		}
		return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
				mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Method addGeolocationListener.
	 * @param context Context
	 * @param listener GeolocationListener
	 * @return boolean
	 * @throws GeolocationServiceException
	 */
	public synchronized boolean addGeolocationListener(Context context, GeolocationListener listener) {

		if ( null== listener ) {
			throw new IllegalArgumentException("Empty listener");
		}

		Iterator<GeolocationTimeoutListener> it = mListeners.iterator();
		while (it.hasNext()) {
			WeakReference<GeolocationListener> wrListener = it.next().wrListener;
			if ( null!= wrListener ) {
				GeolocationListener l = wrListener.get();
				if ( l == null ) {
					it.remove();
				} else  if (l == listener ) {
					//already listening
					return true;
				}
			}
		}
		mListeners.add(new GeolocationTimeoutListener(System.currentTimeMillis(), listener));

		if ( !mIsRunning ) {
			startUpdates();
		}

		mHandler.postDelayed(mCheckTimeoutRunnable, TIMEOUT_CHECK_FREQUENCE);

		return true;
	}

	/**
	 * Method removeGeolocationListener.
	 * @param context Context
	 * @param listener GeolocationListener
	 * @return boolean
	 * @throws GeolocationServiceException
	 */
	public synchronized boolean removeGeolocationListener(Context context, GeolocationListener listener) {

		if ( null== listener ) {
			throw new IllegalArgumentException("Empty listener");
		}

		Iterator<GeolocationTimeoutListener> it = mListeners.iterator();
		while (it.hasNext()) {
			WeakReference<GeolocationListener> wrListener = it.next().wrListener;
			if ( null!= wrListener ) {
				GeolocationListener l = wrListener.get(); 
				if ( l == null || l == listener ) {
					it.remove();
				}
			}
		}

		// Stops location updates if no more listener is waiting for location
		// and the continuous geolocation is deactivated
		if ( mListeners.size() == 0 &&
				mIsRunning &&
				!CONTINUOUS_GEOLOC ) {
			stopUpdates();
		}

		return true;

	}

	private void startUpdates() {

		if ( DEBUG ) Log.e(TAG, "geoloc updates activated");

		mIsRunning = true;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if ( mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
					mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
							MIN_TIME,
							MIN_DISTANCE,
							mLocationListener);
				} else if ( mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER )) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							MIN_TIME,
							MIN_DISTANCE,
							mLocationListener);
				}
			}
		});
	}

	private void stopUpdates() {

		if ( DEBUG ) Log.e(TAG, "geoloc updates deactivated");

		mIsRunning = false;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mLocationManager.removeUpdates(mLocationListener);
			}
		});
	}



	/**
	 * Method getLastLocation.
	 * @param context Context
	 * @return Location
	 * @throws GeolocationServiceException
	 */
	public Location getLastLocation(Context context) throws GeolocationServiceException {

		return mLastLocation;

	}

	/**
	 * Method getEstimatedAltitude.
	 * @param context Context
	 * @return Location
	 * @throws GeolocationServiceException
	 */
	public double getEstimatedAltitude(Context context) {

		return mLastAltitudes.getAverage();
	}
	

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 * @return boolean
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > MIN_TIME;
		boolean isSignificantlyOlder = timeDelta < -MIN_TIME;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = TextUtils.equals(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

}
