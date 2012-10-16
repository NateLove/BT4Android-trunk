package com.love.apps.BT4U;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Updates extends SherlockFragment 
{
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private BTUpdateArrayAdapter theAdapter;

	 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);

		final View v = inflater.inflate(R.layout.activity_btupdates, container, false);
		ListView lv = (ListView)v.findViewById(R.id.btupdates_list);
		theAdapter = new BTUpdateArrayAdapter(this.getActivity().getBaseContext(),android.R.layout.simple_list_item_1);	
		lv.setAdapter(theAdapter);
		UpdatesGetter updates_getter = new UpdatesGetter();
		updates_getter.execute("http://www.blacksburg.gov/rss.aspx?type=5&cat=17&paramtime=Current");
		return v;
	}
	
	class UpdatesGetter extends AsyncTask<String, Integer, String>{

		 
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
		
		 
		protected void onPostExecute(String result) {
			resultsSoFar = resultsSoFar + result;
			
			if (result!=null && result.contains("</rss>"))
			{
				
				try{
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					DefaultHandler handler = new DefaultHandler(){
						String[] data = {"","","",""};
						
						int index = -1;
						
						public void startElement(String uri, String localName,String qName, 
				                org.xml.sax.Attributes attributes) throws SAXException {
							if (qName.equals("item"))
							{
								data = new String[4];
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
							else if (qName.equals("link"))
							{
								index = 3;
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
								for (int i=0; i<data.length;i++)
								{
									data[i] = data[i].replace("&nbsp;", " ");
								}
								Updates.this.theAdapter.add(new NewsItem(data[0],data[1],data[2],data[3]));
								Log.d("Parser","Added a newsitem");
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
			Log.i("Updates.java", string);
		}
	}
	public class NewsItem
	{
		public String title;
		public String pubDate;
		public String description;
		public String link;
		public NewsItem(String title, String pubDate, String description, String link)
		{
			this.title = title;
			this.pubDate = pubDate;
			this.description = description;
			this.link = link;
		}
	}
	public class BTUpdateArrayAdapter extends BaseAdapter
	{

		ArrayList<NewsItem> items;
		long id;
		public BTUpdateArrayAdapter(Context context, int textViewResourceId) {
			super();
			
			items = new ArrayList<NewsItem>();
			id = textViewResourceId;
		}
		
		public void add(NewsItem ni)
		{
			items.add(ni);
			Log.d("Adapter","size: "+items.size());
			this.notifyDataSetChanged();
		}

		 
		public int getCount() {
			Log.d("Adapter","getCount: "+items.size());
			return items.size();
		}

		 
		public Object getItem(int arg0) {
			return items.get(arg0);
		}

		 
		public long getItemId(int position) {
			return id;
		}

		 
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
			{
				TextView theView = new TextView(Updates.this.getActivity().getBaseContext());
				theView.setTextColor(0xff330000);
				theView.setText(items.get(position).title);
				theView.setMinHeight(50);
				final int index = position;
				theView.setOnClickListener(new OnClickListener(){

					 
					public void onClick(View view) {
						AlertDialog.Builder builder = new AlertDialog.Builder(Updates.this.getActivity());
						builder.setTitle(items.get(index).title)
								.setMessage(items.get(index).description)
								.setPositiveButton("Read on BT's site", new DialogInterface.OnClickListener(){

									 
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(items.get(index).link));
										Updates.this.getActivity().startActivity(intent);
									}
									
								})
								.setNegativeButton("Close", new DialogInterface.OnClickListener(){

									 
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
									
								});
						builder.show();
					}
					
				});
				return theView;
			}
			else
			{
				((TextView)convertView).setText(items.get(position).title);
				return convertView;
			}
		}
	}
	private final int settingsId = 1;
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		MenuItem addFavItem = menu.add(Menu.NONE, settingsId, settingsId,
				"Settings");
		addFavItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case settingsId:
			Intent i = new Intent(this.getActivity(), SettingsActivity.class);
			startActivity(i);
			return true;
		}
		return false;
	}

}
