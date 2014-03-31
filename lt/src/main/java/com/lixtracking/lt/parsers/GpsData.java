package com.lixtracking.lt.parsers;

/**
 * Created by saiber on 30.03.2014.
 */
public class GpsData {
    public static final String GPS_DATA_TAG = "gpsData";
    public static final String MSG_ID = "msg_id";
    public static final String GPS_ID = "gps_id";
    public static final String GPS_TIME = "gps_time";
    public static final String GPS_STATUS = "gps_status";
    public static final String LAT = "latitude";
    public static final String LNG = "longitude";
    public static final String NORTH_LAT = "north_lat";
    public static final String WEST_LON = "west_lon";
    public static final String SPEED = "speed";
    public static final String DIRECTION = "direction";

    public String msg_id = "";
    public String gps_id = "";
    public String gps_time = "";
    public String gps_status = "";
    public String lat = "0.0";
    public String lng = "0.0";
    public int north_lat = 0;
    public int west_lon = 0;
    public float speed = 0.0f;
    public float direction = 0.0f;

    /*<?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
    <getRealTimeGpsDataResponse xmlns="http://lixtracking.com/webservice/">
    <getRealTimeGpsDataResult>
    <msg_id>string</msg_id>
    <gps_id>string</gps_id>
    <gps_time>string</gps_time>
    <gps_status>string</gps_status>
    <latitude>string</latitude>
    <longitude>string</longitude>
    <north_lat>int</north_lat>
    <west_lon>int</west_lon>
    <speed>decimal</speed>
    <direction>decimal</direction>
    </getRealTimeGpsDataResult>
    </getRealTimeGpsDataResponse>
    </soap:Body>
    </soap:Envelope>*/
}
