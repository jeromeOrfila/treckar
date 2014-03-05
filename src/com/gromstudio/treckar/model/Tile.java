package com.gromstudio.treckar.model;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;

import com.gromstudio.treckar.model.mesh.MeshES20;
import com.gromstudio.treckar.model.mesh.TileMesh;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.MathUtils;

public class Tile {

	
	public static class TileMetadata {
		public String code;
		public float alt_min;
		public float alt_max;
		public float[] tl;
		public float[] tr;
		public float[] br;
		public float[] bl;
		public TileMetadata() {
			
		}
	}
	
	static final float MINIMUM_ERROR = 0.00f;
	
	public enum VDirectionEnum {N, S};
	public enum HDirectionEnum {E, W};
	
	// tile info
	VDirectionEnum mVDirection;
	int mVValue;
	HDirectionEnum mHDirection;
	int mHValue;
	
	boolean mIsCenter;
	
	// pixels data
	int[] mPixels;
	int mWidth, mHeight;
	MeshES20 mMesh;

	// metadata
	TileMetadata mMetadata;

	public Tile(float latitude, float longitude) {
		
		if ( latitude>=0.0f ) {
			mVDirection=VDirectionEnum.N;	
			mVValue=(int) Math.abs(latitude);
		} else {
			mVDirection=VDirectionEnum.S;
			mVValue=(int) Math.abs(latitude)+1;
		}

		if ( longitude>=0.0f ) {
			mHDirection=HDirectionEnum.E;	
			mHValue=(int) Math.abs(longitude);
		} else {
			mHDirection=HDirectionEnum.W;
			mHValue=(int) Math.abs(longitude)+1;
		}
		
		mPixels = null;
		mWidth = -1;
		mHeight = -1;
		mMesh = null;
		mMetadata = null;
		
		mIsCenter = false;
		
	}
	
	public void setMetadata(TileMetadata meta) {
		mMetadata = meta;
	}
	
	public TileMetadata getMetadata() {
		return mMetadata ;
	}
	
	public void setCenter(boolean isCenter) {
		mIsCenter = isCenter;
	}
	
	public boolean isCenter() {
		return mIsCenter;
	}
	
	public int getPixelColor(float latitude, float longitude) {
		
		if ( null==mPixels ) {
			return 0;
		}
		float dy = Math.abs(latitude) - mVValue;
		float dx = Math.abs(longitude) - mHValue;
		if ( dx<0.0f || dx>=1.0f || dy<0.0f || dy>=1.0f ) {
			return 0;
		}
		int x = (int)(dx* (float) mWidth);
		if ( mHDirection == HDirectionEnum.W ) {
			x = mWidth-x;
		}
		int y = (int)(dx* (float) mHeight);
		if ( mHDirection == HDirectionEnum.W ) {
			y = mWidth-y;
		}
		return mPixels[y*mWidth + x];
	}
	
	public float[] getCenter() {

		float latitude = (mVDirection==VDirectionEnum.S ? -mVValue : mVValue) + .5f;
		float longitude = (mHDirection==HDirectionEnum.W ? -mHValue : mHValue) + .5f;		
		return new float[] {latitude, longitude};
		
	}
	
	public Tile getRelativeTile(float dLatitude, float dLongitude) {
		
		float[] c = getCenter();
		float ltd = c[0]+dLatitude;
		float lgd = c[1]+dLongitude;
		if ( ltd<-180f) {
			ltd += 360f;
		}
		if ( ltd>=180f) {
			ltd -= 360f;
		}
		if ( lgd<-90f) {
			ltd += 180f;
		}
		if ( lgd>=90f) {
			ltd -= 180f;
		}
		return new Tile(ltd, lgd);
	}
	
	public void setPixels(int[] pixels, int width, int height) {
		mPixels = pixels;
		mWidth = width;
		mHeight = height;
	}

	public int[] getPixels() {
		return mPixels;
	}
	
