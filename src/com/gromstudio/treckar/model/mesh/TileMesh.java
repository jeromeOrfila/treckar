package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.gromstudio.treckar.model.Tile;
import com.gromstudio.treckar.model.Tile.TilePoint;
import com.gromstudio.treckar.model.Tile.TileTriangle;
import com.gromstudio.treckar.util.CoordinateConversions;
import com.gromstudio.treckar.util.GLES20Program;

public class TileMesh extends MeshES20 {

	static final String TAG= "TileMesh";

	public static TileMesh generate(List<TilePoint> points, List<TileTriangle> triangle) {
		
		ByteBuffer bb = ByteBuffer.allocateDirect(points.size() * POSITION_STRIDE_BYTES);
	    bb.order(ByteOrder.nativeOrder());
	    FloatBuffer pointsBuffer = bb.asFloatBuffer();
	    pointsBuffer.position(0);
	    
	    bb = ByteBuffer.allocateDirect(points.size() * COLOR_STRIDE_BYTES);
	    bb.order(ByteOrder.nativeOrder());
	    FloatBuffer colorsBuffer = bb.asFloatBuffer();
	    colorsBuffer.position(0);
	    
	    bb = ByteBuffer.allocateDirect(triangle.size() * 3 * BYTES_PER_SHORT);
	    bb.order(ByteOrder.nativeOrder());
	    ShortBuffer indicesBuffer = bb.asShortBuffer();
	    indicesBuffer.position(0);
	    
	    for ( int i = 0; i<points.size(); i++) {
	    	
	    	TilePoint p = points.get(i);
	    	
	    	pointsBuffer.put(p.x);	
	    	pointsBuffer.put(p.y);
	    	pointsBuffer.put(p.z);	
	    	
			colorsBuffer.put((float)Color.red(p.color)/255.0f);
			colorsBuffer.put((float)Color.green(p.color)/255.0f);
			colorsBuffer.put((float)Color.blue(p.color)/255.0f);
			colorsBuffer.put(1.0f);

	    }
	    
	    for (int i = 0; i<triangle.size(); i++) { 
	    	
	    	TileTriangle t = triangle.get(i);
	    	
	    	short indP1 = (short)points.indexOf(t.p1);
	    	short indP2 = (short)points.indexOf(t.p2);
	    	short indP3 = (short)points.indexOf(t.p3);
	    	
			indicesBuffer.put(indP1);
			indicesBuffer.put(indP2);
			indicesBuffer.put(indP3);

		}
	    
	    return new TileMesh(pointsBuffer, colorsBuffer, indicesBuffer, triangle.size(), points.size());
	    
	}
	
		
	private FloatBuffer mPointsBuffer;
	private FloatBuffer mColorsBuffer;
	private ShortBuffer mIndicesBuffer;
	private int mPointsCount;
	private int mTrianglesCount;
	private float[] mModelMatrix = new float[16];
	

	private TileMesh(FloatBuffer points, FloatBuffer colors, ShortBuffer indicesBuffer, int nbTriangles, int pointsCount) {
		mPointsBuffer = points;
		mColorsBuffer = colors;
		mIndicesBuffer = indicesBuffer;
		mPointsCount = pointsCount;
		mTrianglesCount = nbTriangles;
		
		Matrix.setIdentityM(mModelMatrix, 0);
	}
	
	@Override
	public void drawMesh(GLES20Program program, final float[] model) {
		
		super.drawMesh(program, model);
		
		GLES20.glCullFace(GLES20.GL_FRONT_AND_BACK);
		
		GLES20.glEnableVertexAttribArray(program.getPositionHandle());
		checkGlError("glEnableVertexAttribArray");

		GLES20.glVertexAttribPointer(program.getPositionHandle(), 
				POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                POSITION_STRIDE_BYTES,
                mPointsBuffer);
		
		checkGlError("glVertexAttribPointer");
		
		GLES20.glEnableVertexAttribArray(program.getColorHandle());
		checkGlError("glEnableVertexAttribArray");
		
		GLES20.glVertexAttribPointer(program.getColorHandle(), 
				COLOR_DATA_SIZE, 
        		GLES20.GL_FLOAT, 
        		false, 
        		COLOR_STRIDE_BYTES, 
        		mColorsBuffer);
		
		checkGlError("glVertexAttribPointer");
		
		program.glPrepareMVPMatrix(model);
        
		mPointsBuffer.position(0);
		mColorsBuffer.position(0);
		mIndicesBuffer.position(0);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mTrianglesCount*3, GLES20.GL_UNSIGNED_SHORT, mIndicesBuffer);
        checkGlError("glDrawElements");
//        
		mPointsBuffer.position(0);
		mColorsBuffer.position(0);
		//GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPointsCount);

        GLES20.glDisableVertexAttribArray(program.getPositionHandle());
        GLES20.glDisableVertexAttribArray(program.getColorHandle());
        
	}
	

}
