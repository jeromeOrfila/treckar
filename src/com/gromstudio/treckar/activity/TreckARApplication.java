package com.gromstudio.treckar.activity;

import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.Geolocation;
import com.gromstudio.treckar.util.Geolocation.GeolocationListener;
import com.gromstudio.treckar.util.Geolocation.GeolocationServiceException;

import android.app.Application;
import android.location.Location;
import android.os.AsyncTask;

public class TreckARApplication extends Application implements GeolocationListener {

	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Geolocation geoloc = Geolocation.getInstance();
		geoloc.initialize(TreckARApplication.this);
		geoloc.addGeolocationListener(TreckARApplication.this, TreckARApplication.this);
		
		new InitializeAsyncTask().execute((Void)null);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		Geolocation geoloc = Geolocation.getInstance();
		geoloc.removeGeolocationListener(this, this);
	}
	
	@Override
	public void onLocationChanged(Location location, long waitingTime) {
	}
	
	public class InitializeAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			
			return null;
		}
		
	}

}
