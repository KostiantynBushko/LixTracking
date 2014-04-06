package com.lixtracking.lt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lixtracking.lt.R;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.GpsData;
import com.lixtracking.lt.data_class.VehicleData;
import com.lixtracking.lt.parsers.ParseGpsData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by saiber on 04.04.2014.
 */
public class TrackingHistoryActivity extends Activity implements View.OnClickListener{
    private static final String PREF_MAP_TYPE = "pref_map_type";
    private int map_type = GoogleMap.MAP_TYPE_NORMAL;
    private SharedPreferences sharedPreferences;
    ImageButton play = null;
    ImageButton next = null;
    ImageButton previous = null;
    public VehicleData currentVehicle = new VehicleData();
    private List<GpsData> gpsDatas = null;
    private List<LatLng> gpsPoints  = null;
    private LatLng firstPoint = null;
    private LatLng lastPoint = null;
    private LatLng currentPoint = null;
    private int currentIndex = 0;
    private PolylineOptions  polylineOptions = new PolylineOptions();

    private GoogleMap map = null;
    private Context context;

    private Timer playTimer;
    private int playStatus = 2; // 1 - play 2 - pause 3 - refresh
    private PlayTimer playTask;
    private final Handler myHandler = new Handler();

    private ProgressBar progressBar = null;
    private ProgressBar playProgress = null;
    private boolean loadingStatus = false;
    private Marker currentMarker = null;
    private Marker firstMarker = null;

    private int lineAlpha = 150;

