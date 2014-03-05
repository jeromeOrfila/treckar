package com.gromstudio.treckar.ui;

import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.Compass.OnCompassChangedListener;
import com.gromstudio.treckar.util.Geolocation;
import com.gromstudio.treckar.util.Geolocation.GeolocationListener;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class AltimeterView extends TextView implements GeolocationListener, OnCompassChangedListener {

	public AltimeterView(Context context) {
		super(context);
		initialize(context, null, 0);
	}

	public AltimeterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyle) {
	
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		Compass.getInstance(getContext()).addCompassChangeListener(this);
		   
		if ( !Geolocation.getInstance().isInitialized() ) {
			Geolocation.getInstance().initialize(getContext().getApplicationContext());
		}
		Geolocation.getInstance().addGeolocationListener(getContext(), this);	 
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Compass.getInstance(getContext()).removeCompassChangeListener(this);
		Geolocation.getInstance().removeGeolocationListener(getContext(), this); 
	}

	@Override
	public void onLocationChanged(Location location, long waitingTime) {
		setText(Geolocation.getInstance().getEstimatedAltitude(getContext()) + "m");
	}

	int count = 0;
	@Override
	public void onCompassChanged(Compass compass) {
		count++;
		if ( count == 50 ) {
			count = 0;
			float[] orientation = compass.getOrientation();
			setText(String.format(" orientation %.2f, %.2f, %.2f", orientation[0], orientation[1], orientation[2]));
		}
	}
}
