package com.lixtracking.lt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lixtracking.lt.R;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.GpsData;
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

/**
 * Created by saiber on 14.04.2014.
 */
public class AlertMapActivity extends Activity {
    private static final String ALERT_TIME = "alert_time";
    private static final String GPS_ID = "gps_id";
    private static final String USER_ID = "user_id";
    private static final String ALERT_ID = "alert_id";

    private String user_id = null;
    private String alert_id = null;
    private String alert_time = null;
    private List<GpsData> gpsData = null;

    private GoogleMap map = null;
    private Context context = null;

    private boolean isRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_map_activity);
        context = this;
        Intent intent = getIntent();
        user_id = intent.getStringExtra(USER_ID);
        alert_id = intent.getStringExtra(ALERT_ID);
        alert_time = intent.getStringExtra(ALERT_TIME);

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(4);
        map.animateCamera(CameraUpdateFactory.zoomTo(9));
        //UiSettings uiSettings = map.getUiSettings();
        //uiSettings.setZoomControlsEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetAlertGpsData().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_alert, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!isRunning) {
                    new GetAlertGpsData().execute();
                }
                break;
        }
        return true;
    }

    class GetAlertGpsData extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected void onPreExecute() {
            isRunning = true;
        }
        @Override
        protected String doInBackground(Void... voids) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.GetAlertGpsDataUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("alert_id",alert_id));
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
            Log.i("info", " ************************************************ ");
            Log.i("info", " ALERT RESULT : " + result);
            Log.i("info", " ************************************************ ");
            if(result != null) {
                gpsData = new ParseGpsData(context).parceXml(result);
                if((gpsData != null) && (!gpsData.isEmpty())) {
                    if(map != null) {
                        float lat = Float.parseFloat(gpsData.get(0).lat);
                        float lng = Float.parseFloat(gpsData.get(0).lng);

                        if(lat != 0.0f && lng != 0.0f) {
                            LatLng latLon = new LatLng(lat,lng);
                            map.clear();
                            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                            map.addMarker(new MarkerOptions()
                                            .position(latLon)
                                            .title("GPS ID : " + gpsData.get(0).gps_id)
                                            .snippet("time : " + gpsData.get(0).gps_time)
                            );
                        }
                    }
                }
            }
            isRunning = false;
        }
    }
}
