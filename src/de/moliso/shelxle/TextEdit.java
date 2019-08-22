package de.moliso.shelxle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
//import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class TextEdit extends EditText {
	public TextEdit(Context context) {
		super(context);
	}

	public TextEdit(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	public final Pattern nl = Pattern.compile("[^\n\r]+[\r\n]");
	
	public final Pattern p = Pattern.compile(
			 "(?<=\n)(ACTA)|(?<=\n)(AFIX)|(?<=\n)(MPLA)|(?<=\n)(ANIS)|(?<=\n)(BASF)|(?<=\n)(BIND)|(?<=\n)(BLOC)|"+
			 "(?<=\n)(BOND)|(?<=\n)(BUMP)|(?<=\n)(CELL)|(?<=\n)(CGLS)|(?<=\n)(CHIV)|(?<=\n)(CONF)|(?<=\n)(CONN)|"+
			 "(?<=\n)(DAMP)|(?<=\n)(DANG)|(?<=\n)(DEFS)|(?<=\n)(DELU)|(?<=\n)(DFIX)|(?<=\n)(DISP)|(?<=\n)(EADP)|"+
			  "(?<=\n)(EGEN)|(?<=\n)(END)|(?<=\n)(EQIV)|(?<=\n)(ESEL)|(?<=\n)(EXTI)|(?<=\n)(EXYZ)|"+
			 "(?<=\n)(FLAT)|(?<=\n)(FMAP)|(?<=\n)(FRAG)|(?<=\n)(FREE)|(?<=\n)(FVAR)|(?<=\n)(GRID)|(?<=\n)(HFIX)|"+
			 "(?<=\n)(HKLF)|(?<=\n)(HOPE)|(?<=\n)(HTAB)|(?<=\n)(INIT)|(?<=\n)(ISOR)|(?<=\n)(LAST)|(?<=\n)(LATT)|"+
			 "(?<=\n)(LAUE)|(?<=\n)(LIST)|(?<=\n)(MERG)|(?<=\n)(MOLE)|(?<=\n)(MORE)|(?<=\n)(MOVE)|(?<=\n)(L\\.S\\.)|"+
			 "(?<=\n)(NCSY)|(?<=\n)(OMIT)|(?<=\n)(PART)|(?<=\n)(PATT)|(?<=\n)(PHAN)|(?<=\n)(PHAS)|(?<=\n)(PLAN)|"+
			 "(?<=\n)(PSEE)|(?<=\n)(RESI)|(?<=\n)(RTAB)|(?<=\n)(SADI)|(?<=\n)(SAME)|(?<=\n)(SFAC)|"+
			 "(?<=\n)(SHEL)|(?<=\n)(SIMU)|(?<=\n)(SIZE)|(?<=\n)(SPEC)|(?<=\n)(SPIN)|(?<=\n)(STIR)|(?<=\n)(SUMP)|"+
			 "(?<=\n)(SWAT)|(?<=\n)(SYMM)|(?<=\n)(TEMP)|(?<=\n)(TEXP)|(?<=\n)(TIME)|"+
			 "(?<=\n)(TWIN)|(?<=\n)(UNIT)|(?<=\n)(VECT)|(?<=\n)(WPDB)|(?<=\n)(WGHT)|(?<=\n)(ZERR)|(?<=\n)(XNPD)|"+
			 "(?<=\n)(REST)|(?<=\n)(CHAN)|(?<=\n)(RIGU)|(?<=\n)(FLAP)|(?<=\n)(RNUM)|(?<=\n)(SOCC)|(?<=\n)(PRIG)|"+
			 "(?<=\n)(WIGL)|(?<=\n)(RANG)|(?<=\n)(TANG)|(?<=\n)(ADDA)|(?<=\n)(STAG)|(?<=\n)(ATOM)|(?<=\n)(HETA)|"+
			 "(?<=\n)(SCAL)|(?<=\n)(ABIN)|(?<=\n)(ANSC)|(?<=\n)(ANSR)|(?<=\n)(NOTR)|(?<=\n)(NEUT)|(?<=\n)(TWST)", Pattern.CASE_INSENSITIVE);
	
	public final Pattern Integers = Pattern.compile("\\b[0-9]{2,}\\.[0-9]+\\b");
	public final Pattern rem = Pattern.compile("REM.*|TITL.*|!.*", Pattern.CASE_INSENSITIVE);
    //public boolean doHighLight=true;
	public void highLight() {
		if (!globalVariables.doHighLight) return;
		String content = getText().toString();

		
		  Matcher match =p.matcher(content); 
		  int start = 0; 
		  int found = 0;
		  Spannable spannable = getText(); 
		  while (match.find()){ 
			  found = match.start(); 
			  start = match.end(); 
			  spannable.setSpan( new BackgroundColorSpan(
					  Color.parseColor("#aaffaa")), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
			  spannable.setSpan( new ForegroundColorSpan(
					  Color.parseColor("#800000")), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		  
		  }
		 		match = Integers.matcher(content);
		 start = 0;
		 found = 0;
		 spannable = getText();
		// while ((found >= 0)&&(start<content.length() - 6)) {
		while (match.find()) {
			found = match.start();
			start = match.end();

			spannable.setSpan( new BackgroundColorSpan(
					  Color.parseColor("#000000")), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
			/*spannable.setSpan( new BackgroundColorSpan(
					  backgroundColor), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);*/
			spannable.setSpan(
					new ForegroundColorSpan(Color.parseColor("#008b8b")),
					found, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		}//Integers

		 match = rem.matcher(content);
		 start = 0;
		 found = 0;
		 spannable = getText();
		// while ((found >= 0)&&(start<content.length() - 6)) {
		while (match.find()) {
			found = match.start();
			start = match.end();

			spannable.setSpan( new BackgroundColorSpan(
					  Color.parseColor("#000000")), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);// */
			/*spannable.setSpan( new BackgroundColorSpan(
					  backgroundColor), 
					  found, start,Spannable.SPAN_INCLUSIVE_INCLUSIVE);*/
			spannable.setSpan(
					new ForegroundColorSpan(Color.parseColor("#0000FF")),
					found, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		}//REM

	}

};;
