package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
import android.os.Environment;
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
import com.love.qsort.MyQsort;

public class Routes extends SherlockFragment {

	private TextView RouteInfoTextView = null;// where times are printed to
	private static Spinner RouteInfoSpinner = null;// bus names
	private static Spinner StopNameSpinner = null;// bus stops
	private ArrayAdapter<String> adapter_ = null;
	private String array_spinner[];
	private Map<String, String> stops_ = new HashMap<String, String>();
	ArrayAdapter<String> adapter2_ = null;
	private String[] CurrentStops_ = null;
	private Map<String, String> routes_ = new HashMap<String, String>();
	private Map<String, String> routesActual_ = new HashMap<String, String>();
	public static final String PREFS_NAME = "MyPrefsFile";
	public int timesToShow;
	private volatile ProgressDialog pd;
	static boolean isLoggingEnabled = true;
	private Favorites favorites_ = new Favorites();
	private ArrivalsAdapter arrivals_list_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		final View v = inflater.inflate(R.layout.activity_routes, container,
				false);
		log("Entering onCreateView");
		pd = ProgressDialog.show(getActivity(), "",
				"Loading Routes. Please wait...", true);

		// set up gui elements
		// RouteInfoTextView= (TextView) v.findViewById(R.id.textView2);
		arrivals_list_adapter = new ArrivalsAdapter(this.getActivity());
		ListView lv = (ListView) v.findViewById(R.id.arrivals_list);
		lv.setAdapter(arrivals_list_adapter);

		RouteInfoSpinner = (Spinner) v.findViewById(R.id.spinner1);
		StopNameSpinner = (Spinner) v.findViewById(R.id.spinner2);
		RouteInfoSpinner.setBackgroundResource(R.drawable.back);
		StopNameSpinner.setBackgroundResource(R.drawable.back);
		// RouteInfoTextView.setMovementMethod(new ScrollingMovementMethod());

		array_spinner = new String[15];

		// Restore preferences
		SharedPreferences settings = getActivity().getSharedPreferences(
				PREFS_NAME, 0);
		timesToShow = settings.getInt("timesShown", 5);
		// pd.show();

		StopNameSpinner.setClickable(false);
		FileRead reader = new FileRead();
		Resources myResource = getResources();
		reader.readFromFile(myResource);
		stops_ = reader.stops_;
		setUpRoutes();
		RouteGetter rg = new RouteGetter();
		rg.execute("http://www.bt4u.org/BT4U_WebService.asmx/GetCurrentRoutes?");

		// sets up what happens if item is selected in spinner
		RouteInfoSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						if (RouteInfoSpinner.getSelectedItem().equals(
								"---Select---")) {
							// RouteInfoTextView.setText("");
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
							String url = ("http://www.bt4u.org/BT4U_WebService.asmx/GetScheduledStopCodes?routeShortName=" + routes_
									.get(RouteInfoSpinner.getSelectedItem()
											.toString()));
							StopGetter sg = new StopGetter();
							log(url);
							sg.execute(url);
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
						// view2_.append("\n" + url + "  " +
						// array_spinner[spinner_.getSelectedItemPosition()] +
						// "\n");
						TimeGetter tg = new TimeGetter();
						tg.execute(url);
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
				// RouteInfoTextView.setText("");
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

		routesActual_.put("MSN", "Main Street Northbound");
		routesActual_.put("MSS", "Main Street Southbound");
		routesActual_.put("HDG", "Harding Ave");
		routesActual_.put("HWD", "Hethwood");
		routesActual_.put("TTT", "Two Town Trolley");
		routesActual_.put("UMS", "University Mall Shuttle");
		routesActual_.put("PRG", "Progress Street");
		routesActual_.put("PH", "Patrick Henry");
		routesActual_.put("HXP", "Hokie Express");
		routesActual_.put("TC", "Toms Creek");
		routesActual_.put("CRC", "Corporate Research Center");
		routesActual_.put("UCB", "University City Boulevard");
		routesActual_.put("BTC", "BT Commuter Service");
		routesActual_.put("TE", "Tuesday Route");

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
			// TODO Auto-generated method stub
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
				data = printXml(buff.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {

			if (RouteInfoTextView != null) {
				RouteInfoTextView.setTextSize(17);
				RouteInfoTextView.setText(result);
			}
			Routes.this.arrivals_list_adapter.notifyDataSetChanged();
		}

		public String printXml(String xml) throws XmlPullParserException,
				IOException, InterruptedException {

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(xml));
			int eventType = xpp.getEventType();
			String temp = "Arrival Times: \n";
			SharedPreferences settings = getActivity().getSharedPreferences(
					PREFS_NAME, 0);
			timesToShow = settings.getInt("timesShown", 5);
			int i = 0;
			while (i < timesToShow) {

				if (eventType == XmlPullParser.START_DOCUMENT) {
					temp += "";
				} else if (eventType == XmlPullParser.START_TAG) {

					String name = xpp.getName();
					if (name.equalsIgnoreCase("adjusteddeparturetime")) {
						xpp.next();
						temp += "\t\t\t" + xpp.getText().split(" ")[1] + " "
								+ xpp.getText().split(" ")[2];
						Log.i("INFO", xpp.getText());

						Arrival arrival = new Arrival(xpp.getText());
						arrivals_list_adapter.add(arrival);
						temp += "\t\t" + arrival.timeUntil();

						temp += "\n";
						i++;
					} else if (name.equalsIgnoreCase("TripNotes")) {
						xpp.next();
						arrivals_list_adapter.last().setNote(xpp.getText());
					}
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
					i = timesToShow;
				} else if (eventType == XmlPullParser.TEXT) {
					temp += ("");
				}
				eventType = xpp.next();
			}
			if (temp.equals("Arrival Times: \n")) {
				temp = ("There is no more route info available for today. \n\nYou should probably start walking.");
			}
			return temp;
		}
	}

