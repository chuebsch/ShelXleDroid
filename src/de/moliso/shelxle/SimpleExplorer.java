package de.moliso.shelxle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
//import java.io.FileDescriptor;
//import android.util.Log;

public class SimpleExplorer extends ListActivity {
	private class Item {
		String item;
		String path;

		public Item(String it, String pt) {
			item = it;
			path = pt;
		}
	}

	private List<Item> pice;
	private String root = "/";
	private TextView myPath;
	private Properties setting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myPath = (TextView) findViewById(R.id.path);
		setting = new Properties();
		File sdcard = Environment.getExternalStorageDirectory();
		try {
			setting.load(new FileInputStream(sdcard.getAbsolutePath()
					+ "/.ShelXle/config"));
		} catch (IOException e) {// don't care if it don't work to load.
		}
		if (setting.containsKey("LASTDIR")) {
			getDir(setting.getProperty("LASTDIR"));
		} else
			getDir(sdcard.getAbsolutePath());
	}

	private void getDir(String dirPath) {
		myPath.setText("Location: " + dirPath);
		pice = new ArrayList<Item>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (!dirPath.equals(root)) {
			pice.add(new Item(root, root));
			pice.add(new Item("../", f.getParent()));
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				pice.add(new Item(file.getName().concat("/"), file.getPath()));
			} else if ((file.getName().endsWith(".res"))
					|| (file.getName().endsWith(".ins"))) {
				Date lastModDate = new Date(file.lastModified());
				String size = String.valueOf(file.length() / 1024) + "kB";
				pice.add(new Item(String.format("%-30s %12s %20s",
						file.getName(), lastModDate.toString(), size), file
						.getPath()));
			}
		}
		Collections.sort(pice, new Comparator<Item>() {
			@Override
			public int compare(Item i1, Item i2) {
				if (i1.item == "/")
					return -900000;
				if (i2.item == "/")
					return 900000;
				if ((i1.item.contains("/") && (!i2.item.contains("/"))))
					return -1000+i1.item.compareToIgnoreCase(i2.item);
				else if ((i2.item.contains("/") && (!i1.item.contains("/"))))
					return 1000-i1.item.compareToIgnoreCase(i2.item);
				return i1.item.compareToIgnoreCase(i2.item);
			}

		});
		ArrayList<String> item = new ArrayList<String>();
		for (int i = 0; i < pice.size(); i++)
			item.add(pice.get(i).item);
		MyCustomAdapter fileList = new MyCustomAdapter();
		fileList.setData(item);
		setListAdapter(fileList);

	}

	private class MyCustomAdapter extends BaseAdapter {

		private ArrayList<String> mData = new ArrayList<String>();
		private LayoutInflater mInflater;

		public MyCustomAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(ArrayList<String> data) {
			mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public String getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//System.out.println("getView " + position + " " + convertView);
			ViewHolder holder = null;
			//if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row, null);
				holder = new ViewHolder();
				if (mData.get(position).contains("res")) {
					convertView = mInflater.inflate(R.layout.row, null);

					holder.textView = (TextView) convertView
							.findViewById(R.id.rowtext1);
				}else
				if (mData.get(position).contains("/")) {
					convertView = mInflater.inflate(R.layout.dirrow, null);

					holder.textView = (TextView) convertView
							.findViewById(R.id.rowtext3);

				} else {
					convertView = mInflater.inflate(R.layout.altrow, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.rowtext2);
				}
				convertView.setTag(holder);
			//} else {
			//	holder = (ViewHolder) convertView.getTag();
			//}
			holder.textView.setText(mData.get(position));
			return convertView;
		}

	}

	public static class ViewHolder {
		public TextView textView;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(pice.get(position).path);
		if (file.isDirectory()) {
			if (file.canRead()) {
				try {
					setting.setProperty("LASTDIR", pice.get(position).path);
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
				getDir(pice.get(position).path);
			} else {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle(
								"[" + file.getName()
										+ "] folder can't be read!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		} else {
			// resultFile=file.getAbsolutePath();
			Intent returnIntent = getIntent();
			returnIntent.putExtra("result", file.getAbsolutePath());
			File sdcard = Environment.getExternalStorageDirectory();
			try {
				setting.store(new FileOutputStream(sdcard.getAbsolutePath()
						+ "/.ShelXle/config"), null);
			} catch (IOException e) {

			}
			setResult(RESULT_OK, returnIntent);
			finish();
		}
	}
}
