package org.openenergymonitor.emoncmsapp4;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openenergymonitor.emoncmsapp4.MainActivity.ResponseReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class FeedIntentService extends IntentService {
	public static final String PARAM_IN_MSG = "imsg";
	public static final String PARAM_OUT_MSG = "omsg";


	public FeedIntentService() {
		super("SimpleIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {		     
		String job = intent.getStringExtra(PARAM_IN_MSG); 
		FeedManager fm = new FeedManager(this);
		Log.i("FeedIntentService", "Running Service"); //Log Message
		String jobResult = "";
		
		if(job.equals("all")) {
			Log.i("FeedIntentService", "Service: Getting all feed data"); //Log Message
			fm.getFeedData();
			fm.saveFile();
			jobResult = "all";
		}
		
		if(job.equals("live")) {
			Log.i("FeedIntentService", "Service: Updating live data"); //Log Message
			fm.updateLiveFeed();
			fm.saveFile();
			jobResult = "live";
		}
		
		if(job.equals("historic")) {
			Log.i("FeedIntentService", "Service: Updating historic data"); //Log Message
			fm.updateHistoricData();
			fm.saveFile();
			jobResult = "historic";
		}
		
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, jobResult);
        sendBroadcast(broadcastIntent);
		
		
	}


}
