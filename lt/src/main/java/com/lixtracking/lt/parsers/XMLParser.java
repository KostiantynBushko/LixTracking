package com.lixtracking.lt.parsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.List;

/**
 * Created by saiber on 03.04.2014.
 */
public class XMLParser <E> {

    private List<E> objects = null;

    public List<E> parseXml(String xmlData) {
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
        return objects;
    }
}
