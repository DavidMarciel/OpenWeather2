package com.entropy.david.openweather2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    private static final String HEADER  = "http://";
    private static final String URL     = "api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String DAYS    = "&cnt=7";             //number of days
    private static final String UNITS   = "&units=metric";             //units used (Cº)
    private static final String API_ID  = "&APPID=dbb12ae244be260ec25479caa5810b9b";    //key

    private static final String ACTION_FOR_INTENT_CALLBACK = "THIS_IS_A_UNIQUE_KEY_WE_USE_TO_COMMUNICATE";

    private Toast waitToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waitToast = Toast.makeText(this, "Por favor, espere mientras recuperamos la información.", Toast.LENGTH_LONG);
        waitToast.show();

        getContent();
    }

    /**Assembles the URL
     *
     * @return url for REST request
     */
    public String getUrl() {

        String coordinates = getCoordinates();

        if(coordinates != null) {
            return HEADER + URL + getCoordinates() + DAYS + UNITS + API_ID;
        }
        else return "";
    }

    /**Generate the request and get the info from the server
     *
     */
    private void getContent()
    {
        try
        {
            String url = getUrl();
            Log.d("URL", url);

            if(url != null) {
                HttpGet httpGet = new HttpGet(new URI(url));
                RestTask task = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);
                task.execute(httpGet);
            }
            else{
//                Toast.makeText()
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**Gets the Json string and shows the required information in a table
     * we should add any member we want to show (and place it´s name at the header)
     * @param incomingJson
     */
    private void showInfo(String incomingJson) {

        waitToast.cancel();

        //the info of the first day is the one of today
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);

        //table
        TableLayout tbl = (TableLayout) findViewById(R.id.tabla);

        //table header
        TableRow header = new TableRow(this);

        TextView tv = new TextView(this);
        tv.setText("Dia");
        header.addView(tv);

        tv = new TextView(this);
        tv.setText("Temperatura");
        header.addView(tv);

        tv = new TextView(this);
        tv.setText("T. Máxima");
        header.addView(tv);

        tv = new TextView(this);
        tv.setText("T. Mínima");
        header.addView(tv);

        tbl.addView(header);


        //JSon parse
        String tempDay = "";
        String tempMin = "";
        String tempMax = "";

        JSONArray list = null;
        int nElements = 0;

        try {
            JSONObject jsonObject = new JSONObject(incomingJson);
            list = (JSONArray) jsonObject.getJSONArray("list");

            nElements = list.length();

        }catch (Exception e){
            e.printStackTrace();
        }

        TableRow newRow;
        for (int i = 1; i < nElements; i++) {

            //load day data
            try{
                JSONObject itemInList = (JSONObject) list.get(i);
                JSONObject itemTemp = itemInList.getJSONObject("temp");

                tempMax = itemTemp.getString("max");
                tempMin = itemTemp.getString("min");
                tempDay = itemTemp.getString("day");

            }catch (Exception e){
                e.printStackTrace();
            }

            //show day data
            newRow = new TableRow(this);

            tv = new TextView(this);
            tv.setText("" + day);
            newRow.addView(tv);
            //increase day
            c.add(Calendar.DATE,1);
            day = c.get(Calendar.DAY_OF_MONTH);

            tv = new TextView(this);
            tv.setText(tempDay);
            newRow.addView(tv);

            tv = new TextView(this);
            tv.setText(tempMax);
            newRow.addView(tv);

            tv = new TextView(this);
            tv.setText(tempMin);
            newRow.addView(tv);

            tbl.addView(newRow);
        }

    }

    /**Get the value of the requested item
     * 
     * @param find requested item
     * @param allInfo Json encoded info of the object
     * @return value of "find" value
     */
    private String getInString(String find, String allInfo) {

        int start = allInfo.indexOf(find) + find.length() +2;
        int end = allInfo.indexOf(",\"", start);

        return allInfo.substring(start,end);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }


    /**Boradcast Receiver
     * It is executed when the http data is received
     */
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            String response = intent.getStringExtra("httpResponse");
            Log.d("Response: ", response);

            showInfo(response);
        }
    };


    public String getCoordinates() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 500, 10, locationListener);

        try {
            long GPSLocationTime = 0;
            long NetLocationTime = 0;

            if (null != locationGPS) {
                GPSLocationTime = locationGPS.getTime();
            }
            if (null != locationNet) {
                NetLocationTime = locationNet.getTime();
            }

            String coordinatesAsString;

            if (0 < GPSLocationTime - NetLocationTime) {
                coordinatesAsString = "lat=" + locationGPS.getLatitude() + "&lon=" + locationGPS.getLongitude();
            } else {
                coordinatesAsString = "lat=" + locationGPS.getLatitude() + "&lon=" + locationGPS.getLongitude();
            }


            Log.d("coordinates", coordinatesAsString);
            return coordinatesAsString;

        } catch (Exception e) {
            Toast.makeText(this, "Por favor, active su GPS", Toast.LENGTH_LONG).show();
        }

        return null;
    }
}

////    Incoming data example
//    {"city":{"id":2632287,"name":"Dalvik",
//        "coord":{"lon":-18.52861,"lat":65.970177},"country":"IS","population":0},
//        "cod":"200","message":0.0087,"cnt":2,
//        "list":
//        [
//        {"dt":1460768400,"temp":
//            {"day":283.15,"min":283.15,"max":283.15,"night":283.15,"eve":283.15,"morn":283.15},
//            "pressure":968.93,"humidity":94,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],
//            "speed":1.47,"deg":144,"clouds":92,"rain":0.36},
//
//        {"dt":1460854800,"temp":
//            {"day":280.63,"min":276.36,"max":282.54,"night":276.77,"eve":276.36,"morn":282.54},
//            "pressure":960.52,"humidity":96,"weather":[{"id":601,"main":"Snow","description":"snow","icon":"13d"}],
//            "speed":3.66,"deg":234,"clouds":56,"rain":3.42,"snow":5.55}
//        ]
//    }