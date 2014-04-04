package com.gromstudio.treckar.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.gromstudio.treckar.R;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.service.ServicesManager;
import com.gromstudio.treckar.service.WorldMapService;
import com.gromstudio.treckar.service.WorldMapService.NoTileException;
import com.gromstudio.treckar.service.WorldMapService.NotLocalizedException;
import com.gromstudio.treckar.ui.BasePopup;
import com.gromstudio.treckar.ui.CompassView;
import com.gromstudio.treckar.ui.InformPopup;
import com.gromstudio.treckar.ui.WorldMapView;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.Geolocation;
import com.gromstudio.treckar.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ARActivity extends Activity implements OnClickListener{

	static final String TAG = "ARActivity";
	
	static final int REQUEST_CODE_SETTINGS = 101;
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	
	WorldMapView mWorldMapView;
	GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ar);

	    CompassView compassView = (CompassView) findViewById(R.id.compass_view);
	    compassView.setZOrderMediaOverlay(true);
	    
		mWorldMapView = (WorldMapView) findViewById(R.id.world_map_view);
		mWorldMapView.setZOrderMediaOverlay(true);

		findViewById(R.id.updownbutton).setOnClickListener(this);
	    CoordinateConversions.testColor();
	    
	    

		gestureDetector = new GestureDetector(this, new WorldViewGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        mWorldMapView.setOnTouchListener(gestureListener);
	    

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		checkGeolocation();
		
		new GenerateMapTask().execute((Void)null);
	}

	/**
	 * <b>The user modified its settings to accept qgeolocation.</b>
	 * <p>
	 * Test the geolocation, if active wait for lacation
	 * </p>
	 */
	private void checkGeolocation() {

		Geolocation geolocation = Geolocation.getInstance();

		if ( !geolocation.isInitialized() ) {
			geolocation.initialize(getApplicationContext());
		}
		
		try {

			if ( !geolocation.isEnabled(this) ) {

				activateGeolocation();
				
			}

		} catch (com.gromstudio.treckar.util.Geolocation.GeolocationServiceException e) {
			Log.e(TAG, "Exception " + (null!=e.getMessage()?e.getMessage():"") , e);
		}
	}
	
	private void activateGeolocation() {
		
		final InformPopup geolocalize = new InformPopup();
		geolocalize.initialize(null, getString(R.string.popup_geolocalize_text), null, new BasePopup.PopupListener() {
			
			@Override
			public void onTerminate(BasePopup popup, Bundle result) {

				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);					
				startActivityForResult(intent, REQUEST_CODE_SETTINGS);

			}
			
			@Override
			public void onDismissed(BasePopup popup) {
				activateGeolocation();
			}
		});
		geolocalize.show(getFragmentManager(), TAG+"GEOLOCATION");
	}
	
	
	public final class GenerateMapTask extends AsyncTask<Void, Void, MeshES20> {

		@Override
		protected MeshES20 doInBackground(Void... params) {
			
			
			
			
			WorldMapService service = ServicesManager.getWorldMapService();
			try {
				service.initialize(ARActivity.this);
			} catch (NotLocalizedException e) {
				e.printStackTrace();
			}
			
			MeshES20 m = service.loadTileMesh(Geolocation.getInstance().getLastLocation(ARActivity.this)[0],
					Geolocation.getInstance().getLastLocation(ARActivity.this)[1]);
			
			try {
				Log.e(TAG, String.format("ATLITUDE from la NASA %s",
						Integer.toHexString(service.getColorFromLoadedTile(
								Geolocation.getInstance().getLastLocation(ARActivity.this)[0],
								Geolocation.getInstance().getLastLocation(ARActivity.this)[1]))));
				
			} catch (NoTileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return m;	
		}
		
		@Override
		protected void onPostExecute(MeshES20 result) {
			super.onPostExecute(result);
			mWorldMapView.setMesh(result);
		}
		
	}


	@Override
	public void onClick(View v) {
		if ( v.getId()==R.id.updownbutton) {
			mWorldMapView.switchCameraUpDown();
		}
		
	}
	
	class WorldViewGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
//                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
//                    return false;
                // right to left swipe
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	mWorldMapView.animateCameraUp();
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	mWorldMapView.animateCameraDown();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

            @Override
        public boolean onDown(MotionEvent e) {
              return true;
        }
    }

	}
