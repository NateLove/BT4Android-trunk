package com.love.apps.BT4U.webservice;

import java.util.ArrayList;

/**
 * Defines the current route that a bus is taking.
 * 
 * @author Hamilton Turner
 * 
 */
public class Route {
	
	protected Route() {}
	
	/** A human-readable, non-standard name for the route such as 'University City via Progress' */ 
	public String routeName;
	
	/**
	 * The latitude/longitude of each route stop point. For each data item,
	 * index 0 is latitude and index 1 is longitude
	 */
	public ArrayList<double[]> data;
}
