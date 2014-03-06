package com.falconx.android.falcon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SlideActivity extends Activity {
    ImageView slideView;
    Timer timer;
    TimerTask myTimerTask;
    public int currentimageindex=0;
	private ImageView imageCtrlButton;
	//List<String> imageArray = new ArrayList<String>();
	String[][] imageArray = new String[60][2];
	private Integer currentImageCtrlButtonId;
	private Integer playButton;
	private Integer pauseButton;
	TextView dispMessage;
	public static final String URL = "http://myntra.myntassets.com/images/style/properties/FabAlley-Women-Dresses_e4b93380ed7bed147df27a887f2a3aff_images_360_480_mini.jpg";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slide);
		playButton = R.drawable.play_button;
		pauseButton = R.drawable.pause_button;
		slideView = (ImageView)findViewById(R.id.slide_view);
		imageCtrlButton = (ImageView)findViewById(R.id.image_ctrl_button);
		imageCtrlButton.setImageResource(playButton);
		imageCtrlButton.setTag(playButton); 
		dispMessage = (TextView)findViewById(R.id.disp_message);
		// Create an object for subclass of AsyncTask
	   if(isConnected()){
          new HttpAsyncTask().execute("http://192.168.0.100:3000/images.json");
        
	    }
	   else
	     {
		   dispMessage.setText("no internet connection");
		   
	     }
	     slideView.setOnClickListener(new View.OnClickListener(){
	       @Override
	       public void onClick(View V){
	    	   String image_id = imageArray[currentimageindex][0];
	    	   Toast.makeText(getBaseContext(), image_id, Toast.LENGTH_LONG).show();
	       }
	     });
	     imageCtrlButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View V){
			  currentImageCtrlButtonId = (Integer) imageCtrlButton.getTag();
			  if(currentImageCtrlButtonId == playButton ){
			    imageCtrlButton.setImageResource(pauseButton);
			    imageCtrlButton.setTag(pauseButton); 
			    startSlide();
			    dispMessage.setText("");
			  }
			  else
			  {
				imageCtrlButton.setImageResource(playButton);
				imageCtrlButton.setTag(playButton);	
				stopSlide();
			  }
			}
			
		});
	}

    public void startSlide(){
		final Handler mHandler = new Handler();
		final Runnable mUpdateResults = new Runnable() {
			public void run(){
				AnimateandSlideShow();
			}
		};
		
        int delay = 3000; // delay for 1 sec.

        int period = 3000; // repeat every 4 sec.
        Timer timer = new Timer();
        myTimerTask = new TimerTask() {

            public void run() {

            	 mHandler.post(mUpdateResults);

            }

            };
            timer.scheduleAtFixedRate(myTimerTask, delay, period);
     }
    
    
	public void stopSlide(){
		myTimerTask.cancel();
		//finish();
       // android.os.Process.killProcess(android.os.Process.myPid());
	}
	
    private void AnimateandSlideShow() {
    	
    	if(currentimageindex < imageArray.length && imageArray.length > 0)
    	{
    		  GetXMLTask task = new GetXMLTask();
              task.execute(new String[] { imageArray[currentimageindex][1] });
    	}
    	
    	else
    	{
    		stopSlide();
    	}
      
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.slide, menu);
		return true;
	}
	//image.setImageResource(R.drawable.android3d);
    
	//checking connection
	public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;  
    }
	
	//get json
	
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
    }
 
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
	
	// showing image
	private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }
 
        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            slideView.setImageBitmap(result);
        	currentimageindex++;
       		Animation rotateimage = AnimationUtils.loadAnimation(SlideActivity.this, R.anim.fade_in);
        	slideView.startAnimation(rotateimage);
        }
 
        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;
 
            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.
                        decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }
 
        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
 
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
 
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }
	
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Ready!", Toast.LENGTH_LONG).show();
            String image_url = "";
            try
            {
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
            	JSONObject objects = arr.getJSONObject(i);
            	image_url =  (String) objects.get("image_url");
            	imageArray[i][0] = (String) objects.get("id");
            	imageArray[i][1] = image_url;
            	}
            dispMessage.setText("Click Play To Start Slide");
            }
            catch (JSONException e) {
            	dispMessage.setText("error");
            	
            }
       }
    }
}
