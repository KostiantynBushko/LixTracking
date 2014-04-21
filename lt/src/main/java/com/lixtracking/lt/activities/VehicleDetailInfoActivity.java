package com.lixtracking.lt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

/**
 * Created by saiber on 13.04.2014.
 */
public class VehicleDetailInfoActivity extends Activity{
    private VehicleData vehicleData = new VehicleData();
    private GpsData gpsData = new GpsData();
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_detail_info_layout);
        context = this;
        Intent intent = getIntent();
        vehicleData.vin = intent.getStringExtra(VehicleData.VIN);
        vehicleData.gps_id = intent.getStringExtra(VehicleData.GPS_ID);
        vehicleData.first_name = intent.getStringExtra(VehicleData.FIRST_NAME);
        vehicleData.last_name = intent.getStringExtra(VehicleData.LAST_NAME);
        vehicleData.make = intent.getStringExtra(VehicleData.MAKE);
        vehicleData.model = intent.getStringExtra(VehicleData.MODEL);
        vehicleData.stock_number = intent.getStringExtra(VehicleData.STOCK_NUMBER);
        vehicleData.user_id = intent.getStringExtra(VehicleData.USER_ID);
        vehicleData.status = intent.getIntExtra(VehicleData.STATUS,0);
        vehicleData.year = intent.getIntExtra(VehicleData.YEAR,0);
        //gpsData.gps_id = intent.getStringExtra(GpsData.GPS_ID);
        //gpsData.speed = intent.getIntExtra(GpsData.SPEED,0);
        //gpsData.direction = intent.getFloatExtra(GpsData.DIRECTION,0.0f);


        ((TextView)findViewById(R.id.text1)).setText(vehicleData.first_name + " " + vehicleData.last_name);
        ((TextView)findViewById(R.id.text2)).setText("user id : " + vehicleData.user_id);
        ((TextView)findViewById(R.id.text3)).setText("VIN : " + vehicleData.vin);
        ((TextView)findViewById(R.id.textView5)).setText(vehicleData.model);
        ((TextView)findViewById(R.id.textView7)).setText(vehicleData.make);
        ((TextView)findViewById(R.id.textView10)).setText(Integer.toString(vehicleData.year));
        ((TextView)findViewById(R.id.textView13)).setText(vehicleData.stock_number);
        ((TextView)findViewById(R.id.tv_gps_id)).setText("GPS ID : " + vehicleData.gps_id);

        new getRealTimeGpsData().execute(vehicleData.gps_id);

    }

    class getRealTimeGpsData extends AsyncTask<String, Void, String> {
        private String resultString = "...";
        private String message = "";
        private String index = "";
        //@Override
        //protected void onPreExecute() { indicator.setVisibility(View.VISIBLE);}
        @Override
        protected String doInBackground(String... params) {
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
                gpsData = tmpData.get(0);
                ((TextView)findViewById(R.id.tv_speed)).setText(Float.toString(gpsData.speed) + " km/h");
                ((TextView)findViewById(R.id.tv_direction)).setText(Float.toString(gpsData.direction) + "\u00b0");
                Log.i("info"," GPS TIME : " + gpsData.gps_time);
                Log.i("info"," GPS ID : " + gpsData.gps_id);
                String date = gpsData.gps_time.substring(0,4) + "/" +
                        gpsData.gps_time.substring(4,6) + "/"
                        + gpsData.gps_time.substring(6,8) +" "
                        + gpsData.gps_time.substring(8,10) +":"
                        + gpsData.gps_time.substring(10,12) +":"
                        + gpsData.gps_time.substring(12,14);
                ((TextView)findViewById(R.id.tv_gps_time)).setText(date);
            }
        }
    }
}