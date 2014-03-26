package com.lixtracking.lt.fragment;

/**
 * Created by saiber on 26.03.2014.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lixtracking.lt.R;

public class FragmentMap extends Fragment{
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }
}
