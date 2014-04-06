package com.lixtracking.lt.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.TrackingHistoryActivity;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.data_class.VehicleData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by saiber on 01.04.2014.
 */
public class FragmentHistory extends Fragment {
    private static final String TITLE = "title";
    private static final String FROM = "from";
    private static final String TO = "to";

    private View view;
    public VehicleData vehicleData = null;
    private ListView listView;

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
        listView = (ListView)view.findViewById(R.id.listView);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        ArrayList<HashMap<String, Object>> listObjects = new ArrayList<HashMap<String, Object>>();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        SimpleDateFormat month = new SimpleDateFormat("MM");
        SimpleDateFormat day = new SimpleDateFormat("dd");

        Calendar c = Calendar.getInstance();
        String fromDatae = year.format(c.get(Calendar.YEAR)) + "-" + month.format(c.get(Calendar.MONTH)) + "-"
                + day.format(c.get(Calendar.DAY_OF_MONTH));
        fromDatae += " 00-00-01";
        String toDatae = format.format(new Date());

        HashMap<String, Object>item = new HashMap<String, Object>();
        item.put(TITLE,"Today");
        item.put(FROM, fromDatae);
        item.put(TO, toDatae);
        listObjects.add(item);

        item = new HashMap<String, Object>();
        item.put(TITLE,"Past three day");
        item.put(FROM, fromDatae);
        item.put(TO, toDatae);
        listObjects.add(item);

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.select_history_item,
                new String[]{TITLE,FROM,TO,},
                new int[]{R.id.text1, R.id.text2,R.id.text3});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
}
