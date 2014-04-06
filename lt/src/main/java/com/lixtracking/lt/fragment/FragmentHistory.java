package com.lixtracking.lt.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.TrackingHistoryActivity;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.data_class.VehicleData;

/**
 * Created by saiber on 01.04.2014.
 */
public class FragmentHistory extends Fragment {
    private View view;
    public VehicleData vehicleData = null;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        view = layoutInflater.inflate(R.layout.fragment_history, container, false);

        vehicleData = ((VehicleDetailActivity)getActivity()).vehicleData;

        ((Button)view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TrackingHistoryActivity.class);
                intent.putExtra(VehicleData.VIN, vehicleData.vin);
                intent.putExtra(VehicleData.GPS_ID, vehicleData.gps_id);
                intent.putExtra(VehicleData.USER_ID, vehicleData.user_id);
                intent.putExtra(VehicleData.FIRST_NAME, vehicleData.first_name);
                intent.putExtra(VehicleData.LAST_NAME, vehicleData.last_name);
                intent.putExtra(VehicleData.MAKE, vehicleData.make);
                intent.putExtra(VehicleData.MODEL, vehicleData.model);
                intent.putExtra(VehicleData.STOCK_NUMBER, vehicleData.stock_number);
                intent.putExtra(VehicleData.YEAR, vehicleData.year);
                intent.putExtra(VehicleData.STATUS, vehicleData.status);
                startActivity(intent);
            }
        });
        return view;
    }
}