	public String getCode() {
		
		return String.format("%s%02d%s%03d", 
				mVDirection.toString(), 
				mVValue, 
				mHDirection.toString(), 
				mHValue);
	}
	
	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}
	
	public static class TilePoint {
		public float x; public float y; public float z;
		public int color;
		TilePoint[] neighors = null;
		public TilePoint(float x, float y, float z, int c) {
			this.x = x; this.y = y; this.z = z; this.color = c;
		}
	}
	
	public static class TileTriangle{
		public TilePoint p1; 
		public TilePoint p2; 
		public TilePoint p3;
		public TileTriangle t1; 
		public TileTriangle t2; 
		public TileTriangle t3;
		public float e = 1.0f;
		public boolean deleted = false;
		public TileTriangle(TilePoint point1, TilePoint point2, TilePoint point3) {
			this.p1 = point1; 
			this.p2 = point2; 
			this.p3 = point3;
		}
		public void setTriangle1(TileTriangle triangle) {
			t1 = triangle;
		}
		public void setTriangle2(TileTriangle triangle) {
			t2 = triangle;
		}
		public void setTriangle3(TileTriangle triangle) {
			t3 = triangle;
		}
		public void getNormal(float[] result, int offset) {
//			float[] p1p2 = new float[] {p2.x-p1.x, p2.y-p1.y,p2.z-p1.z};
//			float[] p1p3 = new float[] {p3.x-p1.x, p3.y-p1.y,p3.z-p1.z};
			result[offset] = (p2.y-p1.y)*(p3.z-p1.z) - (p2.z-p1.z)*(p3.y-p1.y);
			result[offset+1] = (p2.z-p1.z)*(p3.x-p1.x) - (p2.x-p1.x)*(p3.z-p1.z);
			result[offset+2] = (p2.x-p1.x)*(p3.y-p1.y) - (p2.y-p1.y)*(p3.x-p1.x);			
			MathUtils.normalize(3, result, offset);
			
		}
		
		public boolean hasAllNeighbors() {
			return t1!=null && t2!=null && t3!=null;
		}
		
		public boolean hasAllNeighbors2() {
			return hasAllNeighbors() && t1.hasAllNeighbors() && t2.hasAllNeighbors() && t3.hasAllNeighbors();
		}
		
		/**
		 * 
		 * @param tmp must be of length 9
		 */
		public float calculate(float[] tmp) {
			if ( !hasAllNeighbors2() ) {
				e = 1.0f;
				return e;
			}
			getNormal(tmp, 0);
			t1.getNormal(tmp, 3);
			t2.getNormal(tmp, 6);
			t3.getNormal(tmp, 9);
			MathUtils.add(3, tmp, 3, tmp, 6, tmp, 9);
			MathUtils.normalize(3, tmp, 9);			
			e = MathUtils.dot(3, tmp, 0, tmp, 9);
			return e;
		}
		
		public boolean getBarycenter(float[] center) {
			if ( !hasAllNeighbors() || !t1.hasAllNeighbors() || !t2.hasAllNeighbors() || !t3.hasAllNeighbors() ) {
				return false;
			}
			center[0] = (p1.x + p2.x + p3.x) / 3.0f;
			center[1] = (p1.y + p2.y + p3.y) / 3.0f;
			center[2] = (p1.z + p2.z + p3.z) / 3.0f;
			return true;
		}
		
		public void replaceLink(TileTriangle oldTriangle, TileTriangle newTriangle) {
			if (t1==oldTriangle ) {
				t1 = newTriangle;
			}
			if (t2==oldTriangle ) {
				t2 = newTriangle;
			}
			if (t3==oldTriangle ) {
				t3 = newTriangle;
			}
		}
			
		public void neighborDiscarded(TileTriangle t) {
			if ( t==t1 ) {
				t2.replaceLink(this, t3);
				t3.replaceLink(this, t2);
			} 
			if ( t==t2 ) {
				t1.replaceLink(this, t3);
				t3.replaceLink(this, t1);
			}
			if ( t==t3 ) {
				t1.replaceLink(this, t2);
				t2.replaceLink(this, t1);
			}
			deleted = true;
		}
		
		public void discard() {
			
			t1.neighborDiscarded(this);
			t2.neighborDiscarded(this);
			t3.neighborDiscarded(this);
			
			deleted = true;
		}
		
		public void replacePoint(TilePoint oldPoint, TilePoint newPoint) {
			boolean replaced = false;
			if ( p1==oldPoint ) {
				p1 = newPoint;
				replaced = true;
			}
			if ( p2==oldPoint ) {
				p2 = newPoint;
				replaced = true;
			}
			if ( p3==oldPoint ) {
				p2 = newPoint;
				replaced = true;
			}
			if ( replaced ) {
				// update neighbors
				t1.replacePoint(oldPoint, newPoint);
				t2.replacePoint(oldPoint, newPoint);
				t3.replacePoint(oldPoint, newPoint);
			}
		}
	}
	
	public MeshES20 buildTile() {
		return buildTile(Math.max(mWidth, mHeight));
	}
	
	public MeshES20 buildTile(int samples) {

		if ( mMetadata==null ) {
			throw new IllegalStateException("Tile's metadata not initialized.");
		}

		int nbSamples = Math.min(samples, Math.max(mWidth, mHeight));
		if ( nbSamples<2 ) {
			return null;
		}
		
		int pointsCount = nbSamples*nbSamples;
		
		float[] topLeft= mMetadata.tl;
		float altmin = 0;
		float altmax = 10000;
		
		float [] geod = new float[3];
		float [] geoc = new float[3];
		ArrayList<TilePoint> points = new ArrayList<TilePoint>(pointsCount);
		
		final float deltaLat = (mMetadata.tl[0] - mMetadata.bl[0])/(float) (nbSamples-1);
		final float deltaLong = (mMetadata.tr[1] - mMetadata.tl[1])/(float) (nbSamples-1);
		
		float dLat = 0.0f; // positive number between 0.0f and 1.0f 
		float dLgt = 0.0f; // positive number between 0.0f and 1.0f
		for (int j = 0; j<nbSamples; j++) { // delta on latitudes
			dLgt = 0.0f;
			int y = (int)(dLat * (float)mHeight);
			if ( y>=mHeight ) {
				y= mHeight-1;
			}
			for (int i = 0; i<nbSamples; i++) { // delta on longitude
					
				int x = (int)(dLgt * (float)mWidth);
				if ( x>=mWidth ) {
					x= mWidth-1;
				}
				if ( x<0 ) {
					x=0;
				}
				
				
				int color = mPixels[y*mWidth+x];
				
				geod[0] = mMetadata.tl[0] - dLat;
				geod[1] = mMetadata.tl[1] + dLgt;
				geod[2] = CoordinateConversions
						.altitudeFromRangeColor(geod[0], geod[1], mMetadata.alt_min, mMetadata.alt_max, color); 
				CoordinateConversions.getGeocCoords(geod, geoc);
				
				points.add(new TilePoint(geoc[0], geoc[1], geoc[2], 
						CoordinateConversions.globeColorFromAltitude(geod[2])));
				
				dLgt += deltaLong;
			}
			dLat += deltaLat;
		}
			
		int triangleCount = (nbSamples-1) * (nbSamples-1) * 2;
		ArrayList<TileTriangle> triangles  = new ArrayList<TileTriangle>(triangleCount);
		int index = 0;
		for (int j = 0; j<(nbSamples-1); j++) { // delta on latitudes
			for (int i = 0; i<(nbSamples-1); i++) { // delta on longitude
				
				int indexPoint1 = j*nbSamples+i;
				
				TilePoint p1 = points.get(indexPoint1);
				TilePoint p2 = points.get(indexPoint1+1);
				TilePoint p3 = points.get(indexPoint1+nbSamples+1);
				TilePoint p4 = points.get(indexPoint1+nbSamples);
				
				TileTriangle t1 = new TileTriangle (p1, p2, p4);
				TileTriangle t2 = new TileTriangle (p4, p2, p3);

				t2.setTriangle1(t1);
				t1.setTriangle2(t2);

				if ( j >= 1 ) {
					TileTriangle top = triangles.get(index+1-(2*(nbSamples-1)));
					top.setTriangle3(t1);
					t1.setTriangle1(top);
				}
				
				if ( i >= 1 ) {
					TileTriangle left = triangles.get(index-1);
					left.setTriangle2(t1);
					t1.setTriangle3(left);
				}
				triangles.add(t1);
				triangles.add(t2);
				index++;
				index++;
			}			
		}

//		float[] tmp = new float[12];
//		boolean simplified = true;
//		int passes = 0;
//		while(simplified && passes<100 ) {
//			simplified = false;
//			Iterator<TileTriangle> it = triangles.iterator();
//			while (it.hasNext()) {
//				TileTriangle triangle = it.next();
//				if ( triangle!=null && !triangle.deleted) {
//					float res = triangle.calculate(tmp);
//					if ( res < MINIMUM_ERROR ) {
//	
//						// delete triangle
//						float[] center = new float[3];
//						if ( triangle.getBarycenter(center) ) {
//							TilePoint p = new TilePoint(center[0], 
//									center[1], 
//									center[2],
//									CoordinateConversions.colorFromAltitude(center[2]));
//							points.add(p);
//		
//							TilePoint p1 = triangle.p1;
//							TilePoint p2 = triangle.p2;
//							TilePoint p3 = triangle.p3;
//							
//							triangle.discard();
//							
//							points.remove(p1);
//							points.remove(p2);
//							points.remove(p3);
//							
//							simplified = true;
//						}
//					}
//				}
//			}
//		}
//		Iterator<TileTriangle> it = triangles.iterator();
//		while (it.hasNext()) {
//			TileTriangle triangle = it.next();
//			if ( triangle!=null && triangle.deleted) {
//				it.remove();
//			}
//		}
		return TileMesh.generate(points, triangles);
	}
}

