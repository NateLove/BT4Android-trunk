package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.love.apps.BT4U.webservice.BT4U;
import com.love.apps.BT4U.webservice.ScheduledDeparture;
import com.love.apps.BT4U.webservice.ScheduledStop;
import com.love.qsort.MyQsort;

public class Routes extends SherlockFragment {

	private static Spinner RouteInfoSpinner = null;// bus names
	private static Spinner StopNameSpinner = null;// bus stops
	private ArrayAdapter<String> adapter_ = null;
	private String array_spinner[];
	ArrayAdapter<String> adapter2_ = null;
	private String[] CurrentStops_ = null;
	private Map<String, String> routes_ = new HashMap<String, String>();
	private Map<String, String> routeCodeToName = new HashMap<String, String>();
	public static final String PREFS_NAME = "MyPrefsFile";
	//public int timesToShow;
	private volatile ProgressDialog pd;
	static boolean isLoggingEnabled = true;
	private Favorites favorites_ = new Favorites();
	private ArrivalsAdapter arrivals_list_adapter;
	private String[] routesActual = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		final View v = inflater.inflate(R.layout.activity_routes, container,
				false);
		log("Entering onCreateView");
		pd = ProgressDialog.show(getActivity(), "",
				"Loading Routes. Please wait...", true);

		// set up gui elements
		arrivals_list_adapter = new ArrivalsAdapter(this.getActivity());
		ListView lv = (ListView) v.findViewById(R.id.arrivals_list);
		lv.setAdapter(arrivals_list_adapter);

		RouteInfoSpinner = (Spinner) v.findViewById(R.id.spinner1);
		StopNameSpinner = (Spinner) v.findViewById(R.id.spinner2);
		RouteInfoSpinner.setBackgroundResource(R.drawable.back);
		StopNameSpinner.setBackgroundResource(R.drawable.back);

		array_spinner = new String[15];

	

		StopNameSpinner.setClickable(false);
		FileRead reader = new FileRead();
		Resources myResource = getResources();
		reader.readFromFile(myResource);
		setUpRoutes();
		RouteGetter rg = new RouteGetter();
		rg.execute();

