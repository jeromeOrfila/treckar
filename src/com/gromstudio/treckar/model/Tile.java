package com.gromstudio.treckar.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	static final float MINIMUM_ERROR = 0.0001f;
	
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
		public boolean deleted = false;
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
		public boolean deleted=false;
		public byte passdone=0;
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
			result[offset] = (p2.y-p1.y)*(p3.z-p1.z) - (p2.z-p1.z)*(p3.y-p1.y);
			result[offset+1] = (p2.z-p1.z)*(p3.x-p1.x) - (p2.x-p1.x)*(p3.z-p1.z);
			result[offset+2] = (p2.x-p1.x)*(p3.y-p1.y) - (p2.y-p1.y)*(p3.x-p1.x);			
			MathUtils.normalize(3, result, offset);
		}
		
		public boolean hasAllNeighbors() {
			return t1!=null && t2!=null && t3!=null;
		}
		
		/**
		 * 
		 * @param tmp must be of length 9
		 */
		public byte calculateSimilarity(byte pass, float[] tmp, float[] result) {
			byte maxIndex = 0;
			getNormal(tmp, 0);
			if ( t1.passdone >= pass ) {
				t1.getNormal(tmp, 3);
				result[0] = MathUtils.dot(3, tmp, 0, tmp, 3);			
			} else {
				result[0] = 0.0f;
			}
			if ( t2.passdone >= pass ) {
				t2.getNormal(tmp, 3);
				result[1] = MathUtils.dot(3, tmp, 0, tmp, 3);		
				if ( result[1]>result[maxIndex] ) {
					maxIndex = 1;
				}
			} else {
				result[1] = 0.0f;
			}
			if ( t3.passdone >= pass ) {
				t3.getNormal(tmp, 3);
				result[2] = MathUtils.dot(3, tmp, 0, tmp, 3);				
				if ( result[2]>result[maxIndex] ) {
					maxIndex = 2;
				}
			} else {
				result[2] = 0.0f;
			}
			passdone = pass;
			return maxIndex;
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

		public void replacePoint(TilePoint oldPoint, TilePoint newPoint) {
			if ( p1==oldPoint ) {
				p1=newPoint;
				t1.replacePoint(oldPoint, newPoint);
				t3.replacePoint(oldPoint, newPoint);
			} else if ( p2==oldPoint ) {
				p2=newPoint;
				t2.replacePoint(oldPoint, newPoint);
				t1.replacePoint(oldPoint, newPoint);
			} else if ( p3==oldPoint ) {
				p3=newPoint;
				t3.replacePoint(oldPoint, newPoint);
				t2.replacePoint(oldPoint, newPoint);
			}
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
			passdone = 0;
		}
		
		public static TilePoint edgeCenter(TilePoint p1, TilePoint p2) {
			TilePoint result = new TilePoint(
					(p2.x+p1.x)/2.0f,
					(p2.y+p1.y)/2.0f,
					(p2.z+p1.z)/2.0f,
					Color.rgb(
							(Color.red(p1.color) + Color.red(p2.color))/2,
							(Color.green(p1.color) + Color.green(p2.color))/2,
							(Color.blue(p1.color) + Color.blue(p2.color))/2));
			return result;
		}
		
		public void collapseEdgeWithNeighbor(TileTriangle t) {
			if ( t!=t1 && t!=t2 && t!=t3 ) {
				return;
			}
			if ( t==t1 ) {
				TilePoint p = edgeCenter(p1, p2);
				replacePoint(p1, p);
				replacePoint(p2, p);
				t2.replaceLink(this, t3);
				t3.replaceLink(this, t2);
				p1.deleted = true;
				p2.deleted = true;
			} else if ( t==t2 ) {
				TilePoint p = edgeCenter(p2, p3);
				replacePoint(p2, p);
				replacePoint(p3, p);
				t1.replaceLink(this, t3);
				t3.replaceLink(this, t1);
				p2.deleted = true;
				p3.deleted = true;
			} else if ( t==t3 ) {
				TilePoint p = edgeCenter(p3, p1);
				replacePoint(p3, p);
				replacePoint(p1, p);
				t1.replaceLink(this, t2);
				t2.replaceLink(this, t1);
				p3.deleted = true;
				p1.deleted = true;
			}
			t.deleted=true;
			deleted = true;
		}
	}
	
	public MeshES20 buildTile() {
		return buildTile(Math.max(mWidth, mHeight));
	}
	
	private MeshES20 buildTile(int samples) {

		if ( mMetadata==null ) {
			throw new IllegalStateException("Tile's metadata not initialized.");
		}

		Log.e("Simplification", "Starting tile " + getCode() + " generation.");

		int nbSamples = Math.min(samples, Math.min(mWidth, mHeight));
		if ( nbSamples<2 ) {
			return null;
		}
		
		int pointsCount = nbSamples*nbSamples;
		
		float [] geod = new float[3];
		float [] geoc = new float[3];
		ArrayList<TilePoint> points = new ArrayList<TilePoint>(pointsCount);
		
		final float deltaLat = (mMetadata.tl[0] - mMetadata.bl[0])/(float) (nbSamples-1);
		final float deltaLong = (mMetadata.tr[1] - mMetadata.tl[1])/(float) (nbSamples-1);

		Log.e("Simplification", "  Generating "+pointsCount+" points for " + getCode() + ".");

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
						CoordinateConversions.globeColorFromAltitude(geod[2],mMetadata.alt_min, mMetadata.alt_max)));
				
				dLgt += deltaLong;
			}
			dLat += deltaLat;
		}
			
		int triangleCount = (nbSamples-1) * (nbSamples-1) * 2;
		Log.e("Simplification", "  Generating "+triangleCount+" triangles for " + getCode() + ".");
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

		Log.e("Simplification", "  Simplificating surface for " + getCode() + ".");
