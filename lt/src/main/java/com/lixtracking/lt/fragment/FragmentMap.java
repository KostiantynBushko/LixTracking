package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.VehicleDetailInfoActivity;
import com.lixtracking.lt.common.Constant;
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

public class FragmentMap extends Fragment implements GoogleMap.OnInfoWindowClickListener{
    private static final String PREF_MAP_TYPE = "pref_map_type";
    private int map_type = GoogleMap.MAP_TYPE_NORMAL;
    private SharedPreferences sharedPreferences;
    private GoogleMap map = null;
    private float currentZoom = Constant.mapZoom;
    View view = null;
    List<VehicleData> vehicleDatas = null;
    List<GpsData> gpsDatas = new ArrayList<GpsData>();
    GpsData firstActive = null;
    List<Marker>markerList = new ArrayList<Marker>();
    List<Marker>activeMarker = new ArrayList<Marker>();
    private int currentMarker = 0;

    private boolean updateIsRunning = false;
    Context context = null;

    //Scheduler update
    private static int currentIndex = 0;
    private Timer updateTimer = null;
    private UpdateTik updateScheduler = null;
    private int updateInterval = 5000;

    TextView indicator = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = layoutInflater.inflate(R.layout.fragment_map, container, false);
            indicator = (TextView)view.findViewById(R.id.textView);