		// sets up what happens if item is selected in spinner
		RouteInfoSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						if (RouteInfoSpinner.getSelectedItem().equals(
								"---Select---")) {
							StopNameSpinner.setClickable(false);
							String[] routes = new String[10];
							routes[0] = "---Select---";
							adapter2_ = new ArrayAdapter<String>(
									v.getContext(),
									android.R.layout.simple_spinner_item,
									routes);
							adapter2_
									.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

							StopNameSpinner.setAdapter(adapter2_);
							StopNameSpinner.setSelection(0);

							return;
						} else {
							pd = ProgressDialog.show(getActivity(), "",
									"Loading Stops. Please wait...", true);
							String routeCode = routes_.get(RouteInfoSpinner
									.getSelectedItem().toString());
							StopGetter sg = new StopGetter();
							log(routeCode);
							sg.execute(routeCode);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		// sets up what happens if item is selected in spinner2
		StopNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (!RouteInfoSpinner.getSelectedItem().equals("---Select---")) {
					StopNameSpinner.setClickable(true);
					if (!StopNameSpinner.getSelectedItem().equals(
							"---Select---")) {
						refreshItem.setVisible(true);
						addFavItem.setVisible(true);
						String routeName = null;
						if ((routeName = routes_.get(RouteInfoSpinner
								.getSelectedItem().toString())) == null)
							routeName = RouteInfoSpinner.getSelectedItem()
									.toString();
						String url = "http://bt4u.org/BT4U_WebService.asmx/GetNextDepartures?routeShortName="
								+ routeName
								+ "&stopCode="
								+ CurrentStops_[StopNameSpinner
										.getSelectedItemPosition()];// BT4U_WebService.asmx/GetNextDepartures?routeShortName=" + array_spinner[spinner_.getSelectedItemPosition()] +  "&stopCode="
																	// +
																	// CurrentStops_[spinner2_.getSelectedItemPosition()];
																	// +
																	// spinner_.getSelectedItem();
						Map<String, String> args = new HashMap<String, String>();
						args.put("routeShortName",
								array_spinner[RouteInfoSpinner
										.getSelectedItemPosition()]);
						args.put("StopCode", CurrentStops_[StopNameSpinner
								.getSelectedItemPosition()]);

						String routeCode = array_spinner[RouteInfoSpinner
								.getSelectedItemPosition()];
						String stopCode = CurrentStops_[StopNameSpinner
								.getSelectedItemPosition()];

						TimeGetter tg = new TimeGetter();
						tg.execute(routeCode, stopCode);
						return;
					} else if (StopNameSpinner.getSelectedItem().equals(
							"---Select---")) {
						refreshItem.setVisible(false);
						addFavItem.setVisible(false);
					}
				}
				if (RouteInfoSpinner.getSelectedItem().equals("---Select---")) {
					StopNameSpinner.setClickable(false);
					if (refreshItem != null)
						refreshItem.setVisible(false);
					if (addFavItem != null)
						addFavItem.setVisible(false);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		return v;
	}

	// adds actual names for bus routes
	private void setUpRoutes() {
		// TODO Auto-generated method stub
		routes_.put("---Select---", "1");
		routes_.put("MSN - Main Street Northbound", "MSN");
		routes_.put("MSS - Main Street Southbound", "MSS");
		routes_.put("HDG - Harding Ave", "HDG");
		routes_.put("HWD - Hethwood", "HWD");
		routes_.put("TTT - Two Town Trolley", "TTT");
		routes_.put("UMS - University Mall Shuttle", "UMS");
		routes_.put("PRG - Progress Street", "PRG");
		routes_.put("PH - Patrick Henry", "PH");
		routes_.put("HXP - Hokie Express", "HXP");
		routes_.put("TC - Toms Creek", "TC");
		routes_.put("CRC - Corporate Research Center", "CRC");
		routes_.put("UCB - University City Boulevard", "UCB");
		routes_.put("BTC - BT Commuter Service", "BTC");
		routes_.put("TE - Tuesday Route", "TE");

		routeCodeToName.put("MSN", "Main Street Northbound");
		routeCodeToName.put("MSS", "Main Street Southbound");
		routeCodeToName.put("HDG", "Harding Ave");
		routeCodeToName.put("HWD", "Hethwood");
		routeCodeToName.put("TTT", "Two Town Trolley");
		routeCodeToName.put("UMS", "University Mall Shuttle");
		routeCodeToName.put("PRG", "Progress Street");
		routeCodeToName.put("PH", "Patrick Henry");
		routeCodeToName.put("HXP", "Hokie Express");
		routeCodeToName.put("TC", "Toms Creek");
		routeCodeToName.put("CRC", "Corporate Research Center");
		routeCodeToName.put("UCB", "University City Boulevard");
		routeCodeToName.put("BTC", "BT Commuter Service");
		routeCodeToName.put("TE", "Tuesday Route");

	}

	private static void log(String message) {
		if (isLoggingEnabled)
			Log.i("BT4Android.route_info", message);
	}

	public String[] getStops(String Route, String xml)
			throws XmlPullParserException, IOException {

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(xml));
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();
				if (name.equalsIgnoreCase("patternpointname")) {
					xpp.next();
				}
			}
		}
		eventType = xpp.next();
		return array_spinner;
	}

	// async class that is used to process all data from httputils in background
	// threads
	class PrintXml extends AsyncTask<HttpResponse, Void, String> {

		@Override
		protected String doInBackground(HttpResponse... params) {
			String data = null;
			try {
				InputStream in = params[0].getEntity().getContent();
				InputStreamReader ir = new InputStreamReader(in);
				BufferedReader bin = new BufferedReader(ir);
				String line = null;
				StringBuffer buff = new StringBuffer();
				while ((line = bin.readLine()) != null) {
					buff.append(line + "\n");
				}
				bin.close();
				//data = printXml(buff.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {

			Routes.this.arrivals_list_adapter.notifyDataSetChanged();
		}

	}


	class RouteGetter extends AsyncTask<Void, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Void... params) {
			return BT4U.getService().getActiveRouteCodes();
		}

		@Override
		protected void onPostExecute(List<String> results) {

			log("Entering onPostExecute");

			if (results == null || results.size() == 0) {
				log("There were no route results");
				String[] routesActual = new String[routes_.size()];
				routes_.keySet().toArray(routesActual);
				MyQsort.qsort(routesActual, 0, routesActual.length - 1, 0);
				adapter_ = new ArrayAdapter<String>(Routes.this.getActivity(),
						android.R.layout.simple_spinner_item, routesActual);
				adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				RouteInfoSpinner.setAdapter(adapter_);
				if (pd != null)
					pd.dismiss();
				return;
			} else {
				array_spinner = new String[results.size() + 1];
				log("There are " + results.size() + " active routes");
				array_spinner[0] = "---Select---";
				routesActual = new String[results.size() + 1];
				routesActual[0] = "---Select---";
				for (int i = 0; i < results.size(); i++) {

					String routeCode = results.get(i);
					array_spinner[i + 1] = routeCode;
					if (routeCodeToName.get(routeCode) == null)
						routesActual[i + 1] = routeCode;
					else
						routesActual[i + 1] = routeCode + " - "
								+ routeCodeToName.get(routeCode);
				}
				if (array_spinner.length == 1) {
					if (pd != null)
						pd.dismiss();
					log(array_spinner.length + " ");

				} else {
					if (pd != null)
						pd.dismiss();
					adapter_ = new ArrayAdapter<String>(
							Routes.this.getActivity(),
							android.R.layout.simple_spinner_item, routesActual);
					adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					RouteInfoSpinner.setAdapter(adapter_);
				}
				if (pd != null)
					pd.dismiss();
			}
		}
	}

	class StopGetter extends AsyncTask<String, Void, List<ScheduledStop>> {

		@Override
		protected List<ScheduledStop> doInBackground(String... routeCode) {
			return BT4U.getService().getScheduledStopsForRoute(routeCode[0]);
		}

		@Override
		protected void onPostExecute(List<ScheduledStop> stops) {
			pd.dismiss();

			if (stops.size() == 0) {
				StopNameSpinner.setClickable(false);
				return;
			}

			CurrentStops_ = null;
			CurrentStops_ = new String[stops.size() + 1];
			String[] routes = new String[stops.size() + 1];
			CurrentStops_[0] = "---Select---";
			routes[0] = "---Select---";
			for (int i = 0; i < stops.size(); i++) {
				ScheduledStop s = stops.get(i);
				CurrentStops_[i + 1] = s.getStopCode();
				routes[i + 1] = CurrentStops_[i + 1] + " - " + s.stopName;
			}

			MyQsort.qsort(CurrentStops_, 0, CurrentStops_.length - 1, 0);
			MyQsort.qsort(routes, 0, routes.length - 1, 0);
			adapter2_ = new ArrayAdapter<String>(Routes.this.getActivity(),
					android.R.layout.simple_spinner_item, routes);
			adapter2_
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			StopNameSpinner.setAdapter(adapter2_);
			StopNameSpinner.setSelected(false);

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

			List<ScheduledDeparture> departures = BT4U.getService()
					.getScheduledDeparturesFromStop(routeCode, stopCode);

			if (departures.size() == 0)
				return new ArrayList<Arrival>();

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Routes.this.getActivity());
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
			// TODO if we're using a listView (which is much better than manual
			// text formatting), we need to include a subclass of Arrival that
			// allows us to draw an error message on the screen, such as
			// ArrivalError
			arrivals_list_adapter.clear();
			for (Arrival a : stops)
				arrivals_list_adapter.add(a);
			arrivals_list_adapter.notifyDataSetChanged();
		}
	}

	private static MenuItem refreshItem = null;
	private static MenuItem addFavItem = null;
	final int addID = 0, refreshID = 1, aboutID = 2, settingsID = 4;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		addFavItem = menu.add(Menu.NONE, addID, addID, "Favorites");
		addFavItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		addFavItem.setIcon(android.R.drawable.ic_menu_add);
		refreshItem = menu.add(Menu.NONE, refreshID, refreshID, "Refresh");
		refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refreshItem.setIcon(android.R.drawable.ic_popup_sync);
		MenuItem settingsItem = menu.add(Menu.NONE, settingsID, settingsID,
				"Settings");
		settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		checkMenu();
	}

	public static void checkMenu() {
		log("checking menu");
		if (addFavItem == null || refreshItem == null)
			return;
		if (StopNameSpinner == (null)) {
			addFavItem.setVisible(false);
			refreshItem.setVisible(false);
		} else if (StopNameSpinner.getSelectedItem() == null) {
			addFavItem.setVisible(false);
			refreshItem.setVisible(false);

		} else if (StopNameSpinner.getSelectedItem().equals("---Select---")) {
			addFavItem.setVisible(false);
			refreshItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case addID:
			log("add button");
			if (RouteInfoSpinner.getSelectedItem().equals("---Select---")
					|| StopNameSpinner.getSelectedItem().equals("---Select---")) {
				return true;
			}

			try {

				String content = "";
				BufferedReader br = new BufferedReader(new InputStreamReader(
						Routes.this.getActivity()
								.openFileInput("favorites.txt"), "UTF-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					content += line;
					content += "\n";
				}

				String yourdata = content
						+ RouteInfoSpinner.getSelectedItem()
						+ ","
						+ routes_.get(RouteInfoSpinner.getSelectedItem()
								.toString())
						+ ","
						+ StopNameSpinner.getSelectedItem()
						+ ","
						+ CurrentStops_[StopNameSpinner
								.getSelectedItemPosition()];

				FileOutputStream fos = Routes.this.getActivity()
						.openFileOutput("favorites.txt", Context.MODE_PRIVATE);
				fos.write(yourdata.getBytes());
				fos.close();
				Toast.makeText(this.getActivity(), "Saved to Favorites",
						Toast.LENGTH_SHORT).show();
				favorites_.updateFavorites();
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		case refreshID:
			log("refresh button");
			if (RouteInfoSpinner.getSelectedItem().equals("---Select---")
					|| StopNameSpinner.getSelectedItem().equals("---Select---")) {
				return true;
			}
			String url = "http://bt4u.org/BT4U_WebService.asmx/GetNextDepartures?routeShortName="
					+ routes_
							.get(RouteInfoSpinner.getSelectedItem().toString())
					+ "&stopCode="
					+ CurrentStops_[StopNameSpinner.getSelectedItemPosition()];// BT4U_WebService.asmx/GetNextDepartures?routeShortName=" + array_spinner[spinner_.getSelectedItemPosition()] +  "&stopCode="
																				// +
																				// CurrentStops_[spinner2_.getSelectedItemPosition()];
																				// +
																				// spinner_.getSelectedItem();

			Map<String, String> args = new HashMap<String, String>();
			args.put("routeShortName",
					routes_.get(RouteInfoSpinner.getSelectedItem().toString()));
			args.put("StopCode",
					CurrentStops_[StopNameSpinner.getSelectedItemPosition()]);
			TimeGetter tg = new TimeGetter();
			tg.execute(url);
			break;

		case settingsID:
			log("settings button");
			Intent i = new Intent(this.getActivity(), SettingsActivity.class);
			startActivity(i);
			break;
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		log("OnStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		log("OnResume");
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs,
			Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);
		log("onInflate");
	}
	
	
}
