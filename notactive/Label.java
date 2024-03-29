// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package de.moliso.shelxle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * A class that is able to render a single label to an OpenGL texture,
 * and then render the texture. Also renders the copyright note.
 *
 * Label rendering is done synchronously on the render thread.
 */
public class Label {
    private Context mContext;
    private String VERTEX_SHADER;

    private String FRAGMENT_SHADER;
    private static String TAG = "Label.java";
    private int mVbo;
    private int mShader;
    private int mTransformLoc;
    private int mTextureLoc;

    private int mLabelTexture;
    private int mLabelTexWidth, mLabelTexHeight;
    private String mCurrentLabelString;
	private int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e(TAG, "Could not compile shader " + shaderType + ":");
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	private int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0) {
			return 0;
		}

		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (pixelShader == 0) {
			return 0;
		}

		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
//			checkGlError("glAttachShader");
			GLES20.glAttachShader(program, pixelShader);
//			checkGlError("glAttachShader");
			GLES20.glLinkProgram(program);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				Log.e(TAG, "Could not link program: ");
				Log.e(TAG, GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

    public void initialize(Context context) {
    	mContext =context;
        // Array buffer.
        short[] vertices = {
            0, 0,
            1, 0,
            0, 1,
            1, 1,
        };
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 2);
        byteBuf.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = byteBuf.asShortBuffer();
        buffer.put(vertices);
        buffer.position(0);

        int[] vbos = { 0 };
        GLES20.glGenBuffers(1, vbos, 0);
        mVbo = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo);
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER, vertices.length * 2, buffer, GLES20.GL_STATIC_DRAW);

        // Shader.
        VERTEX_SHADER = RawResourceReader.readTextFileFromRawResource(mContext,
				de.moliso.shelxle.R.raw.atoms_vetrex_shader);
        FRAGMENT_SHADER = RawResourceReader.readTextFileFromRawResource(
				mContext, de.moliso.shelxle.R.raw.atoms_fragment_shader);

		mShader = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		if (mShader == 0) {
			return;
		}
        GLES20.glBindAttribLocation(mShader, 0, "position");
        GLES20.glLinkProgram(mShader);
        mTransformLoc = GLES20.glGetUniformLocation(mShader, "transform");
        mTextureLoc = GLES20.glGetUniformLocation(mShader, "textureSampler");

        // Copyright texture.
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
      //  Bitmap bitmap = BitmapFactory.decodeResource(
      //          context.getResources(), R.drawable.body_copyright, opts);
      //  mCopyrightWidth = bitmap.getWidth();
      //  mCopyrightHeight = bitmap.getHeight();
      //  mCopyrightTexture = Textures.loadTexture(bitmap);
      //  GLES20.glTexParameteri(
      //          GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
      //  GLES20.glTexParameteri(
      //          GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
      //  bitmap.recycle();
    }

    // Delete all labels
    public void clearLabel() {
        mCurrentLabelString = null;
        if (mLabelTexture == 0) return;
        int[] textures = { mLabelTexture };
        GLES20.glDeleteTextures(1, textures, 0);
        mLabelTexture = 0;
    }

    // Update display deletes existing labels.
    public void updateDisplay(GLES20TriangleRenderer render, float canvasWidth, float canvasHeight) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glUseProgram(mShader);
        GLES20.glUniform1i(mTextureLoc, 0);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
        GLES20.glDisableVertexAttribArray(2);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo);
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_SHORT, false, 2 * 2, 0);

        // Label.
        if (mCurrentLabelString != null) {
            float[] coords = getCoords(render, mTargetEntity, canvasWidth, canvasHeight);
            float x = coords[0] - mLabelTexWidth / 2;
            float y = coords[1];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLabelTexture);
            drawRect(x, y, mLabelTexWidth, mLabelTexHeight, canvasWidth, canvasHeight);
        }

        // Copyright texture.
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCopyrightTexture);
        //drawRect(
        //        canvasWidth - mCopyrightWidth,
        //        canvasHeight - mCopyrightHeight,
        //        mCopyrightWidth, mCopyrightHeight, canvasWidth, canvasHeight);
    }

    private void drawRect(
            float x, float y, float width, float height, float canvasWidth, float canvasHeight) {
        // Map from (0, 0, canvasWidth, canvasHeigth) to (-1, -1, 1, 1)
        float w = 2 * width / canvasWidth;
        float h = 2 * height/ canvasHeight;
        x = 2 * x / canvasWidth - 1;
        y = -(2 * (y + height) / canvasHeight - 1);
        GLES20.glUniform4f(mTransformLoc, x, y, w, h);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    /**
     * @return Top center coordinate of label in pixels.
     */
    private float[] getCoords(
    		GLES20TriangleRenderer render, Base.EntityInfo targetEntity, float canvasWidth, float canvasHeight) {
        float[] center = {
                (targetEntity.bblx + targetEntity.bbhx) / 2,
                (targetEntity.bbly + targetEntity.bbhy) / 2,
                (targetEntity.bblz + targetEntity.bbhz) / 2
        };
        float[] coords = render.viewportCoords(center);

        // Find the lowest transformed bounding box corner.
        float[][] corners = {
                {targetEntity.bblx, targetEntity.bbly, targetEntity.bblz},
                {targetEntity.bblx, targetEntity.bbly, targetEntity.bbhz},
                {targetEntity.bblx, targetEntity.bbhy, targetEntity.bblz},
                {targetEntity.bblx, targetEntity.bbhy, targetEntity.bbhz},
                {targetEntity.bbhx, targetEntity.bbly, targetEntity.bblz},
                {targetEntity.bbhx, targetEntity.bbly, targetEntity.bbhz},
                {targetEntity.bbhx, targetEntity.bbhy, targetEntity.bblz},
                {targetEntity.bbhx, targetEntity.bbhy, targetEntity.bbhz},
        };
        for (float[] corner : corners) {
            float[] corner2d = render.viewportCoords(corner);
            coords[1] = Math.max(coords[1], corner2d[1]);
        }
        // Push the label down completely out of the bounding box.
        // (close enough).
        coords[1] += 20;
        // Bring it back into view if it's too far down.
        float maxHeight = canvasHeight - 75;
        if (coords[1] > maxHeight) {
            coords[1] = maxHeight;
        }
        // And if it's too far left or right.
        coords[0] = Math.max(mLabelTexWidth / 2, coords[0]);
        coords[0] = Math.min(canvasWidth - mLabelTexWidth / 2, coords[0]);

        return coords;
    }

    public void label(String text, EntityInfo targetEntity) {
        this.mTargetEntity = targetEntity;
        uploadTextTexture(text);
    }

    // Renders |text| into a bitmap and uploads that to OpenGL.
    private void uploadTextTexture(String text) {
        if (text.equals(mCurrentLabelString)) return;
        mCurrentLabelString = text;
        if (mLabelTexture == 0) {
            int[] textures = { 0 };
            GLES20.glGenTextures(1, textures, 0);
            mLabelTexture = textures[0];
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLabelTexture);
        GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        Bitmap bitmap = renderLabel(mCurrentLabelString);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        mLabelTexWidth = bitmap.getWidth();
        mLabelTexHeight = bitmap.getHeight();
        bitmap.recycle();
    }

    // Renders |text| into a bitmap and returns the bitmap.
    private Bitmap renderLabel(String text) {
        // Measure text.
        Paint textPaint = new Paint();
        textPaint.setTextSize(20);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int w = bounds.width() + 26;
        int h = bounds.height() + 24;

        // Allocate bitmap.
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(0);

        // Framed box.
        RectF rect = new RectF(0, 0, w, h);

        Paint fillPaint = new Paint();
        fillPaint.setColor(0xff000000);
        fillPaint.setAntiAlias(true);
        canvas.drawRoundRect(rect, 10, 10, fillPaint);

        // Text.
        textPaint.setARGB(0xff, 0xff, 0xff, 0xff);

        // drawText puts the baseline on y, but we want to visually center vertically.
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                text,
                (w - bounds.width()) / 2,
                h - (h - bounds.height()) / 2 - metrics.bottom/2,
                textPaint);
        return bitmap;
    }

    public boolean isLabelVisible() {
        return mCurrentLabelString != null;
    }

    public void reupload() {
        String label = mCurrentLabelString;
        clearLabel();
        label(label, mTargetEntity);
    }
}
