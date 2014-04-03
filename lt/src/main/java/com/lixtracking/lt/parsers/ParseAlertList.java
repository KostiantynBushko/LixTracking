package com.lixtracking.lt.parsers;

import com.lixtracking.lt.data_class.AlertData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by saiber on 01.04.2014.
 */
public class ParseAlertList {
    List<AlertData> alertDataList = new ArrayList<AlertData>();

    public List<AlertData> parceXml(String xmlData) {
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
        AlertData alertData = null;
        try {
            XmlPullParser xpp = xppf;
            String current_tag = null;
            int step = -1; //(-1) = not set 0= end, 1 = start,
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        //Log.i("info"," START_DOCUMENT:");
                        break;
                    case XmlPullParser.START_TAG:
                        step = 1;
                        current_tag = xpp.getName();
                        //Log.i("info"," START_TAG: " + xpp.getName());
                        if(xpp.getName().equals(AlertData.ALERT_DATA_TAG))
                            alertData = new AlertData();
                        break;
                    case XmlPullParser.END_TAG:
                        step = 0;
                        //Log.i("info"," END_TAG: " + xpp.getName());
                        if(xpp.getName().equals(AlertData.ALERT_DATA_TAG)) {
                            alertDataList.add(alertData);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //Log.i("info"," TEXT: " + xpp.getText());
                        if(step == 0)
                            break;
                        if(current_tag.equals(AlertData.ALERT_ID)) {
                            alertData.alert_id = xpp.getText();
                        }else if(current_tag.equals(AlertData.GPS_ID)) {
                            alertData.gps_id = xpp.getText();
                        }else if(current_tag.equals(AlertData.USER_ID)) {
                            alertData.user_id = xpp.getText();
                        }else if(current_tag.equals(AlertData.ALERT_TIME)) {
                            alertData.alert_time = xpp.getText();
                        }else if(current_tag.equals(AlertData.ALERT_TYPE)) {
                            alertData.alert_type = xpp.getText();
                        }else if(current_tag.equals(AlertData.ALERT_MESSAGE)) {
                            alertData.alert_message = xpp.getText();
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

        return alertDataList;
    }
}
