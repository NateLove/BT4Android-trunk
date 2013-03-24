package com.love.apps.BT4U.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Holds route information in-memory until we can flatten it into a more concise
 * format
 */
class RouteBuilder {

	private String routeName;
	private ArrayList<Point> points = new ArrayList<Point>(50);

	public RouteBuilder(String routeName) {
		this.routeName = routeName;
	}

	public void addPoint(String rank, String latitude, String longitude) {
		Point p = new Point();
		try {
			p.lat = Double.parseDouble(latitude);
			p.lon = Double.parseDouble(longitude);
			p.rank = Integer.parseInt(rank);
		} catch (NumberFormatException nfe) {
			return;
		}

		points.add(p);
	}

	private class Point {
		int rank;
		double lat;
		double lon;
	}

	public Route build() {
		Route r = new Route();
		routeName = routeName.replace("_x0020_", " ");
		routeName = routeName.replace("RSA ", "");
		r.routeName = routeName;

		Collections.sort(points, new Comparator<Point>() {

			
			public int compare(Point lhs, Point rhs) {
				if (lhs.rank < rhs.rank)
					return -1;
				else if (lhs.rank > rhs.rank)
					return 1;
				else
					return 0;
			}
		});

		r.data = new ArrayList<double[]>(points.size());
		for (Point p : points)
			r.data.add(new double[] { p.lat, p.lon });
		
		return r;
	}
}
