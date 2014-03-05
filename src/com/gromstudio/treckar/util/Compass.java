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
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public abstract class Compass {

	public interface OnCompassChangedListener {
		void onCompassChanged(Compass compass);
	}
	
	static final String TAG = "Compass";

	public int TYPE_LIGHT = 0x000001;
	public int TYPE_ORIENTATION = 0x000002;

	protected Context mContext;

	private boolean mIsAttached;

	protected Flags mFlags;

	int mCount = 0;

	Handler mHandler;
	
	List<WeakReference<OnCompassChangedListener>> mListeners;

	protected Compass() {
		
		mListeners = new ArrayList<WeakReference<OnCompassChangedListener>>();
		mIsAttached = false;
		mFlags = new Flags();
		
		mFlags.addFlag(TYPE_ORIENTATION);
		mFlags.addFlag(TYPE_LIGHT);

	}
	static Compass sInstance = new CompassFusedImpl();
	public static Compass getInstance(Context context) {
		if (!sInstance.isInitialized() ) {
			sInstance.initialize(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public void initialize(Context context) {
		mContext = context;		
		mHandler = new Handler(context.getMainLooper());
	}
	
	public boolean isInitialized() {
		return mContext!=null && mHandler!=null;
	}
	
	public boolean isAttached() {
		return mIsAttached;
	}

	public void addCompassChangeListener(OnCompassChangedListener listener) {
		mListeners.add(new WeakReference<OnCompassChangedListener>(listener));
	}

	public void removeCompassChangeListener(OnCompassChangedListener listener) {
		Iterator<WeakReference<OnCompassChangedListener>> it = mListeners.iterator();
		while (it.hasNext()) {
			WeakReference<OnCompassChangedListener> wl = it.next();
			if ( wl == null || wl.get() == null || wl.get() == listener ) {
				it.remove();
			}
		}
	}
	
	protected void dispatchCompasChanged() {

		if ( isInitialized() ) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					Iterator<WeakReference<OnCompassChangedListener>> it = mListeners.iterator();
					while (it.hasNext()) {
						WeakReference<OnCompassChangedListener> wl = it.next();
						if ( null==wl || wl.get()==null ) {
							it.remove();
						} else {
							OnCompassChangedListener l = wl.get();
							if( null!=l ) {
								l.onCompassChanged(Compass.this);
							}
						}
					}
				}
			});
		}
	}
	
	public void addType(int type) {
		if ( type==TYPE_LIGHT || 
				type == TYPE_ORIENTATION ) {
			mFlags.addFlag(type);
		} else {
			throw new IllegalArgumentException("Unknown type.");
		}
		updateSensors();
	}

	public void removeType(int type) {
		if ( type==TYPE_LIGHT || 
				type == TYPE_ORIENTATION ) {
			mFlags.removeFlag(type);
		} else {
			throw new IllegalArgumentException("Unknown type.");
		}
		updateSensors();
	}

	public void attach() {
		mIsAttached = true;
	}

	public void detach() {
		mIsAttached = false;
	}

	public abstract void updateSensors();

	public abstract float[] getOrientation();
	
	public abstract float[] getRotationMatrix();
	
}
