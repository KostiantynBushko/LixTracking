package com.lixtracking.lt.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.SetCustomDateTime;
import com.lixtracking.lt.activities.TrackingHistoryActivity;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.data_class.VehicleData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by saiber on 01.04.2014.
 */
public class FragmentHistory extends Fragment {
    private static final String TITLE = "title";
    private static final String FROM = "from";
    private static final String TO = "to";
    SharedPreferences sharedPreferences;

    private View view;
    public VehicleData vehicleData = null;
    private ListView listView;
    ArrayList<HashMap<String, Object>> listObjects = new ArrayList<HashMap<String, Object>>();
    SimpleAdapter adapter;
    String customFrom = null;
    String customTo = null;

    EditText textViewFrom = null;
    EditText textViewTo = null;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        view = layoutInflater.inflate(R.layout.fragment_history, container, false);
        sharedPreferences = getActivity().getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);

        vehicleData = ((VehicleDetailActivity)getActivity()).vehicleData;

        ((Button)view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //HashMap<String, Object> item = listObjects.get(3);
                String from = textViewFrom.getText().toString(); //item.get(FROM).toString();
                String to = textViewTo.getText().toString(); //item.get(TO).toString();
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
                intent.putExtra("FROM",from);
                intent.putExtra("TO",to);
                startActivity(intent);
            }
        });
        textViewFrom = (EditText)view.findViewById(R.id.editText);
        textViewFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SetCustomDateTime.class);
                intent.putExtra("START", true);
                intent.putExtra("FROM", customFrom);
                intent.putExtra("TO", customTo);
                startActivityForResult(intent, 1);
            }
        });
        textViewTo = (EditText)view.findViewById(R.id.editText2);
        textViewTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SetCustomDateTime.class);
                intent.putExtra("END", true);
                intent.putExtra("FROM", customFrom);
                intent.putExtra("TO", customTo);
                startActivityForResult(intent, 2);
            }
        });

        listObjects.clear();
        listView = (ListView)view.findViewById(R.id.listView);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar c = GregorianCalendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);

        String year = Integer.toString(y);
        String month = m < 10 ? "0" + Integer.toString(m) : Integer.toString(m);
        String day = d < 10 ? "0" + Integer.toString(d) : Integer.toString(d);
        String fromDataeToday = year + "/" + month + "/" + day + " " + "00:00:00";
        String toDataeToday = format.format(new Date());

        c.add(Calendar.DAY_OF_YEAR, -3);
        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH) + 1;
        d = c.get(Calendar.DAY_OF_MONTH);

        year = Integer.toString(y);
        month = m < 10 ? "0" + Integer.toString(m) : Integer.toString(m);
        day = d < 10 ? "0" + Integer.toString(d) : Integer.toString(d);

        String fromDataePast = year + "/" + month + "/" + day + " " + "00:00:00";
        String toDataePast = format.format(new Date());

        HashMap<String, Object>item = new HashMap<String, Object>();
        item.put(TITLE,"Today");
        item.put(FROM, fromDataeToday);
        item.put(TO, toDataeToday);
        listObjects.add(item);

        item = new HashMap<String, Object>();
        item.put(TITLE,"Past three day");
        item.put(FROM, fromDataePast);
        item.put(TO, toDataePast);
        listObjects.add(item);

        customFrom = sharedPreferences.getString(FROM,fromDataePast);
        customTo = sharedPreferences.getString(TO,toDataePast);

        textViewFrom.setText(customFrom);
        textViewTo.setText(customTo);
        /*item = new HashMap<String, Object>();
        item.put(TITLE,"Custom date time");
        item.put(FROM, customFrom);
        item.put(TO, customTo);
        listObjects.add(item);*/

        adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.select_history_item,
                new String[]{TITLE,FROM,TO,},
                new int[]{R.id.text1, R.id.text2,R.id.text3});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> item = listObjects.get(i);
                String from = item.get(FROM).toString();
                String to = item.get(TO).toString();
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
                intent.putExtra("FROM",from);
                intent.putExtra("TO",to);
                startActivity(intent);
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        Log.i("info"," FragmentHistory RESUME");
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        if(requestCode == 1) {
            String date = data.getStringExtra("DATE_TIME");
            Log.i("info", "START " + date);
            customFrom = date;
            sharedPreferences.edit().putString(FROM,customFrom).commit();
        }else if(requestCode == 2) {
            String date = data.getStringExtra("DATE_TIME");
            Log.i("info","END " + date);
            customTo = date;
            sharedPreferences.edit().putString(TO,customTo).commit();
        }

        textViewFrom.setText(customFrom);
        textViewTo.setText(customTo);
        /*listObjects.clear();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Calendar c = GregorianCalendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);

        String year = Integer.toString(y);
        String month = m < 10 ? "0" + Integer.toString(m) : Integer.toString(m);
        String day = d < 10 ? "0" + Integer.toString(d) : Integer.toString(d);
        String fromDataeToday = year + "-" + month + "-" + day + " " + "00-00-00";
        String toDataeToday = format.format(new Date());

        c.add(Calendar.DAY_OF_YEAR, -3);
        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH) + 1;
        d = c.get(Calendar.DAY_OF_MONTH);

        year = Integer.toString(y);
        month = m < 10 ? "0" + Integer.toString(m) : Integer.toString(m);
        day = d < 10 ? "0" + Integer.toString(d) : Integer.toString(d);

        String fromDataePast = year + "-" + month + "-" + day + " " + "00-00-00";
        String toDataePast = format.format(new Date());

        HashMap<String, Object>item = new HashMap<String, Object>();
        item.put(TITLE,"Today");
        item.put(FROM, fromDataeToday);
        item.put(TO, toDataeToday);
        listObjects.add(item);

        item = new HashMap<String, Object>();
        item.put(TITLE,"Past three day");
        item.put(FROM, fromDataePast);
        item.put(TO, toDataePast);
        listObjects.add(item);*/

        /*item = new HashMap<String, Object>();
        item.put(TITLE,"Custom date time");
        item.put(FROM, customFrom);
        item.put(TO, customTo);
        listObjects.add(item);*/

        //adapter.notifyDataSetChanged();
    }
}
