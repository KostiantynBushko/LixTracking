package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.lixtracking.lt.parsers.GpsData;
import com.lixtracking.lt.parsers.ParseGpsData;
import com.lixtracking.lt.parsers.VehicleData;

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

public class FragmentMap extends Fragment{
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
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = layoutInflater.inflate(R.layout.fragment_map, container, false);
            //Setup google here
            if (map == null) {
                // Try to obtain the map from the SupportMapFragment.
                map = ((SupportMapFragment) MainActivity.fragmentManager
                        .findFragmentById(R.id.map)).getMap();
                if(map != null) {
                    map.setMapType(1);
                }
            }

        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        context = getActivity();
        return view;
    }
    @Override
    public void onResume() {
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        super.onResume();
        if(!updateIsRunning){
            new Thread(new Runnable() {
                int count = vehicleDatas.size();
                @Override
                public void run() {
                    while (vehicleDatas == null){
                        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
                    }
                    int coun = vehicleDatas.size();
                    int i = 0;
                    /*while (updateDataList() == false) {
                        Log.i("info"," get next");
                        SystemClock.sleep(500);
                    }*/
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
    private boolean updateDataList () {
        vehicleDatas = ((MainActivity)getActivity()).getVehicle();
        if(vehicleDatas == null)
            return false;
        for(int i = 0; i<vehicleDatas.size(); i++) {
            new getRealTimeGpsData().execute(vehicleDatas.get(i).gps_id, Integer.toString(i));
        }
        return true;
    }
    private void updateMarker() {

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
            Log.i("info", " START: getVehiclesTask");
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
                Log.i("info","--------------------------------------------------------------------");
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
                });

                List<GpsData> tmpData = new ParseGpsData(context).parceXml(resultString);
                if(gpsDatas == null)
                    gpsDatas = new ArrayList<GpsData>();
                if(tmpData != null)
                    gpsDatas.addAll(tmpData);

                if(map != null) {
                    for(int i = 0; i<tmpData.size(); i++) {
                        float lat = Float.parseFloat(tmpData.get(i).lat);
                        float lng = Float.parseFloat(tmpData.get(i).lng);

                        if(firstActive == null) {
                            firstActive = tmpData.get(i);
                            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
                        }
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
                            );
                            markerList.add(marker);
                        }
                    }
                }
            }
            updateIsRunning = false;
        }
    }

}
