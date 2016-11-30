package com.scit.samiemad.taskscheduler.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;
import com.scit.samiemad.taskscheduler.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sami on 12/8/2015.
 */
public class Event extends SqlObject {

    private static final int DEFAULT_EVENT_COLOR = Color.parseColor("#59dbe0");
    public static String EXTRA_EVENT_ID = "myExtraEventID";
    public static String TYPE = "type";
    public static String NAME = "name";
    public static String START_YEAR = "startYear";
    public static String START_MONTH = "startMonth";
    public static String START_DAY = "startDay";
    public static String START_HOUR = "startDay";
    public static String START_MINUTE = "startDay";
    public static String END_YEAR = "endYear";
    public static String END_MONTH = "endMonth";
    public static String END_DAY = "endDay";
    public static String END_HOUR = "endDay";
    public static String END_MINUTE = "endDay";
    public static String ALL_DAY = "allDay";
    public static String LOCATION = "location";
    public static String PEOPLE = "people";
    public static String DETAILS = "details";
    public static String REMINDER_TIME = "reminderTime";
    public static String IMAGE = "image";

    public int type;
    public String name;
    public int startYear, startMonth, startDay, startHour, startMinute;
    public int endYear, endMonth, endDay, endHour, endMinute;
    public boolean allDay;
    public String location;
    public String people;
    public String details;
    public int reminderTime;
    public String image;

    public Event(int type, String name, int startYear, int startMonth, int startDay, int startHour, int startMinute, int endYear, int endMonth, int endDay, int endHour, int endMinute, boolean allDay, String location, String people, String details, int reminderTime) {
        this.type = type;
        this.name = name;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endYear = endYear;
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.allDay = allDay;
        this.location = location;
        this.people = people;
        this.details = details;
        this.reminderTime = reminderTime;
    }

    public Event() {
    }

    public boolean create(SQLiteDatabase db) {
        return super.createAndGenerateId(db);
    }

    public static ArrayList<WeekViewEvent> getEventsForMonth(SQLiteDatabase db, int month) {
        Event e = new Event();
        Cursor c = db.query(e.getTableName(), e.getColNames(), "", null, null, null, null);
        Log.d("Events", "cursor size = " + c.getCount());
        ArrayList<WeekViewEvent> events = new ArrayList<>();
        if (!c.moveToFirst()) {
            c.close();
            return events;
        }
        for (int i = 0; i < c.getCount(); ++i) {
            e = new Event();
            e.readFromCursor(c);
            events.add(e.getWeekViewEvent());
            c.moveToNext();
        }
        Log.d("Events", "Query for month " + month + " resulted with " + events.size() + " events.");
        c.close();
        return events;
    }

    public WeekViewEvent getWeekViewEvent() {
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, startDay);
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        startTime.set(Calendar.MONTH, startMonth);
        startTime.set(Calendar.YEAR, startYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.DAY_OF_MONTH, endDay);
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);
        endTime.set(Calendar.MONTH, endMonth);
        endTime.set(Calendar.YEAR, endYear);
        WeekViewEvent event = new WeekViewEvent(id, name, startTime, endTime);
        event.setColor(DEFAULT_EVENT_COLOR);
        return event;
    }

    public void setImage(Bitmap bitmap) {
        Log.d("EVENT", "converting " + bitmap.getWidth() + "x" + bitmap.getHeight() + "p to Base64");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] b = baos.toByteArray();
        image = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("EVENT", "result size " + image.length());
        Log.e("EVENT", "Image encoded = " + this.image);
    }

    public Bitmap getImage() {
        byte[] decodedByte = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static void delete(SQLiteDatabase db, long id) {
        Event e = new Event();
        e.id = id;
        e.delete(db);
//        Cursor c = db.query(e.getTableName(), e.getColNames(), "ID = "+id, null, null, null, null);
//        if( c.getCount()>0 ){
//            c.moveToFirst();
//            e.readFromCursor(c);
//        }
    }
}
