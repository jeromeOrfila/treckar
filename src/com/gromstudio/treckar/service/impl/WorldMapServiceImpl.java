package com.gromstudio.treckar.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.gromstudio.treckar.R;
import com.gromstudio.treckar.model.Tile;
import com.gromstudio.treckar.model.Tile.TileMetadata;
import com.gromstudio.treckar.model.mesh.CoordinateSystemMesh;
import com.gromstudio.treckar.model.mesh.GlobeMesh;
import com.gromstudio.treckar.model.mesh.LineMesh;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TriangleMesh;
import com.gromstudio.treckar.service.WorldMapService;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.LogUtils;
import com.gromstudio.treckar.util.Geolocation.GeolocationListener;

public class WorldMapServiceImpl extends WorldMapService implements GeolocationListener  {


	static final String ENCODING = "UTF-8";
	
	enum DirectionEnum {
		NORTH, SOUTH, EAST, WEST
	}

	static final String TAG = "WorldMapServiceImpl";

	private static final int NB_SAMPLES_PER_TILE = 1000;

	private Context mContext;
	
	private Tile[] mCurrentTile = new Tile[9];

	private boolean mIsInitialized = false;

	public WorldMapServiceImpl() {
		mIsInitialized = false;
		mCurrentTile = null;
		
	}

	public void initialize(Context context) throws NotLocalizedException {

		mContext = context;
		mIsInitialized = true;
		mCurrentTile = new Tile[9];

	}
	
