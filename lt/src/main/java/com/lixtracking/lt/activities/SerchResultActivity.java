package com.lixtracking.lt.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.R;
import com.lixtracking.lt.common.LixApplication;
import com.lixtracking.lt.data_class.VehicleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by saiber on 12.04.2014.
 */
public class SerchResultActivity extends Activity {
    private static final String NAME = "name";
    private static final String GPS_ID = "gps_id";
    private static final String VIN = "vin";
    private static final String STATUS = "status";
    private static final String STOCK_NUMBER = "stok_number";
    private static final String ID  = "id";
    private static final String ICON  = "icon";
    private int[] vehicleId;

    public List<VehicleData> vehicleDataList = null;
    public List<VehicleData> vehicleDataListResult = new ArrayList<VehicleData>();
    private ListView listView = null;
    private ArrayList<HashMap<String, Object>> listObjects = new ArrayList<HashMap<String, Object>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serch_activity_layout);

        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),VehicleDetailActivity.class);
                VehicleData data = vehicleDataList.get(vehicleId[i]);
                intent.putExtra(VehicleData.VIN, data.vin);
                intent.putExtra(VehicleData.GPS_ID, data.gps_id);
                intent.putExtra(VehicleData.USER_ID, data.user_id);
                intent.putExtra(VehicleData.FIRST_NAME, data.first_name);
                intent.putExtra(VehicleData.LAST_NAME, data.last_name);
                intent.putExtra(VehicleData.MAKE, data.make);
                intent.putExtra(VehicleData.MODEL, data.model);
                intent.putExtra(VehicleData.STOCK_NUMBER, data.stock_number);
                intent.putExtra(VehicleData.YEAR, data.year);
                intent.putExtra(VehicleData.STATUS, data.status);
                startActivity(intent);
            }
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            Log.i("info", " SEARCH RESULT : " + query);
            vehicleDataList = LixApplication.getInstance().getVehicleDataList();

            if(vehicleDataList != null && (!vehicleDataList.isEmpty())) {
                vehicleId = new int[vehicleDataList.size()];
                int j = 0;
                for(int i = 0; i<vehicleDataList.size(); i++) {
                    int bv = 0;
                    int bn = 0;
                    int bl = 0;
                    String v = vehicleDataList.get(i).vin.toLowerCase();
                    String n = vehicleDataList.get(i).first_name.toLowerCase();
                    String l = vehicleDataList.get(i).last_name.toLowerCase();
                    bv = v.contains(query) ? 1 : 0;
                    bn = n.contains(query) ? 1 : 0;
                    bl = l.contains(query) ? 1 : 0;
                    int s = bv + bn + bl;
                    if(s > 0) {
                        vehicleId[j] = i;
                        j++;
                    }
                }
                for(int i = 0; i<j; i++) {
                    VehicleData data = vehicleDataList.get(vehicleId[i]);
                    HashMap<String, Object>item = new HashMap<String, Object>();
                    item.put(ID,Integer.toString(vehicleId[i]+1));
                    item.put(NAME, data.first_name + " " + data.last_name);
                    item.put(GPS_ID, "gps id : " + data.gps_id);
                    item.put(VIN,"VIN : " + data.vin);
                    item.put(STATUS,data.status == 1 ? "on" : "off");
                    item.put(STOCK_NUMBER, "stock number : " + data.stock_number);
                    if(data.status == 1) {
                        item.put(ICON, R.drawable.car);
                    } else {
                        item.put(ICON, R.drawable.car_na);
                    }
                    listObjects.add(item);
                }

                SimpleAdapter adapter = new SimpleAdapter(this, listObjects ,R.layout.vehicle_item,
                        new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER,VIN, STATUS},
                        new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3, R.id.text4, R.id.text5});
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        }
    }
}
