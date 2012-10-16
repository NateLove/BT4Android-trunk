package com.love.apps.BT4U;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	public int timesToShow;
	public int defaultTab;
	public static final String PREFS_NAME = "MyPrefsFile";
	private int selection;

	// handles what happens when activity is started
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
//		getListView().setBackgroundColor(Color.TRANSPARENT);
//
//		getListView().setCacheColorHint(Color.TRANSPARENT);
//
//		getListView().setBackgroundColor(Color.rgb(4, 26, 55));

		// setContentView(R.layout.settings);
		BT4Android.getTracker().trackPageView("/settings");
		BT4Android.getTracker().dispatch();
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		timesToShow = settings.getInt("timesShown", 5);
		defaultTab = settings.getInt("defaultTab", 0);
		//
		// TextView tabs = (TextView) findViewById(R.id.default_tab);
		// TextView times = (TextView) findViewById(R.id.time_settings);
		// TextView about = (TextView) findViewById(R.id.about_settings);
		//
		// tabs.setClickable(true);
		// times.setClickable(true);
		// about.setClickable(true);
		//
		// tabs.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// refreshTabs();
		// }
		// });
		// times.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// refreshTimes();
		// }
		// });
		// about.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// Intent i = new Intent(getApplicationContext(), aboutMe.class);
		// startActivity(i);
		// }
		// });

	}

	public void refreshTimes() {
		final String[] items = { "1", "3", "5", "All" };

		String current = Integer.toString(timesToShow);
		if (current.equals("200"))
			current = "All";
		// makeToast("Current: " + current);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Times To Display:");
		int selected = 0;
		if (timesToShow == 1)
			selected = 0;
		if (timesToShow == 3)
			selected = 1;
		if (timesToShow == 5)
			selected = 2;
		if (timesToShow == 200)
			selected = 3;
		builder.setSingleChoiceItems(items, selected,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						// makeToast(Integer.toString(timesToShow));
						if (item == 3)
							selection = 200;
						else
							selection = Integer.parseInt(items[item]);
					}

				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// String value = input.getText().toString();
						// Do something with value!

					}
				});
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				timesToShow = selection;
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("timesShown", timesToShow);

				// Commit the edits!
				editor.commit();

			}
		});
		builder.show();

	}

	public void refreshTabs() {
		final String[] items = { "Routes", "Favorites" };

		// makeToast("Current: " + current);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Default Tab:");

		builder.setSingleChoiceItems(items, defaultTab,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						// makeToast(Integer.toString(item));
						selection = item;
					}

				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// String value = input.getText().toString();
						// Do something with value!

					}
				});
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				defaultTab = selection;
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("defaultTab", defaultTab);

				// Commit the edits!
				editor.commit();

			}
		});
		builder.show();

	}

	public void makeToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

}
