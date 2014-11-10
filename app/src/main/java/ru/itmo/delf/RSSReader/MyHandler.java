package ru.itmo.delf.RSSReader;

import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class MyHandler extends DefaultHandler {

    StringBuilder sb = null;
    public enum TAG {ENTRY, SUMMARY, LINK, TITLE}
    HashMap<String, TAG> tagType;
    ArrayList<String> summaries;
    ArrayList<String> links;
    ArrayList<String> titles;

    MyHandler(ArrayList<String> summaries, ArrayList<String> links, ArrayList<String> titles) {
        super();
        this.summaries = summaries;
        this.links = links;
        this.titles = titles;
        tagType = new HashMap<String, TAG>();
        tagType.put("entry", TAG.ENTRY);
        tagType.put("item", TAG.ENTRY);
        tagType.put("summary", TAG.SUMMARY);
        tagType.put("description", TAG.SUMMARY);
        tagType.put("id", TAG.LINK);
        tagType.put("link", TAG.LINK);
        tagType.put("title", TAG.TITLE);
        sb = new StringBuilder();
    }

    boolean insideItem = false;
    String summary = null;
    String link = null;
    String title = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        super.startElement(uri, localName, qName, attr);
        TAG tag = tagType.get(qName);
        if (tag == null) {
            return;
        }
        if (tag == TAG.ENTRY) {
            insideItem = true;
        } else {
            sb.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        sb.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        TAG tag = tagType.get(qName);
        if (tag == null) {
            return;
        }
        switch (tag) {
            case ENTRY:
                summaries.add(summary);
                links.add(link);
                titles.add(title);
                insideItem = false;
                break;
            case SUMMARY:
                if (sb.length() > 0) {
                    summary = sb.toString();
                }
                break;
            case LINK:
                if (sb.length() > 0) {
                    link = sb.toString();
                }
                break;
            case TITLE:
                if (sb.length() > 0) {
                    title = sb.toString();
                }
        }
    }
}
