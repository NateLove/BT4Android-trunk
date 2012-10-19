package com.love.apps.BT4U.webservice;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Represents a single departure that is expected to happen later today
 * 
 * @author Hamilton Turner
 * 
 */
@SuppressWarnings("unused")
@XStreamAlias("NextDepartures")
public class ScheduledDeparture {
	/*
	 * Full XML: <RouteShortName>HWD</RouteShortName> <PatternPointName>Burruss
	 * Hall</PatternPointName> <AdjustedDepartureTime>9/30/2012 6:43:16
	 * PM</AdjustedDepartureTime>
	 * <ServiceLevelID>f78b077b-8185-4f47-b7fa-1bca1905113e</ServiceLevelID>
	 * <ServiceLevelName>Full Service</ServiceLevelName>
	 * <BlockID>a4ff1c0d-0d35-4ed2-8048-6b92ca184265</BlockID>
	 * <TripID>51b023be-4011-49f9-8421-28e133ad2be0</TripID>
	 * <TripStartTime>1/1/1980 6:15:00 PM</TripStartTime> <TripNotes>Last
	 * Departure from Stop</TripNotes>
	 */

	@XStreamAlias("AdjustedDepartureTime")
	public String departureTime;

	@XStreamAlias("ServiceLevelName")
	public String serviceLevel;

	/**
	 * Typically a note indicating this is the last bus, or about a delay. There
	 * are more than 1 bus on some routes, so there can be multiple `last
	 * departure` notes for single route/stop/day combination
	 */
	@XStreamAlias("TripNotes")
	public String tripNotes;

	/*
	 * All of the below are intended to be ignored, I can't get XStream to
	 * ignore the unknown XML
	 */

	private String RouteShortName;
	private String PatternPointName;
	private String ServiceLevelID;
	private String BlockID;
	private String TripID;
	private String TripPointID;
	private String TripStartTime;

	protected void cleanup() {
		RouteShortName = null;
		PatternPointName = null;
		ServiceLevelID = null;
		BlockID = null;
		TripID = null;
		TripPointID = null;
		TripStartTime = null;
	}

}
