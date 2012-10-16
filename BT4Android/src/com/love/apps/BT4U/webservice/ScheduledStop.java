package com.love.apps.BT4U.webservice;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents a single stop that is scheduled to happen sometime later today on
 * a bus route
 * 
 * @author Hamilton Turner
 */
@XStreamAlias("ScheduledStops")
public class ScheduledStop {

	@XStreamAlias("StopCode")
	public int stopCode;

	@XStreamAlias("StopName")
	public String stopName;
	
	public String getStopCode() {
		return Integer.toString(stopCode);
	}
}
