package io.github.utshaw.iut.hospitals;

/**
 * Created by Utshaw on 9/29/2017.
 */
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import io.github.utshaw.iut.LocationInfo;
import io.github.utshaw.iut.R;


/**
 * Created by utshaw on 6/29/17.
 */

public class BusListActivity extends AppCompatActivity {

    private  final String LOG_TAG = BusListActivity.class.getSimpleName();
    private  final String REQUEST_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private  final String API_KEY = "AIzaSyA1cG_jB6x7WLcMcAgqNgpkCL8XntUzQAo";

    String temp = "https://maps.googleapis.com/maps/api/place/search/json?location=37.785835,-122.406418&rankby=distance&types=hospital&sensor=false&key=AIzaSyA1cG_jB6x7WLcMcAgqNgpkCL8XntUzQAo";
    // Lost Survive project key: AIzaSyBqJ1_AhhhS81n1pQ4PtCqm__3MlH2HAOE

    private BusAdapter mAdapter;


    private String latitude;
    private String longitude;

    private  final int WIFI_ENABLE_REQUEST = 0x1006;
    ConnectivityManager connMgr;
    private TextView mEmptyStateTextView;


    TextView textView;
    private  int selectedItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        latitude = String.valueOf(LocationInfo.getLat());
        longitude = String.valueOf(LocationInfo.getLon());




        mAdapter = new BusAdapter(this,new ArrayList<Bus>());

        ListView hospitalListView = (ListView) findViewById(R.id.list);

        hospitalListView.setAdapter(mAdapter);
        hospitalListView.setEmptyView(mEmptyStateTextView);


        hospitalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bus clickedBus = mAdapter.getItem(i);
                String geoUri = null;
                geoUri = "http://maps.google.com/maps?saddr=" + LocationInfo.getLat() +","+LocationInfo.getLon()+"&daddr="+ clickedBus.getmLatitude() +","+ clickedBus.getmLongitude();
//                String geoUri = "http://maps.google.com/maps?q=loc:" + clickedBus.getmLatitude() + "," + clickedBus.getmLongitude() + " (" + clickedBus.getmName() + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(geoUri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);


            }
        });

        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("location",latitude+ "," +
                longitude);


        uriBuilder.appendQueryParameter("rankby","distance");
        uriBuilder.appendQueryParameter("types","bus_station");
        uriBuilder.appendQueryParameter("sensor","false");
//        uriBuilder.appendQueryParameter("key",getResources().getString(R.string.google_maps_key));
        uriBuilder.appendQueryParameter("key","AIzaSyBqJ1_AhhhS81n1pQ4PtCqm__3MlH2HAOE");

        Log.e("Utshaw",uriBuilder.toString());
        new MyTask().execute(uriBuilder.toString());

    }


    class MyTask extends AsyncTask<String,Void,ArrayList<Bus>>
    {


        @Override
        protected ArrayList<Bus> doInBackground(String... strings) {

            ArrayList<Bus> arrayList = new ArrayList<Bus>();
            URL url=null;
            try {
                url = new URL(strings[0]);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            String jsonResponse = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if(urlConnection.getResponseCode() == 200)
                {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
                else
                {
                    Log.e(LOG_TAG,"Error response Code " +  urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
            }
            finally {
                if(urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if(inputStream != null)
                {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            double lat;
            double lng;
            String name="";
            String vicinity = "";

            try {
                if(jsonResponse != null)
                {
                    JSONObject root = new JSONObject(jsonResponse);
                    JSONArray resultsArray = root.getJSONArray("results");
                    for(int i=0;i<resultsArray.length();i++)
                    {
                        JSONObject currentObject = resultsArray.getJSONObject(i);
                        JSONObject geoObject = currentObject.getJSONObject("geometry");
                        JSONObject locationObject = geoObject.getJSONObject("location");
                        lat = locationObject.getDouble("lat");
                        lng = locationObject.getDouble("lng");
                        name = currentObject.getString("name");
                        vicinity = currentObject.getString("vicinity");
                        float[] rslt = new float[10];
                        android.location.Location.distanceBetween(Double.parseDouble(latitude),Double.parseDouble(longitude),lat,lng,rslt);
                        arrayList.add(new Bus(name,lat,lng,vicinity,rslt[0]));
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return arrayList;

        }

        @Override
        protected void onPostExecute(ArrayList<Bus> busList) {

            super.onPostExecute(busList);

            ProgressBar loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);


            mAdapter.clear();
            mEmptyStateTextView.setText("No nearby bus stations found");

            if(busList != null && !busList.isEmpty())
            {
                mAdapter.addAll(busList);
            }
        }



    }

    private  String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output  = new StringBuilder();

        if(inputStream != null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while(line != null)
            {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


}