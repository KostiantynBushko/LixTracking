package com.lixtracking.lt.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lixtracking.lt.R;

/**
 * Created by saiber on 26.03.2014.
 */
public class FragmentHome extends Fragment {
    private ListView listView;
    View view;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        view = layoutInflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onResume() {
        listView = (ListView)view.findViewById(R.id.listView);
        listView.setDivider(null);
        listView.setDividerHeight(10);
    }
}
