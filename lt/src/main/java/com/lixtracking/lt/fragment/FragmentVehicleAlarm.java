package com.lixtracking.lt.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.VehicleDetail;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.data_class.AlertData;
import com.lixtracking.lt.parsers.ParseAlertList;
import com.lixtracking.lt.data_class.VehicleData;

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
 * Created by saiber on 01.04.2014.
 */
public class FragmentVehicleAlarm extends Fragment {
    private static final String ID = "id";
    private View view;
    private Context context;
    private ArrayList<HashMap<String, Object>> listObjects = null;
    private ListView listView;
    List<AlertData>alertDataList = null;
    VehicleData vehicleData = null;
    ProgressBar progressBar = null;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        view = layoutInflater.inflate(R.layout.fragment_vehicle_alarm, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.loading_spinner);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        listView = (ListView)view.findViewById(R.id.listView);
        vehicleData = ((VehicleDetail)getActivity()).vehicleData;
        new getAlertDataListTask().execute();
    }

    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getAlertDataListTask extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected String doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.getAlertListUrl);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(2);
            nameValuePairList.add(new BasicNameValuePair("user_id", vehicleData.user_id));
            nameValuePairList.add(new BasicNameValuePair("gps_id", vehicleData.gps_id));

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
                if(((MainActivity)getActivity()).getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                    result = "Error connection";
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                return;
            }
            listObjects = new ArrayList<HashMap<String, Object>>();
            alertDataList = new ParseAlertList().parceXml(result);
            for(int i = 0; i<alertDataList.size(); i++) {
                AlertData data = alertDataList.get(i);
                HashMap<String, Object>item = new HashMap<String, Object>();
                item.put(ID,Integer.toString(i+1));
                item.put(AlertData.GPS_ID, data.gps_id);
                item.put(AlertData.ALERT_ID, data.alert_id);
                item.put(AlertData.USER_ID, data.user_id);
                item.put(AlertData.ALERT_TIME, data.alert_time);
                item.put(AlertData.ALERT_TYPE, data.alert_type);
                item.put(AlertData.ALERT_MESSAGE, data.alert_message);
                listObjects.add(item);
            }
            if(((VehicleDetail)getActivity()).getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                if (listView.getAdapter() == null) {
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                            new String[]{AlertData.GPS_ID, AlertData.ALERT_ID, AlertData.ALERT_TIME, AlertData.ALERT_TYPE},
                            new int[]{R.id.textView5, R.id.textView6, R.id.textView7, R.id.textView8});
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }else {
                    BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                    adapter.notifyDataSetChanged();
                }
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
