package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class CircleMesh extends Mesh {
	

	public static final int NB_POINTS = 32;
	
	byte indices[];
	float vertices[];
	
	float colors[] = {
			0,0,0,0,
			1,0,0,1,
			0,1,0,1,
			0,0,1,1
	};

	private FloatBuffer colorBuffer;
	private FloatBuffer vertexBuffer;
	private ByteBuffer indexBuffer;


	public CircleMesh(int axis, float diameter, float[] color) {
		
		vertices = new float[NB_POINTS*3];
		indices = new byte[NB_POINTS*2+2];
		colors = new float[NB_POINTS*4];
		double currentAngle = 0.0;
		double step = (2.0*Math.PI/(double)NB_POINTS);
		for ( int i= 0; i < NB_POINTS; i++ ) {
			
			indices[2*i] = (byte)i;
			indices[2*i+1] = (byte)(i+1);
			
			float cos = diameter * (float) Math.cos(currentAngle);
			float sin = diameter * (float) Math.sin(currentAngle);

			switch (axis) {
			case AXIS_X:
				vertices[3*i] = 0.0f;
				vertices[3*i+1] = cos;
				vertices[3*i+2] = sin;
				break;
			case AXIS_Y:
				vertices[3*i] = cos;
				vertices[3*i+1] = 0.0f;
				vertices[3*i+2] = sin;
				break;
			case AXIS_Z:
				vertices[3*i] = sin;
				vertices[3*i+1] = cos;
				vertices[3*i+2] = 0.0f;
				break;
			}
			colors[4*i] = color[0];
			colors[4*i+1] = color[1];
			colors[4*i+2] = color[2];
			colors[4*i+3] = color[3];
			currentAngle += step;
			
		}
		indices[2*NB_POINTS] = NB_POINTS-1;
		indices[2*NB_POINTS+1] = 0;

		ByteBuffer vbb;
		vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		vbb = ByteBuffer.allocateDirect(colors.length*4);
		vbb.order(ByteOrder.nativeOrder());
		colorBuffer = vbb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	public void draw(GL10 gl) {

		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

		gl.glDrawElements(GL10.GL_LINES, NB_POINTS, GL10.GL_UNSIGNED_BYTE, indexBuffer);

	}

}
