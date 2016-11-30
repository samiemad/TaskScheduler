package com.scit.samiemad.taskscheduler.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.w3c.dom.Comment;

/**
 * Created by Sami on 12/8/2015.
 */

public class DbHelperSingleton extends SQLiteOpenHelper {

    private static DbHelperSingleton mInstance = null;

    private static final String DATABASE_NAME = "samiemad.scit.db";
    private static final int DATABASE_VERSION = 3;

    public static DbHelperSingleton getInstance(Context context) {
        /**
         * Use the application context as suggested by CommonsWare.
         * this will ensure that you don't accidentally leak an Activity's
         * context (see this article for more information:
         * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new DbHelperSingleton(context.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DbHelperSingleton(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        new Event().createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + new Event().getTableName());
        onCreate(db);
    }
}