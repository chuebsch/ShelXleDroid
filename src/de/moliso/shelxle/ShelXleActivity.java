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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ShelXleActivity extends Activity {
	public final ShelXleActivity that = this;

	private class PerformFFTinBG extends AsyncTask<String, String, Integer> {
		protected void onPostExecute(Integer result) {
			mRenderer.msg = "";
			mView.requestRender();
		}

		protected void onProgressUpdate(String... progress) {
			if (progress.length > 0)
				mRenderer.msg = progress[0];

			mView.requestRender();
		}

		protected Integer doInBackground(String... fcfName) {
			if (fcfName.length < 1)
				return 0;
			publishProgress("doing fft ...");
			FourXle fxle = new FourXle();
			float[] fopl = fxle.loadFouAndPerform(fcfName[0], true,
					fxle.positions(mRenderer, mRenderer.mid));

			publishProgress("loading map...");
			if (fopl != null) {
				mRenderer.mFFTfopMap = ByteBuffer
						.allocateDirect(fopl.length * FLOAT_SIZE_BYTES)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				mRenderer.mFFTfopMap.put(fopl).position(0);
			}
			// public native float[] getFoPlus();
			float[] fomi = fxle.getFoMinus();
			if (fomi != null) {
				Log.d("fft", "Fo Minus = " + fomi.length);
				mRenderer.mFFTfomMap = ByteBuffer
						.allocateDirect(fomi.length * FLOAT_SIZE_BYTES)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				mRenderer.mFFTfomMap.put(fomi).position(0);
				Log.d("fft", "Fo Minus = " + fomi.length);

			}
			float[] dipl = fxle.getDifPlus();
			if (dipl != null) {
				Log.d("fft", "Fo-Fc Plus = " + dipl.length);
				mRenderer.mFFTdipMap = ByteBuffer
						.allocateDirect(dipl.length * FLOAT_SIZE_BYTES)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				try {
					mRenderer.mFFTdipMap.put(dipl).position(0);
				} catch (BufferOverflowException e) {

				}
				Log.d("fft", "Fo-Fc Plus = " + dipl.length);

			}
			float[] dimi = fxle.getDifMinus();
			if (dimi != null) {
				Log.d("fft", "Fo-Fc Minus = " + dimi.length);
				mRenderer.mFFTdimMap = ByteBuffer
						.allocateDirect(dimi.length * FLOAT_SIZE_BYTES)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				mRenderer.mFFTdimMap.put(dimi).position(0);
				Log.d("fft", "Fo-Fc Minus = " + dimi.length);

			}
			publishProgress("finished");
			return 1;
		}
	}

	Timer time = new Timer();
	private Properties setting = null;
	private ShelXleView mView;
	private GLES20TriangleRenderer mRenderer;

	public String res;
	public String versionName="";
	
	// public AtomLabelView alv;
	@Override
	public void onBackPressed() {

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Finish?");
		adb.setMessage("Do you realy want to quit ShelXle.Droid?");
		adb.setCancelable(true);
		adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		adb.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		adb.create();
		adb.show();

	}

	private void CopyRawToFile(int resourceId, File sdcard, String filename)
			throws IOException {
		InputStream input = getResources().openRawResource(resourceId);
		FileOutputStream output = new FileOutputStream(new File(sdcard,
				filename));

		byte[] buffer = new byte[1024 * 4];
		int a;
		while ((a = input.read(buffer)) > 0)
			output.write(buffer, 0, a);

		input.close();
		output.close();
	}

	@Override
	protected void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.surface);
		globalVariables.doHighLight=true;
		mView = (ShelXleView) findViewById(R.id.gles);
		try{
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}catch (NameNotFoundException e){
			//wurst
		}

		// alv = (AtomLabelView) findViewById(R.id.view1);
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
		setting = new Properties();
		File sdcard = Environment.getExternalStorageDirectory();
		try {
			setting.load(new FileInputStream(sdcard.getAbsolutePath()
					+ "/.ShelXle/config"));
		} catch (IOException e) {// don't care if it don't work to load.
		}
		// mView = new ShelXleView(getApplication());

		// setContentView(mView);
		mView.setActivity(this);
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			mView.setEGLContextClientVersion(2);

			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			// Set the renderer to our demo renderer, defined below.
			mRenderer = new GLES20TriangleRenderer(mView.getContext(), this);
			mView.setRenderer(mRenderer, displayMetrics.density);
		} else {
			Toast.makeText(this, "OpenGL ES 1.x not supported!",
					Toast.LENGTH_LONG).show();
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}
		if (!setting.containsKey("RES")) {
			// File sdcard = Environment.getExternalStorageDirectory();
			try {
				File dir = new File(sdcard, "ShelXleDroidExamle");
				if (!dir.exists())
					dir.mkdir();

				CopyRawToFile(R.raw.qqqbtp02res, dir, "qqqbtp02.res");
				CopyRawToFile(R.raw.qqqbtp02fcf, dir, "qqqbtp02.fcf");
				CopyRawToFile(R.raw.qqqbtp02hkl, dir, "qqqbtp02.hkl");
			} catch (IOException e) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
			res = sdcard.getAbsolutePath() + "/ShelXleDroidExamle/qqqbtp02.res";

			try {

				setting.setProperty("RES", res);
				setting.setProperty("LASTDIR", new File(sdcard,
						"ShelXleDroidExamle").getAbsolutePath());
				File dir = new File(sdcard, ".ShelXle/");
				if (!dir.exists())
					dir.mkdir();
				setting.store(new FileOutputStream(sdcard.getAbsolutePath()
						+ "/.ShelXle/config"), null);
			} catch (IOException e) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
			mRenderer.load_sheldrick(res);
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setTitle("ShelXle.Droid ("+versionName+")");
				ab.setSubtitle(res);
			}
			/*
			 * BitmapDrawable bmdbl=new BitmapDrawable(Resources.getSystem(),
			 * mRenderer.renderLabel("C1a1")); ab.setIcon((Drawable)bmdbl);
			 */
			time.schedule(new TimerTask() {
				@Override
				public void run() {
					float fps = (float) mRenderer.renderCnt / 1.0f;
					mRenderer.fps = fps;
					// mRenderer.renderCnt = 0;
					mView.requestRender();
					mRenderer.renderCnt = 0;
				}
			}, 0, 1000);
			/*
			 * final Intent launchIntent = new Intent(this,
			 * SimpleExplorer.class);
			 * 
			 * try { startActivityForResult(launchIntent, 1);
			 * 
			 * } catch (Exception e) { // AlertDialog.Builder adb = new
			 * AlertDialog.Builder(this); adb.setTitle("Error!");
			 * adb.setMessage(e.getMessage()); adb.setCancelable(true);
			 * adb.setPositiveButton("OK", new DialogInterface.OnClickListener()
			 * {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { dialog.cancel(); } }); adb.create(); adb.show();
			 * 
			 * }
			 */
		} else {
			// setting.setProperty("RES", res);
			res = setting.getProperty("RES", "");
			mRenderer.load_sheldrick(res);
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setTitle("ShelXle.Droid ("+versionName+")");
				ab.setSubtitle(res);
			}
			/*
			 * BitmapDrawable bmdbl=new BitmapDrawable(Resources.getSystem(),
			 * mRenderer.renderLabel("C1a1")); ab.setIcon((Drawable)bmdbl);
			 */
			time.schedule(new TimerTask() {
				@Override
				public void run() {
					float fps = (float) mRenderer.renderCnt / 1.0f;
					mRenderer.fps = fps;
					// mRenderer.renderCnt = 0;
					mView.requestRender();
					mRenderer.renderCnt = 0;
				}
			}, 0, 1000);

		}

		// mView.setVisibility(View.INVISIBLE);

		// Log.d("ShelXle",
		// (mView.isShown())?"mView is visible ":"mView is a ninja! ");
		// alv.setVisibility(View.VISIBLE);
		// alv.bringToFront();
		// Log.d("ShelXle",
		// (alv.isShown())?"alv is visible ":"alv is a ninja! ");
		// runXL(res);
	}

	@Override
	protected void onPause() {
		super.onPause();
		time.cancel();
		mView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mView.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {// SimpleExplorer
			if (resultCode == android.app.Activity.RESULT_OK) {
				res = data.getStringExtra("result");
				String fcf = res.replaceAll("(.res)|(.ins)$", ".fcf");
				File fcff = new File(fcf);
				menu1.setGroupVisible(R.id.fourier, fcff.exists());

				mRenderer.load_sheldrick(res);
				time.cancel();
				time = new Timer();
				time.schedule(new TimerTask() {
					@Override
					public void run() {
						float fps = (float) mRenderer.renderCnt / 1.0f;
						mRenderer.fps = fps;
						mRenderer.renderCnt = 0;
						mView.requestRender();
						mRenderer.renderCnt = 0;
					}
				}, 0, 1000);
				File sdcard = Environment.getExternalStorageDirectory();
				try {
					setting.load(new FileInputStream(sdcard.getAbsolutePath()
							+ "/.ShelXle/config"));
				} catch (IOException e) {// don't care if it don't work to load.
				}
				if (Build.VERSION.SDK_INT > 10) {
					ActionBar ab = getActionBar();
					ab.setTitle("ShelXle.Droid ("+versionName+")");
					ab.setSubtitle(res);
				}
				try {
					setting.setProperty("RES", res);
					File dir = new File(sdcard, ".ShelXle/");
					if (!dir.exists())
						dir.mkdir();
					setting.store(new FileOutputStream(sdcard.getAbsolutePath()
							+ "/.ShelXle/config"), null);
				} catch (IOException e) {
					AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
		}
		if (requestCode == 2) {// Refinement
			if ((resultCode == android.app.Activity.RESULT_OK)
					|| (resultCode == android.app.Activity.RESULT_CANCELED)) {
				res = data.getStringExtra("result");
				if ((res == null) || (res == "") || (!res.endsWith(".res"))
						&& (!res.endsWith(".ins"))) {
					res = setting.getProperty("res", "");
					return;
				}
				String fcf = res.replaceAll("(.res)|(.ins)$", ".fcf");
				File fcff = new File(fcf);
				menu1.setGroupVisible(R.id.fourier, fcff.exists());

				mRenderer.load_sheldrick(res);

				time.cancel();
				time = new Timer();
				time.schedule(new TimerTask() {
					@Override
					public void run() {
						float fps = (float) mRenderer.renderCnt / 1.0f;
						mRenderer.fps = fps;
						mRenderer.renderCnt = 0;
						mView.requestRender();
						mRenderer.renderCnt = 0;
					}
				}, 0, 1000);
				if (Build.VERSION.SDK_INT > 10) {
					ActionBar ab = getActionBar();
					ab.setTitle("ShelXle.Droid ("+versionName+")");
					ab.setSubtitle(res);
				}
				try {
					setting.setProperty("RES", res);
					File sdcard = Environment.getExternalStorageDirectory();
					File dir = new File(sdcard, ".ShelXle/");
					if (!dir.exists())
						dir.mkdir();
					setting.store(new FileOutputStream(sdcard.getAbsolutePath()
							+ "/.ShelXle/config"), null);
				} catch (IOException e) {
					AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
		}
		if (requestCode == 3) {// Editor
			res = data.getStringExtra("result");
			if ((res == null) || (res == "") || (!res.endsWith(".res"))
					&& (!res.endsWith(".ins"))) {
				res = setting.getProperty("res", "");
			}
			if ((res.endsWith(".res")) || (res.endsWith(".ins")))
				mRenderer.load_sheldrick(res);
			String fcf = res.replaceAll("(.res)|(.ins)$", ".fcf");
			File fcff = new File(fcf);
			menu1.findItem(R.id.high).setChecked(globalVariables.doHighLight);
			menu1.setGroupVisible(R.id.fourier, fcff.exists());
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setTitle("ShelXle.Droid ("+versionName+")");
				ab.setSubtitle(res);
			}
			time.cancel();
			time = new Timer();
			time.schedule(new TimerTask() {
				@Override
				public void run() {
					float fps = (float) mRenderer.renderCnt / 1.0f;
					mRenderer.fps = fps;
					// mRenderer.renderCnt = 0;
					mView.requestRender();
					mRenderer.renderCnt = 0;
				}
			}, 0, 1000);

		}
	}

	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menue, menu);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts
		/*
		 * menu.add(0, OPEN_ID, 0, R.string.OpenResFile).setShortcut('0', 'o');
		 * menu.add(0, REFINE_ID, 0, R.string.RefineXL).setShortcut('1', 'r');
		 * menu.add(0, QUIT_ID, 0, R.string.Quit).setShortcut('2', 'q');
		 */

		menu1 = menu;
		return true;
	}

	Menu menu1 = null;

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.

		menu1 = menu;
		// MenuItem mi=
		menu.findItem(R.id.refineMenu).setVisible(
				(res != null) && (!res.isEmpty()) && (new File(res).exists()));
		if (res != null) {
			String fcf = res.replaceAll("(.res)|(.ins)$", ".fcf");
			File fcff = new File(fcf);
			menu1.setGroupVisible(R.id.fourier, fcff.exists());
		} else
			menu1.setGroupVisible(R.id.fourier, false);
		if (res != null) {
			String lst = res.replaceAll("(.res)|(.ins)$", ".lst");
			File lstf = new File(lst);
			menu1.findItem(R.id.lstMenu).setVisible(lstf.exists());
		}
		return true;
	}

	private boolean drawLabels = false;
	private boolean drawADPs = true;;

	private class GrowTask extends AsyncTask<Integer, String, Integer> {
		protected Integer doInBackground(Integer... integers) {
			publishProgress("growing structure...");
			mView.requestRender();
			mRenderer.grow();
			return 1;
		}

		protected void onProgressUpdate(String... progress) {
			if (progress.length > 0)
				mRenderer.msg = progress[0];

			mView.requestRender();
		}

		protected void onPostExecute(Integer result) {
			mRenderer.msg = "";
			mView.requestRender();
			if (hotsedraet) {
				menu1.findItem(R.id.contRot).setChecked(true);
				mView.startContinousRotation();
			}

		}
	}

	private boolean hotsedraet = false;// read it Swabian
	private static final int FLOAT_SIZE_BYTES = 4;

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.fps: {
			time.cancel();
			mRenderer.countFrames = !menu1.findItem(R.id.fps).isChecked();
			menu1.findItem(R.id.fps).setChecked(mRenderer.countFrames);

			if (mRenderer.countFrames) {
				time = new Timer();
				mRenderer.renderCnt = 0;
				time.schedule(new TimerTask() {
					@Override
					public void run() {
						float fps = (float) mRenderer.renderCnt / 1.0f;
						mRenderer.fps = fps;
						mRenderer.renderCnt = 0;
						mView.requestRender();
						mRenderer.renderCnt = 0;
					}
				}, 0, 1000);
			} else
				mView.requestRender();
			return true;
		}
		case R.id.fftfcf: {
			String fcfName = res;
			fcfName = fcfName.replace(".res", ".fcf");
			if (mRenderer.mFFTfomMap != null)
				mRenderer.mFFTfomMap.clear();
			if (mRenderer.mFFTfopMap != null)
				mRenderer.mFFTfopMap.clear();
			if (mRenderer.mFFTdimMap != null)
				mRenderer.mFFTdimMap.clear();
			if (mRenderer.mFFTdipMap != null)
				mRenderer.mFFTdipMap.clear();

			new PerformFFTinBG().execute(fcfName);
			return true;

		}
		case R.id.grow: {
			hotsedraet = menu1.findItem(R.id.contRot).isChecked();
			menu1.findItem(R.id.contRot).setChecked(false);
			mView.stopContinousRotation();
			mView.requestRender();
			new GrowTask().execute(1);

			mRenderer.picked = -1;
			mRenderer.qpicked = -1;
			mRenderer.ppicked = -1;
			mRenderer.pppicked = -1;
			mRenderer.ppppicked = -1;
			return true;
		}
		case R.id.fuse: {
			hotsedraet = menu1.findItem(R.id.contRot).isChecked();
			menu1.findItem(R.id.contRot).setChecked(false);
			mView.stopContinousRotation();

			mRenderer.picked = -1;
			mRenderer.qpicked = -1;
			mRenderer.ppicked = -1;
			mRenderer.pppicked = -1;
			mRenderer.ppppicked = -1;

			mRenderer.fuse();
			mView.requestRender();
			if (hotsedraet) {
				menu1.findItem(R.id.contRot).setChecked(true);
				mView.startContinousRotation();
			}
			return true;
		}
		case R.id.labelMenu: {
			drawLabels = !drawLabels;
			mRenderer.setLabels(drawLabels);
			mView.requestRender();
			return true;
		}
		case R.id.fom: {
			mRenderer.drawFo = !mRenderer.drawFo;
			item.setChecked(mRenderer.drawFo);
			mView.requestRender();
			return true;
		}
		case R.id.dim: {
			mRenderer.drawDif = !mRenderer.drawDif;
			item.setChecked(mRenderer.drawDif);
			mView.requestRender();
			return true;
		}
		case R.id.adps: {
			drawADPs = !drawADPs;
			item.setChecked(drawADPs);
			mRenderer.setADPs(drawADPs);
			mView.requestRender();
			return true;
		}
		case R.id.niceAtoms: {
			item.setChecked(!item.isChecked());
			mRenderer.setHighRes(item.isChecked());
			mView.requestRender();
			return true;
		}
		case R.id.contRot: {
			boolean b = item.isChecked();
			item.setChecked(!b);
			if (!b) {
				mView.startContinousRotation();
			} else {
				mView.stopContinousRotation();
			}
			return true;
		}
		case R.id.lstMenu: {
			final Intent launchIntent = new Intent(this, LSTFileView.class);
			this.startActivity(launchIntent);
			return true;
		}
		case R.id.high: {
			boolean b = item.isChecked();
			item.setChecked(!b);
			globalVariables.doHighLight = !b;
			return true;

		}
		case R.id.mailMenu: {
			StringBuilder sb = new StringBuilder();
			try {
				Process p = Runtime.getRuntime().exec(
						"/system/bin/logcat -d long *:V");
				BufferedReader in = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					sb.append(line + "\n");
				}
				p = Runtime.getRuntime().exec("/system/bin/logcat -c");
			} catch (IOException e) {
			}
			String emailAddressList[] = { "shelxle@moliso.de" };

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL, emailAddressList);
			intent.putExtra(Intent.EXTRA_SUBJECT, "ShelXle.Droid");
			intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
			// intent.putExtra(Intent.EXTRA_STREAM, value)
			startActivity(Intent.createChooser(intent,
					"Choice App to send log via email:"));
		}
			return true;
		case R.id.quitMenu:
			finish();
			return true;
		case R.id.editMenu: {
			final Intent launchIntent = new Intent(this, Editor.class);

			try {

				this.startActivityForResult(launchIntent, 3);

			} catch (Exception e) {
				//
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
			return true;

		}
		case R.id.openMenu: {
			final Intent launchIntent = new Intent(this, SimpleExplorer.class);

			try {

				startActivityForResult(launchIntent, 1);

			} catch (Exception e) {
				//
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
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

			return true;
		}
		case R.id.refineMenu: {
			final Intent launchIntent = new Intent(this, RefineXL.class);

			try {
				// this.startActivity(launchIntent);
				startActivityForResult(launchIntent, 2);

			} catch (Exception e) {
				//
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
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

			}// */
		}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
