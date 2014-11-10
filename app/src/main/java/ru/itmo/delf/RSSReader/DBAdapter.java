package ru.itmo.delf.RSSReader;
/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

    public static final String KEY_C_TITLE = "title";
    public static final String KEY_C_URL = "url";
    public static final String KEY_C_TIME = "cTime";
    public static final String KEY_RSS = "rss";
    public static final String KEY_A_SUMMARY = "summary";
    public static final String KEY_A_LINK = "link";
    public static final String KEY_A_TITLE = "title";
    public static final String KEY_ROW_ID = "_id";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String RSSS_TABLE = "rsss";
    private static final String DATABASE_NAME = "dbRssLes7";
    private static final int DATABASE_VERSION = 1;

    private static final String INIT_RSSS =
            "create table " + RSSS_TABLE + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                            + KEY_C_TITLE + " text not null, "
                            + KEY_C_URL + " text not null, "
                            + KEY_C_TIME + " text not null)";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public static final String[] rssTitles = {"bash",
                                                      "IT happens",
                                                      "Zadolba.li"};
        public static final String[] rssURLs = {"http://bash.im/rss",
                                                    "http://ithappens.ru/rss",
                                                    "http://zadolba.li/rss"};

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(INIT_RSSS);
            for (int i = 0; i < rssTitles.length; i++) {
                ContentValues initialValues = new ContentValues();
                initialValues.put(KEY_C_TITLE, rssTitles[i]);
                initialValues.put(KEY_C_URL, rssURLs[i]);
                initialValues.put(KEY_C_TIME, "never");
                db.insert(RSSS_TABLE, null, initialValues);
                db.execSQL("create table " + KEY_RSS + (i + 1) + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                                + KEY_A_SUMMARY + " text not null, "
                                + KEY_A_LINK + " text not null, "
                                + KEY_A_TITLE + " text not null)");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + RSSS_TABLE);
            onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    void createRssTable(long rssID) {
        mDb.execSQL("create table " + KEY_RSS + (rssID) + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                        + KEY_A_SUMMARY + " text not null, "
                        + KEY_A_LINK + " text not null, "
                        + KEY_A_TITLE + " text not null)");
    }

    void dropRssTable(long rssID) {
        mDb.execSQL("drop table if exists " + KEY_RSS + (rssID));
    }

    void clearRssTable(long rssID) {
        mDb.delete(KEY_RSS+(rssID), null, null);
    }

    public long addNews(int rssID, String summary, String link, String title) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_A_SUMMARY, summary);
        initialValues.put(KEY_A_LINK, link);
        initialValues.put(KEY_A_TITLE, title);
        return mDb.insert(KEY_RSS + rssID, null, initialValues);
    }

    public long createRss(String title, String url, String cTime) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_C_TITLE, title);
        initialValues.put(KEY_C_URL, url);
        initialValues.put(KEY_C_TIME, cTime);
        return mDb.insert(RSSS_TABLE, null, initialValues);
    }

    public boolean deleteRss(long rowId) {
        dropRssTable(rowId);
        return mDb.delete(RSSS_TABLE, KEY_ROW_ID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllRsss() {
        return mDb.query(RSSS_TABLE, new String[] {KEY_ROW_ID, KEY_C_TITLE,
                KEY_C_URL, KEY_C_TIME}, null, null, null, null, null);
    }

    public Cursor fetchAllArticles(int rssID) {

        return mDb.query(KEY_RSS + rssID, new String[] {KEY_ROW_ID, KEY_A_TITLE,
                KEY_A_SUMMARY, KEY_A_LINK}, null, null, null, null, null);
    }

    public boolean updateRss(long rowId, String title, String url, String cTime) {
        ContentValues args = new ContentValues();
        args.put(KEY_C_TITLE, title);
        args.put(KEY_C_URL, url);
        args.put(KEY_C_TIME, cTime);
        return mDb.update(RSSS_TABLE, args, KEY_ROW_ID + "=" + rowId, null) > 0;
    }
}
