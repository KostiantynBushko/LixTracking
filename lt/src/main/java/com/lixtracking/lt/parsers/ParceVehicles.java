package com.lixtracking.lt.parsers;

import android.content.Context;

import com.lixtracking.lt.data_class.VehicleData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by saiber on 29.03.2014.
 */
public class ParceVehicles {
    List<VehicleData>vehicleDataList = new ArrayList<VehicleData>();

    public ParceVehicles(Context context) {
    }

    public List<VehicleData> parceXml(String xmlData) {
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
        VehicleData vehicleData = null;
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
                        if(xpp.getName().equals(VehicleData.VEHICLE_DATA_TAG))
                            vehicleData = new VehicleData();
                        break;
                    case XmlPullParser.END_TAG:
                        step = 0;
                        //Log.i("info"," END_TAG: " + xpp.getName());
                        if(xpp.getName().equals(VehicleData.VEHICLE_DATA_TAG)) {
                            vehicleDataList.add(vehicleData);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //Log.i("info"," TEXT: " + xpp.getText());
                        if(step == 0)
                            break;
                        if(current_tag.equals(VehicleData.VIN)) {
                            vehicleData .vin = xpp.getText();
                        }else if(current_tag.equals(VehicleData.GPS_ID)) {
                            vehicleData.gps_id = xpp.getText();
                        }else if(current_tag.equals(VehicleData.STOCK_NUMBER)) {
                            vehicleData.stock_number = xpp.getText();
                        }else if(current_tag.equals(VehicleData.LAST_NAME)) {
                            vehicleData.last_name = xpp.getText();
                        }else if(current_tag.equals(VehicleData.FIRST_NAME)) {
                            vehicleData.first_name = xpp.getText();
                        }else if(current_tag.equals(VehicleData.MAKE)) {
                            vehicleData.make = xpp.getText();
                        }else if(current_tag.equals(VehicleData.MODEL)) {
                            vehicleData.model = xpp.getText();
                        }else if(current_tag.equals(VehicleData.YEAR)) {
                            String s = xpp.getText();
                            vehicleData.year = Integer.parseInt(s);
                        }else if(current_tag.equals(VehicleData.STATUS)) {
                            String s = xpp.getText();
                            vehicleData.status = Integer.parseInt(s);
                        }else if(current_tag.equals(VehicleData.USER_ID)) {
                            vehicleData.user_id = xpp.getText();
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

        return vehicleDataList;
    }
}
