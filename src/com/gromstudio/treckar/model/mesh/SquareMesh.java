package com.gromstudio.treckar.model.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class SquareMesh extends Mesh {

	private float vertices[];

	private float texture[] = {         
			// Mapping coordinates for the vertices
			0.0f, 1.0f,     // top left     (V2)
			0.0f, 0.0f,     // bottom left  (V1)
			1.0f, 1.0f,     // top right    (V4)
			1.0f, 0.0f      // bottom right (V3)
	};

	private FloatBuffer textureBuffer;
	private FloatBuffer vertexBuffer;
	private int[] textures = new int[1];


	public SquareMesh(int axis){

		switch (axis) {
		case AXIS_X:
			vertices = new float[] {
				0.0f, -1.0f, -1.0f,          // V1 - bottom left
				0.0f, -1.0f,  1.0f,          // V2 - top left
				0.0f,  1.0f, -1.0f,          // V3 - bottom right
				0.0f,  1.0f,  1.0f,          // V4 - top right
			};
			break;
		case AXIS_Y:
			vertices = new float[] {
				-1.0f,  0.0f, -1.0f,          // V1 - bottom left
				-1.0f,  0.0f,  1.0f,          // V2 - top left
				 1.0f,  0.0f, -1.0f,       // V3 - bottom right
				 1.0f,  0.0f,  1.0f         // V4 - top right
			};
			break;
		case AXIS_Z:
			vertices = new float[] {
				-1.0f, -1.0f,  0.0f,        // V1 - bottom left
				-1.0f,  1.0f,  0.0f,        // V2 - top left
				 1.0f, -1.0f,  0.0f,        // V3 - bottom right
				 1.0f,  1.0f,  0.0f         // V4 - top right
			};
			break;
		}
		
		ByteBuffer vbb  = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		vbb = ByteBuffer.allocateDirect(texture.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		textureBuffer = vbb.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

	}

	public void loadGLTexture(GL10 gl, Context context, int textId) {
		
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), textId);
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}


	public void draw(GL10 gl){

		gl.glDisable(GL10.GL_CULL_FACE);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		// Point to our buffers
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);


		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glDisable(GL10.GL_CULL_FACE);

	}

}
