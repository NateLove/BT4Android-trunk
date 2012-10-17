package com.love.apps.BT4U;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Arrival {

	Date arrivalTime;
	String note = "";

	public Arrival(String time) {
		note = "";
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("M/d/y h:m:s a");

		try {
			arrivalTime = sdf.parse(time);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return human-readable description of time remaining until stop
	 */
	public String timeUntil() {
		long msuntil = arrivalTime.getTime() - System.currentTimeMillis();
		long seconds = (msuntil / (1000)) % 60;
		long hours = msuntil / (60 * 60 * 1000);
		long minutes = (msuntil / (60 * 1000)) % 60;
		return (hours > 0 ? hours + "h" : "")
				+ " "
				+ (hours == 0 && minutes == 0 && seconds < 30 ? "Now"
						: (minutes >= 1 ? minutes + "m" : "1m"));
	}

	public void setNote(String text) {
		note = text;
	}

	/** Returns exact stop time, in the format "hour:minute" */
	public String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("hh:mm");
		return sdf.format(arrivalTime);
	}
}
