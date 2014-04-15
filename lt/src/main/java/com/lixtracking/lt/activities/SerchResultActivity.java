package com.lixtracking.lt.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lixtracking.lt.R;
import com.lixtracking.lt.data_class.VehicleData;

import java.util.List;

/**
 * Created by saiber on 12.04.2014.
 */
public class SerchResultActivity extends Activity {
    public List<VehicleData> vehicleDataList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serch_activity_layout);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i("info", " SEARCH RESULT : " + query);
        }
    }
}
