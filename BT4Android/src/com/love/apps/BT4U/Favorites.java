package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import com.love.apps.BT4U.webservice.BT4U;
import com.love.apps.BT4U.webservice.ScheduledDeparture;

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
		adapter_ = new ArrayAdapter<String>(currActivity, R.layout.list_item,
				Items);
		setListAdapter(adapter_);
		adapter_.notifyDataSetChanged();
	}

	// shows the stop times in new window for giiven stop
	private void showDialog(stops s) {
		if (favorites_.isEmpty())
			return;
		
		alert = new AlertDialog.Builder(currActivity.getActivity());
		TimeGetter tg = new TimeGetter();
		tg.execute(s.shortRoute, s.stopCode);
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
	
	class TimeGetter extends AsyncTask<Object, Void, List<Arrival>> {

		/**
		 * Our @link TimeGetter requires two parameters. params[0] should be the
		 * route code, such as HWD, and params[1] should be the stop code, such
		 * as 1103 (as either a String or Integer)
		 */
		@Override
		protected List<Arrival> doInBackground(Object... params) {
			String routeCode = (String) params[0];
			int stopCode = -1;
			try {
				stopCode = Integer.parseInt((String) params[1]);
			} catch (NumberFormatException nfe) {
				log("TimeGetter was passed " + params[1]
						+ ", which could not be parsed to an int");
				return new ArrayList<Arrival>();
			}

			List<ScheduledDeparture> departures = BT4U.getService()
					.getScheduledDeparturesFromStop(routeCode, stopCode);

			if (departures.size() == 0)
				return new ArrayList<Arrival>();

			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(Favorites.this.getActivity());
			int timesToShow = sharedPref.getInt("times", 5);

			int i = 0;
			List<Arrival> result = new ArrayList<Arrival>();
			for (ScheduledDeparture departure : departures) {
				if (i++ > timesToShow)
					break;

				Arrival a = new Arrival(departure.departureTime);
				result.add(a);
			}

			return result;
		}

		@Override
		protected void onPostExecute(List<Arrival> stops) {

			try {
				
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Favorites.this.getActivity());
				int times2Show = sharedPref.getInt("times", 5);
				
				StringBuffer buffer = new StringBuffer("");
				int i = 0;
				for (Arrival a : stops) {
					if (i++ > times2Show) 
						break;
					
					buffer.append(a.timeUntil()).append(" ").append(a.note);
				}
								
				alert.setTitle("Next Stop Times");
				alert.setMessage(buffer.toString());

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
		log("OnResume");
	}
	
	private static void log(String message) {
		Log.i("Favorites", message);
	}

}
