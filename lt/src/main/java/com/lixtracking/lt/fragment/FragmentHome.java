package com.lixtracking.lt.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.common.Settings;
import com.lixtracking.lt.common.URL;
import com.lixtracking.lt.parsers.ParceVehicles;
import com.lixtracking.lt.parsers.VehicleData;

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
    private static final String GPS_ID = "vin";
    private static final String STOCK_NUMBER = "stok_number";
    private static final String ID  = "id";
    private static final String ICON  = "icon";

    private View view;
    private Context context;

    //List of data
    ArrayList<HashMap<String, Object>> listObjects = null;
    private ListView listView;
    List<VehicleData>vehicleDataList = null;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        Log.i("info"," CREATE: FragmentHome");
        view = layoutInflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity();
        listView = (ListView)view.findViewById(R.id.listView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("info"," RESUME: FragmentHome");
        if(listObjects != null){
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                    new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER},
                    new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3});
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }else {
            listView = (ListView)view.findViewById(R.id.listView);
            listView.setDivider(null);
            listView.setDividerHeight(5);
            new getVehiclesTask().execute();
        }
    }

    /**********************************************************************************************/
    /**/
    /**********************************************************************************************/
    class getVehiclesTask extends AsyncTask<Void, Void, String> {
        private String resultString = null;
        @Override
        protected String doInBackground(Void... voids) {
            Log.i("info"," START: getVehiclesTask");
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

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
            vehicleDataList = new ParceVehicles(context).parceXml(result);
            MainActivity activity = (MainActivity)getActivity();
            activity.vehicleDataListGlobal = vehicleDataList;

            for(int i = 0; i<vehicleDataList.size(); i++) {
                VehicleData data = vehicleDataList.get(i);
                HashMap<String, Object>item = new HashMap<String, Object>();
                item.put(ID,Integer.toString(i+1));
                item.put(NAME, data.first_name + " " + data.last_name + " :" + data.vin);
                item.put(GPS_ID, data.gps_id);
                item.put(STOCK_NUMBER, data.stock_number);
                if(data.status == 1) {
                    item.put(ICON, R.drawable.car);
                } else {
                    item.put(ICON, R.drawable.car_na);
                }
                listObjects.add(item);
            }
            if(((MainActivity)getActivity()).getCurrentFragmentTag() == MainActivity.TAB_HOME) {
                if (listView.getAdapter() == null) {
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.vehicle_item,
                            new String[]{ICON,ID,NAME,GPS_ID,STOCK_NUMBER},
                            new int[]{R.id.icon,R.id.u_id, R.id.text1, R.id.text2,R.id.text3});
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }else {
                    BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