    private TextView textDateTime = null;
    private TextView textLatitude = null;
    private TextView textLongitude = null;
    private TextView textSpeed = null;
    RelativeLayout trackingView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.tracking_history_activity);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        context = this;

        map_type = sharedPreferences.getInt(PREF_MAP_TYPE, map_type);
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(map_type);
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);

        // Button
        play = (ImageButton)findViewById(R.id.button_play);
        play.setOnClickListener(this);
        next = (ImageButton)findViewById(R.id.button_next);
        next.setOnClickListener(this);
        previous = (ImageButton)findViewById(R.id.button_previous);
        previous.setOnClickListener(this);

        play.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
        previous.setVisibility(View.INVISIBLE);

        // Intent data
        Intent intent = getIntent();
        currentVehicle.vin = intent.getStringExtra(VehicleData.VIN);
        currentVehicle.gps_id = intent.getStringExtra(VehicleData.GPS_ID);
        currentVehicle.user_id = intent.getStringExtra(VehicleData.USER_ID);
        currentVehicle.first_name = intent.getStringExtra(VehicleData.FIRST_NAME);
        currentVehicle.last_name = intent.getStringExtra(VehicleData.LAST_NAME);
        currentVehicle.stock_number = intent.getStringExtra(VehicleData.STOCK_NUMBER);
        currentVehicle.model = intent.getStringExtra(VehicleData.MODEL);
        currentVehicle.make = intent.getStringExtra(VehicleData.MAKE);
        currentVehicle.year = intent.getIntExtra(VehicleData.YEAR, 0);
        currentVehicle.status = intent.getIntExtra(VehicleData.STATUS, 0);
        //Progress bar
        progressBar = (ProgressBar)findViewById(R.id.loading_spinner);

        //Tracking view
        trackingView = (RelativeLayout)findViewById(R.id.trackingView);
        textDateTime = (TextView)findViewById(R.id.textView1);
        textLatitude = (TextView)findViewById(R.id.textView2);
        textLongitude = (TextView)findViewById(R.id.textView3);
        textSpeed = (TextView)findViewById(R.id.textView4);
        showTrackingView(View.INVISIBLE);
        ((ImageButton)findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTrackingView(View.INVISIBLE);
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        new getHistoryGpsDataTask().execute();
    }
    @Override
    public void onResume() {
        Log.i("info", " TrackingHistoryActivity : RESUME" );
        super.onResume();
    }
    @Override
    public void onPause() {
        Log.i("info", " TrackingHistoryActivity : PAUSE" );
        super.onPause();
        if(playTimer != null) {
            playTask.run = false;
            playTimer.cancel();
            playTimer = null;
            playTask.cancel();
            playTask = null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_map:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL).commit();
                break;
            case R.id.action_map_satellite:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID).commit();
                break;
        }
        return true;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play:
                if(playStatus == 1){
                    showTrackingView(View.VISIBLE);
                    play.setImageResource(R.drawable.play_button_selector);
                    next.setVisibility(View.VISIBLE);
                    previous.setVisibility(View.VISIBLE);
                    playStatus = 2;
                    if(playTimer != null) {
                        playTimer.cancel();
                        playTimer = null;
                        playTask.run = false;
                        playTask.cancel();
                        playTask = null;
                    }
                }else if(playStatus == 2){
                    showTrackingView(View.VISIBLE);
                    play.setImageResource(R.drawable.pause_button_selector);
                    next.setVisibility(View.INVISIBLE);
                    previous.setVisibility(View.INVISIBLE);
                    currentIndex = 1;
                    map.clear();
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.argb(lineAlpha,50,50, 255));
                    map.addPolyline(polylineOptions);
                    map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction_down))
                            .position(firstPoint)
                            .title(" Start")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    currentMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_stop))
                            .position(firstPoint)
                            .title(" Finish")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    playStatus = 1;
                    playTimer = new Timer();
                    playTask = new PlayTimer();
                    playTimer.scheduleAtFixedRate(playTask, 1, 5000);
                }else if(playStatus == 3) {
                    if(!loadingStatus)
                        new getHistoryGpsDataTask().execute();
                }
                break;
            case R.id.button_next:
                showTrackingView(View.VISIBLE);
                if(currentIndex < gpsPoints.size()){
                    polylineOptions.add(gpsPoints.get(currentIndex)).color(Color.argb(lineAlpha,50,50, 255));
                    map.clear();
                    map.addPolyline(polylineOptions);
                    addFirstLast(gpsPoints.get(currentIndex));
                    currentMarker.setPosition(gpsPoints.get(currentIndex));
                    map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex)));
                    updateTrackingView();
                    currentIndex++;
                }
                break;
            case R.id.button_previous:
                if (currentIndex > 1){
                    currentIndex--;
                    map.clear();
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.argb(lineAlpha,50,50, 255));
                    for (int i = 1; i<currentIndex; i++) {
                        polylineOptions.add(gpsPoints.get(i)).color(Color.argb(lineAlpha,50,50, 255));
                    }
                    map.addPolyline(polylineOptions);
                    map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction_down))
                            .position(firstPoint)
                            .title(" Start")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    map.addPolyline(polylineOptions);
                    currentMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_stop))
                            .position(gpsPoints.get(currentIndex - 1))
                            .title(" Finish")
                            .snippet("Lat " + Double.toString(gpsPoints.get(currentIndex).latitude)
                                    + " Lng : " + Double.toString(gpsPoints.get(currentIndex).latitude)));
                    map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex-1)));
                    updateTrackingView();
                }
                break;
        }
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getHistoryGpsDataTask extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... voids) {
            loadingStatus = true;
            Log.i("info", " TrackingHistoryActivity : START TASK" );
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.getHistoryGpsDataUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(3);
            nameValuePairList.add(new BasicNameValuePair("terminal_id", currentVehicle.gps_id));
            nameValuePairList.add(new BasicNameValuePair("start_time","2013-01-01 01-00-00"));
            nameValuePairList.add(new BasicNameValuePair("end_time","2014-03-01 01-00-00"));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                resultString = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultString;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.i("info", " TrackingHistoryActivity : END TASK" );
            if(resultString == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Error...");
                builder.setMessage("Error...");
                builder.setCancelable(true);
                builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                playStatus = 3;
                play.setVisibility(View.VISIBLE);
                play.setImageResource(R.drawable.refresh_button_selector);
            }else {
                List<GpsData> tmpData = new ParseGpsData(context).parceXml(resultString);
                gpsDatas = tmpData;
                if((tmpData != null) && (!tmpData.isEmpty())) {
                    gpsPoints = new ArrayList<LatLng>();
                    float lat = Float.parseFloat(tmpData.get(0).lat);
                    float lng = Float.parseFloat(tmpData.get(0).lng);
                    firstPoint = new LatLng(lat,lng);
                    polylineOptions.add(firstPoint);
                    lat = Float.parseFloat(tmpData.get(tmpData.size()-1).lat);
                    lng = Float.parseFloat(tmpData.get(tmpData.size()-1).lng);
                    lastPoint = new LatLng(lat,lng);

                    for(int i = 0; i<tmpData.size(); i++) {
                        lat = Float.parseFloat(tmpData.get(i).lat);
                        lng = Float.parseFloat(tmpData.get(i).lng);
                        gpsPoints.add(new LatLng(lat,lng));
                        Log.i("info", "--------------------------------------------------------------------");
                        Log.i("info","terminal_id : " + tmpData.get(i).gps_id);
                        Log.i("info","speed       : " + tmpData.get(i).speed);
                        Log.i("info","lat         : " + tmpData.get(i).lat);
                        Log.i("info","lng         : " + tmpData.get(i).lng);
                        Log.i("info","--------------------------------------------------------------------");
                    }
                    playStatus = 2;
                    play.setImageResource(R.drawable.play_button_selector);
                    play.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    previous.setVisibility(View.VISIBLE);
                    // Map
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstPoint, 15);
                    map.moveCamera(cameraUpdate);
                    currentMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_stop))
                            .position(firstPoint).title(" Finish")
                            .snippet("Lat " + Double.toString(firstPoint.latitude)
                                    + " Lng : " + Double.toString(firstPoint.longitude)));
                    firstMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction_down))
                            .position(firstPoint)
                            .title(" Start")
                            .snippet("Lat " + Double.toString(firstPoint.latitude)
                                    + " Lng : " + Double.toString(firstPoint.longitude)));
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(),"...try again later",Toast.LENGTH_SHORT);
                    toast.show();
                    playStatus = 3;
                    next.setVisibility(View.INVISIBLE);
                    previous.setVisibility(View.INVISIBLE);
                    play.setImageResource(R.drawable.refresh_button_selector);
                    play.setVisibility(View.VISIBLE);
                }
            }
            progressBar.setVisibility(View.INVISIBLE);
            loadingStatus = false;
        }
    }
    /**********************************************************************************************/
    /* TIMER TASK */
    /**********************************************************************************************/
    class PlayTimer extends TimerTask {
        @Override
        public void run() {
            Log.i("info", " PLAY TIMER RUN : ");
            Log.i("info", " points count = " + Integer.toString(gpsPoints.size()));
            int i = 0;
            while (run) {
                SystemClock.sleep(1000);
                Log.i("info", " TICK : " + Integer.toString(i));
                i++;
                if(currentIndex < gpsPoints.size()) {
                    //polylineOptions.color(Color.argb(lineAlpha,50,50, 255));
                    polylineOptions.add(gpsPoints.get(currentIndex)).color(Color.argb(lineAlpha,50,50, 255));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("info", " PLAY TIMER RUN UI : update map");
                            Log.i("info", " PLAY TIMER RUN UI : current index = " + Integer.toString(currentIndex));
                            map.clear();
                            map.addPolyline(polylineOptions);
                            addFirstLast(gpsPoints.get(currentIndex-1));
                            map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex-1)));
                            updateTrackingView();
                            /*currentMarker.setPosition(gpsPoints.get(currentIndex-1));
                            currentMarker.setSnippet("Lat " + Double.toString(gpsPoints.get(currentIndex-1).latitude)
                                    + " Lng : " + Double.toString(gpsPoints.get(currentIndex-1).longitude));*/
                        }
                    });
                    currentIndex++;
                }else {
                    run = false;
                    this.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("info", " PLAY TIMER FINISH UI : return button");
                            play.setImageResource(R.drawable.play_button_selector);
                            next.setVisibility(View.VISIBLE);
                            previous.setVisibility(View.VISIBLE);
                            playStatus = 2;
                            currentMarker.setPosition(gpsPoints.get(currentIndex-1));
                            currentMarker.setSnippet("Lat " + Double.toString(firstPoint.latitude)
                                    + " Lng : " + Double.toString(firstPoint.longitude));
                        }
                    });
                }
            }
            Log.i("info", " PLAY TIMER STOP : ");
        }
        public boolean run = true;
    }
    private void addFirstLast(LatLng currentLatLog) {
        map.addMarker(new MarkerOptions().position(currentLatLog)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_stop))
                .title("End").snippet(
                        "Lat " + Double.toString(currentLatLog.latitude)
                                + " Lng : " + Double.toString(currentLatLog.longitude)
                ));
        map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction_down))
                .position(firstPoint)
                .title("Start").snippet(
                "Lat " + Double.toString(firstPoint.latitude)+ " Lng : " + Double.toString(firstPoint.longitude)
        ));
    }
    private void updateTrackingView() {
        Log.i("info"," updateTrackingView");
        if(gpsDatas == null || gpsDatas.isEmpty())
            return;
        Log.i("info"," Update gps view");
        textDateTime.setText(gpsDatas.get(currentIndex).gps_time);
        textLatitude.setText(gpsDatas.get(currentIndex).lat);
        textLongitude.setText(gpsDatas.get(currentIndex).lng);
        textSpeed.setText(Float.toString(gpsDatas.get(currentIndex).speed));
    }
    private void showTrackingView(int visible) {
        trackingView.setVisibility(visible);
    }
}
