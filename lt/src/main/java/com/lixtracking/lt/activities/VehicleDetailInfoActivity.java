package com.lixtracking.lt.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.lixtracking.lt.R;
import com.lixtracking.lt.data_class.VehicleData;

/**
 * Created by saiber on 13.04.2014.
 */
public class VehicleDetailInfoActivity extends Activity{
    private VehicleData vehicleData = new VehicleData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_detail_info_layout);

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

        ((TextView)findViewById(R.id.text1)).setText(vehicleData.first_name + " " + vehicleData.last_name);
        ((TextView)findViewById(R.id.text2)).setText("user id : " + vehicleData.user_id);
        ((TextView)findViewById(R.id.text3)).setText("VIN : " + vehicleData.vin);
        ((TextView)findViewById(R.id.textView5)).setText(vehicleData.model);
        ((TextView)findViewById(R.id.textView7)).setText(vehicleData.make);
        ((TextView)findViewById(R.id.textView10)).setText(Integer.toString(vehicleData.year));
        ((TextView)findViewById(R.id.textView13)).setText(vehicleData.stock_number);
    }
}