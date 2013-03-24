package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
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
	// public int timesToShow;
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

			}
		});
		return v;
	}

	// adds actual names for bus routes
	private void setUpRoutes() {
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
		routes_.put("TE - The Explorer", "TE");
		routes_.put("CRCH - Corporate Research Center/Hospital", "CRCH");

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
		routeCodeToName.put("TE", "The Explorer");
		routeCodeToName.put("CRCH", "Corporate Research Center/Hospital");


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

	class RouteGetter extends AsyncTask<Void, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Void... params) {
			List<String> currentRoutes = new ArrayList<String>();

			 BufferedReader in = null;
		        try {
		            HttpClient client = new DefaultHttpClient();
		            HttpGet request = new HttpGet();
		            request.setURI(new URI("http://www.bt4u.org/webservices/bt4u_webservice.asmx/GetCurrentRoutes"));
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
		            System.out.println(page);

		            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		            DocumentBuilder builder = factory.newDocumentBuilder();

		            InputSource is = new InputSource(new StringReader(page));
		            Document doc = builder.parse(is);

		            NodeList currentRoutesRunning = doc.getElementsByTagName("CurrentRoutes");
		            log("Number of nodes: " + currentRoutesRunning.getLength());
		            for (int i = 0; i < currentRoutesRunning.getLength(); ++i)
		            {

		            	//log("Node Value: " + ((Element) currentRoutesRunning.item(i)).getElementsByTagName("RouteShortName").item(0).getNodeValue());
		                Element currentRoute = (Element) currentRoutesRunning.item(i);
		                //String labTestType = currentRoute.getAttribute("RouteShortName");

		                Element routeShortName = (Element) (currentRoute.getElementsByTagName("RouteShortName").item(0));
		                log("Node Value: " + routeShortName.getFirstChild().getNodeValue());
		                currentRoutes.add(routeShortName.getFirstChild().getNodeValue());


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
				return currentRoutes;
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
			List<ScheduledStop> currentStops = new ArrayList<ScheduledStop>();

			 BufferedReader in = null;
		        try {
		            HttpClient client = new DefaultHttpClient();
		            //HttpGet request = new HttpGet();
		            HttpPost request = new HttpPost();
		            request.setURI(new URI("http://www.bt4u.org/webservices/bt4u_webservice.asmx/GetScheduledStopNames"));

		            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		            params.add(new BasicNameValuePair("routeShortName", routeCode[0]));
		            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
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
		            System.out.println("HERE IS STOPGETTER RESPONSE\n\n" + page);

		            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		            DocumentBuilder builder = factory.newDocumentBuilder();

		            InputSource is = new InputSource(new StringReader(page));
		            Document doc = builder.parse(is);

		            NodeList currentRoutesRunning = doc.getElementsByTagName("ScheduledStops");
		            log("Number of nodes in StopGetter: " + currentRoutesRunning.getLength());
		            for (int i = 0; i < currentRoutesRunning.getLength(); ++i)
		            {

		            	//log("Node Value: " + ((Element) currentRoutesRunning.item(i)).getElementsByTagName("RouteShortName").item(0).getNodeValue());
		                Element currentRoute = (Element) currentRoutesRunning.item(i);
		                //String labTestType = currentRoute.getAttribute("RouteShortName");

		                Element stopName = (Element) (currentRoute.getElementsByTagName("StopName").item(0));
		                Element stopCode = (Element) (currentRoute.getElementsByTagName("StopCode").item(0));
		                log( "StopName: " + stopName + " StopCode: " + stopCode);
		                String stopNameString = stopName.getFirstChild().getNodeValue();
		                String stopCodeString = stopCode.getFirstChild().getNodeValue();

		                ScheduledStop newStop = new ScheduledStop();
		                newStop.setStopCode(Integer.parseInt(stopCodeString));
		                newStop.setStopName(stopNameString);
		                currentStops.add(newStop);


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
				return currentStops;
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
					.getDefaultSharedPreferences(Routes.this.getActivity());
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
			log("Adding stop to favorites");
			if (RouteInfoSpinner.getSelectedItem().equals("---Select---")
					|| StopNameSpinner.getSelectedItem().equals("---Select---")) {
				return true;
			}

			/*
			 * try {
			 *
			 * String content = ""; File favs =
			 * Favorites.getOrCreateFavoritesStorage(getActivity()); if (favs ==
			 * null) { Toast.makeText( getActivity(),
			 * "Unable to access favorites. Is your SD card available?",
			 * Toast.LENGTH_LONG).show(); return true; }
			 *
			 * BufferedReader br = new BufferedReader(new InputStreamReader( new
			 * FileInputStream(favs))); String line = null; while ((line =
			 * br.readLine()) != null) { content += line; content += "\n"; }
			 *
			 * String yourdata = content + RouteInfoSpinner.getSelectedItem() +
			 * "," + routes_.get(RouteInfoSpinner.getSelectedItem() .toString())
			 * + "," + StopNameSpinner.getSelectedItem() + "," +
			 * CurrentStops_[StopNameSpinner .getSelectedItemPosition()];
			 *
			 *
			 *
			 * FileOutputStream fos = Routes.this.getActivity()
			 * .openFileOutput("favorites.txt", Context.MODE_PRIVATE);
			 * fos.write(yourdata.getBytes()); fos.close();
			 * Toast.makeText(this.getActivity(), "Saved to Favorites",
			 * Toast.LENGTH_SHORT).show(); favorites_.updateFavorites(); } catch
			 * (IOException e) { e.printStackTrace(); }
			 */

			Favorites.addStopToFavorites(getActivity(),
					RouteInfoSpinner.getSelectedItem().toString(),
					routes_.get(RouteInfoSpinner.getSelectedItem().toString()),
					StopNameSpinner.getSelectedItem().toString(),
					CurrentStops_[StopNameSpinner.getSelectedItemPosition()]);
			favorites_.updateFavorites(getActivity());

			break;
		case refreshID:
			log("refresh button");
			if (RouteInfoSpinner.getSelectedItem().equals("---Select---")
					|| StopNameSpinner.getSelectedItem().equals("---Select---")) {
				return true;
			}

			String routeCode = routes_.get(RouteInfoSpinner.getSelectedItem()
					.toString());
			String stopCode = CurrentStops_[StopNameSpinner
					.getSelectedItemPosition()];

			TimeGetter tg = new TimeGetter();
			tg.execute(routeCode, stopCode);

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
