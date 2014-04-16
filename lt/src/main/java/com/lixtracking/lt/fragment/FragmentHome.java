package com.lixtracking.lt.fragment;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.ToggleButton;

import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.common.LixApplication;
import com.lixtracking.lt.common.Settings;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.VehicleData;
import com.lixtracking.lt.parsers.ParceVehicles;

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
import java.util.HashMap;
import java.util.List;

/**
 * Created by saiber on 26.03.2014.
 */
public class FragmentHome extends Fragment {
    private static final String NAME = "name";
    private static final String GPS_ID = "gps_id";
    private static final String VIN = "vin";
    private static final String STATUS = "status";
    private static final String STOCK_NUMBER = "stok_number";
    private static final String ID  = "id";
    private static final String ICON  = "icon";
    private ProgressBar progressBar;

    private ToggleButton toggleButton1;
    private ToggleButton toggleButton2;
    private ToggleButton toggleButton3;

    int online = 0;
    int offline = 0;
    int all = 0;

    private View view;
    private Context context;

    //List of data
    ArrayList<HashMap<String, Object>> listObjects = null;
    private ListView listView;
    List<VehicleData>vehicleDataList = null;
    private static boolean isRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        Log.i("info"," CREATE: FragmentHome");
        view = layoutInflater.inflate(R.layout.fragment_home, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.INVISIBLE);
        context = getActivity();
        listView = (ListView)view.findViewById(R.id.listView);
        toggleButton1 = (ToggleButton)view.findViewById(R.id.toggleButton1);
        toggleButton2 = (ToggleButton)view.findViewById(R.id.toggleButton2);
        toggleButton3 = (ToggleButton)view.findViewById(R.id.toggleButton3);
        toggleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButton2.setChecked(false);
                toggleButton3.setChecked(false);
                if(toggleButton1.isChecked())
                    toggleButton1.setChecked(true);
            }
        });
        toggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButton1.setChecked(false);
                toggleButton3.setChecked(false);
                if(toggleButton2.isChecked())
                    toggleButton2.setChecked(true);
            }
        });
        toggleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButton1.setChecked(false);
                toggleButton2.setChecked(false);
                if(toggleButton3.isChecked())
                    toggleButton3.setChecked(true);
            }
        });
        toggleButton1.setChecked(true);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("info"," RESUME: FragmentHome");
        if(listObjects != null){
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                    new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER,VIN, STATUS},
                    new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3, R.id.text4, R.id.text5});
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }else {
            listView = (ListView)view.findViewById(R.id.listView);
            listView.setDivider(null);
            listView.setDividerHeight(5);
            new getVehiclesTask().execute();
        }
        listView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                Log.i("info"," list item : " + Integer.toString(item));
                VehicleData data = vehicleDataList.get(item);
                Log.i("info"," vin    : " + data.vin);
                Log.i("info"," gps id : " + data.gps_id);

                Intent intent = new Intent(context,VehicleDetailActivity.class);
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
    }
    /**********************************************************************************************/
    /* MENU */
    /**********************************************************************************************/
   @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.menu_home,menu);
       super.onCreateOptionsMenu(menu,inflater);

       // Associate searchable configuration with the SearchView
       SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
       SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
       searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String s) {
               Log.i("info"," SEARCH TEXT SUBMIT : " + s);
               return false;
           }
           @Override
           public boolean onQueryTextChange(String s) {
               Log.i("info"," SEARCH TEXT CHANGE : " + s);
               return false;
           }
       });
       /*SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String s) {
               Log.i("info"," SEARCH TEXT SUBMIT : " + s);
               return false;
           }
           @Override
           public boolean onQueryTextChange(String s) {
               Log.i("info"," SEARCH TEXT CHANGE : " + s);
               return false;
           }
       });
       searchView.setOnCloseListener(new SearchView.OnCloseListener() {
           @Override
           public boolean onClose() {
               Log.i("info", " SEARCH VIEW CLOSE : ");
               return false;
           }
       });
       searchView.setOnSearchClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Log.i("info"," SEARCH CLICK : ");
           }
       });
       searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
           @Override
           public boolean onSuggestionSelect(int i) {
               Log.i("info"," SEARCH SUGGESTION : ");
               return false;
           }

           @Override
           public boolean onSuggestionClick(int i) {
               return false;
           }
       });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!isRunning) {
                    listView.setAdapter(null);
                    new getVehiclesTask().execute();
                }
                break;
            case R.id.action_search:
                // search action
                Log.i("info"," ACTION SEARCH : ");
                return true;
        }
        return true;
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getVehiclesTask extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... voids) {
            if (isRunning == true)
                return null;
            isRunning = true;
            Log.i("info"," START: getVehiclesTask");
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.getVehiclesUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("user_id", new Settings(context).getUserId()));

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
            Log.i("info"," END: getVehiclesTask");
            if(result == null && listObjects == null) {
                if(((MainActivity)getActivity()).getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                    result = "Error connection";
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage(result);
                    builder.setIcon(R.drawable.ic_action_warning_dark);
                    builder.setCancelable(true);
                    builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                progressBar.setVisibility(View.INVISIBLE);
                isRunning = false;
                return;
            }else if(result == null && listObjects != null) {
                if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                            new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER},
                            new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3});
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
                progressBar.setVisibility(View.INVISIBLE);
                isRunning = false;
                return;
            }

            listObjects = new ArrayList<HashMap<String, Object>>();
            vehicleDataList = new ParceVehicles(context).parceXml(result);

            if(vehicleDataList != null && (!vehicleDataList.isEmpty())) {
                all = vehicleDataList.size();
                // pass reference to vehicle data list in to application
                LixApplication.getInstance().setVehicleDataList(vehicleDataList);

                MainActivity activity = (MainActivity)getActivity();
                activity.vehicleDataListGlobal = vehicleDataList;

                for(int i = 0; i<vehicleDataList.size(); i++) {
                    VehicleData data = vehicleDataList.get(i);
                    HashMap<String, Object>item = new HashMap<String, Object>();
                    item.put(ID,Integer.toString(i+1));
                    item.put(NAME, data.first_name + " " + data.last_name);
                    item.put(GPS_ID, "gps id : " + data.gps_id);
                    item.put(VIN,"VIN : " + data.vin);
                    item.put(STATUS,data.status == 1 ? "on" : "off");
                    item.put(STOCK_NUMBER, "stock number : " + data.stock_number);
                    if(data.status == 1) {
                        item.put(ICON, R.drawable.car);
                        online+=1;
                    } else {
                        item.put(ICON, R.drawable.car_na);
                        offline+=1;
                    }
                    listObjects.add(item);
                }

                // Update list vehicle
                if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                            new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER,VIN, STATUS},
                            new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3, R.id.text4, R.id.text5});
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    toggleButton1.setTextOff("All["+Integer.toString(all) + "]");
                    toggleButton2.setTextOff("Online["+Integer.toString(online) + "]");
                    toggleButton3.setTextOff("Offline["+Integer.toString(offline) + "]");
                    toggleButton1.setTextOn("All["+Integer.toString(all) + "]");
                    toggleButton2.setTextOn("Online["+Integer.toString(online) + "]");
                    toggleButton3.setTextOn("Offline["+Integer.toString(offline) + "]");
                    toggleButton1.invalidate();
                    toggleButton2.invalidate();
                    toggleButton2.invalidate();
                }
            }
            progressBar.setVisibility(View.INVISIBLE);
            isRunning = false;
        }
    }
    /**********************************************************************************************/
}
