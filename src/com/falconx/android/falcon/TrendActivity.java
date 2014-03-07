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
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TrendActivity extends Activity
{
	public String serverBaseUrl = "http://192.168.0.100:3000";
	TextView dispMessage;
	ImageView picture;
	public int count = 0;
	String[][] imageArray = new String[20][2];
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);
        dispMessage = (TextView)findViewById(R.id.disp_trend_message);

 	   if(isConnected()){
           new HttpAsyncTask().execute(serverBaseUrl+"/images.json");
         
 	    }
 	   else
 	     {
 		   dispMessage.setText("no internet connection");
 		   
 	     }
 	   
  
    }
	public void  startGrid(){
		
		   GridView gridView = (GridView)findViewById(R.id.gridview);
           gridView.setAdapter(new MyAdapter(this));
	}
	//checking connection
	public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;  
    }
	
	
	

    private class MyAdapter extends BaseAdapter
    {
        public List<Item> items = new ArrayList<Item>();
        private LayoutInflater inflater;

        public MyAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
            for (int k = 0; k < 20; k++) {
            	 items.add(new Item(imageArray[k][1], k));
            	}
       	     
//       	    items.add(new Item("Image 1", R.drawable.nature1));
//            items.add(new Item("Image 2", R.drawable.nature2));
//            items.add(new Item("Image 3", R.drawable.tree1));
//            items.add(new Item("Image 4", R.drawable.nature3));
//            items.add(new Item("Image 5", R.drawable.tree2));
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i)
        {
            return items.get(i);
        }

        @Override
        public long getItemId(int i)
        {
        	 return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View v = view;
            
            TextView name;

            if(v == null)
            {
               v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
               v.setTag(R.id.picture, v.findViewById(R.id.picture));
               v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            picture = (ImageView)v.getTag(R.id.picture);
            name = (TextView)v.getTag(R.id.text);
             
            Item item = (Item)getItem(i);
            GetXMLTask task = new GetXMLTask();
            task.execute(new String[] { item.imgUrl });
            //picture.setImageResource(R.drawable.tree2);
            name.setText(i+"");

            return v;
        }

        private class Item
        {
            final int name;
            final String imgUrl;

            Item(String imgUrl, int id)
            {
                this.name = id;
                this.imgUrl = imgUrl;
            }
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
            for (int i = 0; i < 20; i++) {
            	JSONObject objects = arr.getJSONObject(i);
            	image_url =  (String) objects.get("image_url");
            	imageArray[i][0] = (String) objects.get("id");
            	imageArray[i][1] = image_url;
            	}
             startGrid();
            }
            catch (JSONException e) {
            	dispMessage.setText("error json");
            	
            }
       }
    }
    
	
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
        	
            picture.setImageBitmap(result);
        	
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
	
}