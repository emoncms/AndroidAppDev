package org.openenergymonitor.emoncmsapp4;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class Networking extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... urls) {
		
		StringBuilder builder = new StringBuilder();
		String result = "";
		
		for(String url : urls) { 
			HttpClient client = new DefaultHttpClient();
			String apiKey = "xxxxxxxxxxxxxxxxxxxxxxxxxx"; //Manually set API key for the moment		
			HttpGet httpGet = new HttpGet(url +"&apikey="+apiKey);
			//Log.i(MainActivity.class.getName(), url+"&apikey="+apiKey); //Log Message

			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(content));
					String line;

					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					
					result = builder.toString();
				}
				else {
					Log.e(MainActivity.class.toString(), "Failed to download JSON"); //Log Message
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			} 			
		}
		return result;
	}

}
