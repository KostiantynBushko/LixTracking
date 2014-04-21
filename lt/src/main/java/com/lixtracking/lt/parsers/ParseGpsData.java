package com.lixtracking.lt.parsers;

import android.content.Context;

import com.lixtracking.lt.data_class.GpsData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by saiber on 30.03.2014.
 */
public class ParseGpsData {
    List<GpsData> gpsDataList = null;

    public ParseGpsData(Context context) {
    }

    public List<GpsData> parceXml(String xmlData) {
        XmlPullParserFactory factory = null;
        XmlPullParser xppf = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xppf = factory.newPullParser();
            xppf.setInput(new StringReader(xmlData));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        String tmp;
        GpsData gpsData = null;
        try {
            XmlPullParser xpp = xppf;
            String current_tag = null;
            int step = -1; //(-1) = not set 0= end, 1 = start,
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        //Log.i("info", " START_DOCUMENT:");
                        break;
                    case XmlPullParser.START_TAG:
                        step = 1;
                        current_tag = xpp.getName();
                        //Log.i("info"," START_TAG: " + xpp.getName());
                        if(xpp.getName().equals(GpsData.GPS_DATA_TAG))
                            gpsData = new GpsData();
                        break;
                    case XmlPullParser.END_TAG:
                        step = 0;
                        //Log.i("info", " END_TAG: " + xpp.getName());
                        if(xpp.getName().equals(GpsData.GPS_DATA_TAG)) {
                            if(gpsDataList == null)
                                gpsDataList = new ArrayList<GpsData>();
                            double _lat = Double.parseDouble(gpsData.lat);
                            double _lng = Double.parseDouble(gpsData.lng);
                            int lngFlag = gpsData.west_lon > 0 ? -1 : 1;
                            int latFlag = gpsData.north_lat > 0 ? 1 : -1;
                            _lat = latFlag * _lat;
                            _lng = lngFlag * _lng;
                            gpsData.lat = Double.toString(_lat);
                            gpsData.lng = Double.toString(_lng);
                            gpsDataList.add(gpsData);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //Log.i("info"," TEXT: " + xpp.getText());
                        if(step == 0)
                            break;
                        if(current_tag.equals(GpsData.GPS_ID)) {
                            gpsData.gps_id = xpp.getText();
                        }else if(current_tag.equals(GpsData.MSG_ID)) {
                            gpsData.msg_id = xpp.getText();
                        }else if(current_tag.equals(GpsData.GPS_TIME)) {
                            gpsData.gps_time = xpp.getText();
                        }else if(current_tag.equals(GpsData.GPS_STATUS)) {
                            gpsData.gps_status = xpp.getText();
                        }else if(current_tag.equals(GpsData.LAT)) {
                            gpsData.lat = xpp.getText();
                        }else if(current_tag.equals(GpsData.LNG)) {
                            gpsData.lng = xpp.getText();
                        }else if(current_tag.equals(GpsData.NORTH_LAT)) {
                            gpsData.north_lat = Integer.parseInt(xpp.getText());
                        }else if(current_tag.equals(GpsData.WEST_LON)) {
                            gpsData.west_lon = Integer.parseInt(xpp.getText());
                        }else if(current_tag.equals(GpsData.SPEED)) {
                            gpsData.speed = Float.parseFloat(xpp.getText());
                        }else if(current_tag.equals(gpsData.DIRECTION)) {
                            gpsData.direction = Float.parseFloat(xpp.getText());
                        }
                        break;
                    default: break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gpsDataList;
    }
}
