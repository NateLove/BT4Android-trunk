package com.love.apps.BT4U;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.love.qsort.MyQsort;

public class Updates extends SherlockFragment 
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private ArrayAdapter<String> theAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		super.onCreateView(inflater, container, savedInstanceState);
		
		final View v = inflater.inflate(R.layout.activity_btupdates, container, false);
		ListView lv = (ListView)v.findViewById(R.id.btupdates_list);
		theAdapter = new ArrayAdapter<String>(this.getActivity().getBaseContext(),android.R.layout.simple_list_item_1);
		lv.setAdapter(theAdapter);
		UpdatesGetter updates_getter = new UpdatesGetter();
		updates_getter.execute("http://www.blacksburg.gov/rss.aspx?type=5&cat=17&paramtime=Current");
		return v;
	}
	
	class UpdatesGetter extends AsyncTask<String, Integer, String>{

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
				while((line = bin.readLine())!=null){
					buff.append(line+"\n");
				}
				bin.close();	
				return buff.toString();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		private String resultsSoFar = "";
		
		@Override
		protected void onPostExecute(String result) {
			resultsSoFar = resultsSoFar + result;
			
			if (result.contains("</rss>"))
			{
				
				try{
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					DefaultHandler handler = new DefaultHandler(){
						String[] data = {"","",""};
						
						int index = -1;
						
						public void startElement(String uri, String localName,String qName, 
				                org.xml.sax.Attributes attributes) throws SAXException {
							if (qName.equals("item"))
							{
								data = new String[3];
								for (int i=0; i<data.length; i++)
								{
									data[i] = "";
								}
								index = -1;
							}
							else if (qName.equals("title"))
							{
								index = 0;
							}
							else if (qName.equals("pubDate"))
							{
								index = 1;
							}
							else if (qName.equals("description"))
							{
								index = 2;
							}
							else 
							{
								index = -1;
							}
						}
						public void endElement(String uri, String localName, String qName) 
						{
							if (qName.equals("item"))
							{
								String message = data[0]+": "+data[2];
								message = message.replace("&nbsp;", " ");
								Updates.this.theAdapter.add(message);
								index = -1;
							}
						}
						public void characters(char[] ch, int start, int length) 
						{
							if (index !=-1)
								data[index] = data[index] + new String(ch,start,length);
						}
					};
					
					saxParser.parse(new ByteArrayInputStream(resultsSoFar.getBytes()), handler);
				}catch(Exception e){e.printStackTrace();};
				
			}
			else
			{
				
			}
		}

		private void log(String string) {
			// TODO Auto-generated method stub
			
		}
	}
}
