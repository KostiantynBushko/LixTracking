package com.lixtracking.lt.data_class;

/**
 * Created by saiber on 01.04.2014.
 */
public class AlertData {
    public static final String ALERT_DATA_TAG = "alertData";

    public static final String ALERT_ID = "alert_id";
    public static final String GPS_ID = "gps_id";
    public static final String USER_ID = "user_id";
    public static final String ALERT_TIME = "alert_time";
    public static final String ALERT_TYPE = "alert_type";
    public static final String ALERT_MESSAGE = "alert_message";

    public String alert_id = "";
    public String gps_id = "";
    public String user_id = "";
    public String alert_time = "";
    public String alert_type = "";
    public String alert_message = "";

    /*<alertData>
        <alert_id>string</alert_id>
        <gps_id>string</gps_id>
        <user_id>string</user_id>
        <alert_time>string</alert_time>
        <alert_type>string</alert_type>
        <alert_message>string</alert_message>
    </alertData>*/
}
