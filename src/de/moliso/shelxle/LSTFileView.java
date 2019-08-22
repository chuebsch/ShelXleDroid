package de.moliso.shelxle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

public class LSTFileView extends Activity {
	TextView mPut = null;
	public String res = "";
	public String lst = "";
	
	private Properties setting = null;
//	private static final int NEW_INPUT = 1;

	TwoDScrollView scrl;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setting = new Properties();
		setContentView(R.layout.listfile);
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
		mPut = (TextView) findViewById(R.id.lstfile);
		mPut.setVerticalScrollBarEnabled(true);
		mPut.setHorizontalScrollBarEnabled(true);
		mPut.setEnabled(true);

        scrl= (TwoDScrollView) findViewById(R.id.twoDScrollView3);
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
		if (setting.containsKey("RES")) {
			// setting.setProperty("RES", res);
			res = setting.getProperty("RES", "");
			lst = res;
            lst = lst.replaceAll("(.res$)|(.ins$)",".lst");
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar ab = getActionBar();
				ab.setSubtitle(lst);
			}

		}

		if (!lst.isEmpty()){
		loadLST();	
		}
                  // */
		/*
		 * // Intent returnIntent = getIntent(); //
		 * returnIntent.putExtra("result", res); // setResult(RESULT_CANCELED,
		 * returnIntent); // finish();
		 */
	}
	public final Pattern Integers = Pattern.compile("(?<=\\s)[-]?[0-9]+(?=\\s)");

	public final Pattern Floats = Pattern.compile("(?<=\\s)[-]?[0-9]+[.][0-9]*(?=\\s)");
	public final Pattern Alerts = Pattern.compile("(?<=\\s)[*]{2,2}.*[*]{2,2}(?=\\s)");
	/*private class HighLight extends AsyncTask<Integer,String,Integer>{
		protected Integer doInBackground(Integer...integers){
			publishProgress("Highlight text...");
			highLight();
			return 1;
		}
		protected void onProgressUpdate(String... progress){
			
		}
		protected void onPostExecute(Integer result){
			
	    
		}
	}*/
	public void loadLST(){
	File file = new File(lst);
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
	mPut.setText("");
    mPut.append(text.toString());
    //new HighLight().execute(1);
    highLight();
	Intent returnIntent = getIntent();
	returnIntent.putExtra("result", res);
	setResult(RESULT_OK, returnIntent);
}
private void highLight(){
	if (!globalVariables.doHighLight)return;
	Spannable spannable = mPut.getEditableText();
	if (spannable!=null){
    String content=spannable.toString();
    Matcher match = Integers.matcher(content);
	 int start = 0;
	 int found = 0;
	while (match.find()) {
		found = match.start();
		start = match.end();
				spannable.setSpan(
				new ForegroundColorSpan(Color.parseColor("#0000FF")),
				found, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

	}//Integers
    match = Floats.matcher(content);
    start = 0;
    found = 0;
	while (match.find()) {
		found = match.start();
		start = match.end();
		spannable.setSpan(
				new ForegroundColorSpan(Color.parseColor("#FF0000")),
				found, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

	}//Floats*/
	match = Alerts.matcher(content);
	start = 0;
    found = 0;
	while (match.find()) {
		found = match.start();
		start = match.end();

		spannable.setSpan( new BackgroundColorSpan(
				  Color.parseColor("#FF0000")), 
				  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
		spannable.setSpan(
				new ForegroundColorSpan(Color.parseColor("#FFFF00")),
				found, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

	}//Alerts*/
	 }
}

}

