package com.love.apps.BT4U.webservice;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.DoubleConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;

/**
 * Holds real-time information on a BTransit bus.  
 * 
 * @author hamiltont
 * 
 */
@XStreamAlias("RTFInfo")
public class Bus {

	/** A unique identifier for this vehicle in this agency (btransit) */
	@XStreamAlias("AgencyVehicleName")
	@XStreamConverter(value=IntConverter.class)
	public int agencyVehicleName;
	
	/** The short (3 or 4 character) code for this route, such as HWD */
	@XStreamAlias("RouteShortName")
	@XStreamConverter(value=StringConverter.class)
	public String routeShortName;
	
	@XStreamAlias("TripStartTime")
	@XStreamConverter(value=StringConverter.class)
	public String tripStartTime;

	@XStreamAlias("TripPointName")
	@XStreamConverter(value=StringConverter.class)
	public String tripPointName;
	
	@XStreamAlias("StopCode")
	@XStreamConverter(value=IntConverter.class)
	public int stopCode;

	@XStreamAlias("IsTimePoint")
	@XStreamConverter(value=StringConverter.class)
	public String isTimePoint;
	
	@XStreamAlias("LatestEvent")
	@XStreamConverter(value=StringConverter.class)
	public String latestEvent;
	
	@XStreamAlias("LatestRSAEvent")
	@XStreamConverter(value=StringConverter.class)
	public String latestRSAEvent;
	
	@XStreamAlias("Latitude")
	@XStreamConverter(value=DoubleConverter.class)
	public double latitude;
	
	@XStreamAlias("Longitude")
	@XStreamConverter(value=DoubleConverter.class)
	public double longitude;
	
	@XStreamAlias("Direction")
	@XStreamConverter(value=IntConverter.class)
	public int direction;
	
	@XStreamAlias("Speed")
	@XStreamConverter(value=IntConverter.class)
	public int speed;
	
	@XStreamAlias("TotalCount")
	@XStreamConverter(value=IntConverter.class)
	public int totalCount;
}