            String s = this.getClass().getSimpleName();
            sharedPreferences = getActivity().getSharedPreferences(this.getClass().getSimpleName(),Context.MODE_PRIVATE);
            map_type = sharedPreferences.getInt(PREF_MAP_TYPE, map_type);
            //Setup google here
            if (map == null) {
                // Try to obtain the map from the SupportMapFragment.
                map = ((SupportMapFragment) MainActivity.fragmentManager.findFragmentById(R.id.map)).getMap();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(Constant.baseLatLng, currentZoom));
                if(map != null) {
                    map.setMapType(map_type);
                    map.setOnInfoWindowClickListener(this);
                }
            }
        } catch (InflateException e) {
            e.printStackTrace();
        }
        /******************************************************************************************/
        ((ImageButton)view.findViewById(R.id.imageButton2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((markerList.size()-1) > (currentMarker)) {
                    //markerList.get(currentMarker).getPosition().latitude == -100 && (markerList.size()-1) > (currentMarker)
                    while (!markerList.get(currentMarker).isVisible() && (markerList.size()-1) > currentMarker) {
                        currentMarker++;
                        Log.i("info"," .. " + Integer.toString(currentIndex));
                    }
                    if((markerList.size()-1) >= (currentMarker)) {
                        if(markerList.get(currentMarker).isVisible()){
                            markerList.get(currentMarker).showInfoWindow();
                            map.animateCamera(CameraUpdateFactory.newLatLng(markerList.get(currentMarker).getPosition()));
                            currentMarker++;
                        }
                    }
                }else {
                    currentMarker = 0;
                    Log.i("info"," + NO MARKER");
                }
            }
        });
        ((ImageButton)view.findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((currentMarker) > 0 && (markerList.size() > 0)) {
                    while (!markerList.get(currentMarker).isVisible() && currentMarker > 0) {
                        currentMarker--;
                        Log.i("info"," .. " + Integer.toString(currentIndex));
                    }
                    if((currentMarker > 0) && (markerList.size() > 0)) {
                        if(markerList.get(currentMarker).isVisible()){
                            markerList.get(currentMarker).showInfoWindow();
                            map.animateCamera(CameraUpdateFactory.newLatLng(markerList.get(currentMarker).getPosition()));
                            currentMarker--;
                        }
                    }
                }else {
                    currentMarker = markerList.size()-1;
                    Log.i("info"," - NO MARKER");
                }
            }
        });
        /******************************************************************************************/
        context = getActivity();
        return view;
    }
    /**********************************************************************************************/
    /* RESUME */
    /**********************************************************************************************/
    @Override
    public void onResume() {
        Log.i("info"," FragmentMap RESUME");
        super.onResume();
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        if(vehicleDatas != null) {
            gpsDatas = new ArrayList<GpsData>(vehicleDatas.size());
            startUpdateTask();
        }
    }
    @Override
    public void onPause() {
        Log.i("info"," FragmentMap PAUSE");
        super.onPause();
        stopUpdateTask();
    }
    @Override
    public void onStop() {
        Log.i("info"," FragmentMap STOP");
        super.onStop();
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    private void stopUpdateTask() {
        Log.i("info"," STOP UpdateTask");
        if(updateTimer != null) {
            updateTimer.cancel();
            updateScheduler.cancel();
            updateTimer = null;
            updateScheduler = null;
        }
    }
    private boolean startUpdateTask() {
        Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Start monitoring "
                + Integer.toString(vehicleDatas.size()) + " vehicle",Toast.LENGTH_LONG);
        toast.show();
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        if((vehicleDatas != null) && (!vehicleDatas.isEmpty())) {
            updateTimer = new Timer();
            updateScheduler = new UpdateTik();
            updateTimer.scheduleAtFixedRate(updateScheduler, 100, updateInterval);
            return true;
        }
        return false;
    }
    class UpdateTik extends TimerTask {
        public boolean run = true;
        @Override
        public void run() {
            if(!updateIsRunning) {
                Log.i("info"," Update task tick : " + Integer.toString(currentIndex));
                if(currentIndex >= vehicleDatas.size() ) {
                    currentIndex = 0;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new getRealTimeGpsData().execute(vehicleDatas.get(currentIndex).gps_id, Integer.toString(currentIndex));
                    }
                });
            }
        }
    }
    /**********************************************************************************************/
    /* MENU */
    /**********************************************************************************************/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map,menu);
        super.onCreateOptionsMenu(menu,inflater);
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
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, 4).commit();
                break;
        }
        return true;
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    @Override
    public void onInfoWindowClick(Marker marker) {
        String marker_title = marker.getTitle().substring(6);
        Log.i("info"," TITLE :" +  marker.getTitle());
        Log.i("info"," VIN :" +  marker_title);
        for(int i = 0; i<vehicleDatas.size(); i++) {
            if(marker_title.compareTo(vehicleDatas.get(i).vin) == 0) {
                Log.i("info"," NUMBER : " + Integer.toString(i));
                Intent intent = new Intent(getActivity(), VehicleDetailInfoActivity.class);
                intent.putExtra(VehicleData.GPS_ID, vehicleDatas.get(i).gps_id);
                intent.putExtra(VehicleData.VIN,vehicleDatas.get(i).vin);
                intent.putExtra(VehicleData.USER_ID,vehicleDatas.get(i).user_id);
                intent.putExtra(VehicleData.STOCK_NUMBER,vehicleDatas.get(i).stock_number);
                intent.putExtra(VehicleData.FIRST_NAME,vehicleDatas.get(i).first_name);
                intent.putExtra(VehicleData.LAST_NAME,vehicleDatas.get(i).last_name);
                intent.putExtra(VehicleData.MODEL,vehicleDatas.get(i).model);
                intent.putExtra(VehicleData.MAKE,vehicleDatas.get(i).make);
                intent.putExtra(VehicleData.STATUS,vehicleDatas.get(i).status);
                intent.putExtra(VehicleData.YEAR,vehicleDatas.get(i).year);
                //intent.putExtra(GpsData.GPS_ID, gpsDatas.get(i).gps_id);
                //intent.putExtra(GpsData.SPEED,gpsDatas.get(i).speed);
                //intent.putExtra(GpsData.GPS_TIME,gpsDatas.get(i).gps_time);
                getActivity().startActivity(intent);
                break;
            }
        }
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getRealTimeGpsData extends AsyncTask<String, Void, String> {
        private String resultString = "...";
        private String message = "";
        private String index = "";
        @Override
        protected void onPreExecute() {
            indicator.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... params) {
            updateIsRunning = true;
            index = params[1];
            Log.i("info", " START: getRealTimeGpsData FragmentMap");
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.getRealTimeGpsDataUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("terminal_id", params[0]));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                resultString = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                message = "Server does not respond";
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultString;
        }
        @Override
        protected void onPostExecute(String result) {
            if(resultString == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Message: " + index);
                builder.setMessage(message);
                builder.setCancelable(true);
                builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                List<GpsData> tmpData = new ParseGpsData(context).parceXml(resultString);
                if(map != null) {
                    if(tmpData == null || tmpData.isEmpty()){
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(0.0,0.0))
                                .title(" GPS ID : -")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_gray))
                                .snippet("speed : -"));
                        markerList.add(marker);
                         marker.setVisible(false);
                    }else{
                        Log.i("info"," : ");
                        try{
                            gpsDatas.set(currentIndex,tmpData.get(0));
                        }catch (IndexOutOfBoundsException e) {
                            gpsDatas.add(tmpData.get(0));
                        }
                        float lat = Float.parseFloat(tmpData.get(0).lat);
                        float lng = Float.parseFloat(tmpData.get(0).lng);
                        //Log.i("info","lat :" + Float.toString(lat));
                        //Log.i("info","lng :" + Float.toString(lng));
                        LatLng latLon = new LatLng(lat,lng);

                        int r = R.drawable.marker_car_gray;
                        if(vehicleDatas.get(Integer.parseInt(index)).status == 1)
                            r = R.drawable.marker_car;

                        if(markerList.size() < vehicleDatas.size()) {
                            Marker marker = map.addMarker(new MarkerOptions()
                                            .position(latLon)
                                            .title("VIN : " + vehicleDatas.get(currentIndex).vin)
                                            .icon(BitmapDescriptorFactory.fromResource(r))
                                            .snippet("speed : " + tmpData.get(0).speed + " km/h")
                            );
                            markerList.add(marker);
                            if(lat == 0.0 || lng == 0.0) {
                                marker.setVisible(false);
                            }else {
                                marker.setVisible(true);
                            }
                            //Log.i("info"," Add new marker - ");
                            if(firstActive == null) {
                                firstActive = tmpData.get(0);
                                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                                if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_MAP)
                                    marker.showInfoWindow();
                            }
                        }else {
                            if((lng != -100.0f) && (lng != -100.0f)) {
                                markerList.get(currentIndex).setPosition(latLon);
                                markerList.get(currentIndex).setTitle(" VIN : " + vehicleDatas.get(currentIndex).vin);
                                markerList.get(currentIndex).setSnippet("speed : " + tmpData.get(0).speed  + " km/h");
                                markerList.get(currentIndex).setIcon(BitmapDescriptorFactory.fromResource(r));
                            }
                            if(lat == 0.0 || lng == 0.0) {
                                markerList.get(currentIndex).setVisible(false);
                            }else {
                                markerList.get(currentIndex).setVisible(true);
                            }
                            //Log.i("info"," Update exist marker - ");
                        }
                    }
                    currentIndex++;
                }
            }
            updateIsRunning = false;
            indicator.setVisibility(View.INVISIBLE);
            /*Log.i("info"," currentIndex : " + Integer.toString(currentIndex));
            Log.i("info"," Marker list size : " + Integer.toString(markerList.size()));
            Log.i("info"," END: getRealTimeGpsData FragmentMap");
            Log.i("info","---------------------------------------------------------------------");*/
        }
    }

    /**********************************************************************************************/
    class PopupAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater=null;
        PopupAdapter(LayoutInflater inflater) {
            this.inflater=inflater;
        }
        @Override
        public View getInfoWindow(Marker marker) {
            return(null);
        }
        @Override
        public View getInfoContents(Marker marker) {
            View view = getLayoutInflater(null).inflate(R.layout.map_info_window, null);
            TextView title = ((TextView)view.findViewById(R.id.textView));
            title.setText(marker.getTitle());
            ((TextView)view.findViewById(R.id.textView2)).setText(marker.getSnippet());
            return view;
        }
    }
}
