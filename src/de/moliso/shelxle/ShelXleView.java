/*
 * Copyright (C) 2011 The Android Open Source Project
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

//import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
import android.view.MotionEvent;

public class ShelXleView extends GLSurfaceView {
	private GLES20TriangleRenderer mRenderer;
	private ShelXleActivity act;
	// Offsets for touch events
	private float mPreviousX;
	private float mPreviousY;
	private final int INIT_STATE = 0;
	private final int ROTATE_STATE = 1;
	private final int ZOOM_STATE = 2;

	private final int FOUR_STATE = 4;
	private final int QPEAK_STATE = 8;
	private final int LABEL_STATE = 16;
	private int state = INIT_STATE;
	private float S = 0.0f;

	// private Context mContext;
	private float mDensity;

	public void setActivity(ShelXleActivity a) {
		act = a;
	}

	Context mContext;

	public ShelXleView(Context context) {
		super(context);
		mContext = context;
		// Toast.makeText(mContext, String.valueOf(mRenderer.idx2.length),
		// Toast.LENGTH_LONG).show();
	}

	public ShelXleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	private float x0 = 0.0f;
	private float y0 = 0.0f;
	private float x1 = 0.0f;
	private float y1 = 0.0f;
	private float x11 = 0f;
	private float y11 = 0f;
	Timer time = new Timer();

	public void startContinousRotation() {
		time.purge();
		try {
			time.schedule(new TimerTask() {
				@Override
				public void run() {
					mRenderer.mDeltaX = mRenderer.dx / 2.5f;
					mRenderer.mDeltaY = mRenderer.dy / 2.5f;
					if ((mRenderer.dx * mRenderer.dx + mRenderer.dy
							* mRenderer.dy) > 0.29f)
						requestRender();
					else
						mRenderer.dx = mRenderer.dy = 0;
				}
			}, 0, 10);
		} catch (IllegalStateException e) {// Timer is dead so create a new one!

			time = new Timer();
			time.schedule(new TimerTask() {
				@Override
				public void run() {
					mRenderer.mDeltaX = mRenderer.dx / 2.5f;
					mRenderer.mDeltaY = mRenderer.dy / 2.5f;
					if ((mRenderer.dx * mRenderer.dx + mRenderer.dy
							* mRenderer.dy) > 0.29f)
						requestRender();
					else
						mRenderer.dx = mRenderer.dy = 0;
				}
			}, 0, 10);
		}

	}

	public void stopContinousRotation() {
		time.cancel();

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event == null) {
			return super.onTouchEvent(event);
		}
		int action = event.getAction();
		int count = event.getPointerCount();
		// if (event != null){
		if ((count == 4)) {
			state = FOUR_STATE;
			return true;
		}

		if (action == MotionEvent.ACTION_UP) {

			if (state == FOUR_STATE) {
				final Intent launchIntent = new Intent(act,
						SimpleExplorer.class);

				try {

					act.startActivityForResult(launchIntent, 1);

				} catch (Exception e) {
					//
					AlertDialog.Builder adb = new AlertDialog.Builder(act);
					adb.setTitle("Error!");
					adb.setMessage(e.getMessage());
					adb.setCancelable(true);
					adb.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					adb.create();
					adb.show();

				}
			}
			state = INIT_STATE;

		}
		if ((count == 1) && (action == MotionEvent.ACTION_DOWN)) {
			mPreviousX = event.getX();
			mPreviousY = event.getY();
			float nahda = 2000, da = 0;
			int nahdai = -1;
			// boolean changed=false;

			for (int j = 0; j < mRenderer.qmol.size(); j++) {
				da = ((mRenderer.qmol.get(j).screenXY[0] - mPreviousX)
						* (mRenderer.qmol.get(j).screenXY[0] - mPreviousX) + (mRenderer.qmol
						.get(j).screenXY[1] - mPreviousY)
						* (mRenderer.qmol.get(j).screenXY[1] - mPreviousY));
				nahdai = (da < nahda) ? j : nahdai;
				nahda = Math.min(nahda, da);
			}

			mRenderer.qpicked = nahdai;
			float adhan = nahda;
			nahda = 2000;
			da = 0;
			nahdai = -1;

			for (int j = 0; j < mRenderer.mol.size(); j++) {
				da = ((mRenderer.mol.get(j).screenXY[0] - mPreviousX)
						* (mRenderer.mol.get(j).screenXY[0] - mPreviousX) + (mRenderer.mol
						.get(j).screenXY[1] - mPreviousY)
						* (mRenderer.mol.get(j).screenXY[1] - mPreviousY));
				nahdai = (da < nahda) ? j : nahdai;
				nahda = Math.min(nahda, da);
			}
			if (adhan < nahda) {
				mRenderer.picked = -1;
			} else {
				mRenderer.qpicked = -1;
				mRenderer.picked = nahdai;

			}
			if ((mRenderer.picked > -1)
					&& (mRenderer.picked < mRenderer.mol.size())) {
				if ((mRenderer.picked + 10000) != mRenderer.ppicked) {
					mRenderer.ppppicked = mRenderer.pppicked;
					mRenderer.pppicked = mRenderer.ppicked;
					mRenderer.ppicked = (mRenderer.picked == -1) ? ((mRenderer.qpicked == -1) ? 0
							: -10000 - mRenderer.qpicked)
							: 10000 + mRenderer.picked;

					Log.d("===SHELXLE===", String.valueOf(mRenderer.ppppicked)
							+ " # " + String.valueOf(mRenderer.pppicked)
							+ " # " + String.valueOf(mRenderer.ppicked) + " # "
							+ String.valueOf(mRenderer.picked - 10000));
					Log.d("===SHELXLE===",
							((mRenderer.ppppicked > 9999) ? mRenderer.mol
									.get(mRenderer.ppppicked - 10000).lab
									: (mRenderer.ppppicked < -9999)?mRenderer.qmol
											.get(-mRenderer.ppppicked - 10000).lab:"")
									+ "=="
									+ ((mRenderer.pppicked > 0) ? mRenderer.mol
											.get(mRenderer.pppicked - 10000).lab
											: (mRenderer.pppicked < -9999)? mRenderer.qmol
													.get(-mRenderer.pppicked - 10000).lab:"")
									+ "=="
									+ ((mRenderer.ppicked > 0) ? mRenderer.mol
											.get(mRenderer.ppicked - 10000).lab
											: (mRenderer.ppicked < -9999)? mRenderer.qmol
													.get(-mRenderer.ppicked - 10000).lab:""));
				}
				requestRender();
			}
			if ((mRenderer.qpicked > -1)
					&& (mRenderer.qpicked < mRenderer.qmol.size())) {

				if ((-10000 - mRenderer.qpicked) != mRenderer.ppicked) {
					mRenderer.ppppicked = mRenderer.pppicked;
					mRenderer.pppicked = mRenderer.ppicked;
					mRenderer.ppicked = (mRenderer.picked == -1) ? ((mRenderer.qpicked == -1) ? 0
							: -10000 - mRenderer.qpicked)
							: 10000 + mRenderer.picked;
					Log.d("===SHELXLE===", String.valueOf(mRenderer.ppppicked)
							+ " - " + String.valueOf(mRenderer.pppicked)
							+ " - " + String.valueOf(mRenderer.ppicked) + " - "
							+ String.valueOf(-mRenderer.ppicked - 10000));//
					Log.d("===SHELXLE===",
							((mRenderer.ppppicked > 9999) ? mRenderer.mol
									.get(mRenderer.ppppicked - 10000).lab
									: (mRenderer.ppppicked < -9999)?mRenderer.qmol
											.get(-mRenderer.ppppicked - 10000).lab:"")
									+ "=="
									+ ((mRenderer.pppicked > 0) ? mRenderer.mol
											.get(mRenderer.pppicked - 10000).lab
											: (mRenderer.pppicked < -9999)? mRenderer.qmol
													.get(-mRenderer.pppicked - 10000).lab:"")
									+ "=="
									+ ((mRenderer.ppicked > 0) ? mRenderer.mol
											.get(mRenderer.ppicked - 10000).lab
											: (mRenderer.ppicked < -9999)? mRenderer.qmol
													.get(-mRenderer.ppicked - 10000).lab:""));}
				requestRender();
			}
			if (mPreviousX < 60) {
				state = QPEAK_STATE;
				mRenderer.qLowerCut = (mPreviousY / mRenderer.pixh)
						* mRenderer.qmin + (1.0f - mPreviousY / mRenderer.pixh)
						* mRenderer.qmax;
				mRenderer.qrebond();
				requestRender();
			} else if ((mRenderer.drawLabels)
					&& (mPreviousX > mRenderer.pixw - 60)) {
				state = LABEL_STATE;
			} else
				state = ROTATE_STATE;
		}
		if ((count == 1) && (action == MotionEvent.ACTION_MOVE)
				&& (state == LABEL_STATE)) {
			float deltaY = ((event.getY() - mPreviousY) / mDensity) / 200f;
			mRenderer.labelScale -= deltaY;
			mRenderer.labelScale = Math.min(mRenderer.labelScale, 3.0f);
			mRenderer.labelScale = Math.max(mRenderer.labelScale, 0.4f);
			mPreviousX = event.getX();
			mPreviousY = event.getY();
			requestRender();
		} else if ((count == 1) && (action == MotionEvent.ACTION_MOVE)
				&& (state == QPEAK_STATE)) {

			mPreviousX = event.getX();
			mPreviousY = event.getY();

			mRenderer.qLowerCut = (mPreviousY / mRenderer.pixh)
					* mRenderer.qmin + (1.0f - mPreviousY / mRenderer.pixh)
					* mRenderer.qmax;
			mRenderer.qrebond();
			requestRender();
		} else if ((count == 1) && (action == MotionEvent.ACTION_MOVE)) {
			if (state != ROTATE_STATE) {
				mPreviousX = event.getX();
				mPreviousY = event.getY();
			}
			state = ROTATE_STATE;
			x11 = event.getX();
			y11 = event.getY();
			if (mRenderer != null) {
				float deltaX = (x11 - mPreviousX) / mDensity / 2f;
				float deltaY = (y11 - mPreviousY) / mDensity / 2f;
				mRenderer.dx = mRenderer.mDeltaX += deltaX;
				mRenderer.dy = mRenderer.mDeltaY += deltaY;
				mPreviousX = x11;
				mPreviousY = y11;
				requestRender();
			}
		} else if ((count == 2) && (event.findPointerIndex(0) > -1)
				&& (event.findPointerIndex(1) > -1)) {
			if (mRenderer != null) {
				if ((action == MotionEvent.ACTION_DOWN)
						|| (action == MotionEvent.ACTION_POINTER_1_DOWN)
						|| (action == MotionEvent.ACTION_POINTER_2_DOWN)) {

					try {
						// if (event.getPointerCount()==2){

						x0 = event.getX(event.findPointerIndex(0));
						y0 = event.getY(event.findPointerIndex(0));
						x1 = event.getX(event.findPointerIndex(1));
						y1 = event.getY(event.findPointerIndex(1));
					} catch (ArrayIndexOutOfBoundsException e) {
						return false;
					}

					float deltaX = Math.abs(x0 - x1) / mDensity / 2f;
					float deltaY = Math.abs(y0 - y1) / mDensity / 2f;
					S = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					state = ZOOM_STATE;
				} else if ((action == MotionEvent.ACTION_MOVE)
						&& (state == ZOOM_STATE)) {
					float X0 = 0.0f;
					float Y0 = 0.0f;
					float X1 = 0.0f;
					float Y1 = 0.0f;
					try {
						X0 = event.getX(event.findPointerIndex(0));
						Y0 = event.getY(event.findPointerIndex(0));
						X1 = event.getX(event.findPointerIndex(1));
						Y1 = event.getY(event.findPointerIndex(1));

					} catch (ArrayIndexOutOfBoundsException e) {
						return false;
					}

					float deltaX = Math.abs(X0 - X1) / mDensity / 2f;
					float deltaY = Math.abs(Y0 - Y1) / mDensity / 2f;
					float s = (float) Math.sqrt(deltaX * deltaX + deltaY
							* deltaY);
					mRenderer.mScale = 1.0f - 0.01f * (S - s);
					mPreviousX = x0 = X0;
					mPreviousY = y0 = Y0;
					x1 = X1;
					y1 = Y1;
					S = s;
					requestRender();
				}
			}
		}

		return true;
		// }
		/*
		 * else { return super.onTouchEvent(event); }
		 */
	}

	public void setRenderer(GLES20TriangleRenderer renderer, float density) {
		mRenderer = renderer;
		mDensity = density;

		// setRenderMode (RENDERMODE_WHEN_DIRTY);
		super.setRenderer(renderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

}
