package ru.itmo.delf.RSSReader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class MyActivity extends Activity {

    String rssURL = null;
    String rssTitle = null;
    String rssTime = null;
    int rssID = 0;

    ArrayList<String> summaries;
    ArrayList<String> links;
    ArrayList<String> titles;

    ListView lvMain;
    TextView tvMain;
    ArrayAdapter<String> adapter;

    MyBroadcastReceiver mbr = null;
    IntentFilter intentFilter;
    boolean registered = false;

    DBAdapter myDBAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent intent = getIntent();
        rssURL = intent.getStringExtra("rssURL");
        rssTitle = intent.getStringExtra("rssTitle");
        rssTime = intent.getStringExtra("rssTime");
        rssID = Integer.parseInt(intent.getStringExtra("rssID"));
        lvMain = (ListView) findViewById(R.id.lvMain);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setText("Latest update: " + rssTime);
        myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        mbr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter(MyIntentService.key);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mbr, intentFilter);
        registered = true;
        showFeed();


    }

    public void showFeed() {
        Cursor cursor = myDBAdapter.fetchAllArticles(rssID);
        startManagingCursor(cursor);
        summaries = new ArrayList<String>();
        links = new ArrayList<String>();
        titles = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            tvMain.setText("Latest update: " + rssTime);
            do {
                summaries.add(cursor.getString(cursor.getColumnIndexOrThrow("summary")));
                links.add(cursor.getString(cursor.getColumnIndexOrThrow("link")));
                titles.add(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            } while (cursor.moveToNext());
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, titles);
            lvMain.setAdapter(adapter);
            lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showRecord(position);
                }
            });
        }
    }

    public void updateFeed(View view) {
        Intent newIntent = new Intent(this, MyIntentService.class);
        newIntent.putExtra("rssTitle", rssTitle);
        newIntent.putExtra("rssURL", rssURL);
        newIntent.putExtra("rssTime", rssTime);
        newIntent.putExtra("rssID", "" + rssID);
        startService(newIntent);
    }

    @Override
    protected void onPause() {
        if (mbr != null && registered) {
            unregisterReceiver(mbr);
            registered = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mbr != null && !registered) {
            registerReceiver(mbr, intentFilter);
            registered = true;
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
    public void showRecord(int position) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("summary", summaries.get(position));
        intent.putExtra("link", links.get(position));
        intent.putExtra("title", titles.get(position));
        startActivity(intent);
    }

}
