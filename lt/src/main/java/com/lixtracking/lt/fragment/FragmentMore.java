package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lixtracking.lt.R;
import com.lixtracking.lt.activities.AboutActivity;
import com.lixtracking.lt.activities.ChangePasswordActivity;
import com.lixtracking.lt.activities.FunctionActivity;
import com.lixtracking.lt.activities.LoginActivity;
import com.lixtracking.lt.common.Settings;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentMore extends Fragment {
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String ICON = "icon";

    private ListView listView;
    ArrayList<HashMap<String, Object>> listObjects = null;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        View view = layoutInflater.inflate(R.layout.fragment_more, container, false);
        listView = (ListView)view.findViewById(R.id.listView);
        listView.setDividerHeight(5);
        ((Button)view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Settings(getActivity()).setUserSession(false);
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        listObjects = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object>item = new HashMap<String, Object>();

        item.put(TITLE,"User account settings");
        item.put(DESCRIPTION," allow user change password");
        item.put(ICON,R.drawable.ic_action_person_dark);
        listObjects.add(item);

        item = new HashMap<String, Object>();
        item.put(TITLE,"About");
        item.put(DESCRIPTION," info about application");
        item.put(ICON,R.drawable.ic_action_about_dark);
        listObjects.add(item);

        item = new HashMap<String, Object>();
        item.put(TITLE,"Function");
        item.put(DESCRIPTION," functionality of application");
        item.put(ICON,R.drawable.ic_action_view_as_grid_dark);
        listObjects.add(item);

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), listObjects ,R.layout.settings_item,
                new String[]{ICON,TITLE,DESCRIPTION},
                new int[]{R.id.icon,R.id.text1, R.id.textView});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Intent intent = new Intent(getActivity(),ChangePasswordActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Intent intent1 = new Intent(getActivity(), AboutActivity.class);
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent(getActivity(), FunctionActivity.class);
                        startActivity(intent2);
                        break;

                }
            }
        });
    }
}
