package com.example.helloworld;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Time in ms in which to fetch a feed value update
    private int updateInterval = 5000;
    // Handler for periodic updates
    private Handler periodicHandler = new Handler();
    // Handler for updating UI on data update
    private Handler uiUpdateHandler = new Handler();

    private TextView powerval;
    private String powervalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.i("EmonLog", "onCreate");

        powerval = (TextView) findViewById(R.id.powervalue);
    }
    
    protected void onPause()
    {
        super.onPause();
        Log.i("EmonLog", "onPause");
        Log.i("EmonLog", "Stopping periodic updater");
        stopRepeatingTask();
    }

    protected void onResume()
    {
        super.onResume();
        Log.i("EmonLog", "onResume");
        Log.i("EmonLog", "Starting periodic updater");
        startRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    Runnable mStatusChecker = new Runnable() {

        @Override 
        public void run() {
          Log.i("EmonLog", "Periodic");

            try {
                String result = new HTTP().execute("http://emoncms.org/feed/value.json?apikey=7f1b46367a013db07d2d65e588d2ad93&id=43348").get();
                result = result.replaceAll("\"","");

                powervalue = result;
                Log.i("EmonLog", "Periodic "+powervalue);

                uiUpdateHandler.post(new Runnable(){
                    public void run() {
                       powerval.setText(powervalue+"W");
                }});

            } catch (Exception e) {

            }

            periodicHandler.postDelayed(mStatusChecker, updateInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run(); 
    }

    void stopRepeatingTask() {
        periodicHandler.removeCallbacks(mStatusChecker);
    }
}

class HTTP extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... params) {
	    String result = "";
	    try {
		    String urlstring = params[0];
		    Log.i("EmonLog", "HTTP Connecting: "+urlstring);
		    URL url = new URL(urlstring);
		    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		
		    try {
		        InputStream reader = new BufferedInputStream(urlConnection.getInputStream());
		        
		        String text = "";
		        int i = 0;
		        while((i=reader.read())!=-1)
		        {
		            text += (char)i;
		        }
		        Log.i("EmonLog", "HTTP Response: "+text);
		        result = text;
		        
		    } catch (Exception e) {
			    Log.i("EmonLog", "HTTP Exception: "+e);
		    }
		    finally {
			    Log.i("EmonLog", "HTTP Disconnecting");
	            urlConnection.disconnect();
		    }
		
	    } catch (Exception e) {
		    e.printStackTrace();
		    Log.i("EmonLog", "HTTP Exception: "+e);
	    }
	
	    return result;
    }
}
