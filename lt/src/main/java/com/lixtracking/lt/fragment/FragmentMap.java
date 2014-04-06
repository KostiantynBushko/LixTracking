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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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

public class FragmentMap extends Fragment implements GoogleMap.OnInfoWindowClickListener{
    private static final String PREF_MAP_TYPE = "pref_map_type";
    private int map_type = GoogleMap.MAP_TYPE_NORMAL;
    private SharedPreferences sharedPreferences;
    private GoogleMap map = null;
    View view = null;
    private MapView mapView;
    List<VehicleData> vehicleDatas = null;
    List<GpsData> gpsDatas = null;
    GpsData firstActive = null;
    List<Marker>markerList = new ArrayList<Marker>();

    private boolean updateIsRunning = false;
    Context context = null;

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
                    map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }
                        @Override
                        public View getInfoContents(Marker marker) {
                            View view = getLayoutInflater(null).inflate(R.layout.map_info_window, null);
                            TextView title = ((TextView)view.findViewById(R.id.textView));
                            title.setText(marker.getTitle());
                            ((TextView)view.findViewById(R.id.textView2)).setText(marker.getSnippet());
                            return view;
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
    /**********************************************************************************************/
    /* RESUME */
    /**********************************************************************************************/
    @Override
    public void onResume() {
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        super.onResume();
        if(!updateIsRunning){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (vehicleDatas == null){
                        //vehicleDatas = ((MainActivity)getActivity()).getVehicle();
                    }
                    int coun = vehicleDatas.size();
                    int i = 0;
                    while (i < coun) {
                        if (updateIsRunning == false) {
                            new getRealTimeGpsData().execute(vehicleDatas.get(i).gps_id, Integer.toString(i));
                            i++;
                        }
                    }
                }
            }).start();
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
    /*private boolean updateDataList () {
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        if(vehicleDatas == null)
            return false;
        for(int i = 0; i<vehicleDatas.size(); i++) {
            new getRealTimeGpsData().execute(vehicleDatas.get(i).gps_id, Integer.toString(i));
        }
        return true;
    }*/
    private void updateMarker() { /**/ }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    @Override
    public void onInfoWindowClick(Marker marker) {

    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getRealTimeGpsData extends AsyncTask<String, Void, String> {
        private String resultString = "...";
        private String message = "";
        private String index = "";
        @Override
        protected String doInBackground(String... params) {
            updateIsRunning = true;
            index = params[1];
            Log.i("info", " START: getRealTimeGpsData");
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
            Log.i("info"," END: getRealTimeGpsData");
            if(resultString == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Mesage: " + index);
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
                /*Log.i("info","--------------------------------------------------------------------");
                Log.i("info","result : " + resultString);
                Log.i("info","--------------------------------------------------------------------");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Mesage: " + index);
                builder.setMessage(resultString);
                builder.setCancelable(true);
                builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });*/
                List<GpsData> tmpData = new ParseGpsData(context).parceXml(resultString);
                if(gpsDatas == null)
                    gpsDatas = new ArrayList<GpsData>();
                if(tmpData != null)
                    gpsDatas.addAll(tmpData);

                if(map != null) {
                    for(int i = 0; i<tmpData.size(); i++) {
                        float lat = Float.parseFloat(tmpData.get(i).lat);
                        float lng = Float.parseFloat(tmpData.get(i).lng);

                        if((lng != 0) && (lng != 0)) {
                            LatLng latLon = new LatLng(lat,lng);
                            int r = R.drawable.car_na_32x32;
                            if(vehicleDatas.get(Integer.parseInt(index)).status == 1) {
                                r = R.drawable.car_32x32;
                            }
                            Marker marker = map.addMarker(new MarkerOptions()
                                            .position(latLon)
                                            .title(" GPS ID : " + tmpData.get(i).gps_id)
                                            .icon(BitmapDescriptorFactory.fromResource(r))
                                            .snippet("speed : " + tmpData.get(i).speed)
                            );
                            markerList.add(marker);
                            if(firstActive == null) {
                                firstActive = tmpData.get(i);
                                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                                if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_MAP)
                                    marker.showInfoWindow();
                            }
                        }
                    }
                }
            }
            updateIsRunning = false;
        }
    }

}
