package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    View view = null;
    List<VehicleData> vehicleDatas = null;
    //List<GpsData> gpsDatas = null;
    GpsData firstActive = null;
    List<Marker>markerList = new ArrayList<Marker>();

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
                if(map != null) {
                    map.setMapType(map_type);
                    map.setOnInfoWindowClickListener(this);
                }
            }
        } catch (InflateException e) {
            e.printStackTrace();
        }
        context = getActivity();
        return view;
    }
    /**********************************************************************************************/
    /* RESUME */
    /**********************************************************************************************/
    @Override
    public void onResume() {
        Log.i("info"," FragmentMap RESUME");
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        super.onResume();
        startUpdateTask();
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
                //new getRealTimeGpsData().execute(vehicleDatas.get(currentIndex).gps_id, Integer.toString(currentIndex));
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
                sharedPreferences.edit().putInt(PREF_MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID).commit();
                break;
        }
        return true;
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.i("info"," GPS ID :" + marker.getTitle());
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
                                .position(new LatLng(-100.0,-100.0))
                                .title(" GPS ID :")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car_gray))
                                .snippet("speed :"));
                        markerList.add(marker);
                    }else{
                        float lat = Float.parseFloat(tmpData.get(0).lat);
                        float lng = Float.parseFloat(tmpData.get(0).lng);
                        Log.i("info","lat :" + Float.toString(lat));
                        Log.i("info","lng :" + Float.toString(lng));
                        LatLng latLon = new LatLng(lat,lng);

                        int r = R.drawable.marker_car_gray;
                        if(vehicleDatas.get(Integer.parseInt(index)).status == 1)
                            r = R.drawable.marker_car;

                        if(markerList.size() < vehicleDatas.size()) {
                            Marker marker = map.addMarker(new MarkerOptions()
                                            .position(latLon)
                                            .title(" GPS ID : " + tmpData.get(0).gps_id)
                                            .icon(BitmapDescriptorFactory.fromResource(r))
                                            .snippet("speed : " + tmpData.get(0).speed)
                            );
                            markerList.add(marker);
                            if(lat == 0.0 || lng == 0.0) {
                                marker.setVisible(false);
                            }else {
                                marker.setVisible(true);
                            }
                            Log.i("info"," Add new marker - ");
                            if(firstActive == null) {
                                firstActive = tmpData.get(0);
                                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                                if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_MAP)
                                    marker.showInfoWindow();
                            }
                        }else {
                            if((lng != -100.0f) && (lng != -100.0f)) {
                                markerList.get(currentIndex).setPosition(latLon);
                                markerList.get(currentIndex).setTitle(" GPS ID : " + tmpData.get(0).gps_id);
                                markerList.get(currentIndex).setSnippet("speed : " + tmpData.get(0).speed);
                                markerList.get(currentIndex).setIcon(BitmapDescriptorFactory.fromResource(r));
                            }
                            if(lat == 0.0 || lng == 0.0) {
                                markerList.get(currentIndex).setVisible(false);
                            }else {
                                markerList.get(currentIndex).setVisible(true);
                            }
                            Log.i("info"," Update exist marker - ");
                        }
                    }
                    currentIndex++;
                }
            }
            updateIsRunning = false;
            indicator.setVisibility(View.INVISIBLE);
            Log.i("info"," currentIndex : " + Integer.toString(currentIndex));
            Log.i("info"," Marker list size : " + Integer.toString(markerList.size()));
            Log.i("info"," END: getRealTimeGpsData FragmentMap");
            Log.i("info","---------------------------------------------------------------------");
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
