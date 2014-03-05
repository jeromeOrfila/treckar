package com.gromstudio.treckar.service;

import android.content.Context;

import com.gromstudio.treckar.model.mesh.Mesh;
import com.gromstudio.treckar.service.impl.WorldMapServiceImpl;

public class ServicesManager {

	private static WorldMapService sWorldMapService = null;
	
	public static WorldMapService getWorldMapService() {
		if ( sWorldMapService==null ) {
			sWorldMapService = new WorldMapServiceImpl();
		}
		return sWorldMapService;
	}
}
