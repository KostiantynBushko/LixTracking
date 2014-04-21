package com.lixtracking.lt.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lixtracking.lt.R;
import com.lixtracking.lt.common.Constant;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.GpsData;
import com.lixtracking.lt.data_class.VehicleData;
import com.lixtracking.lt.dialog.SpeedRouteDialog;
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
public class TrackingHistoryActivity extends FragmentActivity implements View.OnClickListener, SpeedRouteDialog.Listener{
    private static final String PREF_MAP_TYPE = "pref_map_type";
    private static final String PREF_SPEED_ROUTE = "pref_speed_route";

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
    private Polyline polyline;

    private GoogleMap map = null;
    private float currentZoom = Constant.mapZoom;
    private Context context;

    private Timer playTimer;
    private int playStatus = 2; // 1 - play 2 - pause 3 - refresh
    private PlayTimer playTask;

    private ProgressBar progressBar = null;
    private ProgressBar playProgress = null;
    private boolean loadingStatus = false;
    private Marker currentMarker = null;
    private Marker firstMarker = null;

    private int lineAlpha = 100;

    private TextView textDateTime = null;
    private TextView textLatitude = null;
    private TextView textLongitude = null;
    private TextView textSpeed = null;
    RelativeLayout trackingView = null;

    String dateFrom = null;
    String dateTo = null;

    private int route_speed = 1;

