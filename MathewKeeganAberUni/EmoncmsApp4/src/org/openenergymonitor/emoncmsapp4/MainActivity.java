package org.openenergymonitor.emoncmsapp4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.openenergymonitor.emoncmsapp4.FeedIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.Toast;

public class MainActivity extends FragmentActivity {


	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	SharedPreferences prefs;
	EditText apiKeyET; 
	EditText serverUrlET;
	Button apiKeyDoneButton; 
	private ArrayList<Feed> feedsArr;
	private ResponseReceiver receiver;
	String serviceResponse;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		/*Register Broadcast Receiver*/
		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);




		prefs = this.getSharedPreferences("AppPreferences", 0); //Create shared preferences
		String apiKey = prefs.getString("api_key", null);  //Create an API Key pair value

		if(apiKey == null) {			//If an API key isn't set - run set API key activity to enter one
			Log.i(MainActivity.class.getName(), "API Key not set"); //Log Message
			setContentView(R.layout.set_api_key);
			apiKeyET = (EditText) findViewById(R.id.apiKeyEditText);
			serverUrlET = (EditText) findViewById(R.id.serverUrlEditText);
			apiKeyDoneButton = (Button) findViewById(R.id.apiKeyDoneButton);
			setButtonOnClickListeners();
		}
		else { 
			Log.i(MainActivity.class.getName(), "API Key : "+apiKey); //Log Message
			startup(false);
		}		














		//FeedManager fm = new FeedManager(this);

		//fm.getFeedData();
		//fm.saveFile();
		//fm.doesFileExist();
		//fm.loadFile();		
		//fm.printArraylist();
		//fm.updateLiveFeed();
		//fm.printArraylist();
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(receiver);
		super.onDestroy();
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


				String serverUrl = serverUrlET.getText().toString();
				if(serverUrl.matches("")) {
					editor.putString("server_url", "http://emoncms.org"); //If no server specified, use emoncms.org
					Log.i(MainActivity.class.getName(), "In the else statement"); //Log Message
				}
				else {
					editor.putString("server_url", serverUrlET.getText().toString()); //Set key-pair value for server_url to user entered string
					Log.i(MainActivity.class.getName(), "In the if statement"); //Log Message				
				}

				editor.commit(); //Commit changes
				Log.i(MainActivity.class.getName(), "API Key : "+prefs.getString("api_key", null)); //Log Message
				Log.i(MainActivity.class.getName(), "Server URL : "+prefs.getString("server_url", null)); //Log Message

				startup(true);  //Run startup method
			}			
		});

	}

	private void startup(Boolean firstTime) {
		setContentView(R.layout.activity_main);

		if(firstTime) {
			String temp = "all";
			Intent msgIntent = new Intent(this, FeedIntentService.class);        
			msgIntent.putExtra(FeedIntentService.PARAM_IN_MSG, temp);
			startService(msgIntent);		
		}
		else {
			loadFile();
			createGUI();
		}

	}

	@SuppressWarnings("unchecked")
	public void loadFile() {
		File file = getFileStreamPath("feeds.ser");

		try {			
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);				
			feedsArr = (ArrayList<Feed>) ois.readObject();			
			ois.close();			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	} 

	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "org.openenergymonitor.intent.action.MESSAGE_PROCESSED";
		@Override
		public void onReceive(Context context, Intent intent) {                      
			serviceResponse = intent.getStringExtra(FeedIntentService.PARAM_OUT_MSG);
			Log.i(MainActivity.class.getName(), "Response from Service : "+serviceResponse); //Log Message

			if(serviceResponse.equals("all")) {
				loadFile();			
				createGUI();	
			}
			
		}

	}

	public void createGUI() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
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
			Double feedValue = feedsArr.get(position).getValue();						
			args.putDouble(DummySectionFragment.ARG_SECTION_NUMBER, feedValue);
			
			Bundle feedObject = new Bundle();
			feedObject.putSerializable("FeedObject",feedsArr.get(position));
			args.putBundle("FeedObject", feedObject);
			
			
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return feedsArr.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			String feedName = feedsArr.get(position).getName();
			return feedName.toUpperCase();

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
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Double.toString(getArguments().getDouble(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}



}
