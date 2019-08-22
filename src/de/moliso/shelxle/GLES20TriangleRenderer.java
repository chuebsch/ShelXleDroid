/*0
 * Copyright (C) 201;1 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.moliso.shelxle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

class GLES20TriangleRenderer implements GLSurfaceView.Renderer {
	public ArrayList<Molecule> mol = new ArrayList<Molecule>();
	private ArrayList<Molecule> asymm = new ArrayList<Molecule>();
	public ArrayList<Molecule> qmol = new ArrayList<Molecule>();;
	private ArrayList<Bond> bonds = new ArrayList<Bond>();
	private ArrayList<Bond> qbonds = new ArrayList<Bond>();
	private ArrayList<Integer> sfac = new ArrayList<Integer>();;// !<List of
																// Scattering
																// factors.
	private ArrayList<Double> fvar = new ArrayList<Double>();;// !<List of Free
																// Variables.
	// private HashMap<Integer, Integer> fvarCntr = new HashMap<Integer,
	// Integer>();// !<Free
	SparseIntArray fvarCntr2 = new SparseIntArray(); // Variable
	// counter
	public int renderCnt; // QMap.
	private boolean hires = false;
	private boolean adps = true;
	public boolean countFrames = true;
	public float qmin;
	public float qmax;
	boolean drawFo = true;
	boolean drawDif = true;

	public String theFileName;
	Cell cell;
	final Context parent;
	public boolean drawLabels = false;

	public void setLabels(boolean b) {
		drawLabels = b;
	}

	public void setADPs(boolean b) {
		adps = b;
	}

	// AtomLabelView alv;

	// public void setALV(AtomLabelView _alv) {
	// alv = _alv;
	// }

	public void setHighRes(boolean b) {
		hires = b;

		mIndices.clear();
		if (hires) {
			mIndices = ByteBuffer
					.allocateDirect(idx4.length * SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			mIndices.put(idx4).position(0);
		} else {
			mIndices = ByteBuffer
					.allocateDirect(idx2.length * SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			mIndices.put(idx2).position(0);
		}
	}

	public String msg = "";

	public GLES20TriangleRenderer(Context context, Context Parent) {
		mContext = context;
		mScale = 1.0f;
		renderCnt = 0;
		msg = "";
		parent = Parent;
		theFileName = "";
		// alv = new AtomLabelView(parent);
		mTriangleVertices = ByteBuffer
				.allocateDirect(mCubeVerticesData.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangleVertices.put(mCubeVerticesData).position(0);
		if (hires) {
			mIndices = ByteBuffer
					.allocateDirect(idx4.length * SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			mIndices.put(idx4).position(0);
		} else {
			mIndices = ByteBuffer
					.allocateDirect(idx2.length * SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			mIndices.put(idx2).position(0);
		}
		// Toast.makeText(mContext, mFragmentShader.toString(),
		// Toast.LENGTH_LONG).show();
		// Molecule m;
		// mol.clear();
		makeIcosa(0.2f);
		initBill();
		// Toast.makeText(parent, mFragmentShader.toString(),
		// Toast.LENGTH_LONG).show();
		// if (!SimpleExplorer.resultFile.isEmpty())
		// load_sheldrick(SimpleExplorer.resultFile);

	}

	static public float[] matrix(float m1, float m2, float m3, float m4,
			float m5, float m6, float m7, float m8, float m9) {
		float[] erg = new float[9];
		erg[0] = m1;
		erg[1] = m2;
		erg[2] = m3;
		erg[3] = m4;
		erg[4] = m5;
		erg[5] = m6;
		erg[6] = m7;
		erg[7] = m8;
		erg[8] = m9;
		return erg;
	}

	public int picked = -1;
	public int qpicked = -1;
	public int ppicked = -1;
	public int pppicked = -1;
	public int ppppicked = -1;

	public void transformer(final float pmv[], final int viewport[]) {
		for (Molecule m : mol) {
			Molecule.posTo2D(m.pos, pmv, viewport, m.screenXY);
		}
		for (Molecule q : qmol) {
			Molecule.posTo2D(q.pos, pmv, viewport, q.screenXY);
		}
	}

	public static void normalMatrix(float[] result, float[] m4) {
		// result= new float[9];
		result[0] = 1.0f;
		result[1] = 0.0f;
		result[2] = 0.0f;

		result[3] = 0.0f;
		result[4] = 1.0f;
		result[5] = 0.0f;

		result[6] = 0.0f;
		result[7] = 0.0f;
		result[8] = 1.0f;
		// | 0 1 2 |
		// M = | 4 5 6 | det(M) = 0 * (5 10 - 9 6) - 1 * (4 10 - 8 6) + 2 * (4 9
		// - 8 5)
		// | 8 9 10|

		float det = m4[0] * (m4[5] * m4[10] - m4[9] * m4[6]) - m4[1]
				* (m4[4] * m4[10] - m4[8] * m4[6]) + m4[2]
				* (m4[4] * m4[9] - m4[8] * m4[5]);

		if (det == 0)
			return;

		det = 1.0f / det;
		// 00 10 20 0 1 2
		// 01 11 21 4 5 6
		// 02 12 22 8 9 10

		/*
		 * invm[0 + 0 * 3] = (m4[5] * m4[10] - m4[6] * m4[9]) * det; invm[1 + 0
		 * * 3] = -(m4[1] * m4[10] - m4[9] * m4[2]) * det; invm[2 + 0 * 3] =
		 * (m4[1] * m4[6] - m4[5] * m4[2]) * det;
		 * 
		 * invm[0 + 1 * 3] = -(m4[4] * m4[10] - m4[6] * m4[8]) * det; invm[1 + 1
		 * * 3] = (m4[0] * m4[10] - m4[8] * m4[2]) * det; invm[2 + 1 * 3] =
		 * -(m4[0] * m4[6] - m4[4] * m4[2]) * det;
		 * 
		 * invm[0 + 2 * 3] = (m4[4] * m4[9] - m4[8] * m4[5]) * det; invm[1 + 2 *
		 * 3] = -(m4[0] * m4[9] - m4[8] * m4[1]) * det; invm[2 + 2 * 3] = (m4[0]
		 * * m4[5] - m4[1] * m4[4]) * det;
		 */

		result[0] = (m4[5] * m4[10] - m4[6] * m4[9]) * det;
		result[3] = (m4[1] * m4[10] - m4[9] * m4[2]) * -det;
		result[6] = (m4[1] * m4[6] - m4[5] * m4[2]) * det;

		result[1] = (m4[4] * m4[10] - m4[6] * m4[8]) * -det;
		result[4] = (m4[0] * m4[10] - m4[8] * m4[2]) * det;
		result[7] = (m4[0] * m4[6] - m4[4] * m4[2]) * -det;

		result[2] = (m4[4] * m4[9] - m4[8] * m4[5]) * det;
		result[5] = (m4[0] * m4[9] - m4[8] * m4[1]) * -det;
		result[8] = (m4[0] * m4[5] - m4[1] * m4[4]) * det;
		return;
	}

	private float fpsscal = 0.9f;
	private final float fpsMat[] = { -fpsscal, -0.000000f, -0.000000f,
			0.000000f, 0.000000f, -0.000000f, 0.190000f, 0.000000f, -0.000000f,
			0.060000f, 0.000000f, 0.000000f, 1.0f - fpsscal, -0.950000f,
			0.000000f, 1.000000f };
	private final float fpsMat3[] = { -fpsscal, -0.000000f, -0.000000f,
			0.000000f, 0.000000f, -0.000000f, 0.190000f, 0.000000f, -0.000000f,
			0.060000f, 0.000000f, 0.000000f, 1.0f - fpsscal, -0.89000f,
			0.000000f, 1.000000f };

	private final float fpsMat4[] = { -fpsscal, -0.000000f, -0.000000f,
			0.000000f, 0.000000f, -0.000000f, 0.190000f, 0.000000f, -0.000000f,
			0.060000f, 0.000000f, 0.000000f, 1.0f - fpsscal, -0.84000f,
			0.000000f, 1.000000f };

	private final float fpsMat2[] = { -0.23f * fpsscal, -0.000000f, -0.000000f,
			0.000000f, 0.000000f, -0.000000f, 0.050000f, 0.000000f, -0.000000f,
			2.000000f, 0.000000f, 0.000000f, -1.000000f, -1.000000f, 0.000000f,
			1.000000f };

	private Bitmap fpsbitmap;

	private Bitmap dangbitmap, dangbitmap2;
	private String fpsStr = "", oldFpsStr = "xx";
	private String DANGStr = "", DANGStr2 = "", oldDANGStr = "xx", oldDANGStr2 = "xx";

	public void onDrawFrame(GL10 glUnused) {

		renderCnt++;

		IntBuffer viewport = IntBuffer.allocate(4);
		GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport);
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		try {
			GLES20.glUseProgram(mProgram);
			checkGlError("glUseProgram");
		} catch (RuntimeException e) {
			Log.e("glUseProgram", String.valueOf(mProgram));
			return;
		}
		GLES20.glFrontFace(GLES20.GL_CW);
		GLES20.glCullFace(GLES20.GL_FRONT);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		if ((mScale <= 10.0f) && (mScale >= 0.1f))
			Matrix.scaleM(mv, 0, mScale, mScale, mScale);
		else {
			if (mScale > 10.0f)
				mScale = 10.0f;
			if (mScale < 0.1f)
				mScale = 0.1f;
		}
		mScale = 1.0f;
		// Set a matrix that contains the current rotation.
		Matrix.setIdentityM(modelview, 0);
		Matrix.rotateM(modelview, 0, mDeltaX, 0.0f, 0.0f, 1.0f);
		Matrix.rotateM(modelview, 0, -mDeltaY, 1.0f, 0.0f, 0.0f);

		mDeltaX = 0.0f;
		mDeltaY = 0.0f;

		// Multiply the current rotation by the accumulated rotation, and then
		// set the accumulated rotation to the result.
		Matrix.multiplyMM(tm, 0, modelview, 0, mv, 0);
		System.arraycopy(tm, 0, mv, 0, 16);
		System.arraycopy(tm, 0, modelview, 0, 16);
		normalMatrix(invmat1, modelview);
		Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);

		transformer(pmv1, viewport.array());
		GLES20.glUniform1i(lichtH, 0);
		GLES20.glUniform1i(ellipsoidH, 0);

		GLES20.glUniform1i(texturH, 0);
		GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
		GLES20.glUniformMatrix3fv(invmatrixUniform1, 1, false, invmat1, 0);

		paintBonds();
		if ((drawFo) && (mFFTfopMap != null)) {
			GLES20.glUniform3f(colorUniform1, 0.0f, 0.0f, 1.0f);
			paintFOPMap();
			GLES20.glUniform3f(colorUniform1, 1.0f, 1.0f, 0.0f);
			paintFOMMap();
		}
		if ((drawDif) && (mFFTfopMap != null)) {
			GLES20.glUniform3f(colorUniform1, 0.0f, 1.0f, 0.0f);
			paintDIPMap();
			GLES20.glUniform3f(colorUniform1, 1.0f, 0.0f, 0.0f);
			paintDIMMap();
		}
		int qi = 0;
		// Matrix.setIdentityM(modelview, 0);
		// Log.d(TAG,
		// "lower cut " + qLowerCut + " min " + qmin + " size "
		// + qmol.size());
		for (Molecule q : qmol) {
			if (qLowerCut > qmin)
				if (q.phgt <= qLowerCut)
					continue;
			System.arraycopy(tm, 0, modelview, 0, 16);
			// Log.d(TAG, "pos " + q.phgt + " " + q.lab + " " + q.pos.x + " "
			// + q.pos.y + " " + q.pos.z);
			Matrix.translateM(modelview, 0, q.pos.x, q.pos.y, q.pos.z);
			if (qi == qpicked)
				Matrix.scaleM(modelview, 0, 0.7f, 0.7f, 0.7f);
			else
				Matrix.scaleM(modelview, 0, 0.4f, 0.4f, 0.4f);

			normalMatrix(invmat1, modelview);
			Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);

			GLES20.glUniform1i(lichtH, 1);
			GLES20.glUniform1i(ellipsoidH, 0);
			GLES20.glUniform1i(texturH, 0);
			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
			GLES20.glUniformMatrix3fv(invmatrixUniform1, 1, false, invmat1, 0);
			// GLES20.glUniform3f(colorUniform1,0f,0f,1f);
			GLES20.glUniform3f(colorUniform1, q.col.x, q.col.y, q.col.z);
			paintIcosa();
			qi++;
		}
		int mi = 0;
		for (Molecule mo : mol) {
			System.arraycopy(tm, 0, modelview, 0, 16);
			Matrix.translateM(modelview, 0, mo.pos.x, mo.pos.y, mo.pos.z);
			Matrix.rotateM(modelview, 0, mo.ang, mo.ax.x, mo.ax.y, mo.ax.z);
			if (!mo.lab.startsWith("H") && (adps))
				Matrix.scaleM(modelview, 0, mo.ev.x, mo.ev.y, mo.ev.z);
			else
				Matrix.scaleM(modelview, 0, mo.rad, mo.rad, mo.rad);// */
			if (mi == picked)
				Matrix.scaleM(modelview, 0, 1.3f, 1.3f, 1.3f);
			normalMatrix(invmat1, modelview);
			Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);
			GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT,
					false, 0, mTriangleVertices);
			GLES20.glEnableVertexAttribArray(vertexAttr1);
			GLES20.glVertexAttribPointer(normalAttr1, 3, GLES20.GL_FLOAT,
					false, 0, mTriangleVertices);
			GLES20.glEnableVertexAttribArray(normalAttr1);
			GLES20.glUniform1i(lichtH, 1);
			GLES20.glUniform1i(texturH, 0);
			if (mo.lab.startsWith("H") || (!adps))
				GLES20.glUniform1i(ellipsoidH, 0);
			else
				GLES20.glUniform1i(ellipsoidH, 1);
			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
			GLES20.glUniformMatrix3fv(invmatrixUniform1, 1, false, invmat1, 0);
			// GLES20.glUniform3f(colorUniform1,0f,0f,1f);
			float pmngh = ((mo.symmGroup > 0) && (mo.part < 0)) ? 0.2f : 0;
			pmngh = (mi == picked) ? -0.9f : pmngh;
			GLES20.glUniform3f(colorUniform1, mo.col.x - pmngh, mo.col.y
					- pmngh, mo.col.z + pmngh);
			if (hires)
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx4.length,
						GLES20.GL_UNSIGNED_SHORT, mIndices);
			else
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx2.length,
						GLES20.GL_UNSIGNED_SHORT, mIndices);
			checkGlError("glDrawElements");
			mi++;

		}
		int z = mol.size();
		if (drawLabels) {

			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);
			mi = 0;
			for (Bitmap bi : bitmap) {
				// ///////
				if (bi==null) continue;
				float ratio = 0;
				if (texture[0] != 0)
					GLES20.glDeleteTextures(1, texture, 0);
				GLES20.glGenTextures(1, texture, 0);
				checkGlError("glGenTextures");
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				// Bitmap bitmap = renderLabel(mol.get(i).lab);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bi, 0);
				ratio = (float) bi.getWidth() / bi.getHeight();
				// bitmap.recycle();
				// ///////

				System.arraycopy(tm, 0, modelview, 0, 16);
				if (mi >= z) {
					if (qLowerCut > qmin)
						if (qmol.get(mi - z).phgt <= qLowerCut)
							continue;
					Matrix.translateM(modelview, 0, qmol.get(mi - z).pos.x,
							qmol.get(mi - z).pos.y, qmol.get(mi - z).pos.z);
					BillMatrix(modelview);
					Matrix.scaleM(modelview, 0, ratio * 2 * labelScale,
							2 * labelScale, 2 * labelScale);

				} else {
					Matrix.translateM(modelview, 0, mol.get(mi).pos.x,
							mol.get(mi).pos.y, mol.get(mi).pos.z);
					// Matrix.scaleM(modelview, 0, 0.25f, 0.25f, 0.25f);
					BillMatrix(modelview);
					Matrix.scaleM(modelview, 0, ratio * 3 * labelScale,
							3 * labelScale, 3 * labelScale);

				}

				Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);
				GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
				// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				checkGlError("glBindTexture");
				GLES20.glUniform1i(mTextureLoc, 0);// textures[i]);

				GLES20.glUniform1i(texturH, 1);

				paintBill();
				mi++;
			}

			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		} else {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);

			if ((picked >= 0) && (picked < mol.size())&& (picked < bitmap.length)) {
				// ///////
				float ratio = 0;
				if (texture[0] != 0)
					GLES20.glDeleteTextures(1, texture, 0);
				GLES20.glGenTextures(1, texture, 0);
				checkGlError("glGenTextures");
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				// Bitmap bitmap = renderLabel(mol.get(i).lab);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap[picked], 0);
				ratio = (float) bitmap[picked].getWidth()/ bitmap[picked].getHeight();
				// bitmap.recycle();
				// ///////
				System.arraycopy(tm, 0, modelview, 0, 16);
				Matrix.translateM(modelview, 0, mol.get(picked).pos.x,
						mol.get(picked).pos.y, mol.get(picked).pos.z);
				// Matrix.scaleM(modelview, 0, 0.25f, 0.25f, 0.25f);

				BillMatrix(modelview);
				Matrix.scaleM(modelview, 0, ratio * 3 * labelScale,
						3.0f * labelScale, 3.0f * labelScale);

				Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);
				GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
				// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				checkGlError("glBindTexture");
				GLES20.glUniform1i(mTextureLoc, 0);// textures[i]);

				GLES20.glUniform1i(texturH, 1);

				paintBill();

			}
			if ((qpicked > -1) && (qpicked < qmol.size())) {
				float ratio = 0;
				if (texture[0] != 0)
					GLES20.glDeleteTextures(1, texture, 0);
				GLES20.glGenTextures(1, texture, 0);
				checkGlError("glGenTextures");
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				// Bitmap bitmap = renderLabel(mol.get(i).lab);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap[qpicked
						+ mol.size()], 0);
				ratio = (float) bitmap[qpicked + mol.size()].getWidth()
						/ bitmap[qpicked + mol.size()].getHeight();
				// bitmap.recycle();
				// ///////
				System.arraycopy(tm, 0, modelview, 0, 16);
				Matrix.translateM(modelview, 0, qmol.get(qpicked).pos.x,
						qmol.get(qpicked).pos.y, qmol.get(qpicked).pos.z);
				// Matrix.scaleM(modelview, 0, 0.2f, 0.2f, 0.2f);

				BillMatrix(modelview);

				Matrix.scaleM(modelview, 0, ratio * 3 * labelScale,
						3.0f * labelScale, 3.0f * labelScale);
				Matrix.multiplyMM(pmv1, 0, proj, 0, modelview, 0);
				GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, pmv1, 0);
				// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
				checkGlError("glBindTexture");
				GLES20.glUniform1i(mTextureLoc, 0);// textures[i]);

				GLES20.glUniform1i(texturH, 1);

				paintBill();

			}

			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		}

		{
			if (texture[0] != 0)
				GLES20.glDeleteTextures(1, texture, 0);
			GLES20.glGenTextures(1, texture, 0);
			checkGlError("glGenTextures");
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			if (countFrames) {
				fpsStr = (!msg.isEmpty()) ? msg
						: (((picked > -1) && (picked < mol.size())) ? String
								.format(Locale.US, ":%-8s FPS: %6.2f",
										mol.get(picked).lab, fps)// fps
								: ((qpicked > -1) && (qpicked < qmol.size())) ? String
										.format(Locale.US, ":%-8s FPS: %6.2f",
												qmol.get(qpicked).lab, fps)// fps
										: String.format(Locale.US,
												":         FPS: %6.2f", fps));
			} else {
				fpsStr = (!msg.isEmpty()) ? msg
						: (((picked > -1) && (picked < mol.size())) ? String
								.format(Locale.US, ":%-8s      :",
										mol.get(picked).lab)
								: ((qpicked > -1) && (qpicked < qmol.size())) ? String
										.format(Locale.US, ":%-8s      :",
												qmol.get(qpicked).lab) : String
										.format(Locale.US, ""));
			}
			if (oldFpsStr != fpsStr)
				fpsbitmap = renderLabel(fpsStr);
            if (fpsbitmap!=null){  
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, fpsbitmap, 0);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);

			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, fpsMat, 0);
			// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			checkGlError("glBindTexture");
			GLES20.glUniform1i(mTextureLoc, 0);

			GLES20.glUniform1i(texturH, 1);

			paintBill();
            }
			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		}//FPS
		{
			if (texture[0] != 0)
				GLES20.glDeleteTextures(1, texture, 0);
			GLES20.glGenTextures(1, texture, 0);
			checkGlError("glGenTextures");
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			Vector3D dang3=null,dang2=null,dang1=null;
			dang3=(ppppicked>9999)?
					mol.get(ppppicked - 10000).pos:
						(ppppicked < -9999)? qmol.get(-ppppicked - 10000).pos:null;

			dang2=(pppicked>9999)?
					mol.get(pppicked - 10000).pos:
						(pppicked < -9999)? qmol.get(-pppicked - 10000).pos:null;

			dang1=(ppicked>9999)?
					mol.get(ppicked - 10000).pos:
						(ppicked < -9999)? qmol.get(-ppicked - 10000).pos:null;
			double distance=((dang1!=null)&&(dang2!=null))?
					Vector3D.distance(dang2,dang1):-1.0;
			double angle=((distance>=0.0)&&(dang3!=null))?
					Vector3D.winkel(Vector3D.dif(dang3,dang2),Vector3D.dif(dang1,dang2)):-1.0;
			if (distance>=0.0){
			DANGStr=String.format(Locale.US,":%4s=%-4s %7.3f Å",((pppicked > 0) ? mol.get(pppicked - 10000).lab
					: (pppicked < -9999)? qmol.get(-pppicked - 10000).lab:"####")
					, 
					((ppicked > 0) ? mol.get(ppicked - 10000).lab
					: (ppicked < -9999)? qmol.get(-ppicked - 10000).lab:"####"), distance);}
					else DANGStr="             ";
			
			if (oldDANGStr != DANGStr){
				//Log.d("===SHELXLE===", DANGStr+String.valueOf(((dang1!=null)&&(dang2!=null)))+" <-> "+String.valueOf(dang2!=null)+" <-> "+String.valueOf(dang1!=null));
				
				dangbitmap = renderLabel(DANGStr);
				oldDANGStr=DANGStr;
			}
            if (dangbitmap!=null){  
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, dangbitmap, 0);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);

			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, fpsMat3, 0);
			// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			checkGlError("glBindTexture");
			GLES20.glUniform1i(mTextureLoc, 0);

			GLES20.glUniform1i(texturH, 1);

			paintBill();
            }
			if (angle>=0.0){

				String angcha=String.format(Locale.US,":%4s=%s=%-4s ",
						((ppppicked > 0) ? mol.get(ppppicked - 10000).lab
								: (ppppicked < -9999)? qmol.get(-ppppicked - 10000).lab:"####"),
						((pppicked > 0) ? mol.get(pppicked - 10000).lab
								: (pppicked < -9999)? qmol.get(-pppicked - 10000).lab:"####"),							 
								((ppicked > 0) ? mol.get(ppicked - 10000).lab
								: (ppicked < -9999)? qmol.get(-ppicked - 10000).lab:"####")); 
					
				DANGStr2=String.format(Locale.US,"%-15s%7.1f°",angcha, angle);
				}
					else DANGStr2="             ";
            if ((oldDANGStr2 != DANGStr2)){
				//Log.d("===SHELXLE===", DANGStr+String.valueOf(((dang1!=null)&&(dang2!=null)))+" <-> "+String.valueOf(dang2!=null)+" <-> "+String.valueOf(dang1!=null));
				
				dangbitmap2 = renderLabel(DANGStr2);
				oldDANGStr2= DANGStr2;
			}
            if (dangbitmap2!=null){  
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, dangbitmap2, 0);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);

			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, fpsMat4, 0);
			// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			checkGlError("glBindTexture");
			GLES20.glUniform1i(mTextureLoc, 0);

			GLES20.glUniform1i(texturH, 1);

			paintBill();

            }
			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		}
		if ((!qmol.isEmpty())) {
			if (texture[0] != 0)
				GLES20.glDeleteTextures(1, texture, 0);
			GLES20.glGenTextures(1, texture, 0);
			checkGlError("glGenTextures");
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			if (lastCut != qLowerCut) {
				Log.d("Q-PeakLegend ", "last" + lastCut + " lc" + qLowerCut);
				qpeakMap = QPeakLegend();
				lastCut = qLowerCut;

			}
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, qpeakMap, 0);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glUniformMatrix4fv(matrixUniform1, 1, false, fpsMat2, 0);
			// GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			checkGlError("glBindTexture");
			GLES20.glUniform1i(mTextureLoc, 0);

			GLES20.glUniform1i(texturH, 1);

			paintBill();

			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		}
		dx *= 0.985f;
		//
		dy *= 0.985f;
		// */

	}

	public float dx = 0, dy = 0;
	private int[] texture = { 0 };
	private Bitmap bitmap[] = null;
	private Bitmap qpeakMap;
	public float fps = 0;

	// Renders |text| into a bitmap and uploads that to OpenGL.
	// private void uploadTextTextures(ArrayList<Molecule> mol) commented out
	// because
	// call to OpenGL ES API with no current context (logged once per thread)
	// Renders |text| into a bitmap and returns the bitmap.
	public Bitmap renderLabel(String text) {
		// Measure text.
		if (text.isEmpty())
			return null;
		Paint textPaint = new Paint();
		textPaint.setTextSize(22);
		textPaint.setTypeface(Typeface.MONOSPACE);

		textPaint.setAntiAlias(true);
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		int w = Math.max(bounds.width() + 10, 10);
		int h = Math.max(bounds.height() + 10, 10);

		// Allocate bitmap.
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);
		/*
		 * // Framed box. RectF rect = new RectF(0, 0, w, h);
		 * 
		 * Paint fillPaint = new Paint(); fillPaint.setColor(0x7f000055);
		 * fillPaint.setAntiAlias(true); canvas.drawRoundRect(rect, 10, 10,
		 * fillPaint); RectF rect2 = new RectF(10, 10, w/2, h/2); Paint
		 * fillPaint2 = new Paint(); fillPaint2.setColor(0xffff0000);
		 * canvas.drawRoundRect(rect2, 10, 10, fillPaint2); // Text.
		 */
		textPaint.setARGB(0xff, 0x00, 0x00, 0x00);

		// drawText puts the baseline on y, but we want to visually center
		// vertically.
		Paint.FontMetrics metrics = textPaint.getFontMetrics();
		canvas.drawText(text, (w - bounds.width()) / 2 - 1,
				h - (h - bounds.height()) / 2 - metrics.bottom / 2 - 1,
				textPaint);

		canvas.drawText(text, (w - bounds.width()) / 2 - 1,
				h - (h - bounds.height()) / 2 - metrics.bottom / 2 + 1,
				textPaint);
		canvas.drawText(text, (w - bounds.width()) / 2 + 1,
				h - (h - bounds.height()) / 2 - metrics.bottom / 2 - 1,
				textPaint);
		canvas.drawText(text, (w - bounds.width()) / 2 + 1,
				h - (h - bounds.height()) / 2 - metrics.bottom / 2 + 1,
				textPaint);
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		canvas.drawText(text, (w - bounds.width()) / 2,
				h - (h - bounds.height()) / 2 - metrics.bottom / 2, textPaint);
		return bitmap;
	}

	private Bitmap QPeakLegend() {
		// Measure text.
		Paint textPaint = new Paint();
		textPaint.setTextSize(28);
		textPaint.setTypeface(Typeface.MONOSPACE);

		textPaint.setAntiAlias(true);
		Rect bounds = new Rect();
		String text = "-10.77";
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		int w = bounds.width() + 25;
		int h = pixh;

		// w=h/40;
		// Allocate bitmap.

		Bitmap m = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(m);
		m.eraseColor(0xff323232);
		/*
		 * // Framed box. RectF rect = new RectF(0, 0, w, h);
		 * 
		 * Paint fillPaint = new Paint(); fillPaint.setColor(0x7f000055);
		 * fillPaint.setAntiAlias(true); canvas.drawRoundRect(rect, 10, 10,
		 * fillPaint); RectF rect2 = new RectF(10, 10, w/2, h/2); Paint
		 * fillPaint2 = new Paint(); fillPaint2.setColor(0xffff0000);
		 * canvas.drawRoundRect(rect2, 10, 10, fillPaint2); // Text.
		 */

		// drawText puts the baseline on y, but we want to visually center
		// vertically.
		qLowerCut = Math.min(qLowerCut, qmax);
		Paint.FontMetrics metrics = textPaint.getFontMetrics();
		int max = (int) ((float) h / (bounds.height() + 10));
		float maxm = max - 1, dh = h / max * 0.5f;
		textPaint.setARGB(0xff, 0x72, 0x72, 0x72);
		if (qLowerCut > qmin)
			canvas.drawRect(26, h, w, h - ((qLowerCut - qmin) / (qmax - qmin))
					* h, textPaint);

		for (int i = 0; i < max; i++) {

			textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
			float v = (i / maxm) * qmin + (1.0f - i / maxm) * qmax;
			text = String.format(Locale.US, "%5.2f", v);
			canvas.drawText(text, (w - bounds.width()) + 2, i * h / max + dh
					+ metrics.bottom + 2, textPaint);
			canvas.drawText(text, (w - bounds.width()) - 2, i * h / max + dh
					+ metrics.bottom + 2, textPaint);
			canvas.drawText(text, (w - bounds.width()) + 2, i * h / max + dh
					+ metrics.bottom - 2, textPaint);
			canvas.drawText(text, (w - bounds.width()) - 2, i * h / max + dh
					+ metrics.bottom - 2, textPaint);

			Vector3D col = farbverlauf(v, qmin, qmax);
			textPaint.setARGB(0xff, (int) (col.x * 255), (int) (col.y * 255),
					(int) (col.z * 255));
			canvas.drawRect(0, i * h / max, 25, (i + 1) * h / max, textPaint);
			textPaint.setARGB(0xff, (int) (col.x * 255), (int) (col.y * 255),
					(int) (col.z * 255));

			canvas.drawText(text, (w - bounds.width()), i * h / max + dh
					+ metrics.bottom, textPaint);
		}

		return m;

	}

	public float qLowerCut = -6666.0f;
	private float lastCut = -6666.7f;
	public float labelScale = 1.0f;

	private void BillMatrix(float[] pmv12) {
		float d = pmv12[0] * (pmv12[5] * pmv12[10] - pmv12[9] * pmv12[6])
				- pmv12[1] * (pmv12[4] * pmv12[10] - pmv12[8] * pmv12[6])
				+ pmv12[2] * (pmv12[4] * pmv12[9] - pmv12[8] * pmv12[5]);
		// float d1 = (float) Math.sqrt(pmv12[0] * pmv12[0] + pmv12[1] *
		// pmv12[1]
		// + pmv12[2] * pmv12[2]);
		// float d2 = (float) Math.sqrt(pmv12[4] * pmv12[4] + pmv12[5] *
		// pmv12[5]
		// + pmv12[6] * pmv12[6]);
		// float d3 = (float) Math.sqrt(pmv12[8] * pmv12[8] + pmv12[9] *
		// pmv12[9]
		// + pmv12[10] * pmv12[10]);
		//
		d = 1;// (float) Math.sqrt(d);
		pmv12[0] = d;
		pmv12[1] = 0;
		pmv12[2] = 0;
		// pmv12[ 3]=0;
		pmv12[4] = 0;
		pmv12[5] = d;
		pmv12[6] = 0;
		// pmv12[ 7]=0;
		pmv12[8] = 0;
		pmv12[9] = 0;
		pmv12[10] = d;
		// pmv12[11]=0;
		// pmv12[12]=;
		// pmv12[13]=;
		// pmv12[14]=;
		// pmv12[15]=;

	}

	int pixh = 0, pixw = 0;

	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Ignore the passed-in GL10 interface, and use the GLES20
		// class's static methods instead.
		GLES20.glViewport(0, 0, width, height);
		Log.d("ShelXle.Droid Screen Size", width + " " + height);
		pixh = height;
		pixw = width;
		fpsscal = 350.0f / Math.min(width, height);
		fpsMat[0] = -fpsscal;
		fpsMat[12] = 1.0f - fpsscal;
		fpsMat3[0] = -fpsscal*1.24f;
		fpsMat3[12] = 1.0f - fpsscal*1.24f;

		fpsMat4[0] = -fpsscal*1.26f;
		fpsMat4[12] = 1.0f - fpsscal*1.26f;
		fpsMat2[0] = -0.25f * fpsscal;
		Log.d("ShelXle.Droid FPS text scaling ", fpsscal + " ");
		float ratio = (float) width / height;
		float fh = (float) Math.tan(29.0 / 360.0 * 3.14159265358979) * 5.0f;
		float fw = fh * ratio;
		Matrix.frustumM(proj, 0, -fw, fw, -fh, fh, 5.0f, 800.0f);
		Matrix.setLookAtM(mv, 0, 0.0f, 200f, 0f, 0f, 0.0f, 0.0f, 0.0f, 00f, 1f);
		Matrix.multiplyMM(pmv1, 0, proj, 0, mv, 0);
		System.arraycopy(pmv1, 0, proj, 0, 16);

		Matrix.setIdentityM(mv, 0);
		Matrix.scaleM(mv, 0, 9.0f, 9.0f, 9.0f);

		/*
		 * if (f!=null){ f.write(proj); f.write(proj);
		 * 
		 * }
		 */
		// Matrix.setIdentityM(proj,0);
		// Matrix.perspectiveM(proj, 0, 29.0f,ratio,5.0f,800.0f);

	}

	// private FileInputStream f;
    private String versionName="";
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Ignore the passed-in GL10 interface, and use the GLES20

		// class's static methods instead.
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		mVertexShader = RawResourceReader.readTextFileFromRawResource(mContext,
				de.moliso.shelxle.R.raw.atoms_vetrex_shader);
		mFragmentShader = RawResourceReader.readTextFileFromRawResource(
				mContext, de.moliso.shelxle.R.raw.atoms_fragment_shader);

		mProgram = createProgram(mVertexShader, mFragmentShader);
		if (mProgram == 0) {
			return;
		}
		try{
			versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		}catch (NameNotFoundException e){
			//wurst
		}
		texturH = GLES20.glGetUniformLocation(mProgram, "text");
		ellipsoidH = GLES20.glGetUniformLocation(mProgram, "eli");
		lichtH = GLES20.glGetUniformLocation(mProgram, "lighting");

		vertexAttr1 = GLES20.glGetAttribLocation(mProgram, "vertex");

		normalAttr1 = GLES20.glGetAttribLocation(mProgram, "normal");
		matrixUniform1 = GLES20.glGetUniformLocation(mProgram, "matrix");
		invmatrixUniform1 = GLES20.glGetUniformLocation(mProgram, "invmatrix");
		colorUniform1 = GLES20.glGetUniformLocation(mProgram, "col");

		mTextureLoc = GLES20.glGetUniformLocation(mProgram, "textureSampler");
		Matrix.setIdentityM(mv, 0);
		Matrix.scaleM(mv, 0, 9.0f, 9.0f, 9.0f);

		// Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

	}

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
			checkGlError("glAttachShader");
			GLES20.glAttachShader(program, pixelShader);
			checkGlError("glAttachShader");
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

	private void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG,
					op + ": glError " + error + ": "
							+ GLU.gluErrorString(error));
			throw new RuntimeException(op + ": glError " + error + ": "
					+ GLU.gluErrorString(error));

		}
	}

	public final short[] idx4 = {// [1152]
	38, 86, 182,// 0
			86, 14, 182,// 1
			8, 86, 183,// 2
			86, 38, 183,// 3
			38, 87, 184,// 4
			87, 5, 184,// 5
			14, 87, 185,// 6
			87, 38, 185,// 7
			39, 88, 186,// 8
			88, 14, 186,// 9
			1, 88, 187,// 10
			88, 39, 187,// 11
			39, 89, 188,// 12
			89, 8, 188,// 13
			14, 89, 189,// 14
			89, 39, 189,// 15
			40, 90, 190,// 16
			90, 15, 190,// 17
			8, 90, 191,// 18
			90, 40, 191,// 19
			40, 91, 192,// 20
			91, 4, 192,// 21
			15, 91, 193,// 22
			91, 40, 193,// 23
			41, 92, 194,// 24
			92, 15, 194,// 25
			5, 92, 195,// 26
			92, 41, 195,// 27
			41, 93, 196,// 28
			93, 8, 196,// 29
			15, 93, 197,// 30
			93, 41, 197,// 31
			42, 94, 198,// 32
			94, 16, 198,// 33
			8, 94, 199,// 34
			94, 42, 199,// 35
			42, 95, 200,// 36
			95, 0, 200,// 37
			16, 95, 201,// 38
			95, 42, 201,// 39
			43, 96, 202,// 40
			96, 16, 202,// 41
			4, 96, 203,// 42
			96, 43, 203,// 43
			43, 97, 204,// 44
			97, 8, 204,// 45
			16, 97, 205,// 46
			97, 43, 205,// 47
			44, 98, 206,// 48
			98, 17, 206,// 49
			8, 98, 207,// 50
			98, 44, 207,// 51
			44, 99, 208,// 52
			99, 1, 208,// 53
			17, 99, 209,// 54
			99, 44, 209,// 55
			45, 100, 210,// 56
			100, 17, 210,// 57
			0, 100, 211,// 58
			100, 45, 211,// 59
			45, 101, 212,// 60
			101, 8, 212,// 61
			17, 101, 213,// 62
			101, 45, 213,// 63
			46, 102, 214,// 64
			102, 18, 214,// 65
			9, 102, 215,// 66
			102, 46, 215,// 67
			46, 103, 216,// 68
			103, 5, 216,// 69
			18, 103, 217,// 70
			103, 46, 217,// 71
			47, 104, 218,// 72
			104, 18, 218,// 73
			7, 104, 219,// 74
			104, 47, 219,// 75
			47, 105, 220,// 76
			105, 9, 220,// 77
			18, 105, 221,// 78
			105, 47, 221,// 79
			48, 106, 222,// 80
			106, 19, 222,// 81
			9, 106, 223,// 82
			106, 48, 223,// 83
			48, 107, 224,// 84
			107, 1, 224,// 85
			19, 107, 225,// 86
			107, 48, 225,// 87
			49, 108, 226,// 88
			108, 19, 226,// 89
			5, 108, 227,// 90
			108, 49, 227,// 91
			49, 109, 228,// 92
			109, 9, 228,// 93
			19, 109, 229,// 94
			109, 49, 229,// 95
			50, 110, 230,// 96
			110, 20, 230,// 97
			9, 110, 231,// 98
			110, 50, 231,// 99
			50, 111, 232,// 100
			111, 3, 232,// 101
			20, 111, 233,// 102
			111, 50, 233,// 103
			51, 112, 234,// 104
			112, 20, 234,// 105
			1, 112, 235,// 106
			112, 51, 235,// 107
			51, 113, 236,// 108
			113, 9, 236,// 109
			20, 113, 237,// 110
			113, 51, 237,// 111
			52, 114, 238,// 112
			114, 21, 238,// 113
			9, 114, 239,// 114
			114, 52, 239,// 115
			52, 115, 240,// 116
			115, 7, 240,// 117
			21, 115, 241,// 118
			115, 52, 241,// 119
			53, 116, 242,// 120
			116, 21, 242,// 121
			3, 116, 243,// 122
			116, 53, 243,// 123
			53, 117, 244,// 124
			117, 9, 244,// 125
			21, 117, 245,// 126
			117, 53, 245,// 127
			54, 118, 246,// 128
			118, 22, 246,// 129
			10, 118, 247,// 130
			118, 54, 247,// 131
			54, 119, 248,// 132
			119, 7, 248,// 133
			22, 119, 249,// 134
			119, 54, 249,// 135
			55, 120, 250,// 136
			120, 22, 250,// 137
			6, 120, 251,// 138
			120, 55, 251,// 139
			55, 121, 252,// 140
			121, 10, 252,// 141
			22, 121, 253,// 142
			121, 55, 253,// 143
			56, 122, 254,// 144
			122, 23, 254,// 145
			10, 122, 255,// 146
			122, 56, 255,// 147
			56, 123, 256,// 148
			123, 3, 256,// 149
			23, 123, 257,// 150
			123, 56, 257,// 151
			57, 124, 258,// 152
			124, 23, 258,// 153
			7, 124, 259,// 154
			124, 57, 259,// 155
			57, 125, 260,// 156
			125, 10, 260,// 157
			23, 125, 261,// 158
			125, 57, 261,// 159
			58, 126, 262,// 160
			126, 24, 262,// 161
			10, 126, 263,// 162
			126, 58, 263,// 163
			58, 127, 264,// 164
			127, 2, 264,// 165
			24, 127, 265,// 166
			127, 58, 265,// 167
			59, 128, 266,// 168
			128, 24, 266,// 169
			3, 128, 267,// 170
			128, 59, 267,// 171
			59, 129, 268,// 172
			129, 10, 268,// 173
			24, 129, 269,// 174
			129, 59, 269,// 175
			60, 130, 270,// 176
			130, 25, 270,// 177
			10, 130, 271,// 178
			130, 60, 271,// 179
			60, 131, 272,// 180
			131, 6, 272,// 181
			25, 131, 273,// 182
			131, 60, 273,// 183
			61, 132, 274,// 184
			132, 25, 274,// 185
			2, 132, 275,// 186
			132, 61, 275,// 187
			61, 133, 276,// 188
			133, 10, 276,// 189
			25, 133, 277,// 190
			133, 61, 277,// 191
			62, 134, 278,// 192
			134, 26, 278,// 193
			11, 134, 279,// 194
			134, 62, 279,// 195
			62, 135, 280,// 196
			135, 3, 280,// 197
			26, 135, 281,// 198
			135, 62, 281,// 199
			63, 136, 282,// 200
			136, 26, 282,// 201
			2, 136, 283,// 202
			136, 63, 283,// 203
			63, 137, 284,// 204
			137, 11, 284,// 205
			26, 137, 285,// 206
			137, 63, 285,// 207
			64, 138, 286,// 208
			138, 27, 286,// 209
			11, 138, 287,// 210
			138, 64, 287,// 211
			64, 139, 288,// 212
			139, 1, 288,// 213
			27, 139, 289,// 214
			139, 64, 289,// 215
			65, 140, 290,// 216
			140, 27, 290,// 217
			3, 140, 291,// 218
			140, 65, 291,// 219
			65, 141, 292,// 220
			141, 11, 292,// 221
			27, 141, 293,// 222
			141, 65, 293,// 223
			66, 142, 294,// 224
			142, 28, 294,// 225
			11, 142, 295,// 226
			142, 66, 295,// 227
			66, 143, 296,// 228
			143, 0, 296,// 229
			28, 143, 297,// 230
			143, 66, 297,// 231
			67, 144, 298,// 232
			144, 28, 298,// 233
			1, 144, 299,// 234
			144, 67, 299,// 235
			67, 145, 300,// 236
			145, 11, 300,// 237
			28, 145, 301,// 238
			145, 67, 301,// 239
			68, 146, 302,// 240
			146, 29, 302,// 241
			11, 146, 303,// 242
			146, 68, 303,// 243
			68, 147, 304,// 244
			147, 2, 304,// 245
			29, 147, 305,// 246
			147, 68, 305,// 247
			69, 148, 306,// 248
			148, 29, 306,// 249
			0, 148, 307,// 250
			148, 69, 307,// 251
			69, 149, 308,// 252
			149, 11, 308,// 253
			29, 149, 309,// 254
			149, 69, 309,// 255
			70, 150, 310,// 256
			150, 30, 310,// 257
			12, 150, 311,// 258
			150, 70, 311,// 259
			70, 151, 312,// 260
			151, 4, 312,// 261
			30, 151, 313,// 262
			151, 70, 313,// 263
			71, 152, 314,// 264
			152, 30, 314,// 265
			0, 152, 315,// 266
			152, 71, 315,// 267
			71, 153, 316,// 268
			153, 12, 316,// 269
			30, 153, 317,// 270
			153, 71, 317,// 271
			72, 154, 318,// 272
			154, 31, 318,// 273
			12, 154, 319,// 274
			154, 72, 319,// 275
			72, 155, 320,// 276
			155, 6, 320,// 277
			31, 155, 321,// 278
			155, 72, 321,// 279
			73, 156, 322,// 280
			156, 31, 322,// 281
			4, 156, 323,// 282
			156, 73, 323,// 283
			73, 157, 324,// 284
			157, 12, 324,// 285
			31, 157, 325,// 286
			157, 73, 325,// 287
			74, 158, 326,// 288
			158, 32, 326,// 289
			12, 158, 327,// 290
			158, 74, 327,// 291
			74, 159, 328,// 292
			159, 2, 328,// 293
			32, 159, 329,// 294
			159, 74, 329,// 295
			75, 160, 330,// 296
			160, 32, 330,// 297
			6, 160, 331,// 298
			160, 75, 331,// 299
			75, 161, 332,// 300
			161, 12, 332,// 301
			32, 161, 333,// 302
			161, 75, 333,// 303
			76, 162, 334,// 304
			162, 33, 334,// 305
			12, 162, 335,// 306
			162, 76, 335,// 307
			76, 163, 336,// 308
			163, 0, 336,// 309
			33, 163, 337,// 310
			163, 76, 337,// 311
			77, 164, 338,// 312
			164, 33, 338,// 313
			2, 164, 339,// 314
			164, 77, 339,// 315
			77, 165, 340,// 316
			165, 12, 340,// 317
			33, 165, 341,// 318
			165, 77, 341,// 319
			78, 166, 342,// 320
			166, 34, 342,// 321
			13, 166, 343,// 322
			166, 78, 343,// 323
			78, 167, 344,// 324
			167, 7, 344,// 325
			34, 167, 345,// 326
			167, 78, 345,// 327
			79, 168, 346,// 328
			168, 34, 346,// 329
			5, 168, 347,// 330
			168, 79, 347,// 331
			79, 169, 348,// 332
			169, 13, 348,// 333
			34, 169, 349,// 334
			169, 79, 349,// 335
			80, 170, 350,// 336
			170, 35, 350,// 337
			13, 170, 351,// 338
			170, 80, 351,// 339
			80, 171, 352,// 340
			171, 6, 352,// 341
			35, 171, 353,// 342
			171, 80, 353,// 343
			81, 172, 354,// 344
			172, 35, 354,// 345
			7, 172, 355,// 346
			172, 81, 355,// 347
			81, 173, 356,// 348
			173, 13, 356,// 349
			35, 173, 357,// 350
			173, 81, 357,// 351
			82, 174, 358,// 352
			174, 36, 358,// 353
			13, 174, 359,// 354
			174, 82, 359,// 355
			82, 175, 360,// 356
			175, 4, 360,// 357
			36, 175, 361,// 358
			175, 82, 361,// 359
			83, 176, 362,// 360
			176, 36, 362,// 361
			6, 176, 363,// 362
			176, 83, 363,// 363
			83, 177, 364,// 364
			177, 13, 364,// 365
			36, 177, 365,// 366
			177, 83, 365,// 367
			84, 178, 366,// 368
			178, 37, 366,// 369
			13, 178, 367,// 370
			178, 84, 367,// 371
			84, 179, 368,// 372
			179, 5, 368,// 373
			37, 179, 369,// 374
			179, 84, 369,// 375
			85, 180, 370,// 376
			180, 37, 370,// 377
			4, 180, 371,// 378
			180, 85, 371,// 379
			85, 181, 372,// 380
			181, 13, 372,// 381
			37, 181, 373,// 382
			181, 85, 373 // 383
	};

	public final short[] idx2 = { 8, 14, 38,// 0
			14, 5, 38,// 1
			1, 14, 39,// 2
			14, 8, 39,// 3
			8, 15, 40,// 4
			15, 4, 40,// 5
			5, 15, 41,// 6
			15, 8, 41,// 7
			8, 16, 42,// 8
			16, 0, 42,// 9
			4, 16, 43,// 10
			16, 8, 43,// 11
			8, 17, 44,// 12
			17, 1, 44,// 13
			0, 17, 45,// 14
			17, 8, 45,// 15
			9, 18, 46,// 16
			18, 5, 46,// 17
			7, 18, 47,// 18
			18, 9, 47,// 19
			9, 19, 48,// 20
			19, 1, 48,// 21
			5, 19, 49,// 22
			19, 9, 49,// 23
			9, 20, 50,// 24
			20, 3, 50,// 25
			1, 20, 51,// 26
			20, 9, 51,// 27
			9, 21, 52,// 28
			21, 7, 52,// 29
			3, 21, 53,// 30
			21, 9, 53,// 31
			10, 22, 54,// 32
			22, 7, 54,// 33
			6, 22, 55,// 34
			22, 10, 55,// 35
			10, 23, 56,// 36
			23, 3, 56,// 37
			7, 23, 57,// 38
			23, 10, 57,// 39
			10, 24, 58,// 40
			24, 2, 58,// 41
			3, 24, 59,// 42
			24, 10, 59,// 43
			10, 25, 60,// 44
			25, 6, 60,// 45
			2, 25, 61,// 46
			25, 10, 61,// 47
			11, 26, 62,// 48
			26, 3, 62,// 49
			2, 26, 63,// 50
			26, 11, 63,// 51
			11, 27, 64,// 52
			27, 1, 64,// 53
			3, 27, 65,// 54
			27, 11, 65,// 55
			11, 28, 66,// 56
			28, 0, 66,// 57
			1, 28, 67,// 58
			28, 11, 67,// 59
			11, 29, 68,// 60
			29, 2, 68,// 61
			0, 29, 69,// 62
			29, 11, 69,// 63
			12, 30, 70,// 64
			30, 4, 70,// 65
			0, 30, 71,// 66
			30, 12, 71,// 67
			12, 31, 72,// 68
			31, 6, 72,// 69
			4, 31, 73,// 70
			31, 12, 73,// 71
			12, 32, 74,// 72
			32, 2, 74,// 73
			6, 32, 75,// 74
			32, 12, 75,// 75
			12, 33, 76,// 76
			33, 0, 76,// 77
			2, 33, 77,// 78
			33, 12, 77,// 79
			13, 34, 78,// 80
			34, 7, 78,// 81
			5, 34, 79,// 82
			34, 13, 79,// 83
			13, 35, 80,// 84
			35, 6, 80,// 85
			7, 35, 81,// 86
			35, 13, 81,// 87
			13, 36, 82,// 88
			36, 4, 82,// 89
			6, 36, 83,// 90
			36, 13, 83,// 91
			13, 37, 84,// 92
			37, 5, 84,// 93
			4, 37, 85,// 94
			37, 13, 85 // 95
	};
	// ========================
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int SHORT_SIZE_BYTES = 2;
	private final float[] mCubeVerticesData = { -0.577350f, 0.577350f,
			0.577350f,// 0
			0.577350f, 0.577350f, 0.577350f,// 1
			-0.577350f, -0.577350f, 0.577350f,// 2
			0.577350f, -0.577350f, 0.577350f,// 3
			-0.577350f, 0.577350f, -0.577350f,// 4
			0.577350f, 0.577350f, -0.577350f,// 5
			-0.577350f, -0.577350f, -0.577350f,// 6
			0.577350f, -0.577350f, -0.577350f,// 7
			0.000000f, 1.000000f, 0.000000f,// 8
			1.000000f, 0.000000f, 0.000000f,// 9
			0.000000f, -1.000000f, 0.000000f,// 10
			0.000000f, 0.000000f, 1.000000f,// 11
			-1.000000f, 0.000000f, 0.000000f,// 12
			0.000000f, 0.000000f, -1.000000f,// 13
			0.707107f, 0.707107f, 0.000000f,// 14
			0.000000f, 0.707107f, -0.707107f,// 15
			-0.707107f, 0.707107f, 0.000000f,// 16
			0.000000f, 0.707107f, 0.707107f,// 17
			0.707107f, 0.000000f, -0.707107f,// 18
			0.707107f, 0.707107f, 0.000000f,// 19
			0.707107f, 0.000000f, 0.707107f,// 20
			0.707107f, -0.707107f, 0.000000f,// 21
			0.000000f, -0.707107f, -0.707107f,// 22
			0.707107f, -0.707107f, 0.000000f,// 23
			0.000000f, -0.707107f, 0.707107f,// 24
			-0.707107f, -0.707107f, 0.000000f,// 25
			0.000000f, -0.707107f, 0.707107f,// 26
			0.707107f, 0.000000f, 0.707107f,// 27
			0.000000f, 0.707107f, 0.707107f,// 28
			-0.707107f, 0.000000f, 0.707107f,// 29
			-0.707107f, 0.707107f, 0.000000f,// 30
			-0.707107f, 0.000000f, -0.707107f,// 31
			-0.707107f, -0.707107f, 0.000000f,// 32
			-0.707107f, 0.000000f, 0.707107f,// 33
			0.707107f, 0.000000f, -0.707107f,// 34
			0.000000f, -0.707107f, -0.707107f,// 35
			-0.707107f, 0.000000f, -0.707107f,// 36
			0.000000f, 0.707107f, -0.707107f,// 37
			0.325058f, 0.888074f, -0.325058f,// 38
			0.325058f, 0.888074f, 0.325058f,// 39
			-0.325058f, 0.888074f, -0.325058f,// 40
			0.325058f, 0.888074f, -0.325058f,// 41
			-0.325058f, 0.888074f, 0.325058f,// 42
			-0.325058f, 0.888074f, -0.325058f,// 43
			0.325058f, 0.888074f, 0.325058f,// 44
			-0.325058f, 0.888074f, 0.325058f,// 45
			0.888074f, 0.325058f, -0.325058f,// 46
			0.888074f, -0.325058f, -0.325058f,// 47
			0.888074f, 0.325058f, 0.325058f,// 48
			0.888074f, 0.325058f, -0.325058f,// 49
			0.888074f, -0.325058f, 0.325058f,// 50
			0.888074f, 0.325058f, 0.325058f,// 51
			0.888074f, -0.325058f, -0.325058f,// 52
			0.888074f, -0.325058f, 0.325058f,// 53
			0.325058f, -0.888074f, -0.325058f,// 54
			-0.325058f, -0.888074f, -0.325058f,// 55
			0.325058f, -0.888074f, 0.325058f,// 56
			0.325058f, -0.888074f, -0.325058f,// 57
			-0.325058f, -0.888074f, 0.325058f,// 58
			0.325058f, -0.888074f, 0.325058f,// 59
			-0.325058f, -0.888074f, -0.325058f,// 60
			-0.325058f, -0.888074f, 0.325058f,// 61
			0.325058f, -0.325058f, 0.888074f,// 62
			-0.325058f, -0.325058f, 0.888074f,// 63
			0.325058f, 0.325058f, 0.888074f,// 64
			0.325058f, -0.325058f, 0.888074f,// 65
			-0.325058f, 0.325058f, 0.888074f,// 66
			0.325058f, 0.325058f, 0.888074f,// 67
			-0.325058f, -0.325058f, 0.888074f,// 68
			-0.325058f, 0.325058f, 0.888074f,// 69
			-0.888074f, 0.325058f, -0.325058f,// 70
			-0.888074f, 0.325058f, 0.325058f,// 71
			-0.888074f, -0.325058f, -0.325058f,// 72
			-0.888074f, 0.325058f, -0.325058f,// 73
			-0.888074f, -0.325058f, 0.325058f,// 74
			-0.888074f, -0.325058f, -0.325058f,// 75
			-0.888074f, 0.325058f, 0.325058f,// 76
			-0.888074f, -0.325058f, 0.325058f,// 77
			0.325058f, -0.325058f, -0.888074f,// 78
			0.325058f, 0.325058f, -0.888074f,// 79
			-0.325058f, -0.325058f, -0.888074f,// 80
			0.325058f, -0.325058f, -0.888074f,// 81
			-0.325058f, 0.325058f, -0.888074f,// 82
			-0.325058f, -0.325058f, -0.888074f,// 83
			0.325058f, 0.325058f, -0.888074f,// 84
			-0.325058f, 0.325058f, -0.888074f,// 85
			0.382683f, 0.923880f, 0.000000f,// 86
			0.673887f, 0.673887f, -0.302905f,// 87
			0.673887f, 0.673887f, 0.302905f,// 88
			0.382683f, 0.923880f, 0.000000f,// 89
			0.000000f, 0.923880f, -0.382683f,// 90
			-0.302905f, 0.673887f, -0.673887f,// 91
			0.302905f, 0.673887f, -0.673887f,// 92
			0.000000f, 0.923880f, -0.382683f,// 93
			-0.382683f, 0.923880f, 0.000000f,// 94
			-0.673887f, 0.673887f, 0.302905f,// 95
			-0.673887f, 0.673887f, -0.302905f,// 96
			-0.382683f, 0.923880f, 0.000000f,// 97
			0.000000f, 0.923880f, 0.382683f,// 98
			0.302905f, 0.673887f, 0.673887f,// 99
			-0.302905f, 0.673887f, 0.673887f,// 100
			0.000000f, 0.923880f, 0.382683f,// 101
			0.923880f, 0.000000f, -0.382683f,// 102
			0.673887f, 0.302905f, -0.673887f,// 103
			0.673887f, -0.302905f, -0.673887f,// 104
			0.923880f, 0.000000f, -0.382683f,// 105
			0.923880f, 0.382683f, 0.000000f,// 106
			0.673887f, 0.673887f, 0.302905f,// 107
			0.673887f, 0.673887f, -0.302905f,// 108
			0.923880f, 0.382683f, 0.000000f,// 109
			0.923880f, 0.000000f, 0.382683f,// 110
			0.673887f, -0.302905f, 0.673887f,// 111
			0.673887f, 0.302905f, 0.673887f,// 112
			0.923880f, 0.000000f, 0.382683f,// 113
			0.923880f, -0.382683f, 0.000000f,// 114
			0.673887f, -0.673887f, -0.302905f,// 115
			0.673887f, -0.673887f, 0.302905f,// 116
			0.923880f, -0.382683f, 0.000000f,// 117
			0.000000f, -0.923880f, -0.382683f,// 118
			0.302905f, -0.673887f, -0.673887f,// 119
			-0.302905f, -0.673887f, -0.673887f,// 120
			0.000000f, -0.923880f, -0.382683f,// 121
			0.382683f, -0.923880f, 0.000000f,// 122
			0.673887f, -0.673887f, 0.302905f,// 123
			0.673887f, -0.673887f, -0.302905f,// 124
			0.382683f, -0.923880f, 0.000000f,// 125
			0.000000f, -0.923880f, 0.382683f,// 126
			-0.302905f, -0.673887f, 0.673887f,// 127
			0.302905f, -0.673887f, 0.673887f,// 128
			0.000000f, -0.923880f, 0.382683f,// 129
			-0.382683f, -0.923880f, 0.000000f,// 130
			-0.673887f, -0.673887f, -0.302905f,// 131
			-0.673887f, -0.673887f, 0.302905f,// 132
			-0.382683f, -0.923880f, 0.000000f,// 133
			0.000000f, -0.382683f, 0.923880f,// 134
			0.302905f, -0.673887f, 0.673887f,// 135
			-0.302905f, -0.673887f, 0.673887f,// 136
			0.000000f, -0.382683f, 0.923880f,// 137
			0.382683f, 0.000000f, 0.923880f,// 138
			0.673887f, 0.302905f, 0.673887f,// 139
			0.673887f, -0.302905f, 0.673887f,// 140
			0.382683f, 0.000000f, 0.923880f,// 141
			0.000000f, 0.382683f, 0.923880f,// 142
			-0.302905f, 0.673887f, 0.673887f,// 143
			0.302905f, 0.673887f, 0.673887f,// 144
			0.000000f, 0.382683f, 0.923880f,// 145
			-0.382683f, 0.000000f, 0.923880f,// 146
			-0.673887f, -0.302905f, 0.673887f,// 147
			-0.673887f, 0.302905f, 0.673887f,// 148
			-0.382683f, 0.000000f, 0.923880f,// 149
			-0.923880f, 0.382683f, 0.000000f,// 150
			-0.673887f, 0.673887f, -0.302905f,// 151
			-0.673887f, 0.673887f, 0.302905f,// 152
			-0.923880f, 0.382683f, 0.000000f,// 153
			-0.923880f, 0.000000f, -0.382683f,// 154
			-0.673887f, -0.302905f, -0.673887f,// 155
			-0.673887f, 0.302905f, -0.673887f,// 156
			-0.923880f, 0.000000f, -0.382683f,// 157
			-0.923880f, -0.382683f, 0.000000f,// 158
			-0.673887f, -0.673887f, 0.302905f,// 159
			-0.673887f, -0.673887f, -0.302905f,// 160
			-0.923880f, -0.382683f, 0.000000f,// 161
			-0.923880f, 0.000000f, 0.382683f,// 162
			-0.673887f, 0.302905f, 0.673887f,// 163
			-0.673887f, -0.302905f, 0.673887f,// 164
			-0.923880f, 0.000000f, 0.382683f,// 165
			0.382683f, 0.000000f, -0.923880f,// 166
			0.673887f, -0.302905f, -0.673887f,// 167
			0.673887f, 0.302905f, -0.673887f,// 168
			0.382683f, 0.000000f, -0.923880f,// 169
			0.000000f, -0.382683f, -0.923880f,// 170
			-0.302905f, -0.673887f, -0.673887f,// 171
			0.302905f, -0.673887f, -0.673887f,// 172
			0.000000f, -0.382683f, -0.923880f,// 173
			-0.382683f, 0.000000f, -0.923880f,// 174
			-0.673887f, 0.302905f, -0.673887f,// 175
			-0.673887f, -0.302905f, -0.673887f,// 176
			-0.382683f, 0.000000f, -0.923880f,// 177
			0.000000f, 0.382683f, -0.923880f,// 178
			0.302905f, 0.673887f, -0.673887f,// 179
			-0.302905f, 0.673887f, -0.673887f,// 180
			0.000000f, 0.382683f, -0.923880f,// 181
			0.535467f, 0.827549f, -0.168634f,// 182
			0.167277f, 0.971616f, -0.167277f,// 183
			0.464385f, 0.754117f, -0.464385f,// 184
			0.535467f, 0.827549f, -0.168634f,// 185
			0.535467f, 0.827549f, 0.168634f,// 186
			0.464385f, 0.754117f, 0.464385f,// 187
			0.167277f, 0.971616f, 0.167277f,// 188
			0.535467f, 0.827549f, 0.168634f,// 189
			-0.168634f, 0.827549f, -0.535467f,// 190
			-0.167277f, 0.971616f, -0.167277f,// 191
			-0.464385f, 0.754117f, -0.464385f,// 192
			-0.168634f, 0.827549f, -0.535467f,// 193
			0.168634f, 0.827549f, -0.535467f,// 194
			0.464385f, 0.754117f, -0.464385f,// 195
			0.167277f, 0.971616f, -0.167277f,// 196
			0.168634f, 0.827549f, -0.535467f,// 197
			-0.535467f, 0.827549f, 0.168634f,// 198
			-0.167277f, 0.971616f, 0.167277f,// 199
			-0.464385f, 0.754117f, 0.464385f,// 200
			-0.535467f, 0.827549f, 0.168634f,// 201
			-0.535467f, 0.827549f, -0.168634f,// 202
			-0.464385f, 0.754117f, -0.464385f,// 203
			-0.167277f, 0.971616f, -0.167277f,// 204
			-0.535467f, 0.827549f, -0.168634f,// 205
			0.168634f, 0.827549f, 0.535467f,// 206
			0.167277f, 0.971616f, 0.167277f,// 207
			0.464385f, 0.754117f, 0.464385f,// 208
			0.168634f, 0.827549f, 0.535467f,// 209
			-0.168634f, 0.827549f, 0.535467f,// 210
			-0.464385f, 0.754117f, 0.464385f,// 211
			-0.167277f, 0.971616f, 0.167277f,// 212
			-0.168634f, 0.827549f, 0.535467f,// 213
			0.827549f, 0.168634f, -0.535467f,// 214
			0.971616f, 0.167277f, -0.167277f,// 215
			0.754117f, 0.464385f, -0.464385f,// 216
			0.827549f, 0.168634f, -0.535467f,// 217
			0.827549f, -0.168634f, -0.535467f,// 218
			0.754117f, -0.464385f, -0.464385f,// 219
			0.971616f, -0.167277f, -0.167277f,// 220
			0.827549f, -0.168634f, -0.535467f,// 221
			0.827549f, 0.535467f, 0.168634f,// 222
			0.971616f, 0.167277f, 0.167277f,// 223
			0.754117f, 0.464385f, 0.464385f,// 224
			0.827549f, 0.535467f, 0.168634f,// 225
			0.827549f, 0.535467f, -0.168634f,// 226
			0.754117f, 0.464385f, -0.464385f,// 227
			0.971616f, 0.167277f, -0.167277f,// 228
			0.827549f, 0.535467f, -0.168634f,// 229
			0.827549f, -0.168634f, 0.535467f,// 230
			0.971616f, -0.167277f, 0.167277f,// 231
			0.754117f, -0.464385f, 0.464385f,// 232
			0.827549f, -0.168634f, 0.535467f,// 233
			0.827549f, 0.168634f, 0.535467f,// 234
			0.754117f, 0.464385f, 0.464385f,// 235
			0.971616f, 0.167277f, 0.167277f,// 236
			0.827549f, 0.168634f, 0.535467f,// 237
			0.827549f, -0.535467f, -0.168634f,// 238
			0.971616f, -0.167277f, -0.167277f,// 239
			0.754117f, -0.464385f, -0.464385f,// 240
			0.827549f, -0.535467f, -0.168634f,// 241
			0.827549f, -0.535467f, 0.168634f,// 242
			0.754117f, -0.464385f, 0.464385f,// 243
			0.971616f, -0.167277f, 0.167277f,// 244
			0.827549f, -0.535467f, 0.168634f,// 245
			0.168634f, -0.827549f, -0.535467f,// 246
			0.167277f, -0.971616f, -0.167277f,// 247
			0.464385f, -0.754117f, -0.464385f,// 248
			0.168634f, -0.827549f, -0.535467f,// 249
			-0.168634f, -0.827549f, -0.535467f,// 250
			-0.464385f, -0.754117f, -0.464385f,// 251
			-0.167277f, -0.971616f, -0.167277f,// 252
			-0.168634f, -0.827549f, -0.535467f,// 253
			0.535467f, -0.827549f, 0.168634f,// 254
			0.167277f, -0.971616f, 0.167277f,// 255
			0.464385f, -0.754117f, 0.464385f,// 256
			0.535467f, -0.827549f, 0.168634f,// 257
			0.535467f, -0.827549f, -0.168634f,// 258
			0.464385f, -0.754117f, -0.464385f,// 259
			0.167277f, -0.971616f, -0.167277f,// 260
			0.535467f, -0.827549f, -0.168634f,// 261
			-0.168634f, -0.827549f, 0.535467f,// 262
			-0.167277f, -0.971616f, 0.167277f,// 263
			-0.464385f, -0.754117f, 0.464385f,// 264
			-0.168634f, -0.827549f, 0.535467f,// 265
			0.168634f, -0.827549f, 0.535467f,// 266
			0.464385f, -0.754117f, 0.464385f,// 267
			0.167277f, -0.971616f, 0.167277f,// 268
			0.168634f, -0.827549f, 0.535467f,// 269
			-0.535467f, -0.827549f, -0.168634f,// 270
			-0.167277f, -0.971616f, -0.167277f,// 271
			-0.464385f, -0.754117f, -0.464385f,// 272
			-0.535467f, -0.827549f, -0.168634f,// 273
			-0.535467f, -0.827549f, 0.168634f,// 274
			-0.464385f, -0.754117f, 0.464385f,// 275
			-0.167277f, -0.971616f, 0.167277f,// 276
			-0.535467f, -0.827549f, 0.168634f,// 277
			0.168634f, -0.535467f, 0.827549f,// 278
			0.167277f, -0.167277f, 0.971616f,// 279
			0.464385f, -0.464385f, 0.754117f,// 280
			0.168634f, -0.535467f, 0.827549f,// 281
			-0.168634f, -0.535467f, 0.827549f,// 282
			-0.464385f, -0.464385f, 0.754117f,// 283
			-0.167277f, -0.167277f, 0.971616f,// 284
			-0.168634f, -0.535467f, 0.827549f,// 285
			0.535467f, 0.168634f, 0.827549f,// 286
			0.167277f, 0.167277f, 0.971616f,// 287
			0.464385f, 0.464385f, 0.754117f,// 288
			0.535467f, 0.168634f, 0.827549f,// 289
			0.535467f, -0.168634f, 0.827549f,// 290
			0.464385f, -0.464385f, 0.754117f,// 291
			0.167277f, -0.167277f, 0.971616f,// 292
			0.535467f, -0.168634f, 0.827549f,// 293
			-0.168634f, 0.535467f, 0.827549f,// 294
			-0.167277f, 0.167277f, 0.971616f,// 295
			-0.464385f, 0.464385f, 0.754117f,// 296
			-0.168634f, 0.535467f, 0.827549f,// 297
			0.168634f, 0.535467f, 0.827549f,// 298
			0.464385f, 0.464385f, 0.754117f,// 299
			0.167277f, 0.167277f, 0.971616f,// 300
			0.168634f, 0.535467f, 0.827549f,// 301
			-0.535467f, -0.168634f, 0.827549f,// 302
			-0.167277f, -0.167277f, 0.971616f,// 303
			-0.464385f, -0.464385f, 0.754117f,// 304
			-0.535467f, -0.168634f, 0.827549f,// 305
			-0.535467f, 0.168634f, 0.827549f,// 306
			-0.464385f, 0.464385f, 0.754117f,// 307
			-0.167277f, 0.167277f, 0.971616f,// 308
			-0.535467f, 0.168634f, 0.827549f,// 309
			-0.827549f, 0.535467f, -0.168634f,// 310
			-0.971616f, 0.167277f, -0.167277f,// 311
			-0.754117f, 0.464385f, -0.464385f,// 312
			-0.827549f, 0.535467f, -0.168634f,// 313
			-0.827549f, 0.535467f, 0.168634f,// 314
			-0.754117f, 0.464385f, 0.464385f,// 315
			-0.971616f, 0.167277f, 0.167277f,// 316
			-0.827549f, 0.535467f, 0.168634f,// 317
			-0.827549f, -0.168634f, -0.535467f,// 318
			-0.971616f, -0.167277f, -0.167277f,// 319
			-0.754117f, -0.464385f, -0.464385f,// 320
			-0.827549f, -0.168634f, -0.535467f,// 321
			-0.827549f, 0.168634f, -0.535467f,// 322
			-0.754117f, 0.464385f, -0.464385f,// 323
			-0.971616f, 0.167277f, -0.167277f,// 324
			-0.827549f, 0.168634f, -0.535467f,// 325
			-0.827549f, -0.535467f, 0.168634f,// 326
			-0.971616f, -0.167277f, 0.167277f,// 327
			-0.754117f, -0.464385f, 0.464385f,// 328
			-0.827549f, -0.535467f, 0.168634f,// 329
			-0.827549f, -0.535467f, -0.168634f,// 330
			-0.754117f, -0.464385f, -0.464385f,// 331
			-0.971616f, -0.167277f, -0.167277f,// 332
			-0.827549f, -0.535467f, -0.168634f,// 333
			-0.827549f, 0.168634f, 0.535467f,// 334
			-0.971616f, 0.167277f, 0.167277f,// 335
			-0.754117f, 0.464385f, 0.464385f,// 336
			-0.827549f, 0.168634f, 0.535467f,// 337
			-0.827549f, -0.168634f, 0.535467f,// 338
			-0.754117f, -0.464385f, 0.464385f,// 339
			-0.971616f, -0.167277f, 0.167277f,// 340
			-0.827549f, -0.168634f, 0.535467f,// 341
			0.535467f, -0.168634f, -0.827549f,// 342
			0.167277f, -0.167277f, -0.971616f,// 343
			0.464385f, -0.464385f, -0.754117f,// 344
			0.535467f, -0.168634f, -0.827549f,// 345
			0.535467f, 0.168634f, -0.827549f,// 346
			0.464385f, 0.464385f, -0.754117f,// 347
			0.167277f, 0.167277f, -0.971616f,// 348
			0.535467f, 0.168634f, -0.827549f,// 349
			-0.168634f, -0.535467f, -0.827549f,// 350
			-0.167277f, -0.167277f, -0.971616f,// 351
			-0.464385f, -0.464385f, -0.754117f,// 352
			-0.168634f, -0.535467f, -0.827549f,// 353
			0.168634f, -0.535467f, -0.827549f,// 354
			0.464385f, -0.464385f, -0.754117f,// 355
			0.167277f, -0.167277f, -0.971616f,// 356
			0.168634f, -0.535467f, -0.827549f,// 357
			-0.535467f, 0.168634f, -0.827549f,// 358
			-0.167277f, 0.167277f, -0.971616f,// 359
			-0.464385f, 0.464385f, -0.754117f,// 360
			-0.535467f, 0.168634f, -0.827549f,// 361
			-0.535467f, -0.168634f, -0.827549f,// 362
			-0.464385f, -0.464385f, -0.754117f,// 363
			-0.167277f, -0.167277f, -0.971616f,// 364
			-0.535467f, -0.168634f, -0.827549f,// 365
			0.168634f, 0.535467f, -0.827549f,// 366
			0.167277f, 0.167277f, -0.971616f,// 367
			0.464385f, 0.464385f, -0.754117f,// 368
			0.168634f, 0.535467f, -0.827549f,// 369
			-0.168634f, 0.535467f, -0.827549f,// 370
			-0.464385f, 0.464385f, -0.754117f,// 371
			-0.167277f, 0.167277f, -0.971616f,// 372
			-0.168634f, 0.535467f, -0.827549f // 373
	};

	private FloatBuffer icon, icov;

	void makeIcosa(float R) {
		float P = R * (1.0f / 2.0f * (1 + (float) Math.sqrt(5)));
		float xx = 3.0f * (float) Math.sqrt(1 + P * P);
		float l = 1.0f / xx, p = P / xx;
		icon = ByteBuffer.allocateDirect(180 * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		icov = ByteBuffer.allocateDirect(180 * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		icon.position(0);
		icon.put(-p);
		icon.put(0);
		icon.put(2 * p + l); // 3
		icon.put(-p);
		icon.put(0);
		icon.put(2 * p + l); // 6
		icon.put(-p);
		icon.put(0);
		icon.put(2 * p + l); // 9
		icon.put(p);
		icon.put(0);
		icon.put(l + 2 * p); // 12
		icon.put(p);
		icon.put(0);
		icon.put(l + 2 * p); // 15
		icon.put(p);
		icon.put(0);
		icon.put(l + 2 * p); // 18
		icon.put(-l - 2 * p);
		icon.put(-p);
		icon.put(0); // 21
		icon.put(-l - 2 * p);
		icon.put(-p);
		icon.put(0); // 24
		icon.put(-l - 2 * p);
		icon.put(-p);
		icon.put(0); // 27
		icon.put(-p - l);
		icon.put(-l - p);
		icon.put(l + p); // 30
		icon.put(-p - l);
		icon.put(-l - p);
		icon.put(l + p); // 33
		icon.put(-p - l);
		icon.put(-l - p);
		icon.put(l + p); // 36
		icon.put(l + p);
		icon.put(-l - p);
		icon.put(l + p); // 39
		icon.put(l + p);
		icon.put(-l - p);
		icon.put(l + p); // 42
		icon.put(l + p);
		icon.put(-l - p);
		icon.put(l + p); // 45
		icon.put(0);
		icon.put(-l - 2 * p);
		icon.put(p); // 48
		icon.put(0);
		icon.put(-l - 2 * p);
		icon.put(p); // 51
		icon.put(0);
		icon.put(-l - 2 * p);
		icon.put(p); // 54
		icon.put(l + 2 * p);
		icon.put(-p);
		icon.put(0); // 57
		icon.put(l + 2 * p);
		icon.put(-p);
		icon.put(0); // 60
		icon.put(l + 2 * p);
		icon.put(-p);
		icon.put(0); // 63
		icon.put(2 * p + l);
		icon.put(p);
		icon.put(0); // 66
		icon.put(2 * p + l);
		icon.put(p);
		icon.put(0); // 69
		icon.put(2 * p + l);
		icon.put(p);
		icon.put(0); // 72
		icon.put(l + p);
		icon.put(l + p);
		icon.put(l + p); // 75
		icon.put(l + p);
		icon.put(l + p);
		icon.put(l + p); // 78
		icon.put(l + p);
		icon.put(l + p);
		icon.put(l + p); // 81
		icon.put(-l - p);
		icon.put(-p - l);
		icon.put(-l - p); // 84
		icon.put(-l - p);
		icon.put(-p - l);
		icon.put(-l - p); // 87
		icon.put(-l - p);
		icon.put(-p - l);
		icon.put(-l - p); // 90
		icon.put(0);
		icon.put(-2 * p - l);
		icon.put(-p); // 93
		icon.put(0);
		icon.put(-2 * p - l);
		icon.put(-p); // 96
		icon.put(0);
		icon.put(-2 * p - l);
		icon.put(-p); // 99
		icon.put(p + l);
		icon.put(-p - l);
		icon.put(-l - p); // 102
		icon.put(p + l);
		icon.put(-p - l);
		icon.put(-l - p); // 105
		icon.put(p + l);
		icon.put(-p - l);
		icon.put(-l - p); // 108
		icon.put(0);
		icon.put(2 * p + l);
		icon.put(p); // 111
		icon.put(0);
		icon.put(2 * p + l);
		icon.put(p); // 114
		icon.put(0);
		icon.put(2 * p + l);
		icon.put(p); // 117
		icon.put(-p - l);
		icon.put(l + p);
		icon.put(l + p); // 120
		icon.put(-p - l);
		icon.put(l + p);
		icon.put(l + p); // 123
		icon.put(-p - l);
		icon.put(l + p);
		icon.put(l + p); // 126
		icon.put(-2 * p - l);
		icon.put(p);
		icon.put(0); // 129
		icon.put(-2 * p - l);
		icon.put(p);
		icon.put(0); // 132
		icon.put(-2 * p - l);
		icon.put(p);
		icon.put(0); // 135
		icon.put(p);
		icon.put(0);
		icon.put(-2 * p - l); // 138
		icon.put(p);
		icon.put(0);
		icon.put(-2 * p - l); // 141
		icon.put(p);
		icon.put(0);
		icon.put(-2 * p - l); // 144
		icon.put(l + p);
		icon.put(l + p);
		icon.put(-p - l); // 147
		icon.put(l + p);
		icon.put(l + p);
		icon.put(-p - l); // 150
		icon.put(l + p);
		icon.put(l + p);
		icon.put(-p - l); // 153
		icon.put(-l - p);
		icon.put(l + p);
		icon.put(-l - p); // 156
		icon.put(-l - p);
		icon.put(l + p);
		icon.put(-l - p); // 159
		icon.put(-l - p);
		icon.put(l + p);
		icon.put(-l - p); // 162
		icon.put(0);
		icon.put(l + 2 * p);
		icon.put(-p); // 165
		icon.put(0);
		icon.put(l + 2 * p);
		icon.put(-p); // 168
		icon.put(0);
		icon.put(l + 2 * p);
		icon.put(-p); // 171
		icon.put(-p);
		icon.put(0);
		icon.put(-2 * p - l); // 174
		icon.put(-p);
		icon.put(0);
		icon.put(-2 * p - l); // 177
		icon.put(-p);
		icon.put(0);
		icon.put(-2 * p - l); // 180
		icon.position(0);
		icov.position(0);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(P);
		icov.put(0);
		icov.put(-R);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(-R);
		icov.put(P);
		icov.put(0);
		icov.put(R);
		icov.put(P);
		icov.put(0);
		icov.put(0);
		icov.put(R);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.put(-P);
		icov.put(-P);
		icov.put(0);
		icov.put(-R);
		icov.position(0);
	}

	private FloatBuffer mTriangleVertices;
	private ShortBuffer mIndices;
	public FloatBuffer mFFTfopMap = null;
	public FloatBuffer mFFTfomMap = null;
	public FloatBuffer mFFTdipMap = null;
	public FloatBuffer mFFTdimMap = null;

	private int mTextureLoc;
	private String mVertexShader;

	private String mFragmentShader;

	public float[] proj = new float[16];
	private float[] pmv1 = new float[16];

	private float[] tm = new float[16];
	private float[] invmat1 = new float[9];
	private float[] modelview = new float[16];
	private float[] mv = new float[16];

	private int mProgram;
	private int vertexAttr1;
	private int normalAttr1;

	// These still work without volatile, but refreshes are not guaranteed to
	// happen.
	public volatile float mDeltaX;
	public volatile float mDeltaY;
	public volatile float mScale;

	private int matrixUniform1;
	private int invmatrixUniform1;
	private int colorUniform1;
	private int ellipsoidH;
	private int lichtH;
	private int texturH;

	private Context mContext;
	private static String TAG = "GLES20TriangleRenderer";
	static FloatBuffer bb = ByteBuffer.allocateDirect(18 * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();

	private void initBill() {
		bb.position(0);
		bb.put(0);
		bb.put(0);
		bb.put(0);

		bb.put(-1);
		bb.put(0);
		bb.put(0);

		bb.put(0);
		bb.put(0);
		bb.put(1);

		bb.put(-1);
		bb.put(0);
		bb.put(0);

		bb.put(-1);
		bb.put(0);
		bb.put(1);

		bb.put(0);
		bb.put(0);
		bb.put(1);

		bb.position(0);
		bb.put(0);
		bb.put(0);
		bb.put(0);

		bb.put(-1);
		bb.put(0);
		bb.put(0);

		bb.put(0);
		bb.put(0);
		bb.put(1);

		bb.put(-1);
		bb.put(0);
		bb.put(0);

		bb.put(-1);
		bb.put(0);
		bb.put(1);

		bb.put(0);
		bb.put(0);
		bb.put(1);
	}

	private void paintBill() {
		bb.position(0);
		GLES20.glLineWidth(2.0f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				bb);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

		GLES20.glDisableVertexAttribArray(vertexAttr1);

	}

	FloatBuffer bbb = null;
	int bbbsiz = 0;
	FloatBuffer qbbb = null;
	int qbbbsiz = 0;

	void paintBonds() {
		if (bbb == null)
			return;
		GLES20.glUniform3f(colorUniform1, 1.0f, 0.5f, 0.0f);
		bbb.position(0);
		GLES20.glLineWidth(2.5f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				bbb);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, bbbsiz / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
		if (qbbb == null)
			return;
		GLES20.glUniform3f(colorUniform1, 0.7f, 0.3f, 0.0f);
		qbbb.position(0);
		GLES20.glLineWidth(0.7f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				qbbb);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, qbbbsiz / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
	}

	void paintFOPMap() {
		if (mFFTfopMap == null)
			return;
		GLES20.glLineWidth(1.2f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				mFFTfopMap);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, mFFTfopMap.capacity() / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
	}

	void paintFOMMap() {
		if (mFFTfomMap == null)
			return;
		GLES20.glLineWidth(1.2f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				mFFTfomMap);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, mFFTfomMap.capacity() / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
	}

	void paintDIPMap() {
		if (mFFTdipMap == null)
			return;
		GLES20.glLineWidth(1.2f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				mFFTdipMap);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, mFFTdipMap.capacity() / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
	}

	void paintDIMMap() {
		if (mFFTdimMap == null)
			return;
		GLES20.glLineWidth(1.2f);

		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				mFFTdimMap);
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, mFFTdimMap.capacity() / 3);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
	}

	void paintIcosa() {
		GLES20.glEnableVertexAttribArray(vertexAttr1);
		GLES20.glEnableVertexAttribArray(normalAttr1);
		icon.position(0);
		icov.position(0);
		GLES20.glVertexAttribPointer(vertexAttr1, 3, GLES20.GL_FLOAT, false, 0,
				icov);
		GLES20.glVertexAttribPointer(normalAttr1, 3, GLES20.GL_FLOAT, false, 0,
				icon);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 60);

		GLES20.glDisableVertexAttribArray(vertexAttr1);
		GLES20.glDisableVertexAttribArray(normalAttr1);

	}

	private final String[] keywords = { "ACTA",// 0
			"AFIX",// 1
			"MPLA",// 2
			"ANIS",// 3
			"BASF",// 4
			"BIND",// 5
			"BLOC",// 6
			"BOND",// 7 AGS4 to MPLA
			"BUMP",// 8
			"CELL",// 9
			"CGLS",// 10
			"CHIV",// 11
			"CONF",// 12
			"CONN",// 13
			"DAMP",// 14
			"DANG",// 15
			"DEFS",// 16
			"DELU",// 17
			"DFIX",// 18
			"DISP",// 19
			"EADP",// 20
			"EGEN",// 21
			"END",// 22
			"EQIV",// 23
			"ESEL",// 24
			"EXTI",// 25
			"EXYZ",// 26
			"FEND",// 27
			"FLAT",// 28
			"FMAP",// 29
			"FRAG",// 30
			"FREE",// 31
			"FVAR",// 32
			"GRID",// 33
			"HFIX",// 34
			"HKLF",// 35
			"HOPE",// 36
			"HTAB",// 37
			"INIT",// 38
			"ISOR",// 39
			"LAST",// 40
			"LATT",// 41
			"LAUE",// 42
			"LIST",// 43
			"L.S.",// 44
			"MERG",// 45
			"MOLE",// 46
			"MORE",// 47
			"MOVE",// 48
			"NCSY",// 49
			"OMIT",// 50
			"PART",// 51
			"PATT",// 52
			"PHAN",// 53
			"PHAS",// 54
			"PLAN",// 55
			"PSEE",// 56
			"REM", // 57
			"RESI",// 58
			"RTAB",// 59
			"SADI",// 60
			"SAME",// 61
			"SFAC",// 62
			"SHEL",// 63
			"SIMU",// 64
			"SIZE",// 65
			"SPEC",// 66
			"SPIN",// 67
			"STIR",// 68
			"SUMP",// 69
			"SWAT",// 70
			"SYMM",// 71
			"TEMP",// 72
			"TEXP",// 73
			"TIME",// 74
			"TITL",// 75
			"TREF",// 76
			"TWIN",// 77
			"UNIT",// 78
			"VECT",// 79
			"WPDB",// 80
			"WGHT",// 81
			"ZERR",// 82
			"XNPD",// 83
			"REST",// 84
			"CHAN",// 85
			"RIGU",// 86
			"FLAP",// 87
			"RNUM",// 88
			"SOCC",// 89
			"PRIG",// 90
			"WIGL",// 91
			"RANG",// 92
			"TANG",// 93
			"ADDA",// 94
			"STAG",// 95
			"ATOM",// 96PDB dummy commands ...
			"HETA",// 97
			"SCAL",// 98
			"ABIN",// 99
			"ANSC",// 100
			"ANSR",// 101
			"NOTR",// 102
			"NEUT",// 103
			"TWST" };// 104<<

	private int isacommand(String command) {
		String c = command.toUpperCase(Locale.US);
		c.replaceFirst("_\\S*", "");
		if (c.startsWith("+"))
			return 666;// file inclusion
		for (int i = 0; i < keywords.length; i++) {
			if (c.compareTo(keywords[i]) == 0) {
				return i;
			}
		}
		return -1;
	}

	double getNumber(double v, ArrayList<Double> fv) {
		double av = Math.abs(v), res = 0.0, var = 1.0;
		int m = 0;
		while ((-10 * m + av) > 5) {
			m++;
		}
		if (m > 1)
			m = Math.min(m, fv.size());
		if (m > 1)
			var = (Double) fv.get(m - 1);
		if (m == 0)
			res = v;
		else if (v > 0)
			res = (av - (10 * m)) * var;
		else
			res = (av - (10 * m)) * (1.0 - var);
		int tmp = fvarCntr2.get(m);
		tmp++;
		fvarCntr2.put(m, tmp);
		return res;
	}

	double getNumber(double v, ArrayList<Double> fv, double uiso) {
		double av = Math.abs(v), res = 0.0, var = 1.0;
		if ((v < -0.5) && (v > -5.0))
			return res = av * uiso;
		int m = 0;
		while ((-10 * m + av) > 5) {
			m++;
		}
		if (m > 1)
			m = Math.min(m, fv.size());
		if (m > 1)
			var = (Double) fv.get(m - 1);
		if (m == 0)
			return v;
		else if (v >= 0)
			res = (av - (10 * m)) * var;
		else
			res = (av - (10 * m)) * (1.0 - var);
		int tmp = fvarCntr2.get(m);
		tmp++;
		fvarCntr2.put(m, tmp);
		return res;
	}

	Vector3D farbverlauf(float wrt, float min, float max) {
		if (min + 0.001 >= max)
			max += 0.002;
		int lauf = 0;
		final float[][] farbe = { { 1.0f, 0.0f, 0.0f }, { 1.0f, 1.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f },
				{ 0.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 1.0f } };
		float nwrt = (wrt - min) / (max - min);
		nwrt = (nwrt >= 1.0f) ? 0.99999f : nwrt;
		nwrt = (nwrt <= 0.0f) ? 0.00001f : nwrt;
		lauf = ((int) (nwrt / 0.2f));
		nwrt -= (0.2f * lauf);
		nwrt /= (0.2f);
		Vector3D ff = new Vector3D((1.0f - nwrt) * farbe[lauf][0]
				+ farbe[lauf + 1][0] * nwrt, (1.0f - nwrt) * farbe[lauf][1]
				+ farbe[lauf + 1][1] * nwrt, (1.0f - nwrt) * farbe[lauf][2]
				+ farbe[lauf + 1][2] * nwrt);
		return ff;
	}

	public String section(String it, String sep, int start) {
		String s = "";
		String tok[] = it.split(sep);
		if (tok.length > start) {
			tok[start] = tok[start].replaceAll("^[+]", "");
			return tok[start];
		}
		return s;
	}

	public void load_sheldrick(String fileName) {
		ppppicked=-1;
		pppicked=-1;
		SparseArray<Double> unit = new SparseArray<Double>();
		ppicked=-1;
		picked=-1;
		if (!mol.isEmpty())
			mol.clear();
		if (!asymm.isEmpty())
			asymm.clear();
		if (!qmol.isEmpty())
			qmol.clear();
		if (!bonds.isEmpty())
			bonds.clear();
		// alv.clear();

		if (mFFTfopMap != null) {
			mFFTfopMap.clear();
			mFFTfopMap = null;
		}
		if (mFFTfomMap != null) {
			mFFTfomMap.clear();
			mFFTfomMap = null;
		}
		if (mFFTdipMap != null) {
			mFFTdipMap.clear();
			mFFTdipMap = null;
		}
		if (mFFTdimMap != null) {
			mFFTdimMap.clear();
			mFFTdimMap = null;
		}
		qLowerCut = -6666.0f;
		lastCut = -6666.7f;
		File file = new File(fileName);
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			br.close();
		} catch (IOException e) {
			Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
			// You'll need to add proper error handling here
		}
		theFileName = fileName;
		String inhalt = text.toString();

		qmin = 10000.0f;
		qmax = -10000.0f;
		int part = 0, afix = 0;
		int resiNr = 0;
		String ResiClass = "";
		double uiso = 0.05;
		int lattice = 1;
		int isoat = 0, adpat = 0, qpeaks = 0;
		int afixparent = 0;
		sfac.clear();
		fvar.clear();
		fvarCntr2.clear();
		String sep = "\\s+|_";
		inhalt = inhalt.replaceAll("=\\s*[\n]+\\s{1,}", "=");
		inhalt = inhalt.replaceAll("REM[^\n]*\n", "\n");
		inhalt = inhalt.replaceAll("![^\n]*\n", "\n");

		/*
		 * while (inhalt.matches("[\\n^]\\+[^\\n]*\n")){ String incl =
		 * inhalt.split("[\\n^]\\+",1)[1]; incl=incl.split("\n",0)[0]; dirName.
		 * String pre=dirName.section('/',0,-2);
		 * //qDebug()<<pre+"/"+incl<<QFileInfo(pre+"/"+incl).exists()<<dirName;
		 * if (QFileInfo(pre+"/"+incl).exists()){ QFile include(pre+"/"+incl);
		 * QString inst; if (include.open(QIODevice::ReadOnly|QIODevice::Text))
		 * inst=include.readAll(); inhalt.replace("+"+incl,inst); } else
		 * inhalt.remove("+"+incl); }
		 */
		String lines[] = inhalt.split("[\\r\\n]");
		// Toast.makeText(mContext,lines[14], Toast.LENGTH_LONG).show();
		// return;

		boolean fragIgnore = false;
		for (int i = 0; i < lines.length; i++) {
			if (!lines[i].isEmpty()) {
				if (lines[i].startsWith(" "))
					continue;
				lines[i] = lines[i].replace("=", "");
				String cs = section(lines[i], sep, 0);
				// newAtom.orginalLine=lines[i].section("=",0,0);

				if (cs.isEmpty())
					continue;

				int cmd = isacommand(cs);
				// String resiSpez=section(section(lines[i],sep,0),"_",1);//!!!
				if (cmd == 30)
					fragIgnore = true;
				if (cmd == 27)
					fragIgnore = false;
				String tok[] = lines[i].split(sep);
				// newAtom = new Molecule(new Vector3D(0F,0F,0F),-1,"X");
				if (!fragIgnore)

					switch (cmd) {
					case 1:
						afix = Integer.valueOf(section(lines[i], sep, 1));

						break;
					case 9: {
						if (tok.length > 7) {
							cell = new Cell(Float.valueOf(tok[2]),
									Float.valueOf(tok[3]),
									Float.valueOf(tok[4]),
									Float.valueOf(tok[5]),
									Float.valueOf(tok[6]),
									Float.valueOf(tok[7]),
									Float.valueOf(tok[1]));
							cell.symmops.add(matrix(1, 0, 0, 0, 1, 0, 0, 0, 1));
							cell.trans.add(new Vector3D(0, 0, 0));
						}
					}
						break;
					case 32: {
						for (int ifv = 1; ifv < tok.length; ifv++) {
							fvar.add(Double.valueOf(tok[ifv]));
						}
					}
						break;
					// case 35: hklf=true;break;
					case 41:
							try{
								lattice = Integer.valueOf(section(lines[i], sep, 1), 10);
							}catch(NumberFormatException e){
								lattice = -1;
							}
						break;
					case 51:
						part = Integer.valueOf(section(lines[i], sep, 1), 10);
						// minp=(minp<part)?minp:part;
						// maxp=(maxp>part)?maxp:part;

						break;
					case 58:
						if (section(lines[i], sep, 1).matches("^[0-9]+")) {
							resiNr = Integer.valueOf(section(lines[i], sep, 1),
									10);
							ResiClass = section(lines[i], sep, 2);
						} else {
							resiNr = Integer.valueOf(section(lines[i], sep, 2),
									10);
							ResiClass = section(lines[i], sep, 1);
						}
						break;
					case 62: {
						if ((tok.length > 4) && (!tok[2].matches("[A-Za-z]+"))) {
							// if
							// ((tok.at(2).toDouble()==0)&&((tok.at(3).toDouble()==1)))
							// {chgl->neutrons=true;}
							sfac.add(Molecule.getOZ(tok[1]));

							// if (sfac.last()>-2)
							// pserbt[sfac.last()+1]->show();
							// if ((virg)&&(sfac.last()==5))
							// pserbt[sfac.last()+1]->setChecked(true);
							// sfacBox->setFixedSize((sfac.size()+1)*52,70);
						} else {
							for (int isf = 1; isf < tok.length; isf++) {
								sfac.add(Molecule.getOZ(tok[isf]));

								// pserbt[sfac.last()+1]->show();
								// if ((virg)&&(sfac.last()==5))
								// pserbt[sfac.last()+1]->setChecked(true);
								// sfacBox->setFixedSize((sfac.size()+1)*52,70);
							}
						}
					}
						break;
					case 69: {
						int fvix = 4;
						while (tok.length > fvix) {
							int m = Integer.valueOf(tok[fvix]);
							int tmp = (Integer) fvarCntr2.get(m);
							tmp++;
							fvarCntr2.put(m, tmp);
							fvix += 2;
						}
					}
						break;
					case 71:
						cell.decodeSymmCard(lines[i]);
						break;
					case 75:
						// title = QString ("'%1'@ %2")
						// .arg(lines[i].section(" ",1,30).simplified())
						// .arg(fileName.section('/', -1));//
						break;
					case 78:
						// unitAlt = lines[i] ;
						cell.applyLatticeCentro(lattice);
						break;
					case 81:
						// wghtAct->setEnabled(true);
						break;
					case 82:
						cell.Z = lines[i];
						break;
					case 103:
						// chgl->neutrons=true;
						break;
					default:
						break;
					case -1: {// an atom or an error!
						Molecule newAtom = new Molecule(
								new Vector3D(0F, 0F, 0F), -1, "X");
						newAtom.symmGroup = 0;
						newAtom.part = part;
						newAtom.resiNr = resiNr;
						newAtom.afix = afix;
						newAtom.ResiClass = ResiClass;
						if (newAtom.resiNr < 0) {
							newAtom.resiNr = 0;
							newAtom.ResiClass = "";
						}
						if (tok.length == 7) {
							if ((newAtom.part != 0) || (newAtom.resiNr != 0)) {
								StringBuilder stb = new StringBuilder();
								newAtom.lab = stb
										// ("%1_%3%4")
										.append(tok[0])
										.append('_')
										.append((newAtom.resiNr != 0) ? String
												.valueOf(newAtom.resiNr) : "")
										.append((newAtom.part != 0) ? Integer
												.toString(
														(newAtom.part < 0) ? 36 + newAtom.part
																: newAtom.part + 9,
														36)
												: "").toString();
							} else
								newAtom.lab = tok[0];
						
							int fac = -1;
							try {
								fac = Integer.valueOf(tok[1]) - 1;
							} catch (NumberFormatException e) {
								Log.e(TAG, lines[i]);
								return;
							}
							newAtom.an = (Integer) (((fac < 0) || (fac >= sfac
									.size())) ? -2 : sfac.get(fac));
							newAtom.frac = new Vector3D((float) (getNumber(
									Double.valueOf(tok[2]), fvar)),
									(float) (getNumber(Double.valueOf(tok[3]),
											fvar)), (float) (getNumber(
											Double.valueOf(tok[4]), fvar)));
							// +" "+String.valueOf(an)
							newAtom.sof_org=Double.valueOf(tok[5]);
						    newAtom.sof=getNumber(Double.valueOf(tok[5]),fvar);
							double uso = getNumber(Double.valueOf(tok[6]),
									fvar, uiso);

							newAtom.uf = matrix((float) uso, 0f, 0f, 0f,
									(float) uso, 0f, 0f, 0f, (float) uso);
							newAtom.afixParent = -1;
							if (newAtom.an > 0)
								afixparent = asymm.size();
							else
								newAtom.afixParent = afixparent;
							asymm.add(newAtom);
							if (newAtom.an < 1)
								newAtom.rad = 0.15f;
							else
								newAtom.rad = Molecule.Kovalenz_Radien[newAtom.an] * 0.004f;
							isoat++;
						}
						if (tok.length == 8) {
							newAtom.lab = new String(tok[0]);
							if (newAtom.lab.startsWith("Q")) {
								newAtom.an = -1;
								newAtom.resiNr = -1;
								newAtom.ResiClass = "Q-Peak";
								qpeaks++;
							} else {
								int fac = Integer.valueOf(tok[1]) - 1;
								newAtom.an = ((fac < 0) || (fac >= sfac.size())) ? -2
										: sfac.get(fac);

								isoat++;
							}
							newAtom.frac = new Vector3D(Float.valueOf(tok[2]),
									Float.valueOf(tok[3]),
									Float.valueOf(tok[4]));

							// newAtom.sof_org=tok.at(5).toDouble();
							// newAtom.sof=
							// getNumber(tok.at(5).toDouble(),fvar);
							newAtom.sof_org=Double.valueOf(tok[5]);
						    newAtom.sof=getNumber(Double.valueOf(tok[5]),fvar);
							double uso = getNumber(Double.valueOf(tok[6]),
									fvar, uiso);
							newAtom.uf = matrix((float) uso, 0f, 0f, 0f,
									(float) uso, 0f, 0f, 0f, (float) uso);

							newAtom.phgt = Float.valueOf(tok[7]);
							// if (!hklf) qbeforehkl=true;
							if (newAtom.an == -1) {
								qmin = Math.min(qmin, newAtom.phgt);
								qmax = Math.max(qmax, newAtom.phgt);
							}
							newAtom.afixParent = -1;
							if (newAtom.an == -1)
								qmol.add(newAtom);
							else
								asymm.add(newAtom);

						}
						if (tok.length == 12) {
							if ((newAtom.part != 0) || (newAtom.resiNr != 0)) {
								StringBuilder stb = new StringBuilder();
								newAtom.lab = stb
										// ("%1_%3%4")
										.append(tok[0])
										.append('_')
										.append((newAtom.resiNr != 0) ? String
												.valueOf(newAtom.resiNr) : "")
										.append((newAtom.part != 0) ? Integer
												.toString(
														(newAtom.part < 0) ? 36 + newAtom.part
																: newAtom.part + 9,
														36)
												: "").toString();
							} else
								newAtom.lab = new String(tok[0]);
							int fac = Integer.valueOf(tok[1]) - 1;

							newAtom.an = ((fac < 0) || (fac >= sfac.size())) ? -2
									: sfac.get(fac);
							newAtom.frac = new Vector3D((float) (getNumber(
									Double.valueOf(tok[2]), fvar)),
									(float) (getNumber(Double.valueOf(tok[3]),
											fvar)), (float) (getNumber(
											Double.valueOf(tok[4]), fvar)));
							// newAtom.sof_org=tok.at(5).toDouble();
							// newAtom.sof=
							// getNumber(tok.at(5).toDouble(),fvar);
							newAtom.sof_org=Double.valueOf(tok[5]);
						    newAtom.sof=getNumber(Double.valueOf(tok[5]),fvar);
							newAtom.uf = matrix(
									(float) getNumber(Double.valueOf(tok[6]),
											fvar),
									(float) getNumber(Double.valueOf(tok[11]),
											fvar),
									(float) getNumber(Double.valueOf(tok[10]),
											fvar),
									(float) getNumber(Double.valueOf(tok[11]),
											fvar),
									(float) getNumber(Double.valueOf(tok[7]),
											fvar),
									(float) getNumber(Double.valueOf(tok[9]),
											fvar),
									(float) getNumber(Double.valueOf(tok[10]),
											fvar),
									(float) getNumber(Double.valueOf(tok[9]),
											fvar),
									(float) getNumber(Double.valueOf(tok[8]),
											fvar));

							uiso = cell.ueq(newAtom.uf);
							newAtom.iso = (float) uiso;
							if (newAtom.an > 0)
								afixparent = asymm.size();
							newAtom.afixParent = -1;
							if (newAtom.an < 1)
								newAtom.rad = 0.15f;
							else
								newAtom.rad = Molecule.Kovalenz_Radien[newAtom.an] * 0.004f;
							asymm.add(newAtom);
							// System.arraycopy(newAtom.uf, 0,
							// asymm.get(asymm.size() - 1).uf, 0, 9);
							// System.err.format("%f %f\n",
							// newAtom.uf[0],asymm.get(asymm.size()-1).uf[0]);

							adpat++;
						}
					}

						break;// */
					}// */

			}

		}
		mid = new Vector3D(0f, 0f, 0f);

		for (int i = 0; i < asymm.size(); i++) {
			if (asymm.get(i).uf[5] + asymm.get(i).uf[2] + asymm.get(i).uf[1] == 0)
				asymm.get(i).uc = matrix(asymm.get(i).uf[0], 0, 0, 0,
						asymm.get(i).uf[0], 0, 0, 0, asymm.get(i).uf[0]);
			else
				cell.uFrac2UCart(asymm.get(i).uf, asymm.get(i).uc);
		}
		for (int i = 0; i < asymm.size(); i++) {
			if (asymm.get(i).an>=0){
				int key=asymm.get(i).an;
				//Log.e("===SHELXLE===", String.format(Locale.US, "key=%d an=%d i%d", key,asymm.get(i).an,i));
				//Log.e("===SHELXLE===", String.format(Locale.US, "%f %f",unit.get(key),(asymm.get(i).sof*cell.symmops.size())));
				
				double val=((unit.get(key)==null)?0.0:unit.get(key)) +(asymm.get(i).sof*cell.symmops.size());
				unit.put(key, val);}
		}
		globalVariables.unitNeu="UNIT";
		for (int i=0; i<sfac.size();i++){
		globalVariables.unitNeu+=String.format(Locale.US, " %1.0f", unit.get(sfac.get(i)));
		}
		//Log.e("===SHELXLE===",globalVariables.unitNeu);
		// ArrayList<String> bs=new ArrayList<String>();
		// bs.add("3_656:2");
		// ArrayList<Molecule> shtm=atm.packer(bs, asymm, cell);
		// asymm.clear();
		// asymm=(ArrayList<Molecule>)shtm.clone();
		Molecule atm = new Molecule(new Vector3D(0F, 0F, 0F), -1, "X");

		mol = atm.packer(Molecule.sdm(cell, asymm), asymm, cell);
		if (!mol.isEmpty()) {
			for (int i = 0; i < mol.size(); i++) {
				mol.get(i).ang = cell.jacobi(mol.get(i).ax, mol.get(i).uc,
						mol.get(i).ev);
				if (mol.get(i).an < 0)
					mol.get(i).col = new Vector3D(1f, 0f, 1f);
				else
					mol.get(i).col = new Vector3D(
							Molecule.atomColor[mol.get(i).an]);
				// mol.get(i).rad = 0.15f;
				mol.get(i).pos = cell.frac2cart(mol.get(i).frac);
				// alv.addAtom(mol.get(i).lab, mol.get(i).pos);
				mid.add(mol.get(i).pos);
			}
			mid.scale(-1.0f / mol.size());
			for (int i = 0; i < mol.size(); i++) {
				mol.get(i).pos = mol.get(i).pos.add(mid);
			}
		} else {
			for (int i = 0; i < qmol.size(); i++) {
				qmol.get(i).pos = cell.frac2cart(qmol.get(i).frac);
				// alv.addAtom(mol.get(i).lab, mol.get(i).pos);
				mid.add(qmol.get(i).pos);
			}
			mid.scale(-1.0f / qmol.size());
		}
		for (int i = 0; i < qmol.size(); i++) {

			qmol.get(i).col = farbverlauf(qmol.get(i).phgt, qmin, qmax);
			qmol.get(i).pos = cell.frac2cart(qmol.get(i).frac);
			qmol.get(i).pos = qmol.get(i).pos.add(mid);
		}

		for (int i = 0; i < mol.size(); i++) {
			for (int j = i + 1; j < mol.size(); j++) {
				if (((mol.get(i).part < 0) || (mol.get(j).part < 0))
						&& (mol.get(j).symmGroup != mol.get(i).symmGroup))
					continue;
				Vector3D dv = Vector3D.dif(mol.get(i).pos, mol.get(j).pos);
				float soll = 0;
				if ((mol.get(i).an < 0) || (mol.get(j).an < 0))
					soll = 1.0f;
				else
					soll = 1.2f * 0.01f * (Molecule.Kovalenz_Radien[mol.get(i).an] + Molecule.Kovalenz_Radien[mol
							.get(j).an]);
				if ((dv.length() < soll)
						&& ((!mol.get(i).lab.startsWith("H")) || (!mol.get(j).lab
								.startsWith("H"))))
					if ((mol.get(i).part == 0) || (mol.get(j).part == 0)
							|| (mol.get(i).part == mol.get(j).part))

					{
						bonds.add(new Bond(mol.get(i).pos, mol.get(j).pos));
					}
			}
		}
		qbonds.clear();
		for (int i = 0; i < qmol.size(); i++) {
			for (int j = i + 1; j < qmol.size(); j++) {
				Vector3D dv = Vector3D.dif(qmol.get(i).pos, qmol.get(j).pos);
				float soll = 1.6f;
				if (dv.length() < soll) {
					qbonds.add(new Bond(qmol.get(i).pos, qmol.get(j).pos));
				}
			}
		}

		qbbbsiz = qbonds.size() * 6;
		qbbb = ByteBuffer.allocateDirect(qbbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		qbbb.position(0);
		for (int i = 0; i < qbonds.size(); i++) {
			qbbb.put(qbonds.get(i).a.x);
			qbbb.put(qbonds.get(i).a.y);
			qbbb.put(qbonds.get(i).a.z);
			qbbb.put(qbonds.get(i).b.x);
			qbbb.put(qbonds.get(i).b.y);
			qbbb.put(qbonds.get(i).b.z);
		}
		bbbsiz = bonds.size() * 6;
		bbb = ByteBuffer.allocateDirect(bbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		bbb.position(0);
		for (int i = 0; i < bonds.size(); i++) {
			bbb.put(bonds.get(i).a.x);
			bbb.put(bonds.get(i).a.y);
			bbb.put(bonds.get(i).a.z);
			bbb.put(bonds.get(i).b.x);
			bbb.put(bonds.get(i).b.y);
			bbb.put(bonds.get(i).b.z);
		}
		if (bitmap != null)
			for (int i = 0; i < bitmap.length; i++)
				bitmap[i].recycle();
		bitmap = new Bitmap[mol.size() + qmol.size()];
		for (int i = 0; i < bitmap.length; i++) {
			if (i < mol.size())
				bitmap[i] = renderLabel(mol.get(i).lab);
			else
				bitmap[i] = renderLabel(qmol.get(i - mol.size()).lab);
		}
		if (versionName.isEmpty()){try{
			versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		}catch (NameNotFoundException e){
			//wurst
		}}
		if (!mol.isEmpty() || !qmol.isEmpty())
			Toast.makeText(
					mContext,
					"ShelXle.Droid ("+versionName+") "+
					"sucessfully loaded \n" + fileName + "\n\n iso-atoms: "
							+ String.valueOf(isoat) + "\n aniso-atoms: "
							+ String.valueOf(adpat) + "\n q-peaks: "
							+ String.valueOf(qpeaks), Toast.LENGTH_LONG).show();
	}// shelx

	public Vector3D mid;

	public void grow() {
		if (mFFTfopMap != null) {
			mFFTfopMap.clear();
			mFFTfopMap = null;
		}
		if (mFFTfomMap != null) {
			mFFTfomMap.clear();
			mFFTfomMap = null;
		}
		if (mFFTdipMap != null) {
			mFFTdipMap.clear();
			mFFTdipMap = null;
		}
		if (mFFTdimMap != null) {
			mFFTdimMap.clear();
			mFFTdimMap = null;
		}
		mid = new Vector3D(0f, 0f, 0f);
		Molecule atm = new Molecule(new Vector3D(0F, 0F, 0F), -1, "X");

		mol = atm.packer(Molecule.sdm(cell, mol), mol, cell);

		for (int i = 0; i < mol.size(); i++) {
			mol.get(i).ang = cell.jacobi(mol.get(i).ax, mol.get(i).uc,
					mol.get(i).ev);
			if (mol.get(i).an < 0)
				mol.get(i).col = new Vector3D(1f, 0f, 1f);
			else
				mol.get(i).col = new Vector3D(Molecule.atomColor[mol.get(i).an]);
			// mol.get(i).rad = 0.15f;
			mol.get(i).pos = cell.frac2cart(mol.get(i).frac);
			// alv.addAtom(mol.get(i).lab, mol.get(i).pos);
			mid.add(mol.get(i).pos);
		}
		if (!mol.isEmpty())
			mid.scale(-1.0f / mol.size());
		for (int i = 0; i < mol.size(); i++) {
			mol.get(i).pos = mol.get(i).pos.add(mid);
		}
		for (int i = 0; i < qmol.size(); i++) {

			qmol.get(i).col = farbverlauf(qmol.get(i).phgt, qmin, qmax);
			qmol.get(i).pos = cell.frac2cart(qmol.get(i).frac);
			qmol.get(i).pos = qmol.get(i).pos.add(mid);
		}
		bonds.clear();
		for (int i = 0; i < mol.size(); i++) {
			for (int j = i + 1; j < mol.size(); j++) {
				if (((mol.get(i).part < 0) || (mol.get(j).part < 0))
						&& (mol.get(j).symmGroup != mol.get(i).symmGroup))
					continue;
				Vector3D dv = Vector3D.dif(mol.get(i).pos, mol.get(j).pos);
				float soll = 0;
				if ((mol.get(i).an < 0) || (mol.get(j).an < 0))
					soll = 1.0f;
				else
					soll = 1.2f * 0.01f * (Molecule.Kovalenz_Radien[mol.get(i).an] + Molecule.Kovalenz_Radien[mol
							.get(j).an]);
				if ((dv.length() < soll)
						&& ((!mol.get(i).lab.startsWith("H")) || (!mol.get(j).lab
								.startsWith("H"))))
					if ((mol.get(i).part == 0) || (mol.get(j).part == 0)
							|| (mol.get(i).part == mol.get(j).part))

					{
						bonds.add(new Bond(mol.get(i).pos, mol.get(j).pos));
					}
			}
		}

		bbbsiz = bonds.size() * 6;
		bbb = ByteBuffer.allocateDirect(bbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		bbb.position(0);
		for (int i = 0; i < bonds.size(); i++) {
			bbb.put(bonds.get(i).a.x);
			bbb.put(bonds.get(i).a.y);
			bbb.put(bonds.get(i).a.z);
			bbb.put(bonds.get(i).b.x);
			bbb.put(bonds.get(i).b.y);
			bbb.put(bonds.get(i).b.z);
		}

		qbonds.clear();
		for (int i = 0; i < qmol.size(); i++) {
			for (int j = i + 1; j < qmol.size(); j++) {
				Vector3D dv = Vector3D.dif(qmol.get(i).pos, qmol.get(j).pos);
				float soll = 1.6f;
				if (dv.length() < soll) {
					qbonds.add(new Bond(qmol.get(i).pos, qmol.get(j).pos));
				}
			}
		}

		qbbbsiz = qbonds.size() * 6;
		qbbb = ByteBuffer.allocateDirect(qbbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		qbbb.position(0);
		for (int i = 0; i < qbonds.size(); i++) {
			qbbb.put(qbonds.get(i).a.x);
			qbbb.put(qbonds.get(i).a.y);
			qbbb.put(qbonds.get(i).a.z);
			qbbb.put(qbonds.get(i).b.x);
			qbbb.put(qbonds.get(i).b.y);
			qbbb.put(qbonds.get(i).b.z);
		}
		if (bitmap != null)
			for (int i = 0; i < bitmap.length; i++)
				bitmap[i].recycle();
		bitmap = new Bitmap[mol.size() + qmol.size()];
		for (int i = 0; i < bitmap.length; i++) {
			if (i < mol.size())
				bitmap[i] = renderLabel(mol.get(i).lab);
			else
				bitmap[i] = renderLabel(qmol.get(i - mol.size()).lab);
		}
	}

	public void qrebond() {
		qbonds.clear();

		for (int i = 0; i < qmol.size(); i++) {
			for (int j = i + 1; j < qmol.size(); j++) {
				if (qLowerCut > qmin)
					if ((qmol.get(i).phgt <= qLowerCut)
							|| (qmol.get(j).phgt <= qLowerCut))
						continue;
				Vector3D dv = Vector3D.dif(qmol.get(i).pos, qmol.get(j).pos);
				float soll = 1.6f;
				if (dv.length() < soll) {
					qbonds.add(new Bond(qmol.get(i).pos, qmol.get(j).pos));
				}
			}
		}

		qbbbsiz = qbonds.size() * 6;
		qbbb = ByteBuffer.allocateDirect(qbbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		qbbb.position(0);
		for (int i = 0; i < qbonds.size(); i++) {
			qbbb.put(qbonds.get(i).a.x);
			qbbb.put(qbonds.get(i).a.y);
			qbbb.put(qbonds.get(i).a.z);
			qbbb.put(qbonds.get(i).b.x);
			qbbb.put(qbonds.get(i).b.y);
			qbbb.put(qbonds.get(i).b.z);
		}
	}

	@SuppressWarnings("unchecked")
	public void fuse() {
		if (mFFTfopMap != null) {
			mFFTfopMap.clear();
			mFFTfopMap = null;
		}
		if (mFFTfomMap != null) {
			mFFTfomMap.clear();
			mFFTfomMap = null;
		}
		if (mFFTdipMap != null) {
			mFFTdipMap.clear();
			mFFTdipMap = null;
		}
		if (mFFTdimMap != null) {
			mFFTdimMap.clear();
			mFFTdimMap = null;
		}
		mid = new Vector3D(0f, 0f, 0f);
		// Molecule atm = new Molecule(
		// new Vector3D(0F, 0F, 0F), -1, "X");

		mol = (ArrayList<Molecule>) asymm.clone();

		for (int i = 0; i < mol.size(); i++) {
			mol.get(i).ang = cell.jacobi(mol.get(i).ax, mol.get(i).uc,
					mol.get(i).ev);
			if (mol.get(i).an < 0)
				mol.get(i).col = new Vector3D(1f, 0f, 1f);
			else
				mol.get(i).col = new Vector3D(Molecule.atomColor[mol.get(i).an]);
			// mol.get(i).rad = 0.15f;
			mol.get(i).pos = cell.frac2cart(mol.get(i).frac);
			// alv.addAtom(mol.get(i).lab, mol.get(i).pos);
			mid.add(mol.get(i).pos);
		}
		mid.scale(-1.0f / mol.size());
		for (int i = 0; i < mol.size(); i++) {
			mol.get(i).pos = mol.get(i).pos.add(mid);
		}

		for (int i = 0; i < qmol.size(); i++) {

			qmol.get(i).col = farbverlauf(qmol.get(i).phgt, qmin, qmax);
			qmol.get(i).pos = cell.frac2cart(qmol.get(i).frac);
			qmol.get(i).pos = qmol.get(i).pos.add(mid);
		}

		bonds.clear();
		for (int i = 0; i < mol.size(); i++) {
			for (int j = i + 1; j < mol.size(); j++) {
				if (((mol.get(i).part < 0) || (mol.get(j).part < 0))
						&& (mol.get(j).symmGroup != mol.get(i).symmGroup))
					continue;
				Vector3D dv = Vector3D.dif(mol.get(i).pos, mol.get(j).pos);
				float soll = 0;
				if ((mol.get(i).an < 0) || (mol.get(j).an < 0))
					soll = 1.0f;
				else
					soll = 1.2f * 0.01f * (Molecule.Kovalenz_Radien[mol.get(i).an] + Molecule.Kovalenz_Radien[mol
							.get(j).an]);
				if ((dv.length() < soll)
						&& ((!mol.get(i).lab.startsWith("H")) || (!mol.get(j).lab
								.startsWith("H"))))
					if ((mol.get(i).part == 0) || (mol.get(j).part == 0)
							|| (mol.get(i).part == mol.get(j).part))

					{
						bonds.add(new Bond(mol.get(i).pos, mol.get(j).pos));
					}
			}
		}
		bbbsiz = bonds.size() * 6;
		bbb = ByteBuffer.allocateDirect(bbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		bbb.position(0);
		for (int i = 0; i < bonds.size(); i++) {
			bbb.put(bonds.get(i).a.x);
			bbb.put(bonds.get(i).a.y);
			bbb.put(bonds.get(i).a.z);
			bbb.put(bonds.get(i).b.x);
			bbb.put(bonds.get(i).b.y);
			bbb.put(bonds.get(i).b.z);
		}
		qbonds.clear();
		for (int i = 0; i < qmol.size(); i++) {
			for (int j = i + 1; j < qmol.size(); j++) {
				Vector3D dv = Vector3D.dif(qmol.get(i).pos, qmol.get(j).pos);
				float soll = 1.6f;
				if (dv.length() < soll) {
					qbonds.add(new Bond(qmol.get(i).pos, qmol.get(j).pos));
				}
			}
		}

		qbbbsiz = qbonds.size() * 6;
		qbbb = ByteBuffer.allocateDirect(qbbbsiz * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		qbbb.position(0);
		for (int i = 0; i < qbonds.size(); i++) {
			qbbb.put(qbonds.get(i).a.x);
			qbbb.put(qbonds.get(i).a.y);
			qbbb.put(qbonds.get(i).a.z);
			qbbb.put(qbonds.get(i).b.x);
			qbbb.put(qbonds.get(i).b.y);
			qbbb.put(qbonds.get(i).b.z);
		}
		if (bitmap != null)
			for (int i = 0; i < bitmap.length; i++)
				bitmap[i].recycle();
		bitmap = new Bitmap[mol.size() + qmol.size()];
		for (int i = 0; i < bitmap.length; i++) {
			if (i < mol.size())
				bitmap[i] = renderLabel(mol.get(i).lab);
			else
				bitmap[i] = renderLabel(qmol.get(i - mol.size()).lab);
		}
	}
}
