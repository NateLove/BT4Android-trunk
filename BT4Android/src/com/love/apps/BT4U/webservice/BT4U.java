package com.love.apps.BT4U.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Provides an object model for interfacing with the BT4U web service, allowing
 * users to avoid dealing with XML, SOAP, etc and focus instead on requesting
 * and receiving information from the service. All methods are synchronous and
 * will block the calling thread until the request succeeds or times out <br />
 * <br />
 * All methods are guaranteed not to return null. On error empty data containers
 * may be returned, such as empty lists. <br>
 * There are two main time-scales of interest when discussing the webservice:
 * active and real time. As far as I can tell, active time implies any
 * information that's currently in the BT4U database with no filters applied for
 * the precise day, hour, or minute that the API call was made. Real-time, on
 * the other hand, pays attention to the exact time the request was made and
 * returns either filters or updated information<br />
 * <br />
 * <h2>Architecture</h2> Currently all calls do 1) connect to the server 2)
 * buffer the entire response into one big string 3) return that string (in
 * memory) for XML parsing. Obviously this is a relatively memory-intensive
 * process, especially if there are many requests running simultaneously. Future
 * versions could be improved by building and parsing the DOM as it is loaded
 * from the network, perhaps by combining the built-in XML pull parser and
 * Xstream. There are also substantial XML parsing improvements that could be
 * made to make the entire operation lighter and faster, such as actively
 * deciding on Xstream DOM readers and converters<br />
 * <br />
 * Web service located at: http://www.bt4u.org/BT4U_WebService.asmx
 * 
 * 
 * @author Hamilton Turner
 * 
 */
public class BT4U {

	public static BT4U getService() {
		return new BT4U();
	}

