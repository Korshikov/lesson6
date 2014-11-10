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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ChooserActivity extends Activity  {

    final DBAdapter myDBAdapter = new DBAdapter(this);

    MyBroadcastReceiver mbr = null;
    IntentFilter intentFilter;
    boolean registered = false;
    ListView lvChooser;
    SimpleCursorAdapter notes;
    Cursor cursor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_select);
        myDBAdapter.open();
        final Cursor cursor = myDBAdapter.fetchAllRsss();
        this.cursor = cursor;
        startManagingCursor(cursor);
        String[] from = new String[]{DBAdapter.KEY_C_TITLE};
        int[] to = new int[]{R.id.chooser_row_text};
        notes = new SimpleCursorAdapter(this, R.layout.rss_row, cursor, from, to);
        lvChooser = (ListView) findViewById(R.id.list);
        lvChooser.setAdapter(notes);
        lvChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), MyActivity.class);
                cursor.moveToPosition(position);
                String rssURL = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                String rssTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String rssTime = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TIME));
                int rssID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                intent.putExtra("rssURL", rssURL);
                intent.putExtra("rssTitle", rssTitle);
                intent.putExtra("rssTime", rssTime);
                intent.putExtra("rssID", "" + rssID);
                startActivity(intent);
            }
        });
        lvChooser.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int rssID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                myDBAdapter.deleteRss(rssID);
                cursor.requery();
                notes.notifyDataSetChanged();
                lvChooser.invalidateViews();
                return true;
            }
        });

        (findViewById(R.id.add_button)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                               Intent intent = new Intent(view.getContext(), AddRss.class);
                                startActivityForResult(intent, 200);
                                cursor.requery();
                                notes.notifyDataSetChanged();
                                lvChooser.invalidateViews();
                            }

                        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                            if (data == null) {return;}
                            cursor.requery();
                            notes.notifyDataSetChanged();
                            lvChooser.invalidateViews();
                        }

                    });

        mbr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter(MyIntentService.key);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mbr, intentFilter);
        registered = true;

    }

    public void updateAll(View view) {
        Cursor cursor = myDBAdapter.fetchAllRsss();
        startManagingCursor(cursor);
        if (cursor.moveToFirst()) {
            do {
                Intent newIntent = new Intent(this, MyIntentService.class);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TIME));
                int ID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                newIntent.putExtra("rssTitle", title);
                newIntent.putExtra("rssTime", time);
                newIntent.putExtra("rssURL", url);
                newIntent.putExtra("rssID", "" + ID);
                startService(newIntent);
            } while (cursor.moveToNext());
        }
    }

    Intent intent;

    public void addRss(View view)
    {

    }



    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {}
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

}
