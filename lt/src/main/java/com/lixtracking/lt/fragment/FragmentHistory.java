package com.lixtracking.lt.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lixtracking.lt.R;

/**
 * Created by saiber on 01.04.2014.
 */
public class FragmentHistory extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceSatte) {
        View view = layoutInflater.inflate(R.layout.fragment_history, container, false);
        return view;
    }
}
