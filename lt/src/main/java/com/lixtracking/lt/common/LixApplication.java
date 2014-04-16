package com.lixtracking.lt.common;

import android.app.Application;

import com.lixtracking.lt.data_class.VehicleData;

import java.util.List;

/**
 * Created by saiber on 16.04.2014.
 */
public class LixApplication extends Application {

    private static LixApplication instance;

    private List<VehicleData>vehicleDataList = null;

    public static LixApplication getInstance() {
        return instance;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setVehicleDataList(List<VehicleData> vehicleDataList) {
        this.vehicleDataList = vehicleDataList;
    }
    public List<VehicleData> getVehicleDataList() {
        return vehicleDataList;
    }
}
