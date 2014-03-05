package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class CubeMesh {
	private static final float[] _vertices =
		{
		// front
		-0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
		0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,

		// right
		0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f,
		0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f,

		// back
		0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
		-0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,

		// left
		-0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f,
		-0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,

		// top
		-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f,
		0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,

		// bottom
		-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f,
		0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f
		};

	private static final float[] _normals =
		{
		/* front */ 0, 0, 1, 0,  0, 1, 0, 0,  1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
		/* right */ 1, 0, 0, 1,  0, 0, 1, 0,  0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
		/* back */  0, 0,-1, 0,  0,-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
		/* left */ -1, 0, 0,-1,  0, 0,-1, 0,  0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
		/* top */   0, 1, 0, 0,  1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
		/* bottom */ 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0
		};

	private static final float[] _colors =
		{
		/* front  – cyan   */  0,1,1,1, 0,1,1,1, 0,1,1,1, 0,1,1,1, 0,1,1,1, 0,1,1,1, 
		/* right  – red    */  1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1,
		/* back   – green  */  0,1,0,1, 0,1,0,1, 0,1,0,1, 0,1,0,1, 0,1,0,1, 0,1,0,1,
		/* left   – blue   */  0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1,
		/* top    - yellow */  1,1,0,1, 1,1,0,1, 1,1,0,1, 1,1,0,1, 1,1,0,1, 1,1,0,1,
		/* bottom - magenta*/  1,0,1,1, 1,0,1,1, 1,0,1,1, 1,0,1,1, 1,0,1,1, 1,0,1,1
		};

	private FloatBuffer colorBuffer;
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;

	public CubeMesh() {

		ByteBuffer vbb  = ByteBuffer.allocateDirect(_vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(_vertices);
		vertexBuffer.rewind();

		vbb = ByteBuffer.allocateDirect(_colors.length*4);
		vbb.order(ByteOrder.nativeOrder());
		colorBuffer = vbb.asFloatBuffer();
		colorBuffer.put(_colors);
		colorBuffer.rewind();

		vbb = ByteBuffer.allocateDirect(_normals.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		normalBuffer = vbb.asFloatBuffer();
		normalBuffer.put(_normals);
		normalBuffer.rewind();

	}

	public void draw(GL10 gl) {
		// Draw our cube
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0,  _vertices.length / 3);
	}


}