//		float[] tmp = new float[6];
//		float[] similarity = new float[3];
//		boolean simplified = true;
//		byte pass = 1;
//		while(simplified && pass<=100 ) {
//			
//			int o=0;
//			simplified = false;
//			Iterator<TileTriangle> it = triangles.iterator();
//			while (it.hasNext()) {
//				TileTriangle triangle = it.next();
//				if ( triangle!=null ) {
//					if ( triangle.deleted) {
//						it.remove();
//						o++;
//					} else if ( triangle.hasAllNeighbors()) {
//					
//						byte ind = triangle.calculateSimilarity(pass, tmp, similarity);
//						
//						if ( similarity[ind] >= 1.0f-MINIMUM_ERROR ) {
//		
//							// delete triangle
//							float[] center = new float[3];
//							if ( triangle.getBarycenter(center) ) {
//	
//								switch(ind) {
//								case 0: 
//									triangle.collapseEdgeWithNeighbor(triangle.t1);
//	//								triangle.p1.deleted=true;
//	//								triangle.p2.deleted=true;
//									break;
//								case 1: 
//									triangle.collapseEdgeWithNeighbor(triangle.t2);
//	//								triangle.p2.deleted=true;
//	//								triangle.p3.deleted=true;
//									break;
//								case 2: 
//									triangle.collapseEdgeWithNeighbor(triangle.t3);
//	//								triangle.p1.deleted=true;
//	//								triangle.p3.deleted=true;
//									break;
//								}
//								simplified = true;
//							}
//						}
//					}
//				}
//			}
//			Log.e("Simplification", "      pass " + pass + ": "+o+" triangles removed.");
//			pass++;
//		}
//		Iterator<TilePoint> itp = points.iterator();
//		int p = 0;
//		while (itp.hasNext()) {
//			TilePoint point = itp.next();
//			if ( point!=null && point.deleted) {
//				itp.remove();p++;
//			}
//		}
//		Log.e("Simplification", "Tile " + getCode() + " done, "+p+" points removed.");
		return TileMesh.generate(points, triangles);
	}
}

