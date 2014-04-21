package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */

import android.app.AlertDialog;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.AlertMapActivity;
import com.lixtracking.lt.activities.VehicleDetailActivity;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.AlertData;
import com.lixtracking.lt.parsers.ParseAlertList;

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

public class FragmentAlarm extends Fragment{
    private static final String ALERT_TIME = "alert_time";
    private static final String GPS_ID = "gps_id";
    private static final String ALERT_ID = "alert_id";
    private static final String ID = "id";

    private View view;
    private Context context;
    private ArrayList<HashMap<String, Object>> listObjects = null;
    private ListView listView;
    List<AlertData>alertDataList = null;
    //VehicleData vehicleData = null;
    AsyncTask task = null;
    ProgressBar progressBar = null;
    TextView message = null;
    private boolean isRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        view = layoutInflater.inflate(R.layout.fragment_vehicle_alarm, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.loading_spinner);
        message = (TextView)view.findViewById(R.id.textView);
        listView = (ListView)view.findViewById(R.id.listView);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        //vehicleData = ((VehicleDetailActivity)getActivity()).vehicleData;
        if(listObjects == null) {
            task = new getAlertDataListTask().execute();
        }else {
            updateList();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_alert,menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!isRunning) {
                    listView.setAdapter(null);
                    message.setVisibility(View.INVISIBLE);
                    alertDataList = null;
                    progressBar.setVisibility(View.INVISIBLE);
                    new getAlertDataListTask().execute();
                }
                break;
        }
        return true;
    }
    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getAlertDataListTask extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected void onPreExecute() {
            isRunning = true;
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.GetAlertListLimitedUrl);

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(3);
            nameValuePairList.add(new BasicNameValuePair("user_id", "admin"));
            nameValuePairList.add(new BasicNameValuePair("gps_id", ""));
            nameValuePairList.add(new BasicNameValuePair("start", "0"));
            nameValuePairList.add(new BasicNameValuePair("limit", "500"));

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
            if(result == null) {
                if(((VehicleDetailActivity)getActivity()).getCurrentFragmentTag() == VehicleDetailActivity.TAB_ALARM) {
                    result = "Error connection";
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Mesage");
                    builder.setMessage(result);
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
                message.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                isRunning = false;
                return;
            }

            Log.i("info",result);
            listObjects = new ArrayList<HashMap<String, Object>>();
            alertDataList = new ParseAlertList().parceXml(result);
            if(alertDataList.size() == 0) {
                progressBar.setVisibility(View.INVISIBLE);
                message.setVisibility(View.VISIBLE);
                alertDataList = null;
                isRunning = false;
                return;
            }
            message.setVisibility(View.INVISIBLE);
            for(int i = 0; i<alertDataList.size(); i++) {
                AlertData data = alertDataList.get(i);
                String date = data.alert_time.substring(0,4) + "/" + data.alert_time.substring(4,6) + "/"
                        + data.alert_time.substring(6,8) +" "
                        + data.alert_time.substring(8,10) +":"
                        + data.alert_time.substring(10,12) +":"
                        + data.alert_time.substring(12,14);
                HashMap<String, Object>item = new HashMap<String, Object>();
                item.put(ID,Integer.toString(i+1));
                item.put(AlertData.ALERT_ID, data.alert_id);
                item.put(AlertData.USER_ID, data.user_id);
                item.put(AlertData.ALERT_TIME, date);
                item.put(AlertData.ALERT_TYPE, data.gps_id);
                item.put(AlertData.ALERT_MESSAGE, data.alert_message);
                listObjects.add(item);
            }
            /*if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_ALARM) {
                if (listView.getAdapter() == null) {
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.alarm_item,
                            new String[]{ID, AlertData.ALERT_TYPE, AlertData.ALERT_TIME, AlertData.ALERT_MESSAGE},
                            new int[]{R.id.u_id, R.id.text1, R.id.text2, R.id.text3});
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Log.i("info"," Item = " + Integer.toString(i));
                            Intent intent = new Intent(getActivity(), AlertMapActivity.class);
                            intent.putExtra(ALERT_TIME,alertDataList.get(i).alert_time);
                            intent.putExtra(GPS_ID,alertDataList.get(i).gps_id);
                            intent.putExtra(ALERT_ID,alertDataList.get(i).alert_id);
                            startActivity(intent);
                        }
                    });
                }else {
                    BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                    adapter.notifyDataSetChanged();
                }
            }*/
            updateList();
            progressBar.setVisibility(View.INVISIBLE);
            isRunning = false;
        }
    }
    private void updateList() {
        if(MainActivity.getCurrentFragmentTag() == MainActivity.TAB_ALARM) {
            listView.setAdapter(null);
            Log.i("info"," update list alert");
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.alarm_item,
                    new String[]{ID, AlertData.ALERT_TYPE, AlertData.ALERT_TIME, AlertData.ALERT_MESSAGE},
                    new int[]{R.id.u_id, R.id.text1, R.id.text2, R.id.text3});
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.i("info"," Item = " + Integer.toString(i));
                    Intent intent = new Intent(getActivity(), AlertMapActivity.class);
                    intent.putExtra(ALERT_TIME,alertDataList.get(i).alert_time);
                    intent.putExtra(GPS_ID,alertDataList.get(i).gps_id);
                    intent.putExtra(ALERT_ID,alertDataList.get(i).alert_id);
                    startActivity(intent);
                }
            });
        }
    }
}
