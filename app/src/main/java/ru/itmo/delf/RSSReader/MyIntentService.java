package ru.itmo.delf.RSSReader;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MyIntentService extends IntentService {

    public static final String key = "ru.itmo.delf.RSSReader.MyIntentService";

    public MyIntentService() {
        super("MyIntentService");
    }

    ArrayList<String> summaries;
    ArrayList<String> links;
    ArrayList<String> titles;

    String channelURL = null;
    String channelTitle = null;
    String channelTime = null;
    int channelID = 0;

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i("MyIntentService", "entered onHandleIntent");
        channelURL = intent.getStringExtra("rssURL");
        channelTitle = intent.getStringExtra("rssTitle");
        channelTime = intent.getStringExtra("rssTime");
        channelID = Integer.parseInt(intent.getStringExtra("rssID"));
        String result = "";
        try {
            summaries = new ArrayList<String>();
            links = new ArrayList<String>();
            titles = new ArrayList<String>();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(channelURL));
            HttpEntity httpEntity = httpResponse.getEntity();
            String xml = EntityUtils.toString(httpEntity, "UTF-8");
            InputSource is = new InputSource(new StringReader(xml));
            parser.parse(is, new MyHandler(summaries, links, titles));
            result = "good";
        } catch (Exception e) {
            result = e.getMessage();
        }
        Intent response = new Intent();
        response.setAction(key);
        response.addCategory(Intent.CATEGORY_DEFAULT);
        DBAdapter myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        response.putExtra("rssTitle", channelTitle);
        response.putExtra("result", result);
        if ("good".equals(result)) {
            myDBAdapter.clearRssTable(channelID);
            for (int i = 0; i < summaries.size(); i++) {
                myDBAdapter.addNews(channelID, summaries.get(i), links.get(i), titles.get(i));
            }
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss");
            channelTime = sdf.format(calendar.getTime());
        }
        response.putExtra("cTime", channelTime);
        myDBAdapter.updateRss(channelID, channelTitle, channelURL, channelTime);
        myDBAdapter.close();
        sendBroadcast(response);
    }
}