	/**
	 * Synchronously contacts the web service and requests the latest
	 * information on all buses currently operating
	 */
	public List<Bus> getRunningBuses() {

		XStream x = new XStream();
		x.processAnnotations(Bus.class);
		x.alias("DocumentElement", BusCollection.class);
		x.addImplicitArray(BusCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetCurrentBusInfo");
		return ((BusCollection) x.fromXML(xml)).data;
	}

	static private class BusCollection {
		public ArrayList<Bus> data = new ArrayList<Bus>();
	}

	/**
	 * Synchronously contacts the web service and gets all route codes (e.g. HWD
	 * for Hethwood) for currently active routes
	 */
	public List<String> getActiveRouteCodes() {

		XStream x = new XStream();
		x.alias("DocumentElement", RouteCollection.class);
		x.processAnnotations(CurrentRoute.class);
		x.addImplicitArray(RouteCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetCurrentRoutes");
		ArrayList<String> names = new ArrayList<String>();
		for (CurrentRoute route : ((RouteCollection) x.fromXML(xml)).data)
			names.add(route.name);

		return names;
	}

	@XStreamAlias("CurrentRoutes")
	private static class CurrentRoute {
		@XStreamAlias("RouteShortName")
		String name;
	}

	private static class RouteCollection {
		ArrayList<CurrentRoute> data = new ArrayList<BT4U.CurrentRoute>();
	}

	/**
	 * Given the short name of a route, such as "HWD", this will return all
	 * stops that are scheduled on that route. <br />
	 * <br />
	 * Behavior is not consistent if a) that route is not currently running, or
	 * b) the provided short code does not exist. In some cases an empty list
	 * will be returned, and in others a full list will be returned. It seems as
	 * though the intended behavior is to always return stop codes if the
	 * provided route is running during this time of year (e.g. perhaps not
	 * immediately running, but running sometime this week), and return no stop
	 * codes if the provided route doesn't exist in the active database
	 */
	public List<ScheduledStop> getScheduledStopsForRoute(String routeCode) {
		XStream x = new XStream();
		x.processAnnotations(ScheduledStop.class);
		x.alias("DocumentElement", StopCollection.class);
		x.addImplicitArray(StopCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetScheduledStopCodes?routeShortName="
				+ routeCode);

		return ((StopCollection) x.fromXML(xml)).data;
	}

	private static class StopCollection {
		ArrayList<ScheduledStop> data  = new ArrayList<ScheduledStop>();
	}

	/**
	 * For a single stop on a single route, get all scheduled departures from
	 * now until the end of service today. I do not know if this is simply a
	 * relevant subset of the route timetable, or if this is a collection of
	 * updated predictions based on the current location / time of the bus. I
	 * suspect the latter.
	 * 
	 * @param routeShortName
	 *            The 2-3 character short name for a route, e.g. HWD
	 * @param routeStopCode
	 *            The stop code for a specific stop e.g. 1103
	 * @return Empty list if the provided route code / name do not exist in the
	 *         active database
	 */
	public List<ScheduledDeparture> getScheduledDeparturesFromStop(
			String routeShortName, int routeStopCode) {
		XStream x = new XStream();
		x.processAnnotations(ScheduledDeparture.class);
		x.alias("DocumentElement", DepartureCollection.class);
		x.addImplicitArray(DepartureCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetNextDepartures?routeShortName="
				+ routeShortName + "&stopCode=" + routeStopCode);
		List<ScheduledDeparture> departures = ((DepartureCollection) x
				.fromXML(xml)).data;
		for (ScheduledDeparture sd : departures)
			sd.cleanup();
		return departures;
	}

	private static class DepartureCollection {
		ArrayList<ScheduledDeparture> data = new ArrayList<ScheduledDeparture>();
	}

	/**
	 * Synchronously downloads the real-time route information for all running
	 * buses.
	 * 
	 * @return an empty list if there is any error
	 */
	public List<Route> getAllActiveRoutePaths() {
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			XmlPullParser xpp = factory.newPullParser();

			String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetScheduledPatternPoints?routeName=HWD");

			HashMap<String, RouteBuilder> builders = new HashMap<String, RouteBuilder>();

			xpp.setInput(new StringReader(xml));
			int eventType = xpp.getEventType();
			RouteBuilder currentBuilder = null;
			boolean in_rank = false, in_lat = false, in_lon = false;
			String rank = "", lat = "", lon = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String tag = xpp.getName();
					if (tag.startsWith("RSA")) {
						RouteBuilder builder = builders.get(tag);
						if (builder == null) {
							builder = new RouteBuilder(tag);
							builders.put(tag, builder);
						}
						currentBuilder = builder;
					} else if (tag.equals("Rank"))
						in_rank = true;
					else if (tag.equals("Latitude"))
						in_lat = true;
					else if (tag.equals("Longitude"))
						in_lon = true;
				} else if (eventType == XmlPullParser.END_TAG) {
					String tag = xpp.getName();

					if (tag.startsWith("RSA")) {
						currentBuilder.addPoint(rank, lat, lon);
						currentBuilder = null;
						rank = "";
						lat = "";
						lon = "";
					} else if (tag.equals("Rank"))
						in_rank = false;
					else if (tag.equals("Latitude"))
						in_lat = false;
					else if (tag.equals("Longitude"))
						in_lon = false;
				} else if (eventType == XmlPullParser.TEXT) {
					if (in_lat)
						lat = xpp.getText();
					else if (in_lon)
						lon = xpp.getText();
					else if (in_rank)
						rank = xpp.getText();
				}
				eventType = xpp.next();
			}

			List<Route> routes = new ArrayList<Route>(builders.size());
			for (RouteBuilder rb : builders.values())
				routes.add(rb.build());

			return routes;

		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return new ArrayList<Route>();
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Route>();
		}
	}

	/**
	 * Given a specific stop code, this informs you which routes operate on this
	 * stop code. This is not filtered to include routes running only in
	 * real-time, but includes all active routes for a given stop id
	 * 
	 * @param stop
	 *            The ID of that stop, such at 1103
	 * @return
	 */
	public List<String> getRouteCodesForStop(int stop) {
		XStream x = new XStream();
		x.processAnnotations(ScheduledRoute.class);
		x.alias("DocumentElement", ScheduledRouteCollection.class);
		x.addImplicitArray(ScheduledRouteCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetScheduledRoutes?stopCode="
				+ stop);
		ScheduledRouteCollection rc = (ScheduledRouteCollection) x.fromXML(xml);
		ArrayList<String> routes = new ArrayList<String>();
		for (ScheduledRoute r : rc.data)
			routes.add(r.routeCode);

		return routes;
	}

	/**
	 * May actually be performing the same function as
	 * {@link BT4U#getScheduledStopsForRoute(String)}
	 * 
	 * @param route
	 * @return
	 */
	public List<BusStop> getStopsForRoute(String route) {
		XStream x = new XStream();
		x.processAnnotations(BusStop.class);
		x.alias("DocumentElement", BusStopCollection.class);
		x.addImplicitArray(BusStopCollection.class, "data");
		String xml = fetchXML("http://www.bt4u.org/BT4U_WebService.asmx/GetScheduledStopNames?routeShortName="
				+ route);

		return ((BusStopCollection) x.fromXML(xml)).data;
	}

	private static class BusStopCollection {
		List<BusStop> data = new ArrayList<BusStop>();
	}

	private static class ScheduledRouteCollection {
		ArrayList<ScheduledRoute> data = new ArrayList<BT4U.ScheduledRoute>();
	}

	@XStreamAlias("ScheduledRoutes")
	private static class ScheduledRoute {
		@XStreamAlias("RouteShortName")
		String routeCode = "";
	}

	/**
	 * Synchronously downloads the XML from the given web service URL.
	 * 
	 * @param url
	 * @return
	 */
	private String fetchXML(String url) {
		String xml = "";
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpRequest = new HttpGet(url);
			HttpResponse response;
			response = httpClient.execute(httpRequest);
			InputStream in = response.getEntity().getContent();
			InputStreamReader ir = new InputStreamReader(in);
			BufferedReader bin = new BufferedReader(ir);
			String line = null;
			StringBuffer buff = new StringBuffer();
			while ((line = bin.readLine()) != null) {
				buff.append(line).append("\n");
			}
			bin.close();
			xml = buff.toString();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return xml;
	}

	/**
	 * Return a generically-configured future-proof Xstream object. This should
	 * ignore unknown XML elements to protect the parsing code from future
	 * enhancements. Do not use for testing!
	 * 
	 * TODO: Implement and test
	 */
	@SuppressWarnings("unused")
	private static XStream getXStream() {
		XStream xstream = new XStream() {

			@Override
			protected MapperWrapper wrapMapper(MapperWrapper next) {
				return new MapperWrapper(next) {
					@Override
					public boolean shouldSerializeMember(
							@SuppressWarnings("rawtypes") Class definedIn,
							String fieldName) {
						if (definedIn == Object.class) {
							return false;
						}
						return super
								.shouldSerializeMember(definedIn, fieldName);
					}
				};
			}
		};

		return xstream;

	}
}