	private String[] routesActual = null;

	class RouteGetter extends AsyncTask<String, Integer, String> {

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
			String xml = null;
			log("Entering onPostExecute");
			xml = result;
			Document doc = XMLfunctions.XMLfromString(xml);
			int numResults = XMLfunctions.numResults(doc);
			NodeList nodes = null;
			if (doc == null) {
				log("doc is null");
				log("xml: " + xml);
			} else
				nodes = doc.getElementsByTagName("CurrentRoutes");

			if (nodes == null || nodes.getLength() == 0) {
				log("Nodes are empty");
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
				array_spinner = new String[nodes.getLength() + 1];
				log(nodes.getLength() + " ");
				array_spinner[0] = "---Select---";
				routesActual = new String[nodes.getLength() + 1];
				routesActual[0] = "---Select---";
				for (int i = 0; i < nodes.getLength(); i++) {

					Element e = (Element) nodes.item(i);
					array_spinner[i + 1] = XMLfunctions.getValue(e,
							"RouteShortName");
					log("RouteShortName: " + array_spinner[i + 1]);
					routesActual[i + 1] = array_spinner[i + 1] + " - "
							+ routesActual_.get(array_spinner[i + 1]);
					if (routesActual_.get(array_spinner[i + 1]) == null)
						routesActual[i + 1] = array_spinner[i + 1];

				}
				if (array_spinner.length == 1) {
					if (pd != null)
						pd.dismiss();
					log(array_spinner.length + " ");
					// RouteInfoTextView.setText("There doesn't seem to be any route info available. Please try back later.");

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

	class StopGetter extends AsyncTask<String, Integer, String> {

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
			pd.dismiss();
			Document doc2 = null;
			doc2 = XMLfunctions.XMLfromString(result);
			NodeList nodes2 = doc2.getElementsByTagName("ScheduledStops");
			if (nodes2.getLength() == 0) {
				// Routes.this.RouteInfoTextView.setTextSize(15);
				// Routes.this.RouteInfoTextView.setText("There are no more times available for this route.");
				StopNameSpinner.setClickable(false);
				return;
			}
			CurrentStops_ = null;
			CurrentStops_ = new String[nodes2.getLength() + 1];
			String[] routes = new String[nodes2.getLength() + 1];
			CurrentStops_[0] = "---Select---";
			routes[0] = "---Select---";
			for (int i = 0; i < nodes2.getLength(); i++) {

				Element e2 = (Element) nodes2.item(i);
				log("Test " + nodes2.item(i).getNodeValue());
				CurrentStops_[i + 1] = XMLfunctions.getValue(e2, "StopCode");
				routes[i + 1] = CurrentStops_[i + 1] + " - "
						+ XMLfunctions.getValue(e2, "StopName");
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

	class TimeGetter extends AsyncTask<String, Integer, HttpResponse> {

		@Override
		protected HttpResponse doInBackground(String... params) {
			for (String param : params) {
				log("params " + param);
			}
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(params[0]);
				HttpResponse response = httpClient.execute(httpRequest);
				return response;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			PrintXml printer = new PrintXml();
			arrivals_list_adapter.clear();
			printer.execute(result);

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
