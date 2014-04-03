package com.lixtracking.lt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.lixtracking.lt.fragment.FragmentHistory;
import com.lixtracking.lt.fragment.FragmentTracking;
import com.lixtracking.lt.fragment.FragmentVehicleAlarm;
import com.lixtracking.lt.data_class.VehicleData;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by saiber on 01.04.2014.
 */
public class VehicleDetail extends FragmentActivity {
    private TabHost mTabHost;
    private HashMap<String, Stack<Fragment>> fragmentStack;
    private String currentTab;
    public static FragmentManager fragmentManager;

    public static final String TAB_HISTORY    = "tab_history";
    public static final String TAB_ALARM  = "tab_alarm";
    public static final String TAB_TRACK   = "tab_tracking";

    public VehicleData vehicleData = new VehicleData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.in,R.anim.out);

        Intent intent = getIntent();
        vehicleData.vin = intent.getStringExtra(VehicleData.VIN);
        vehicleData.gps_id = intent.getStringExtra(VehicleData.GPS_ID);
        vehicleData.user_id = intent.getStringExtra(VehicleData.USER_ID);
        vehicleData.first_name = intent.getStringExtra(VehicleData.FIRST_NAME);
        vehicleData.last_name = intent.getStringExtra(VehicleData.LAST_NAME);
        vehicleData.stock_number = intent.getStringExtra(VehicleData.STOCK_NUMBER);
        vehicleData.model = intent.getStringExtra(VehicleData.MODEL);
        vehicleData.make = intent.getStringExtra(VehicleData.MAKE);
        vehicleData.year = intent.getIntExtra(VehicleData.YEAR, 0);
        vehicleData.status = intent.getIntExtra(VehicleData.STATUS, 0);

        fragmentManager = getSupportFragmentManager();
        fragmentStack = new HashMap<String, Stack<Fragment>>();
        fragmentStack.put(TAB_TRACK, new Stack<Fragment>());
        fragmentStack.put(TAB_HISTORY, new Stack<Fragment>());
        fragmentStack.put(TAB_ALARM, new Stack<Fragment>());

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setOnTabChangedListener(tabChangeListener);
        mTabHost.setup();
        InitializeTabButton();
    }
    /**********************************************************************************************/
    /* Initialise tab button */
    /**********************************************************************************************/
    private void InitializeTabButton() {
        TabHost.TabSpec spec = mTabHost.newTabSpec(TAB_TRACK);
        mTabHost.setCurrentTab(-3);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_e_state_btn));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_HISTORY);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_f_state_btn));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_ALARM);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_c_state_btn));
        mTabHost.addTab(spec);
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener(){
        @Override
        public void onTabChanged(String tabId) {
            Log.i("info", " TAB : " + tabId);
            currentTab = tabId;
            if(fragmentStack.get(tabId).size() == 0) {
                if(tabId.equals(TAB_TRACK)) {
                    pushFragments(tabId, new FragmentTracking(),true);
                }else if(tabId.equals(TAB_HISTORY)) {
                    pushFragments(tabId, new FragmentHistory(),true);
                }else if(tabId.equals(TAB_ALARM)){
                    pushFragments(tabId, new FragmentVehicleAlarm(), true);
                }
            }else {
                pushFragments(tabId, fragmentStack.get(tabId).lastElement(), false);
            }
        }
    };
    /**********************************************************************************************/
    /* Create tab view */
    /**********************************************************************************************/
    private View createTabView(final int id) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_icon, null);
        ImageView imageView =   (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageDrawable(getResources().getDrawable(id));
        return view;
    }
    /**********************************************************************************************/
    /* Fragment Stack */
    /**********************************************************************************************/
    public void pushFragments(String tag, Fragment fragment, boolean shouldAdd){
        if(shouldAdd)
            fragmentStack.get(tag).push(fragment);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.realtabcontent, fragment);
        ft.commit();
    }
    public void popFragments(){
        Fragment fragment = fragmentStack.get(currentTab).elementAt(fragmentStack.get(currentTab).size() - 2);
        fragmentStack.get(currentTab).pop();
        FragmentManager   manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.realtabcontent, fragment);
        ft.commit();
    }
    public String getCurrentFragmentTag() {
        return currentTab;
    }
}
