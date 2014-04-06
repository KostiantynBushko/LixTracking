package com.lixtracking.lt;

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

import com.lixtracking.lt.data_class.VehicleData;
import com.lixtracking.lt.fragment.FragmentAlarm;
import com.lixtracking.lt.fragment.FragmentHome;
import com.lixtracking.lt.fragment.FragmentMap;
import com.lixtracking.lt.fragment.FragmentMore;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;


public class MainActivity extends FragmentActivity {
    private TabHost mTabHost;
    private HashMap<String, Stack<Fragment>> fragmentStack;
    private static String currentTab;
    public List<VehicleData> vehicleDataListGlobal = null;

    public static FragmentManager fragmentManager;

    public static final String TAB_HOME   = "tab_home";
    public static final String TAB_MAP    = "tab_map";
    public static final String TAB_ALARM  = "tab_alarm";
    public static final String TAB_MORE   = "tab_more";

    private boolean isLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        fragmentStack = new HashMap<String, Stack<Fragment>>();
        fragmentStack.put(TAB_HOME, new Stack<Fragment>());
        fragmentStack.put(TAB_MAP, new Stack<Fragment>());
        fragmentStack.put(TAB_ALARM, new Stack<Fragment>());
        fragmentStack.put(TAB_MORE, new Stack<Fragment>());

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setOnTabChangedListener(tabChangeListener);
        mTabHost.setup();
        InitializeTabButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isLoaded) {
            overridePendingTransition(R.anim.in_a,R.anim.out_a);
        }
        isLoaded = true;
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    /**********************************************************************************************/
    /* Initialise tab button */
    /**********************************************************************************************/
    private void InitializeTabButton() {
        TabHost.TabSpec spec = mTabHost.newTabSpec(TAB_HOME);
        mTabHost.setCurrentTab(-3);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_a_state_btn));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_MAP);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_b_state_btn));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_ALARM);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_c_state_btn));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_MORE);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.tab_d_state_btn));
        mTabHost.addTab(spec);
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener(){
        @Override
        public void onTabChanged(String tabId) {
            Log.i("info"," TAB : " + tabId);
            currentTab = tabId;

            if(fragmentStack.get(tabId).size() == 0) {
                if(tabId.equals(TAB_HOME)) {
                    pushFragments(tabId, new FragmentHome(),true);
                }else if(tabId.equals(TAB_MAP)) {
                    pushFragments(tabId, new FragmentMap(),true);
                }else if(tabId.equals(TAB_ALARM)){
                    pushFragments(tabId, new FragmentAlarm(), true);
                }else if(tabId.equals(TAB_MORE)){
                    pushFragments(tabId, new FragmentMore(), true);
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
    public List<VehicleData> getVehicle() {
        return vehicleDataListGlobal;
    }
    public static String getCurrentFragmentTag() {
        return currentTab;
    }
}