    TextView stepText = null;
    getHistoryGpsDataTask getHistoryTask = null;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.tracking_history_activity);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));

        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0))); //ffff9700
        sharedPreferences = getSharedPreferences(this.getClass().getSimpleName(),Context.MODE_PRIVATE);
        route_speed = sharedPreferences.getInt(PREF_SPEED_ROUTE, 1);
        context = this;

        map_type = sharedPreferences.getInt(PREF_MAP_TYPE, map_type);
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        //map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(map_type);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Constant.baseLatLng, currentZoom));
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

        stepText = (TextView)findViewById(R.id.textView8);
        if(map_type == GoogleMap.MAP_TYPE_NORMAL) {
            stepText.setTextColor(Color.BLACK);
        }else {
            stepText.setTextColor(Color.WHITE);
        }

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

        dateFrom = intent.getStringExtra("FROM");
        dateTo = intent.getStringExtra("TO");

        dateFrom = dateFrom.replace("/","-");
        dateFrom = dateFrom.replace(":","-");
        dateTo = dateTo.replace("/","-");
        dateTo = dateTo.replace(":","-");
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
        getHistoryTask = new getHistoryGpsDataTask();
        getHistoryTask.execute();
    }
    @Override
    public void onResume() {
        Log.i("info", " TrackingHistoryActivity : RESUME" );
        super.onResume();
        stepText = (TextView)findViewById(R.id.textView8);
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
        if(getHistoryTask != null) {
            getHistoryTask.cancel(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_track_history, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_map:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL).commit();
                map_type = GoogleMap.MAP_TYPE_NORMAL;
                stepText.setTextColor(Color.BLACK);
                break;
            case R.id.action_map_satellite:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, 4).commit();
                map_type = GoogleMap.MAP_TYPE_HYBRID;
                stepText.setTextColor(Color.WHITE);
                break;
            case R.id.action_speed:
                if(playStatus == 1) {
                    Log.i("info","STOP PLAY");
                    showTrackingView(View.VISIBLE);
                    play.setImageResource(R.drawable.play_button_selector);
                    next.setVisibility(View.VISIBLE);
                    previous.setVisibility(View.VISIBLE);
                    playStatus = 2;
                    if (playTimer != null) {
                        playTimer.cancel();
                        playTimer = null;
                        playTask.run = false;
                        playTask.cancel();
                        playTask = null;
                    }
                }
                SpeedRouteDialog dialog = new SpeedRouteDialog(route_speed);
                dialog.show(getFragmentManager(), "SetSpeedRouteDialog");
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
                    //currentIndex = 1;
                    map.clear();
                    setPolyLineOptions(polylineOptions);
                    polyline = map.addPolyline(polylineOptions);
                    map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pink))
                            .position(firstPoint)
                            .title(" Start")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    currentMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top_37x37))
                            .position(firstPoint)
                            .anchor(0.5f,0.5f)
                            .rotation(gpsDatas.get(currentIndex).direction)
                            .title(" Finish")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    currentMarker.setVisible(false);
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
                Log.i("info"," NEXT");
                showTrackingView(View.VISIBLE);
                if(currentIndex < (gpsPoints.size()-1)){
                    currentIndex++;
                    polylineOptions.add(gpsPoints.get(currentIndex));
                    setPolyLineOptions(polylineOptions);
                    map.clear();
                    polyline = map.addPolyline(polylineOptions);
                    addFirstLast(gpsPoints.get(currentIndex));
                    map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex)));
                    updateTrackingView();
                    stepText.setText(Integer.toString(currentIndex) + "(" + Integer.toString(gpsPoints.size()) + ")");
                }
                break;
            case R.id.button_previous:
                Log.i("info"," BACK");
                if (currentIndex > 0){
                    showTrackingView(View.VISIBLE);
                    currentIndex--;
                    map.clear();
                    polylineOptions = new PolylineOptions();
                    setPolyLineOptions(polylineOptions);
                    polyline.remove();
                    for (int i = 0; i<=currentIndex; i++) {
                        polylineOptions.add(gpsPoints.get(i));
                    }
                    polyline = map.addPolyline(polylineOptions);
                    firstMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pink))
                            .position(firstPoint)
                            .title(" Start")
                            .snippet("Lat " + Double.toString(firstPoint.latitude) + " Lng : " + Double.toString(firstPoint.longitude)));
                    currentMarker = map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top_37x37))
                            .position(gpsPoints.get(currentIndex))
                            .title(" Finish")
                            .snippet("Lat " + Double.toString(gpsPoints.get(currentIndex).latitude)
                                    + " Lng : " + Double.toString(gpsPoints.get(currentIndex).latitude)));
                    if(currentMarker.getPosition().latitude == firstMarker.getPosition().latitude) {
                        currentMarker.setVisible(false);
                    }

                    map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex)));
                    updateTrackingView();
                    stepText.setText(Integer.toString(currentIndex) + "(" + Integer.toString(gpsPoints.size()) + ")");
                }
                break;
        }
    }

    @Override
    public void onPositiveButtonSpeedRoute(int value) {
        route_speed = value;
        sharedPreferences.edit().putInt(PREF_SPEED_ROUTE,route_speed).commit();
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
            nameValuePairList.add(new BasicNameValuePair("start_time",dateFrom));
            nameValuePairList.add(new BasicNameValuePair("end_time",dateTo));

            Log.i("info","GPS ID    : " + currentVehicle.gps_id);
            Log.i("info","DATE FROM : " + dateFrom);
            Log.i("info","DATE FROM : " + dateTo);
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
                    double lat = Double.parseDouble(tmpData.get(0).lat);
                    double lng = Double.parseDouble(tmpData.get(0).lng);
                    firstPoint = new LatLng(lat,lng);
                    polylineOptions.add(firstPoint);
                    //lat = Double.parseDouble(tmpData.get(tmpData.size()-1).lat);
                    //lng = Double.parseDouble(tmpData.get(tmpData.size()-1).lng);
                    lastPoint = new LatLng(lat,lng);

                    for(int i = 0; i<tmpData.size(); i++) {
                        lat = Double.parseDouble(tmpData.get(i).lat);
                        lng = Double.parseDouble(tmpData.get(i).lng);
                        gpsPoints.add(new LatLng(lat,lng));
                        Log.i("info", "--------------------------------------------------------------------");
                        Log.i("info","terminal_id : " + tmpData.get(i).gps_id);
                        Log.i("info","speed       : " + tmpData.get(i).speed);
                        Log.i("info","lat         : " + tmpData.get(i).lat);
                        Log.i("info","lng         : " + tmpData.get(i).lng);
                        Log.i("info","--------------------------------------------------------------------");
                    }
                    if(gpsPoints != null && (!gpsPoints.isEmpty())) {
                        stepText.setText("0" + "(" + Integer.toString(gpsPoints.size()) + ")");
                        playStatus = 2;
                        play.setImageResource(R.drawable.play_button_selector);
                        play.setVisibility(View.VISIBLE);
                        next.setVisibility(View.VISIBLE);
                        previous.setVisibility(View.VISIBLE);

                        // Map
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstPoint, currentZoom);
                        map.moveCamera(cameraUpdate);
                        currentMarker = map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top_37x37))
                                .anchor(0.5f, 0.5f)
                                .position(firstPoint).title(" Finish")
                                .snippet("Lat " + Double.toString(firstPoint.latitude)
                                        + " Lng : " + Double.toString(firstPoint.longitude)));
                        firstMarker = map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pink))
                                .position(firstPoint)
                                .title(" Start")
                                .snippet("Lat " + Double.toString(firstPoint.latitude)
                                        + " Lng : " + Double.toString(firstPoint.longitude)));
                        currentMarker.setVisible(false);
                    }
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(),"...no data",Toast.LENGTH_SHORT);
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
            //Log.i("info", " PLAY TIMER RUN : ");
            //Log.i("info", " points count = " + Integer.toString(gpsPoints.size()));
            int i = 0;
            while (run) {
                SystemClock.sleep(route_speed * 1000 / 2);
                Log.i("info", " TICK : " + Integer.toString(i) + " : speed =  " + Integer.toString(route_speed * 1000 / 2));
                i++;
                if(currentIndex < gpsPoints.size()) {
                    Log.i("info"," currentIndex : " + Integer.toString(currentIndex));
                    Log.i("info"," points size = : " + gpsPoints.size());
                    Log.i("info"," data size   = : " + gpsDatas.size());
                    Log.i("info","------------------------------------------");
                    polylineOptions.add(gpsPoints.get(currentIndex));
                    setPolyLineOptions(polylineOptions);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i("info", " PLAY TIMER RUN UI : update map");
                            //Log.i("info", " PLAY TIMER RUN UI : current index = " + Integer.toString(currentIndex));
                            map.clear();
                            polyline = map.addPolyline(polylineOptions);
                            addFirstLast(gpsPoints.get(currentIndex));
                            //map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex-1)));
                            map.animateCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex)));
                            map.moveCamera(CameraUpdateFactory.newLatLng(gpsPoints.get(currentIndex)));
                            updateTrackingView();
                            /*currentMarker.setPosition(gpsPoints.get(currentIndex-1));
                            currentMarker.setSnippet("Lat " + Double.toString(gpsPoints.get(currentIndex-1).latitude)
                                    + " Lng : " + Double.toString(gpsPoints.get(currentIndex-1).longitude));*/
                            stepText.setText(Integer.toString(currentIndex) + "(" + Integer.toString(gpsPoints.size()) + ")");
                            currentIndex++;
                        }
                    });
                    //currentIndex++;
                }else {
                    Log.i("info"," currentIndex : " + Integer.toString(currentIndex));
                    Log.i("info"," points size = : " + gpsPoints.size());
                    Log.i("info"," data size   = : " + gpsDatas.size());
                    Log.i("info","++++++++++++++++++++++++++++++++++++++++");
                    run = false;
                    this.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i("info", " PLAY TIMER FINISH UI : return button");
                            play.setImageResource(R.drawable.play_button_selector);
                            next.setVisibility(View.VISIBLE);
                            previous.setVisibility(View.VISIBLE);
                            playStatus = 2;
                            currentMarker.setPosition(gpsPoints.get(currentIndex-1/*-1*/));
                            currentMarker.setSnippet("Lat " + Double.toString(firstPoint.latitude)
                                    + " Lng : " + Double.toString(firstPoint.longitude));
                            currentMarker.setVisible(true);
                            //currentMarker.setRotation(gpsDatas.get(currentIndex).direction);
                            //currentMarker.setRotation();
                        }
                    });
                }
            }
            Log.i("info", " PLAY TIMER STOP : ");
        }
        public boolean run = true;
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    private void addFirstLast(LatLng currentLatLog) {
        LatLng prev = null;
        try{
            double lat = Double.parseDouble(gpsDatas.get(currentIndex-1).lat);
            double lng = Double.parseDouble(gpsDatas.get(currentIndex-1).lng);
            prev = new LatLng(lat,lng);
        }catch (IndexOutOfBoundsException e){
            double lat = Double.parseDouble(gpsDatas.get(currentIndex).lat);
            double lng = Double.parseDouble(gpsDatas.get(currentIndex).lng);
            prev = new LatLng(lat,lng);
        }

        currentMarker = map.addMarker(new MarkerOptions().position(currentLatLog)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top_37x37))
                .anchor(0.5f,0.5f)
                .rotation(gpsDatas.get(currentIndex).direction)
                .title("End").snippet(
                        "Lat " + Double.toString(currentLatLog.latitude)
                                + " Lng : " + Double.toString(currentLatLog.longitude)
                ));

        firstMarker = map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pink))
                .position(firstPoint)
                .title("Start").snippet(
                "Lat " + Double.toString(firstPoint.latitude)+ " Lng : " + Double.toString(firstPoint.longitude)
        ));

        if(currentMarker.getPosition().latitude == firstMarker.getPosition().latitude) {
            currentMarker.setVisible(false);
        }else {
            currentMarker.setVisible(true);
            /*try {
                currentMarker.setRotation(gpsDatas.get(currentIndex).direction);
            }catch (IndexOutOfBoundsException e) {

            }*/
        }
    }
    private void updateTrackingView() {
        //Log.i("info"," updateTrackingView");
        if(currentIndex == gpsDatas.size())
            return;

        int index = currentIndex;
        String date = gpsDatas.get(currentIndex).gps_time.substring(0,4) + "/" +
                gpsDatas.get(index).gps_time.substring(4,6) + "/"
                + gpsDatas.get(index).gps_time.substring(6,8) +" "
                + gpsDatas.get(index).gps_time.substring(8,10) +":"
                + gpsDatas.get(index).gps_time.substring(10,12) +":"
                + gpsDatas.get(index).gps_time.substring(12,14);

        textDateTime.setText(date);
        textLatitude.setText(Double.toString(gpsPoints.get(currentIndex).latitude));
        textLongitude.setText(Double.toString((float) gpsPoints.get(currentIndex).longitude));
        textSpeed.setText(Float.toString(gpsDatas.get(currentIndex).speed) + " km/h");
    }
    private void showTrackingView(int visible) {
        trackingView.setVisibility(visible);
    }

    private void setPolyLineOptions(PolylineOptions polylineOptions) {
        polylineOptions.color(Color.argb(lineAlpha,0,0,255));
        polylineOptions.width(8);
        polylineOptions.zIndex(300);
    }
}
