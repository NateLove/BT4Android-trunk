package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Favorites extends SherlockListFragment {

	static private String[] Items = null;// used to hold items that are in the
											// list view
	private boolean longClick_ = false; // used to tell the gui if click was
										// long click or short click
	ArrayAdapter<String> adapter_ = null; // holds list for listview
	static Map<Integer, stops> favorites_ = new HashMap<Integer, stops>();// holds
																			// all
																			// data
																			// for
																			// each
																			// favorite
	AlertDialog.Builder alert = null; // pop window
	public int timesToShow;
	public static final String PREFS_NAME = "MyPrefsFile";
	static private Favorites favs = null;

	static public Favorites getFavorites() {
		return favs;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		favs = this;
	}

	static SherlockListFragment currActivity = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		currActivity = this;
		favorites_.put(0, new stops());
		SharedPreferences settings = currActivity.getActivity()
				.getSharedPreferences(PREFS_NAME, 0);
		timesToShow = settings.getInt("timesShown", 5);
		this.getActivity()
				.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		boolean isSDpresent = android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDpresent) {
			updateFavorites();
		}

		if (Items == (null)) {
			Items = new String[1];
			Items[0] = "EMPTY";
		}

		adapter_ = new ArrayAdapter<String>(currActivity.getActivity(),
				R.layout.list_item, Items);
		setListAdapter(adapter_);
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// sets up what happens if items are clicked
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (longClick_) {
					longClick_ = false;
					return;
				}
				if (favorites_.isEmpty())
					return;
				showDialog(favorites_.get(arg2));
				updateAdapter(currActivity.getActivity());
				makeToast("Getting Stop Data");
			}
		});

		// sets up what happens if items are long clicked
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				// Get instance of Vibrator from current Context
				Vibrator v = (Vibrator) currActivity.getActivity()
						.getSystemService(Context.VIBRATOR_SERVICE);
				// Vibrate for 300 milliseconds
				v.vibrate(50);
				longClick_ = true;
				AlertDialog.Builder alert = getRemoveDialog(favorites_, arg2);
				alert.show();
				return false;
			}
		});
	}

	// updates the list adapter if a stop is added or removed
	protected void updateAdapter(Context currActivity) {
		// TODO Auto-generated method stub
		adapter_ = new ArrayAdapter<String>(currActivity, R.layout.list_item,
				Items);
		setListAdapter(adapter_);
		adapter_.notifyDataSetChanged();
	}

	// shows the stop times in new window for giiven stop
	private void showDialog(stops s) {
		if (favorites_.isEmpty())
			return;
		String url = "http://bt4u.org/BT4U_WebService.asmx/GetNextDepartures?routeShortName="
				+ s.shortRoute + "&stopCode=" + s.stopCode;
		Map<String, String> args = new HashMap<String, String>();
		args.put("routeShortName", s.shortRoute);
		args.put("StopCode", s.stopCode);
		alert = new AlertDialog.Builder(currActivity.getActivity());
		TimeGetter tg = new TimeGetter();
		tg.execute(url);
	}

	// puts up toast notification that says message
	public static void makeToast(String message) {
		Toast.makeText(currActivity.getActivity(), message, Toast.LENGTH_SHORT)
				.show();
	}

	// shows remove dialog for given stop
	private AlertDialog.Builder getRemoveDialog(
			final Map<Integer, stops> favorites, final int index) {
		AlertDialog.Builder alert = new AlertDialog.Builder(
				currActivity.getActivity());
		alert.setTitle("Remove Favorite");
		alert.setMessage("would you like to remove this stop" + "?");

		// Set an EditText view to get user input
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// String value = input.getText().toString();
				// Do something with value!
				favorites.remove(index);
				Object[] keys = favorites.keySet().toArray();
				String fileData = "";
				for (int i = 0; i < favorites.size(); i++) {
					stops temp = favorites.get(keys[i]);
					fileData += temp.name + "," + temp.shortRoute + ","
							+ temp.location + "," + temp.stopCode + "\n";
				}
				boolean isSDpresent = android.os.Environment
						.getExternalStorageState().equals(
								android.os.Environment.MEDIA_MOUNTED);
				if (isSDpresent) {
					String path = Environment.getExternalStorageDirectory()
							+ "/BT4U/";
					File root = new File(path);
					root.mkdirs();

					try {
						File f = new File(root, "favorites.txt");
						if (!f.exists()) {
							FileOutputStream fos;
							fos = new FileOutputStream(f);
							fos.close();
						}
						FileWriter writer;
						writer = new FileWriter(f);
						writer.write(fileData);
						writer.close();

						FileOutputStream fos = Favorites.this.getActivity()
								.openFileOutput("favorites.txt",
										Context.MODE_PRIVATE);
						fos.write(fileData.getBytes());
						fos.close();

					} catch (Exception e) {
						e.printStackTrace();
					}

					updateFavorites();
					updateAdapter(currActivity.getActivity());
					makeToast("Stop was removed");
				}
			}

		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});

		return alert;
	}

	// class for holding stop information
	// refreshes the items list
	private static void refreshItems() {
		if (favorites_.isEmpty())
			return;
		Items = new String[favorites_.size()];
		for (int i = 0; i < favorites_.size(); i++) {
			stops temp = favorites_.get(i);
			Items[i] = "Route: " + temp.name + "\nLocation:"
					+ temp.location.split("-")[1] + "\nStop Code: "
					+ temp.stopCode;
		}
		try {
			if (favs != null && currActivity != null)
				favs.setListAdapter(new ArrayAdapter<String>(currActivity
						.getActivity(), R.layout.list_item, Items));
		} catch (Exception e) {// Catch exception if any
			Log.i("Favorites", "null pointer  oh well");
		}
	}

	// updates entire favorites list
	public void updateFavorites() {
		Log.i("Favorites", "updating favorites");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this
					.getActivity().openFileInput("favorites.txt"), "UTF-8"));
			
			String strLine ="";;
			// Read File Line By Line
			int i = 0;
			favorites_.clear();
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				// String[] allWords;
				StringTokenizer st = new StringTokenizer(strLine, ",");
				stops temp = new stops();
				temp.name = st.nextToken();
				temp.shortRoute = st.nextToken();
				temp.location = st.nextToken();
				temp.stopCode = st.nextToken();
				favorites_.put(i, temp);
				i++;
			}
			br.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}

		refreshItems();

	}

	// returns stop times from xml file
	public String printXml(String resp) throws XmlPullParserException,
			IOException, InterruptedException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(resp));
		int eventType = xpp.getEventType();
		String temp = "";
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Favorites.this.getActivity());
		String times2Show = sharedPref.getString("times", "5");
		int i = 0;
		while (i < Integer.parseInt(times2Show)) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				temp += "";
			} else if (eventType == XmlPullParser.START_TAG) {

				String name = xpp.getName();
				if (name.equalsIgnoreCase("patternpointname")) {
					xpp.next();
				}
				if (name.equalsIgnoreCase("adjusteddeparturetime")) {
					xpp.next();
					temp += "\t\t\t" + xpp.getText().split(" ")[1] + " "
							+ xpp.getText().split(" ")[2] + "\n";
					i++;
				}
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				i = Integer.parseInt(times2Show);
			} else if (eventType == XmlPullParser.TEXT) {
				temp += ("");

			}
			eventType = xpp.next();
		}

		if (temp.equals("")) {
			temp = ("There is no more route info for today.\n\nYou should probably start walking.");
		}
		return temp;
	}

	class TimeGetter extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(params[0]);
				HttpResponse response = httpClient.execute(httpRequest);
				InputStream in = response.getEntity().getContent();
				InputStreamReader ir = new InputStreamReader(in);
				BufferedReader bin = new BufferedReader(ir);
				String line = null;
				StringBuffer buff = new StringBuffer();
				while ((line = bin.readLine()) != null) {
					buff.append(line + "\n");
				}
				bin.close();
				return buff.toString();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			String data;

			try {
				data = printXml(result);
				alert.setTitle("Next Stop Times");
				alert.setMessage(data);

				// Set an EditText view to get user input
				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// String value = input.getText().toString();
								// Do something with value!
							}
						});
				alert.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static class stops {
		public String name = null;
		public String location = null;
		public String stopCode = null;
		public String shortRoute = null;

	}

	private final int settingsId = 1;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		MenuItem addFavItem = menu.add(Menu.NONE, settingsId, settingsId,
				"Settings");
		addFavItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case settingsId:
			Intent i = new Intent(this.getActivity(), SettingsActivity.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter_ = new ArrayAdapter<String>(this.getActivity(),
				R.layout.list_item, Items);
		setListAdapter(adapter_);
		Log.i("BT4android.Favorites", "OnResume");
	}
}
