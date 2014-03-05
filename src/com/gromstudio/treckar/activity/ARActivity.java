package com.gromstudio.treckar.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.gromstudio.treckar.R;
import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TileMesh;
import com.gromstudio.treckar.service.ServicesManager;
import com.gromstudio.treckar.service.WorldMapService;
import com.gromstudio.treckar.service.WorldMapService.NoTileException;
import com.gromstudio.treckar.service.WorldMapService.NotLocalizedException;
import com.gromstudio.treckar.ui.BasePopup;
import com.gromstudio.treckar.ui.CompassView;
import com.gromstudio.treckar.ui.ConfirmPopup;
import com.gromstudio.treckar.ui.InformPopup;
import com.gromstudio.treckar.ui.PositiveNegativeBasePopup;
import com.gromstudio.treckar.ui.WorldMapView;
import com.gromstudio.treckar.ui.PositiveNegativeBasePopup.PositiveNegativePopupListener;
import com.gromstudio.treckar.util.Compass;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.Geolocation;
import com.gromstudio.treckar.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ARActivity extends Activity {

	static final String TAG = "ARActivity";
	
	static final int REQUEST_CODE_SETTINGS = 101;
	
	
	WorldMapView mWorldMapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ar);

	    CompassView compassView = (CompassView) findViewById(R.id.compass_view);
	    compassView.setZOrderMediaOverlay(true);
	    
		mWorldMapView = (WorldMapView) findViewById(R.id.world_map_view);
		mWorldMapView.setZOrderMediaOverlay(true);

	    CoordinateConversions.testColor();

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
			
			MeshES20 m = service.loadTileMesh(45.5f, 5.4f);
			
			try {
				Log.e(TAG, String.format("ATLITUDE from la NASA %s",
						Integer.toHexString(service.getColorFromLoadedTile(45.5f, 5.4f))));
				
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
	
	}
