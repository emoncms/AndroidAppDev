package com.example.emoncmsapp3;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	EditText apiKeyET;
	Button apiKeyDoneButton; 
	SharedPreferences prefs;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	JSONArray currentFeeds; //Array to store all feed data in while app is running

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();		//Overrides thread policy to allow single thread
		StrictMode.setThreadPolicy(policy);		//Remove this from production code and put in seperate thread

		prefs = this.getSharedPreferences("AppPreferences", 0); //Create shared preferences
		String apiKey = prefs.getString("api_key", null);  //Create an API Key pair value

		if(apiKey == null) {			//If an API key isn't set - run set API key activity to enter one
			Log.i(MainActivity.class.getName(), "API KEY NOT SET");
			setContentView(R.layout.set_api_key);
			apiKeyET = (EditText) findViewById(R.id.apiKeyEditText);
			apiKeyDoneButton = (Button) findViewById(R.id.apiKeyDoneButton);
			setButtonOnClickListeners();
		}
		else {
			Log.i(MainActivity.class.getName(), "API Key : "+apiKey); 			
			startup(); //If API key is stored, run the main activity
		}				
	}
	
	public void startup() { 
		
				
		String feeds = getFeeds("http://emoncms.org/feed/list.json"); //Get JSON of all feeds
		saveFile("feeds.json", feeds);	//Save the JSON data on the phone's internal memory
		
		try {
			currentFeeds = new JSONArray(feeds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
        
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* Set listener action for when the done button is pressed */
	private void setButtonOnClickListeners() {
		apiKeyDoneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor = prefs.edit();  //Make shared preferences editable			
				editor.putString("api_key", apiKeyET.getText().toString()); //Set key-pair value for api_key to user entered string
				editor.commit(); //Commit changes
				Log.i(MainActivity.class.getName(), "API KEY SET : "+prefs.getString("api_key", null));
				startup();  //Run startup method
			}			
		});
		
	}
		
	public String getFeeds(String url) {
		// Get the list of all available feeds + their values
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		String apiKey = prefs.getString("api_key", null);
		String result = null;
		
		HttpGet httpGet = new HttpGet(url +"&apikey="+apiKey);
		Log.i(MainActivity.class.getName(), "API URL :"+url+"&apikey="+apiKey);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;

				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				
				result = builder.toString();
			} else {
				Log.e(MainActivity.class.toString(), "Failed to download JSON");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void saveFile(String filename, String json) {
		/* Save File to Internal Memory */
		FileOutputStream outputStream;

		try {
		  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
		  outputStream.write(json.getBytes());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}
		Log.i(MainActivity.class.getName(), "saveFile() : "+filename);
	}
	
	public JSONArray getCurrentFeeds() {
		return currentFeeds;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			String feedValue = null;
			try {
				JSONObject feed = currentFeeds.getJSONObject(position);
				feedValue = feed.getString("value");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.i(MainActivity.class.getName(), "FEED VALUE : "+feedValue); 			

			
			args.putString(DummySectionFragment.ARG_SECTION_NUMBER, feedValue);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {			
			return currentFeeds.length(); //Return total number of inputs in array
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();					
			try {
				JSONObject feed = currentFeeds.getJSONObject(position);
				String feedName = feed.getString("name");
				return feedName.toUpperCase(l);		
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;			
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		
		
		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
			dummyTextView.setText((getArguments().getString(ARG_SECTION_NUMBER)));
			
			
			

			
			
			
			
			
			return rootView;
		}
	}

	
}
