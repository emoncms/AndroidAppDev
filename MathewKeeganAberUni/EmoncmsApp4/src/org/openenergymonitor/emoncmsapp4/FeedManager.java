package org.openenergymonitor.emoncmsapp4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


public class FeedManager {

	TimeCalculator timeCalc;
	Context fileContext;
	ArrayList<Feed> allFeeds;	
	private static final String FILENAME = "feeds.ser";
	private static final long FORTYEIGHTHOURS = 172800000;

	public FeedManager(Context fileContext) {
		allFeeds = new ArrayList<Feed>();
		timeCalc = new TimeCalculator();
		this.fileContext = fileContext;
	}

	public void getFeedData() {

		String feedList = "";

		try {
			Networking listAllFeeds = new Networking();
			feedList = listAllFeeds.execute(new String[] { "http://emoncms.org/feed/list.json" }).get(); //TODO: Manually set URL as emoncms. Change to sharedPreferences 26/8/13		
		} catch (Exception e) {
			e.printStackTrace();
		} 

		try {
			JSONArray jsonArr = new JSONArray(feedList);	//Put JSON string into JSONarray				
			Long timeNow = System.currentTimeMillis();

			for(int i =0; i < jsonArr.length(); i++) {
				JSONObject jsonObject = jsonArr.getJSONObject(i);
				Long timeSinceUpdate = Long.parseLong(jsonObject.getString("time"));

				if(((timeNow - timeSinceUpdate) < FORTYEIGHTHOURS) && (jsonObject.getInt("datatype") != 0)) {	//If it's been less than 48 hours since last update, include in results
					Feed f = new Feed();					
					f.setId(jsonObject.getInt("id")); //Set ID
					f.setName(jsonObject.getString("name")); //Set name
					f.setDatatype(jsonObject.getInt("datatype")); //Set DataType
					f.setTag(jsonObject.getString("tag")); //Set name					
					f.setTime(jsonObject.getLong("time")); //Set time
					f.setValue(jsonObject.getDouble("value")); // Set value
					//f.setDpInterval(jsonObject.getString("dpinterval")); //Set dpInterval

					allFeeds.add(f);					 
				} 
			}

			/* Get historic data for all active feeds */
			Long end = System.currentTimeMillis();
			Long start;


			for(int i = 0; i < allFeeds.size(); i++) {
				int id = allFeeds.get(i).getId();
				String historicData = "";

				if(allFeeds.get(i).getDatatype() == 1) {
					start = timeCalc.pastDate(1);	//If real-time, get data for past 24 hours 
				}
				else {
					start = timeCalc.pastDate(14);	//If daily, get data for past 14 days
				}

				//Log.i(MainActivity.class.getName(),"Start :"+start+" / End : "+end+" / ID : "+id); //Log Message

				try {
					Networking getHistoricData = new Networking();
					historicData = getHistoricData.execute(new String[] { "http://emoncms.org/feed/data.json&id="+id+"&start="+start+"&end="+end}).get(); //TODO: Manually set URL as emoncms. Change to sharedPreferences 26/8/13		
					JSONArray graphJsonArr = new JSONArray(historicData);					

					for (int j = 0; j < graphJsonArr.length(); j++) {
						JSONArray arr = graphJsonArr.getJSONArray(j);
						long timestamp = arr.getLong(0);
						double value = arr.getDouble(1);
 
						allFeeds.get(i).addElement(timestamp, value);

						//Log.i(MainActivity.class.getName(),"Timestamp : "+timestamp+" / Value : "+value); //Log Message

					}

					Log.i(MainActivity.class.getName(),"Feed ID : "+id); //Log Message
					//Log.i(MainActivity.class.getName(),"http://emoncms.org/feed/data.json&id="+id+"&start="+start+"&end="+end); //Log Message

				} catch (Exception e) {
					e.printStackTrace();
				} 					
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void updateLiveFeed() {

		String feedList = "";

		try {
			Networking listAllFeeds = new Networking();
			feedList = listAllFeeds.execute(new String[] { "http://emoncms.org/feed/list.json" }).get(); //TODO: Manually set URL as emoncms. Change to sharedPreferences 26/8/13		
		} catch (Exception e) {
			e.printStackTrace();
		} 

		try {
			JSONArray jsonArr = new JSONArray(feedList);			

			for(int i =0; i < jsonArr.length(); i++) {
				JSONObject jsonObject = jsonArr.getJSONObject(i);				

				/* 
				 * Iterate through feed arrayList. If the JSON object has matching 
				 * id and a new reading, update the value and timestamp.
				 */
				for(int j = 0; j < allFeeds.size(); j++) {											
					if(jsonObject.getInt("id") == allFeeds.get(j).getId() && jsonObject.getDouble("value") != allFeeds.get(j).getValue()) {
						allFeeds.get(j).setValue(jsonObject.getDouble("value"));
						allFeeds.get(j).setTime(jsonObject.getLong("time"));
						break;
					}						
				}															
			} 

		}
		catch (JSONException e) {
			e.printStackTrace();
		}				
	}

	public void updateHistoricData() {

		/* Get historic data for all active feeds */
		Long end = System.currentTimeMillis();
		Long start;

		for(int i = 0; i < allFeeds.size(); i++) {
			int id = allFeeds.get(i).getId();
			String historicData = "";

			if(allFeeds.get(i).getDatatype() == 1) {
				start = timeCalc.pastDate(1);	//If real-time, get data for past 24 hours 
			}
			else {
				start = timeCalc.pastDate(14);	//If daily, get data for past 14 days
			}

			allFeeds.get(i).clearHistoricFeedData(); //Drop all existing data		
			
			try {
				Networking getHistoricData = new Networking();
				historicData = getHistoricData.execute(new String[] { "http://emoncms.org/feed/data.json&id="+id+"&start="+start+"&end="+end}).get(); //TODO: Manually set URL as emoncms. Change to sharedPreferences 26/8/13		
				JSONArray graphJsonArr = new JSONArray(historicData);					

				for (int j = 0; j < graphJsonArr.length(); j++) {
					JSONArray arr = graphJsonArr.getJSONArray(j);
					long timestamp = arr.getLong(0);
					double value = arr.getDouble(1);
					allFeeds.get(i).addElement(timestamp, value);
				}
				Log.i(MainActivity.class.getName(),"Feed ID : "+id); //Log Message

			} catch (Exception e) {
				e.printStackTrace();
			} 					
		}
		
		
		
		
	}
	
	public void saveFile() {        

		try {            
			FileOutputStream fos = fileContext.getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);  //Added to see if a similar performance increase can be obtained as loadFile()
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(allFeeds);
			os.flush();
			os.close();
			Log.i(MainActivity.class.getName(),"File : "+FILENAME+" serialised successfully."); //Log Message
		}
		catch (Exception e) {
			Log.e(MainActivity.class.getName(),"ERROR : "+e); //Log Message
			e.printStackTrace();
		} 		
	}

	@SuppressWarnings("unchecked")
	public void loadFile() {
		File file = fileContext.getApplicationContext().getFileStreamPath(FILENAME);

		try {			
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);				
			allFeeds = (ArrayList<Feed>) ois.readObject();			
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

	public ArrayList<Feed> getFeedArraylist() {
		return allFeeds;
	}



	/* *********************************************** *
	 *			  HELPER + TESTING METHODS
	 * *********************************************** */
	public void doesFileExist() {
		final String FILENAME = "feeds.ser";
		File file = fileContext.getApplicationContext().getFileStreamPath(FILENAME);

		if(file.exists()) {
			Log.e(MainActivity.class.getName(),"FILE EXISTS"); //Log Message
			Log.e(MainActivity.class.getName(),"Bytes : " +file.length()); //Log Message
		}
		else {
			Log.e(MainActivity.class.getName(),"FILE DOESN'T EXIST"); //Log Message
		}

	}

	public void printArraylist() {

		for(int i = 0; i < allFeeds.size(); i++) {
			Log.i(MainActivity.class.getName(), allFeeds.get(i).toString());
		}
	}

}
