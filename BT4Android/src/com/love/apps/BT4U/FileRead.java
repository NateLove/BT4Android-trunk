package com.love.apps.BT4U;

import java.io.*;
import java.util.*;

import com.love.apps.BT4U.R;



import android.content.res.Resources;

/*
 * This class sets up all the stop codes actual stop names from a csv file
 * for easier route clarification
 */
public class FileRead {
	public Map<String, String> stops_ = new HashMap<String, String>();

	//reads all the stop codes and route names into the map
	public void readFromFile(Resources myResource) 
	{
		
		try {
			// Open the file that is the first
			// command line parameter
			

			InputStream fstream = myResource.openRawResource(R.raw.stops);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line


			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				//String[] allWords;
				

				StringTokenizer st = new StringTokenizer(strLine, ",");
				st.nextToken();
				stops_.put(st.nextToken(), st.nextToken());
				
				}
				

			
			in.close();

		} catch (Exception e) {// Catch exception if any
			//System.err.println("Error: " + e.getMessage());
		}
	}
	


}
