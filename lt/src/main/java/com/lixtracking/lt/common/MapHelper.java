package com.lixtracking.lt.common;

/**
 * Created by saiber on 21.04.2014.
 */
public class MapHelper {

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        distance = Math.acos(distance);
        distance = rad2deg(distance);
        distance = distance * 60 * 1.1515;
        if (unit.equals("K")) {
            distance = distance * 1.609344;
        } else if (unit.equals("N")) {
            distance = distance * 0.8684;
        }
        return (distance);
    }

    public static float direction(double lat1, double lng1, double lat2, double lng2) {
        float d = (float)(Math.atan2(lng1 - lng2, lat1 - lat2) / Math.PI * 180);
        //d = d < 0 ? d + 360 : d;
        return d;
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    //system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
    //system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
    //system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
}
