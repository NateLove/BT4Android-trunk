package com.love.apps.BT4U;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ArrivalsAdapter extends BaseAdapter
{

	ArrayList<Arrival> arrivals;
	long id;
	Context m_context;
	public ArrivalsAdapter(Context context) {
		super();
		arrivals = new ArrayList<Arrival>();
		m_context = context;
	}
	
	public void add(Arrival ni)
	{
		arrivals.add(ni);
		
	}

	public int getCount() {
		
		return arrivals.size();
	}

	public Object getItem(int arg0) {
		return arrivals.get(arg0);
	}

	public long getItemId(int position) {
		return id;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		//if (convertView == null)
		{
			if (m_context == null) Log.d("DEBUG","context is null");
			if (m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)==null)Log.d("DEBUG","li is null");
			if (((LayoutInflater) (m_context.getSystemService( Context.LAYOUT_INFLATER_SERVICE ))).inflate(R.layout.arrival_listitem, null)==null)Log.d("DEBUG","layout is null"); 
			View theView = ((LayoutInflater) (m_context.getSystemService( Context.LAYOUT_INFLATER_SERVICE ))).inflate(R.layout.arrival_listitem, null);
			TextView time = (TextView)theView.findViewById(R.id.arrival_time);
			TextView wait = (TextView)theView.findViewById(R.id.arrival_wait);
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("h:mm:ss a");
			time.setText(sdf.format(arrivals.get(position).arrivalTime)+(arrivals.get(position).note.equals("")?"":" Click for more info"));
			if (!arrivals.get(position).note.equals(""))
			{
				final int index = position;
				theView.setOnClickListener(new OnClickListener(){
	
					public void onClick(View arg0) {
						
						AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
						builder.setMessage(arrivals.get(index).note)
								.setTitle("Trip Note");
						
						builder.show();
					}
					
				});
			}
			wait.setText(arrivals.get(position).timeUntil());
			
			return theView;
		}
//		else
//		{
//			TextView time = (TextView)convertView.findViewById(R.id.arrival_time);
//			TextView wait = (TextView)convertView.findViewById(R.id.arrival_wait);
//			SimpleDateFormat sdf = new SimpleDateFormat();
//			sdf.applyPattern("h:mm:ss a");
//			time.setText(sdf.format(arrivals.get(position).arrivalTime)+(arrivals.get(position).note.equals("")?"":" Click for more info"));
//			wait.setText(arrivals.get(position).timeUntil());
//			return convertView;
//		}
	}

	public void clear() {
		arrivals.clear();
		
	}

	public Arrival last() {
		return arrivals.get(arrivals.size()-1);
	}
	
}