package com.gromstudio.treckar.service;

import android.content.Context;

import com.gromstudio.treckar.model.WorldMapArea;
import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TileMesh;

public abstract class WorldMapService {

	public class NotLocalizedException extends Exception {

		private static final long serialVersionUID = -7421690749668456737L;
		
	}
	
	public class NoTileException extends Exception {
		
	}

	/**
	 * Initializes the service
	 * @param context
	 * @return void
	 */
	public abstract void initialize(Context context) throws NotLocalizedException;
	
	/**
	 * Compute the area around the given position.
	 * @param latitude
	 * @param longitude
	 * @return the mesh map
	 */
	public abstract MeshES20 loadTileMesh(float latitude, float longitude);

	
	public abstract float getAltitudeFromLoadedTile(float latitude, float longitude) throws NoTileException;
	public abstract int getColorFromLoadedTile(float latitude, float longitude) throws NoTileException;

}
