package com.lixtracking.lt.fragment;

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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.activities.VehicleDetailInfoActivity;
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
 * Created by saiber on 01.04.2014.
 */
public class FragmentTracking extends Fragment {
    private static final String PREF_MAP_TYPE = "pref_map_type";
    private int map_type = GoogleMap.MAP_TYPE_NORMAL;
    private SharedPreferences sharedPreferences;
    private AsyncTask realTimeGpsData = null;

    private GoogleMap map = null;
    View view = null;
    VehicleData vehicleData = null;
    List<GpsData> gpsDatas = null;
    GpsData firstActive = null;
    List<Marker>markerList = new ArrayList<Marker>();
    Timer timer;

    private boolean updateIsRunning = false;
    Context context = null;
    Marker marker = null;

    TextView indicator = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        Log.i("info"," ON CRESTE Fragment Tracking");
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
            map = null;
            if (map == null) {
                // Try to obtain the map from the SupportMapFragment.
                map = ((SupportMapFragment) VehicleDetailActivity.fragmentManager
                        .findFragmentById(R.id.map)).getMap();
                if(map != null) {
                    map.setMapType(map_type);
                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(getActivity(), VehicleDetailInfoActivity.class);
                            intent.putExtra(VehicleData.GPS_ID,vehicleData.gps_id);
                            intent.putExtra(VehicleData.VIN,vehicleData.vin);
                            intent.putExtra(VehicleData.USER_ID,vehicleData.user_id);
                            intent.putExtra(VehicleData.STOCK_NUMBER,vehicleData.stock_number);
                            intent.putExtra(VehicleData.FIRST_NAME,vehicleData.first_name);
                            intent.putExtra(VehicleData.LAST_NAME,vehicleData.last_name);
                            intent.putExtra(VehicleData.MODEL,vehicleData.model);
                            intent.putExtra(VehicleData.MAKE,vehicleData.make);
                            intent.putExtra(VehicleData.STATUS,vehicleData.status);
                            intent.putExtra(VehicleData.YEAR,vehicleData.year);
                            getActivity().startActivity(intent);
                        }
                    });
                }
            }
        } catch (InflateException e) {
            e.printStackTrace();
        }
        context = getActivity();
        return view;
    }

    @Override
    public void onResume() {
        vehicleData = ((VehicleDetailActivity)getActivity()).vehicleData;
        super.onResume();
        timer = new Timer();
        TimerTask timerTask = new Task();
        timer.scheduleAtFixedRate(timerTask, 1, 1500);
    }
    /**********************************************************************************************/
    /* Tracking task */
    /**********************************************************************************************/
    class Task extends TimerTask {
        @Override
        public void run() {
            if(updateIsRunning == false){
                if(vehicleData != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            realTimeGpsData = new getRealTimeGpsData().execute(((VehicleDetailActivity)getActivity()).vehicleData.gps_id);
                        }
                    });
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        realTimeGpsData.cancel(true);
        timer.cancel();
        timer = null;
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
    class getRealTimeGpsData extends AsyncTask<String, Void, String> {
        private String resultString = "";
        private String message = "";
        //private String index = "";
        @Override
        protected void onPreExecute() {
            indicator.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... params) {
            updateIsRunning = true;
            //index = params[1];
            Log.i("info", " START: getVehiclesTask");
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.getRealTimeGpsDataUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            Log.i("info"," terminal_id = " + params[0]);
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
                Log.i("info","--------------------------------------------------------------------");
                Log.i("info","result : " + resultString);
                Log.i("info","--------------------------------------------------------------------");
                List<GpsData> tmpDatas = new ParseGpsData(context).parceXml(resultString);
                if((tmpDatas != null) && (!tmpDatas.isEmpty())) {
                    gpsDatas = tmpDatas;
                    if(map != null) {
                        float lat = Float.parseFloat(gpsDatas.get(0).lat);
                        float lng = Float.parseFloat(gpsDatas.get(0).lng);

                        if(lat != 0.0f && lng != 0.0f) {
                            LatLng latLon = new LatLng(lat,lng);
                            boolean isShow = false;
                            if((marker != null) && marker.isInfoWindowShown()){
                                isShow = true;
                            }
                            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

                            int r = R.drawable.marker_car_gray;
                            if(vehicleData.status == 1) {
                                r = R.drawable.marker_car;
                            }
                            if(marker == null) {
                                marker = map.addMarker(new MarkerOptions()
                                                .position(latLon)
                                                .title(" VIN : " + vehicleData.vin)
                                                .icon(BitmapDescriptorFactory.fromResource(r))
                                                .snippet("speed : " + gpsDatas.get(0).speed)
                                );
                            } else {
                                marker.setPosition(latLon);
                                marker.setTitle(" VIN : " + vehicleData.vin);
                                marker.setSnippet("speed : " + gpsDatas.get(0).speed);
                                marker.setIcon(BitmapDescriptorFactory.fromResource(r));
                                if(isShow){
                                    marker.showInfoWindow();
                                }
                            }
                        }
                    }
                }
            }
            updateIsRunning = false;
            indicator.setVisibility(View.INVISIBLE);
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
