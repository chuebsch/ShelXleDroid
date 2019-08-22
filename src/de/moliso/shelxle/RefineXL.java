package de.moliso.shelxle;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RefineXL extends Activity {
	TextView mPut = null;
	private int mProcId;
	private FileDescriptor mTermFd;
	private FileInputStream mTermIn;
	public String softExe = "";
	public String res = "";
	private Properties setting = null;
	private Button ok, cancel, stop;
	private String sult = "";
	private String nosult = "";
	private ByteQueue mByteQueue;
	private byte[] mReceiveBuffer;
	private Thread mPollingThread;
	private static final int NEW_INPUT = 1;
	private boolean mIsRunning = false;
	private Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!mIsRunning) {
				return;
			}
			if (msg.what == NEW_INPUT) {
				readFromProcess();
			}
		}
	};

	@Override
	public void onBackPressed() {
		Toast.makeText(
				this,
				"You can't go back now! Use the 'discard' or 'load result' button.",
				Toast.LENGTH_SHORT).show();

	}

	OnClickListener mOKListener = new OnClickListener() {
		public void onClick(View v) {
			Intent returnIntent = getIntent();
			returnIntent.putExtra("result", sult);
			setResult(RESULT_OK, returnIntent);
			ende();
		}
	};

	OnClickListener mCancelListener = new OnClickListener() {
		public void onClick(View v) {

			Intent returnIntent = getIntent();
			returnIntent.putExtra("result", nosult);
			setResult(RESULT_CANCELED, returnIntent);
			ende();

		}
	};

	OnClickListener mStopListener = new OnClickListener() {
		public void onClick(View v) {

			Intent returnIntent = getIntent();
			returnIntent.putExtra("result", nosult);
			setResult(RESULT_CANCELED, returnIntent);
			ok.setVisibility(View.INVISIBLE);
			stop.setVisibility(View.INVISIBLE);
			cancel.setVisibility(View.VISIBLE);

			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setTitle("Refinement finished unsucessfully!");
			}
			Exec.hangupProcessGroup(mProcId);
			Exec.close(mTermFd);
			mIsRunning = false;
		}
	};

	private void CopyRawToFile(int resourceId, String filename)
			throws IOException {
		InputStream input = getResources().openRawResource(resourceId);
		OutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);

		byte[] buffer = new byte[1024 * 4];
		int a;
		while ((a = input.read(buffer)) > 0)
			output.write(buffer, 0, a);

		input.close();
		output.close();
	}

	private String MakeExecutable(String filename) throws IOException,
			InterruptedException {
		// First get the absolute path to the file
		File folder = getFilesDir();

		String filefolder = folder.getCanonicalPath();
		if (!filefolder.endsWith("/"))
			filefolder += "/";

		String fullpath = filefolder + filename;

		Runtime.getRuntime().exec("chmod 700 " + fullpath).waitFor();// .waitForExit();

		return fullpath;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setting = new Properties();
		setContentView(R.layout.refining);
		Intent returnIntent = getIntent();
		returnIntent.putExtra("result", nosult);
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
		mPut = (TextView) findViewById(R.id.refine);
		mPut.setVerticalScrollBarEnabled(true);
		mPut.setHorizontalScrollBarEnabled(true);
		mPut.setEnabled(false);
		ok = (Button) findViewById(R.id.loadres);
		ok.setOnClickListener(mOKListener);
		cancel = (Button) findViewById(R.id.discard);
		cancel.setOnClickListener(mCancelListener);
		stop = (Button) findViewById(R.id.stopref);
		stop.setOnClickListener(mStopListener);

		ok.setVisibility(View.INVISIBLE);
		stop.setVisibility(View.VISIBLE);
		cancel.setVisibility(View.INVISIBLE);

		scrl = (TwoDScrollView) findViewById(R.id.twoDScrollView1);
		File sdcard = Environment.getExternalStorageDirectory();
		try {
			setting.load(new FileInputStream(sdcard.getAbsolutePath()
					+ "/.ShelXle/config"));
		} catch (IOException e) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Error!");
			adb.setMessage(e.getMessage());
			adb.setCancelable(true);
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			adb.create();
			adb.show();

		}
		if (!setting.containsKey("EXE")) {
			installXL();
		} else {
			softExe = setting.getProperty("EXE", "");
			try {
				MakeExecutable("armxl");
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
			String md5 = checkMD5(softExe);
			if (md5 != "234d34f152e065580f9dd314418565c2") {//
				installXL();
				md5 = checkMD5(softExe);
				Log.d("ShelXle MD5 of armxl", md5);
			}
		}
		if (setting.containsKey("RES")) {
			// setting.setProperty("RES", res);
			res = setting.getProperty("RES", "");
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setSubtitle(res);
			}

		}

		if (!res.isEmpty())
			runXL(res); // */
		/*
		 * // Intent returnIntent = getIntent(); //
		 * returnIntent.putExtra("result", res); // setResult(RESULT_CANCELED,
		 * returnIntent); // finish();
		 */
	}

	private void installXL() {
		File sdcard = Environment.getExternalStorageDirectory();
		try {
			CopyRawToFile(R.raw.armxl, "armxl");
		} catch (IOException e) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Error!");
			adb.setMessage(e.getMessage());
			adb.setCancelable(true);
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			adb.create();
			adb.show();

		}
		try {
			String exe = MakeExecutable("armxl");
			setting.setProperty("EXE", exe);
			softExe = exe;
			try {
				File sd = Environment.getExternalStorageDirectory();
				File dir = new File(sd, ".ShelXle/");
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
		} catch (IOException e) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Error!");
			adb.setMessage(e.getMessage());
			adb.setCancelable(true);
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			adb.create();
			adb.show();
		} catch (InterruptedException e) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Error!");
			adb.setMessage(e.getMessage());
			adb.setCancelable(true);
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}

			});
			adb.create();
			adb.show();
		}

	}

	private String checkMD5(String softExe2) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new FileInputStream(softExe2);
			byte[] digest = new byte[1024];
			int numread;
			try {
				do {
					numread = is.read(digest);
					if (numread > 0) {
						md.update(digest, 0, numread);
					}
				} while (numread != -1);
				is.close();
			} catch (IOException e) {
				return "io error";
			}
			String s = "";
			digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				s += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return s;

		} catch (NoSuchAlgorithmException e) {
			return "-no such Algo";
		} catch (FileNotFoundException e) {
			return "-file not found";
		}
	}

	public String noext;

	public void runXL(String file) {
		mPut.setText("");
		mPut.setEnabled(false);
		File res = new File(file);
		String name = res.getName();
		String dir = res.getPath();
		dir = dir.replaceAll(name + "$", "");
		File d = new File(dir);
		if (d.isDirectory()) {
			noext = name;
			noext = noext.replaceAll(".res$", "");
			noext = noext.replaceAll(".ins$", "");
			sult = dir + noext + ".res";
			nosult = dir + noext + ".ins";

			if (!(new String(dir + noext + ".ins").equalsIgnoreCase(file))) {
				// copy res to ins!
				try {
					InputStream ins = new FileInputStream(file);
					OutputStream out = new FileOutputStream(dir + noext
							+ ".ins");

					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					while ((len = ins.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					ins.close();
					out.close();
				} catch (IOException e) {

				}
			}// copy res to ins!
			File home = getFilesDir();
			File tmp = new File(home, "/tmp");
			String temp = tmp.getAbsolutePath();
			if (!tmp.exists()) {
				tmp.mkdir();
				try {
					Runtime.getRuntime().exec("chmod 700 " + temp).waitFor();
				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
			}
			int processId[] = new int[1];
			mTermFd = Exec.createSubprocess(softExe, noext, "GFORTRAN_TMPDIR="
					+ temp + "/", dir, processId);
			mProcId = processId[0];
			// mTermOut = new FileOutputStream(mTermFd);
			mTermIn = new FileInputStream(mTermFd);

			mIsRunning = true;

			Thread watcher = new Thread() {
				@Override
				public void run() {
					int result = Exec.waitFor(mProcId);
					mMsgHandler.sendEmptyMessage(result);
				}
			};
			watcher.setName("Process watcher");
			watcher.start();
			mReceiveBuffer = new byte[4 * 1024];
			mByteQueue = new ByteQueue(4 * 1024);

			mPollingThread = new Thread() {
				private byte[] mBuffer = new byte[4096];

				@Override
				public void run() {
					try {
						while (true) {
							// scrl.arrowScroll(View.FOCUS_DOWN, false);
							int read = mTermIn.read(mBuffer);
							if (read == -1) {
								// EOF -- process exited
								return;
							}
							// mPut.append(Arrays.toString(mBuffer));
							mByteQueue.write(mBuffer, 0, read);
							mMsgHandler.sendMessage(mMsgHandler
									.obtainMessage(NEW_INPUT));
						}
					} catch (IOException e) {
					} catch (InterruptedException e) {
					}
				}
			};
			mPollingThread.setName("Input reader");
			mPollingThread.start();
			// mPut.setText("");

		}
	}

	/**
	 * Look for new input from the ptty, send it to the terminal emulator.
	 */
	TwoDScrollView scrl;

	public final Pattern Integers = Pattern
			.compile("(?<=\\s)[-]?[0-9]+(?=\\s)");
	public final Pattern Rvalues = Pattern
			.compile("(?<=\\s)(w)?R[1-2(]\\w*[)]?\\s+=\\s+[0-9]+[.][0-9]*(?=\\s)");
	public final Pattern Floats = Pattern
			.compile("(?<=\\s)[-]?[0-9]+[.][0-9]*(?=\\s)");
	public final Pattern Alerts = Pattern
			.compile("(?<=\\s)[*]{2,2}.*[*]{2,2}(?=\\s)");

	private void readFromProcess() {
		int bytesAvailable = mByteQueue.getBytesAvailable();
		int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
		if (bytesToRead > 0) {
			try {
				int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
				// mEmulator.append(mReceiveBuffer, 0, bytesRead);

				mPut.append(new String(mReceiveBuffer), 0, bytesRead);
				String sp = mPut.getText().toString();
				if (sp.contains("finished")) {

					// scrl.scrollBy(0, (int)766);
					// mPut.scrollBy(0, (int)sch+1);
					mPut.setGravity(Gravity.BOTTOM);
					ok.setVisibility(View.VISIBLE);
					stop.setVisibility(View.INVISIBLE);
					cancel.setVisibility(View.VISIBLE);
					globalVariables.unitNeu="";
					if (Build.VERSION.SDK_INT > 10) {
						ActionBar ab = getActionBar();
						ab.setTitle("Refinement finished sucessfully!");
					}

				}
				// //////////////////////////////////////////////////////
				// Syntax Highlighting
				if (globalVariables.doHighLight) {
					Spannable spannable = mPut.getEditableText();
					if (spannable != null) {
						String content = spannable.toString();
						Matcher match = Integers.matcher(content);
						int start = 0;
						int found = 0;
						while (match.find()) {
							found = match.start();
							start = match.end();
							spannable.setSpan(
									new ForegroundColorSpan(Color
											.parseColor("#0000FF")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						}// Integers
						match = Floats.matcher(content);
						start = 0;
						found = 0;
						while (match.find()) {
							found = match.start();
							start = match.end();
							spannable.setSpan(
									new ForegroundColorSpan(Color
											.parseColor("#FF0000")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						}// Floats*/
						match = Alerts.matcher(content);
						start = 0;
						found = 0;
						while (match.find()) {
							found = match.start();
							start = match.end();

							spannable.setSpan(
									new BackgroundColorSpan(Color
											.parseColor("#FF0000")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
							spannable.setSpan(
									new ForegroundColorSpan(Color
											.parseColor("#FFFF00")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						}// Alerts
						match = Rvalues.matcher(content);
						start = 0;
						found = 0;
						while (match.find()) {
							found = match.start();
							start = match.end();

							spannable.setSpan(
									new BackgroundColorSpan(Color
											.parseColor("#FFFF00")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
							spannable.setSpan(
									new ForegroundColorSpan(Color
											.parseColor("#000080")), found,
									start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						}// Rvalues
					}// spannable != null
				}// doHighLight

				// *///////////////////////////////////////////////////////

			} catch (InterruptedException e) {
			}
		}

	}

	public void ende() {

		globalVariables.unitNeu="";
		Log.d("§§§", "test2 "+globalVariables.unitNeu);
		if (mIsRunning) {
			Exec.hangupProcessGroup(mProcId);
			Exec.close(mTermFd);
		}
		mIsRunning = false;
		finish();
		
	}

}