	/**
	 * Compute the area around the given position.
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	@Override
	public MeshES20 loadTileMesh(float latitude, float longitude) {
		
		if ( !mIsInitialized ) {
			throw new IllegalStateException("WorldMapService has not been initialized");
		}
		
		// 1: Prepare tiles
		// Center tile
		mCurrentTile[4] = new Tile(latitude, longitude);
		mCurrentTile[4].setCenter(true);
//		
//		// build empty relative tiles
		// [ 0		 1		 2 ]
		// [ 3		[4]		 5 ]
		// [ 6		 7		 8 ]
		
		mCurrentTile[0] = mCurrentTile[4].getRelativeTile(+1.0f, -1.0f);
		mCurrentTile[1] = mCurrentTile[4].getRelativeTile(+1.0f,  0.0f);
		mCurrentTile[2] = mCurrentTile[4].getRelativeTile(+1.0f,  1.0f);
		
		mCurrentTile[3] = mCurrentTile[4].getRelativeTile( 0.0f, -1.0f);
		mCurrentTile[5] = mCurrentTile[4].getRelativeTile( 0.0f,  1.0f);
		
		mCurrentTile[6] = mCurrentTile[4].getRelativeTile(-1.0f, -1.0f);
		mCurrentTile[7] = mCurrentTile[4].getRelativeTile(-1.0f,  0.0f);
		mCurrentTile[8] = mCurrentTile[4].getRelativeTile(-1.0f,  1.0f);
	
		// 2: load tiles metadata
		for ( int i = 0; i < 9; i ++) {
			if( null!=mCurrentTile[i] ) {
				if ( !loadTileMetadata(mCurrentTile[i]) ) {
					mCurrentTile[i] = null;
				}
			}
		}

		// 2: Load meshes
		MeshES20 mainMesh = new MeshES20();
		mainMesh.addSubMesh(new GlobeMesh());
		
		mainMesh.addSubMesh(loadMesh(3));
		mainMesh.addSubMesh(loadMesh(4));
		mainMesh.addSubMesh(loadMesh(5));
//		mainMesh.addSubMesh(loadMesh(2));
//		mainMesh.addSubMesh(loadMesh(7));

		return mainMesh;
	}

//	private float[] getRelativeMatrix(int index) {
//		switch (index) {
//		case 0: return getRelativeMatrix( 1, -1);
//		case 1: return getRelativeMatrix( 0, -1);
//		case 2: return getRelativeMatrix(-1, -1);
//		case 3: return getRelativeMatrix( 1, 0);
//		case 5: return getRelativeMatrix(-1, 0);
//		case 6: return getRelativeMatrix( 1, 1);
//		case 7: return getRelativeMatrix( 0, 1);
//		case 8: return getRelativeMatrix(-1, 1);
//		}
//		float[] result = new float[16];
//		Matrix.setIdentityM(result,  0);
//		return result;
//	}
	
//	private float[] getRelativeMatrix(float dx, float dy) {
//		float[] result = new float[16];
//		Matrix.setIdentityM(result,  0);
//		Matrix.translateM(result, 0, dx, dy, 0.0f);
//		return result;
//	}

	public MeshES20 loadMesh(int index) {
		Tile tile = mCurrentTile[index];
		if ( null!=tile ) {
			return loadMesh(tile);
		}
		return null;
	}
		
	public MeshES20 loadMesh(Tile tile) {

		if ( !mIsInitialized ) {
			throw new IllegalStateException("WorldMapService has not been initialized");
		}
		Bitmap bmp = getTileBitmap(mContext, tile);
		if ( bmp==null ) {
			Log.e(TAG, String .format("Tile not found. %s", tile.getCode()));
			return null;
		}
		int width = bmp.getWidth();
		int height= bmp.getHeight();
		
		Log.e(TAG, String.format("Bitmap loaded: %dx%d", bmp.getWidth(), bmp.getHeight()));
		
		int[] pixels = new int[width*height]; 
		
		bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
		bmp.recycle();
		bmp = null;

		tile.setPixels(pixels, width, height);

		MeshES20 mesh = tile.buildTile(50);
		
		//MeshES20 mesh =  TileMesh.fromTile(tile, 50);
		tile.setPixels(null, 0, 0);
		
		System.gc();

		mesh.setCoordinatesVisibility(true);

		return mesh;
	}
	
	public boolean  loadTileMetadata(Tile tile) {
	
		boolean result = false;
		InputStream is = null;
		try {
			
			is = mContext.getAssets().open(
							String.format("ASTGTM2_%s_DEM.json", tile.getCode()));
			
			if (null!=is) {
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new InputStreamReader(is, ENCODING));
				TileMetadata  metadata = gson.fromJson(reader, TileMetadata.class);
				tile.setMetadata(metadata);
				
				result = true;
			}

		} catch (IOException e) {
			Log.e(TAG, e.getClass().getSimpleName() + ' ' + (e.getMessage()==null?"":e.getMessage()) , e);
		} finally {
			if ( null!=is) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
		return result;
	}

	@Override
	public float getAltitudeFromLoadedTile(float latitude, float longitude) throws NoTileException {

		int color = 0;
		if ( mCurrentTile[4] != null) {
			color = mCurrentTile[4].getPixelColor(latitude, longitude);
		}
		
		float altmin = 0;
		float altmax = 10000;
		if ( null!=mCurrentTile[4].getMetadata())  {
			altmin = mCurrentTile[4].getMetadata().alt_min;
			altmax = mCurrentTile[4].getMetadata().alt_max;
		}
		
		
		return CoordinateConversions.altitudeFromRangeColor(latitude, longitude, altmin, altmax, color);
		
	}
	
	@Override
	public int getColorFromLoadedTile(float latitude, float longitude) throws NoTileException {
		if ( mCurrentTile[4] != null) {
			mCurrentTile[4].getPixelColor(latitude, longitude);
		}
		return 0;
	}

	public Bitmap getTileBitmap(Context context, Tile tile) {

//		if ( !folderExists(context, "maps") ) {
//			
//			if ( !copyFolder(context, "maps") ) {
//				Log.e(TAG, "impossible to load files");
//				return null;
//			}
//			
//		}
		if ( !copyFolder(context, "maps") ) {
			Log.e(TAG, "impossible to load files");
			return null;
		}
			
		Log.e(TAG, tile.getCode());
		
		File file = new File(
				String.format("%s/%s/%s/ASTGTM2_%s_DEM.PNG", 
				Environment.getExternalStorageDirectory(), 
				context.getString(R.string.app_name), 
				"maps",
				tile.getCode()));

		if (!file.exists()) {
			return null;
		}
		
		InputStream istr = null;
		Bitmap bitmap = null;
		try {
			istr = new FileInputStream(file);
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			if ( !tile.isCenter() ) {
				opts.inSampleSize = 12;
			} else {				
				opts.inSampleSize = 8;
			}
			bitmap = BitmapFactory.decodeStream(istr, null, opts);
			istr.close();
			istr=null;
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	private static boolean folderExists(Context context, String name) {
		
		File folder = new File( String.format("%s/%s/%s", 
				Environment.getExternalStorageDirectory(), 
				context.getString(R.string.app_name), 
				name));
		return folder.exists();
		
	}

	private static boolean copyFolder(Context context, String name) {

		AssetManager assetManager = context.getAssets();
		String[] files = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			try {
				files = assetManager.list(name);
			} catch (IOException e) {
				Log.e("ERROR", "Failed to get asset file list.", e);
			}
			// Analyzing all file on assets subfolder
			for(String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				// First: checking if there is already a target folder
				File folder = new File(String.format("%s/%s", 
						Environment.getExternalStorageDirectory(), 
						context.getString(R.string.app_name)));
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdir();
				}
				if (!success) {
					return false;
				}				

				folder = new File(String.format("%s/%s/%s", 
						Environment.getExternalStorageDirectory(), 
						context.getString(R.string.app_name), 
						name));
				success = true;
				if (!folder.exists()) {
					success = folder.mkdir();
				}
				if (success) {
					// Moving all the files on external SD
					try {
						in = assetManager.open(name + "/" +filename);
						out = new FileOutputStream(String.format("%s/%s/%s/%s", 
								Environment.getExternalStorageDirectory(), 
								context.getString(R.string.app_name), 
								name,
								filename));
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
						
					} catch(IOException e) {
						Log.e("ERROR", "Failed to copy asset file: " + filename, e);
						return false;
					}
				}
				else {
					return false;
				}       
			}
			
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			return false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// is to know is we can neither read nor write
			return false;
		}
		return true;
	}

	//Method used by copyAssets() on purpose to copy a file.
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Override
	public void onLocationChanged(Location location, long waitingTime) {
		// TODO Auto-generated method stub
		
	}

}


