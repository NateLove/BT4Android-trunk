package com.love.apps.BT4U;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
	static Map<Integer, FavoriteStop> favorites_ = new HashMap<Integer, FavoriteStop>();// holds
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
		favorites_.put(0, new FavoriteStop());

		this.getActivity()
				.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		boolean isSDpresent = android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDpresent) {
			updateFavorites(getActivity());
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
	private void showDialog(FavoriteStop s) {
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
			final Map<Integer, FavoriteStop> favorites, final int index) {
		AlertDialog.Builder alert = new AlertDialog.Builder(
				currActivity.getActivity());
		alert.setTitle("Remove Favorite");
		alert.setMessage("would you like to remove this stop?");

		// Set an EditText view to get user input
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// String value = input.getText().toString();
				// Do something with value!
				favorites.remove(index);
				Object[] keys = favorites.keySet().toArray();
				String fileData = "";
				for (int i = 0; i < favorites.size(); i++) {
					FavoriteStop temp = favorites.get(keys[i]);
					fileData += temp.name + "," + temp.shortRoute + ","
							+ temp.location + "," + temp.stopCode + "\n";
				}

				File storage = getOrCreateFavoritesStorage(getActivity());
				if (storage == null) {
					Toast.makeText(getActivity(),
							"Unable to load favorites. Is your SD card busy?",
							Toast.LENGTH_LONG).show();
					return;
				}

				/*
				 * FileWriter writer = null; try { writer = new
				 * FileWriter(storage); writer.write(fileData); } catch
				 * (IOException e) { e.printStackTrace(); } finally { try {
				 * writer.close(); } catch (IOException e) {
				 * e.printStackTrace(); } }
				 */

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(storage);
					fos.write(fileData.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				updateFavorites(getActivity());
				updateAdapter(currActivity.getActivity());
				makeToast("Stop was removed");
			}

		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});

		return alert;
	}

	/**
	 *
	 * @return null if an error occurs, such as the SD card not being present.
	 *         Otherwise returns a write-ready file that can be wrappered with
	 *         something like {@link FileOutputStream}
	 */
	// Due to a bug in Froyo (see
	// https://groups.google.com/forum/?fromgroups=#!topic/android-developers/to1AsfE-Et8)
	// the app files (e.g. favorites list) will be deleted when a user updates
	// the application. It's hard to tell what devices this affects, but it
	// will not affect pushing this application version out, because we are
	// moving from the old (non proper) storage scheme. Version after this may
	// encounter the issue.
	public static File getOrCreateFavoritesStorage(Context c) {
		File directory = c.getExternalFilesDir(null);
		if (directory == null) {
			log("External storage not available");
			return null;
		}
		File favs = new File(directory, "favorites.txt");
		try {
			favs.createNewFile();
		} catch (IOException e) {
			log("Unable to create new favorite file");
			e.printStackTrace();
		}
		return favs;
	}

	// class for holding stop information
	// refreshes the items list
	private static void refreshItems() {
		if (favorites_.isEmpty())
			return;
		Items = new String[favorites_.size()];
		for (int i = 0; i < favorites_.size(); i++) {
			FavoriteStop temp = favorites_.get(i);
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
	public void updateFavorites(Context c) {
		log("Updating favorites");

		try {
			File favs = getOrCreateFavoritesStorage(c);
			if (favs == null) {
				log("Unable to update favorites");
				return;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(favs)));

			String strLine = "";

			// Read File Line By Line
			int i = 0;
			favorites_.clear();
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				// String[] allWords;
				StringTokenizer st = new StringTokenizer(strLine, ",");
				FavoriteStop stop = new FavoriteStop();
				stop.name = st.nextToken();
				stop.shortRoute = st.nextToken();
				stop.location = st.nextToken();
				stop.stopCode = st.nextToken();
				favorites_.put(i, stop);
				i++;
			}
			br.close();
			refreshItems();

		} catch (FileNotFoundException fnf) {
			log("Favorites storage file not found");
		} catch (Exception e) {
			log("Unknown exception when updating favorites: " + e.getMessage());
			e.printStackTrace();
		}

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

			///New Stuff Here

			List<Arrival> AdjustedDepartureTimes = new ArrayList<Arrival>();

			 BufferedReader in = null;
		        try {
		            HttpClient client = new DefaultHttpClient();
		            //HttpGet request = new HttpGet();
		            HttpPost request = new HttpPost();
		            request.setURI(new URI("http://www.bt4u.org/webservices/bt4u_webservice.asmx/GetNextDepartures"));

		            ArrayList<NameValuePair> params1 = new ArrayList<NameValuePair>();
		            params1.add(new BasicNameValuePair("routeShortName", routeCode));
		            params1.add(new BasicNameValuePair("stopCode", (String) params[1]));
		            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params1);
		            request.setEntity(formEntity);
		            HttpResponse response = client.execute(request);
		            in = new BufferedReader
		            (new InputStreamReader(response.getEntity().getContent()));
		            StringBuffer sb = new StringBuffer("");
		            String line = "";
		            String NL = System.getProperty("line.separator");
		            while ((line = in.readLine()) != null) {
		                sb.append(line + NL);
		            }
		            in.close();
		            String page = sb.toString();
		            System.out.println("HERE IS Time Getter Response\n\n" + page);

		            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		            DocumentBuilder builder = factory.newDocumentBuilder();

		            InputSource is = new InputSource(new StringReader(page));
		            Document doc = builder.parse(is);

		            NodeList currentRoutesRunning = doc.getElementsByTagName("NextDepartures");
		            log("Number of nodes in TimeGetter: " + currentRoutesRunning.getLength());
		            for (int i = 0; i < currentRoutesRunning.getLength(); ++i)
		            {

		            	//log("Node Value: " + ((Element) currentRoutesRunning.item(i)).getElementsByTagName("RouteShortName").item(0).getNodeValue());
		                Element currentRoute = (Element) currentRoutesRunning.item(i);
		                //String labTestType = currentRoute.getAttribute("RouteShortName");

		                Element AdjustedDepartureTime = (Element) (currentRoute.getElementsByTagName("AdjustedDepartureTime").item(0));
		                String AdjustedDepartureTimeString = AdjustedDepartureTime.getFirstChild().getNodeValue();

		                Arrival tempArrival = new Arrival(AdjustedDepartureTimeString);
		                AdjustedDepartureTimes.add(tempArrival);



		            }
		            } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} finally {
		            if (in != null) {
		                try {
		                    in.close();
		                    } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            }
		        }
			//END
			if (AdjustedDepartureTimes.size() == 0)
				return new ArrayList<Arrival>();

			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(Favorites.this.getActivity());
			int timesToShow = sharedPref.getInt("times", 5);

			int i = 0;
			List<Arrival> result = new ArrayList<Arrival>();
			for (Arrival arrival : AdjustedDepartureTimes) {
				if (i++ > timesToShow)
					break;

				Arrival a = arrival;
				result.add(a);
			}

			return result;
		}

		@Override
		protected void onPostExecute(List<Arrival> stops) {

			try {

				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(Favorites.this
								.getActivity());
				int times2Show = sharedPref.getInt("times", 5);

				StringBuffer buffer = new StringBuffer("");
				int i = 0;
				for (Arrival a : stops) {
					if (i++ > times2Show)
						break;

					buffer.append(a.getTime()).append(" - ")
							.append(a.timeUntil());
					if (a.note.length() != 0)
						buffer.append(" *").append(a.note);
					buffer.append("\n");
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

	static class FavoriteStop {
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

	public static void addStopToFavorites(Context c, String routeName,
			String routeCode, String stopName, String stopCode) {

		File favs = Favorites.getOrCreateFavoritesStorage(c);
		if (favs == null) {
			Toast.makeText(c,
					"Unable to access favorites. Is your SD card available?",
					Toast.LENGTH_LONG).show();
			return;
		}

		String yourdata = routeName + "," + routeCode + "," + stopName + ","
				+ stopCode + "\n";

		BufferedOutputStream output;
		try {
			output = new BufferedOutputStream(new FileOutputStream(favs, true));
			output.write(yourdata.getBytes());
			output.close();

		} catch (FileNotFoundException e) {
			log("Unable to find the favorites");
			e.printStackTrace();
			Toast.makeText(c, "Error: Unable to access favorites",
					Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			log("Unable to write favorites");
			e.printStackTrace();
			Toast.makeText(c, "Error: Unable to access favorites",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Toast.makeText(c, "Saved to Favorites", Toast.LENGTH_SHORT).show();

	}

	public static void checkForOldFavorites(Context c) {
		boolean isSDpresent = android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDpresent) {

			String path = Environment.getExternalStorageDirectory() + "/BT4U/";
			File root = new File(path);
			File f = new File(root, "favorites.txt");
			if (!f.exists()) {
				log("Old favorites not found");
				return;
			}

			log("Old Favorite file found, migrating contents");

			// Copy old favorites content
			String content = "";
			FileReader r;
			try {
				r = new FileReader(f);

				c.openFileOutput("favorites.txt", Context.MODE_APPEND);
				BufferedReader br = new BufferedReader(r);
				String line = null;
				while ((line = br.readLine()) != null) {
					content += line;
					content += "\n";
				}
				br.close();
			} catch (FileNotFoundException e) {
				log("Unable to load the old favorites file. Aborting");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				log("Unable to read the old favorites file. Aborting");
				e.printStackTrace();
				return;
			}

			// Write new file before deleting old one
			File favs = getOrCreateFavoritesStorage(c);
			if (favs == null) {
				log("Unable to get a handle on new favorites file, aborting");
				return;
			}
			BufferedOutputStream output;
			try {
				output = new BufferedOutputStream(new FileOutputStream(favs));
				output.write(content.getBytes());
				output.close();
			} catch (FileNotFoundException e) {
				log("Unable to load the new favorites file. Aborting");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				log("Unable to write the new favorites file. Aborting");
				e.printStackTrace();
				return;
			}
			log("Migration of contents successful");

			log("Deleting old favorites.txt: " + f.delete());
			log("Deleting BT4U folder: " + root.delete());
		}
	}

}
