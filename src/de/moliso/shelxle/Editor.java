package de.moliso.shelxle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

//import java.io.InputStream;

public class Editor extends Activity {
	public TextEdit edit;
	private Properties setting = null;
	public String res;
	boolean changed = false;

	public void load_shx(String fileName) {
		if (fileName.isEmpty()) {
			edit.setText("");
			return;
		}
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
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
			// You'll need to add proper error handling here
		}
		edit.setText(text);
		if (globalVariables.doHighLight)
			edit.highLight();
		edit.bringToFront();
		edit.addTextChangedListener(new TextWatcher() {
			private int startpo = 0;
			private int countpo = 0;

			@Override
			public void afterTextChanged(Editable s) {

				if (!globalVariables.doHighLight) {
					Spannable spannable = s;
					BackgroundColorSpan[] bcs = spannable.getSpans(0,
							s.length(), BackgroundColorSpan.class);
					for (BackgroundColorSpan span : bcs) {
						spannable.removeSpan(span);
					}
					ForegroundColorSpan[] fcs = spannable.getSpans(0,
							s.length(), ForegroundColorSpan.class);
					for (ForegroundColorSpan span : fcs) {
						spannable.removeSpan(span);
					}
				} else {
					String content = s.toString();

					Matcher match = edit.p.matcher(content);
					Matcher LineBegin = edit.nl.matcher(content);
					int start = Math.max(0, startpo - 2);
					int found = 0;
					int misp = content.length(), masp = 0;
					;
					Spannable spannable = s;
					BackgroundColorSpan[] bcs = spannable.getSpans(start,
							Math.min(startpo + countpo, content.length()),
							BackgroundColorSpan.class);

					for (BackgroundColorSpan span : bcs) {
						misp = Math.min(misp, spannable.getSpanStart(span));
						masp = Math.max(masp, spannable.getSpanEnd(span));
						spannable.removeSpan(span);
					}
					ForegroundColorSpan[] fcs = spannable.getSpans(start,
							Math.min(startpo + countpo, content.length()),
							ForegroundColorSpan.class);
					for (ForegroundColorSpan span : fcs) {
						misp = Math.min(misp, spannable.getSpanStart(span));
						masp = Math.max(masp, spannable.getSpanEnd(span));
						spannable.removeSpan(span);
					}
					while (LineBegin.find()) {
						int an = LineBegin.start(), en = LineBegin.end();
						// Log.d("===SHELXLE===",String.format("st %d sta %d end %d [%s]",start,
						// an,
						// en,LineBegin.group() ));
						if ((start > an) && (start < en)) {
							start = an;
							misp = start;
							break;
						}
					}
					while (match.find(start)) {
						found = match.start();
						start = match.end();
						spannable.setSpan(
								new BackgroundColorSpan(Color
										.parseColor("#aaffaa")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						spannable.setSpan(
								new ForegroundColorSpan(Color
										.parseColor("#800000")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						if (found > masp)
							break;
					}
					match = edit.Integers.matcher(content);
					start = misp;
					found = 0;
					spannable = s;
					// while ((found >= 0)&&(start<content.length() - 6)) {
					while (match.find(start)) {
						found = match.start();
						start = match.end();

						spannable.setSpan(
								new BackgroundColorSpan(Color
										.parseColor("#000000")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
						/*
						 * spannable.setSpan( new BackgroundColorSpan(
						 * backgroundColor), found,
						 * start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						 */
						spannable.setSpan(
								new ForegroundColorSpan(Color
										.parseColor("#008b8b")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						if (found > masp)
							break;
					}// Integers

					match = edit.rem.matcher(content);
					start = misp;
					found = 0;
					spannable = s;
					// while ((found >= 0)&&(start<content.length() - 6)) {
					while (match.find(start)) {
						found = match.start();
						start = match.end();
						spannable.setSpan(
								new BackgroundColorSpan(Color
										.parseColor("#000000")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
						spannable.setSpan(
								new ForegroundColorSpan(Color
										.parseColor("#0000FF")), found, start,
								Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						if (found > masp)
							break;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				startpo = start;
				countpo = count;

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				changed = true;
			}

		});
	}

	public void save() {
		try {
			OutputStream out = new FileOutputStream(res);
			byte[] buf = edit.getText().toString().getBytes();
			out.write(buf, 0, buf.length);
			out.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void onBackPressed() {
		if (changed) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Back to 3D Structure View");
			adb.setMessage("Save changes (if any) to " + res);
			adb.setCancelable(true);
			adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					save();
					Intent returnIntent = getIntent();
					returnIntent.putExtra("result", res);
					setResult(RESULT_OK, returnIntent);
					finish();
				}
			});

			adb.setNegativeButton("No", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					Intent returnIntent = getIntent();
					returnIntent.putExtra("result", res);
					setResult(RESULT_OK, returnIntent);
					finish();
				}
			});

			adb.create();
			adb.show();
		} else {

			Intent returnIntent = getIntent();
			returnIntent.putExtra("result", res);
			setResult(RESULT_OK, returnIntent);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.editor);
		edit = (TextEdit) findViewById(R.id.editText1);
		// edit.setSpans();
		setting = new Properties();
		// ActionBar ab=getActionBar();
		File sdcard = Environment.getExternalStorageDirectory();
		try {
			setting.load(new FileInputStream(sdcard.getAbsolutePath()
					+ "/.ShelXle/config"));
		} catch (IOException e) {// don't care if it don't work to load.
		}
		if (!setting.containsKey("RES")) {
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
		} else {
			// setting.setProperty("RES", res);
			res = setting.getProperty("RES", "");
			load_shx(res);

			changed = false;
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setSubtitle(res);
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == android.app.Activity.RESULT_OK) {
				res = data.getStringExtra("result");
				load_shx(res);
				changed = false;
				if (Build.VERSION.SDK_INT > 10) {
					ActionBar ab = getActionBar();
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
		if (requestCode == 2) {
			if ((resultCode == android.app.Activity.RESULT_OK)
					|| (resultCode == android.app.Activity.RESULT_CANCELED)) {
				res = data.getStringExtra("result");
				load_shx(res);
				changed = false;
				if (Build.VERSION.SDK_INT > 10) {
					ActionBar ab = getActionBar();
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
	}

	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editor_menu, menu);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts
		/*
		 * menu.add(0, OPEN_ID, 0, R.string.OpenResFile).setShortcut('0', 'o');
		 * menu.add(0, REFINE_ID, 0, R.string.RefineXL).setShortcut('1', 'r');
		 * menu.add(0, QUIT_ID, 0, R.string.Quit).setShortcut('2', 'q');
		 */

		return true;
	}

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.
		menu.findItem(R.id.high2).setChecked(globalVariables.doHighLight);
		menu.findItem(R.id.refineMenu2).setVisible(
				(!res.isEmpty()) && (new File(res).exists()));
		if (res != null) {
			String lst = res.replaceAll("(.res)|(.ins)$", ".lst");
			File lstf = new File(lst);
			menu.findItem(R.id.lstMenu2).setVisible(lstf.exists());
		}
		return true;
	}

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.updUnit: {
			// Log.d("===SHELXLE==)",globalVariables.unitNeu);

			if (globalVariables.unitNeu.isEmpty()
					|| (!globalVariables.unitNeu.startsWith("UNIT"))) {
				Toast.makeText(
						getBaseContext(),
						"Please go back to graphical view, to calulate the "
								+ "numbers of atoms in the uni cell, fist and try again.",
						Toast.LENGTH_LONG).show();
				
				return true;
			}
			Spannable sp = edit.getEditableText();
			Pattern unit = Pattern.compile("(?<=\n)(UNIT[^\n]*)",
					Pattern.CASE_INSENSITIVE);
			Matcher m = unit.matcher(sp);
			String after = m.replaceAll(globalVariables.unitNeu);
			edit.setText(after);
			if (globalVariables.doHighLight)
				edit.highLight();
			// Log.d("===SHELXLE===",after);
			return true;
		}
		case R.id.updWght: {
			Spannable sp = edit.getEditableText();
			Pattern wght = Pattern.compile("(?<=\n)(WGHT[^\n]*)",
					Pattern.CASE_INSENSITIVE);
			Pattern end = Pattern.compile("(?<=\n)(END)",
					Pattern.CASE_INSENSITIVE);
			Matcher e = end.matcher(sp);
			int st = 0;
			if (e.find())
				st = e.end();
			Matcher m = wght.matcher(sp);
			if (m.find(st)) {
				String newWght = m.group();
				String after = m.replaceFirst(newWght);
				edit.setText(after);
				if (globalVariables.doHighLight)
					edit.highLight();
			}
			return true;
		}
		case R.id.backMenu2: {
			if (changed) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle("Back to 3D Structure View");
				adb.setMessage("Save changes (if any) to " + res);
				adb.setCancelable(true);
				adb.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								Intent returnIntent = getIntent();
								returnIntent.putExtra("result", res);
								setResult(RESULT_OK, returnIntent);
								save();
								finish();
							}
						});

				adb.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								Intent returnIntent = getIntent();
								returnIntent.putExtra("result", res);
								setResult(RESULT_OK, returnIntent);
								finish();
							}
						});

				adb.create();
				adb.show();
			} else {
				Intent returnIntent = getIntent();
				returnIntent.putExtra("result", res);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
			return true;
		}
		case R.id.high2: {
			boolean b = item.isChecked();
			item.setChecked(!b);
			globalVariables.doHighLight = !b;
			load_shx(res);
			return true;
		}
		case R.id.openMenu2: {
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
		case R.id.lstMenu2: {
			final Intent launchIntent = new Intent(this, LSTFileView.class);
			this.startActivity(launchIntent);
			return true;
		}
		case R.id.refineMenu2: {
			if (changed) {
				save();
			}
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
