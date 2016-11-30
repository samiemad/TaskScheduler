package com.scit.samiemad.taskscheduler;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.scit.samiemad.taskscheduler.database.DbHelperSingleton;
import com.scit.samiemad.taskscheduler.database.Event;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class NewTaskActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 20;
    private static final int IMAGE_PICKER_REQUEST = 1;
    public static final int[] Times = {-1, 5, 15, 30, 60, 120, 360, 720, 1440};
    Button vStartDate, vEndDate;
    Button vStartTime, vEndTime;
    private EditText etName;
    Spinner spinnerType, spinnerReminder;
    private Event event;
    Button bLocation;
    boolean editMode = false;
    private ImageButton ibImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        vStartTime = (Button) findViewById(R.id.startTime);
        vStartDate = (Button) findViewById(R.id.startDate);
        vEndTime = (Button) findViewById(R.id.endTime);
        vEndDate = (Button) findViewById(R.id.endDate);
        etName = (EditText) findViewById(R.id.etTaskName);
        spinnerType = (Spinner) findViewById(R.id.spinnerTaskType);
        bLocation = (Button) findViewById(R.id.bLocation);
        spinnerReminder = (Spinner) findViewById(R.id.spinnerReminder);
        ibImage = (ImageButton) findViewById(R.id.ibEventImage);

        event = new Event();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        long id = getIntent().getLongExtra(Event.EXTRA_EVENT_ID, -1);
        Log.wtf("EVENT", "event id = " + id);
        if (id != -1) {
            SQLiteDatabase db = DbHelperSingleton.getInstance(this).getWritableDatabase();
            if (event.read(db, id)) {
                displayEvent();
                Log.d("EVENT", "event read: " + event.name);
                editMode = true;
            } else {
                Snackbar.make(ibImage, "Could not read Event from Database!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void displayEvent() {
        etName.setText(event.name);
        vStartTime.setText(String.format("%02d:%02d", event.startHour, event.startMinute));
        vStartDate.setText(String.format("%04d/%02d/%02d", event.startYear, event.startMonth, event.startDay));
        vEndTime.setText(String.format("%02d:%02d", event.endHour, event.endMinute));
        vEndDate.setText(String.format("%04d/%02d/%02d", event.endYear, event.endMonth, event.endDay));
        bLocation.setText(event.location);
        spinnerType.setSelection(event.type);
        if (event.image != null)
            ibImage.setImageBitmap(event.getImage());
        else
            Log.d("EVENT", "image is null!");
    }

    public void setTime(String tag, int hourOfDay, int minute) {
        if (tag.equals("startTime")) {
            event.startHour = hourOfDay;
            event.startMinute = minute;
            vStartTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        } else {
            event.endHour = hourOfDay;
            event.endMinute = minute;
            vEndTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }
    }

    public void setDate(String tag, int year, int monthOfYear, int dayOfMonth) {
        if (tag.equals("startDate")) {
            event.startYear = year;
            event.startMonth = monthOfYear;
            event.startDay = dayOfMonth;
            vStartDate.setText(String.format("%02d/%02d/%02d", year, monthOfYear + 1, dayOfMonth));
        } else {
            event.endYear = year;
            event.endMonth = monthOfYear;
            event.endDay = dayOfMonth;
            vEndDate.setText(String.format("%02d/%02d/%02d", year, monthOfYear + 1, dayOfMonth));
        }
    }

    public void clickSave(View v) {
        if (check()) {
            event.name = etName.getText().toString();
            event.type = spinnerType.getSelectedItemPosition();
            event.reminderTime = spinnerReminder.getSelectedItemPosition();
            SQLiteDatabase db = DbHelperSingleton.getInstance(this).getWritableDatabase();
            if (editMode) {
                event.update(db);
            } else {
                event.create(db);
            }
            addReminder();
            finish();
        }
    }

    private void addReminder() {
        if (event.reminderTime == 0)
            return;
        Calendar calendar = (Calendar) event.getWeekViewEvent().getStartTime().clone();
        calendar.add(Calendar.MINUTE, -Times[event.reminderTime]);

        Intent myIntent = new Intent(this, ReminderReceiver.class);
        myIntent.putExtra(Event.EXTRA_EVENT_ID, event.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "تم ضبط التنبيه في وقت: " + calendar.getTime().toString(), Toast.LENGTH_LONG).show();
    }

    private boolean check() {
        boolean good = true;
        String error = "";
        if (etName.getText().toString().length() == 0) {
            etName.setError("الرجاء ادخال اسم.");
            etName.requestFocus();
            good = false;
            error += getString(R.string.errorName);
        }
        WeekViewEvent e = event.getWeekViewEvent();
        if (e.getStartTime().after(e.getEndTime())) {
            good = false;
            error += "\n" + getString(R.string.errorTime);
        }
        e.getStartTime().add(Calendar.MINUTE, -Times[event.reminderTime]);
        if (event.reminderTime > 0 && e.getStartTime().after(Calendar.getInstance())) {
            error += "\n" + getString(R.string.errorReminder);
            good = false;
        }
        Snackbar.make(findViewById(R.id.layoutNewTask), error, Snackbar.LENGTH_LONG).show();
        return good;
    }

    public void clickSetTime(View v) {
        new TimePickerFragment().show(getSupportFragmentManager(), v.getTag().toString());
    }

    public void clickSetDate(View v) {
        new DatePickerFragment().show(getSupportFragmentManager(), v.getTag().toString());
    }

    public void clickImage(View v) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");

        //Create chooser
        Intent chooser = Intent.createChooser(galleryIntent, getString(R.string.pickImage));
//        Snackbar.make(v, "Image is not available right now!!", Snackbar.LENGTH_LONG).show();

        startActivityForResult(chooser, IMAGE_PICKER_REQUEST);
    }

    public void clickLocation(View v) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            AlertDialog dlg = new AlertDialog.Builder(this).create();
            dlg.setTitle(R.string.error_play_services_title);
            dlg.setMessage(getString(R.string.error_play_services_info));
            dlg.setIcon(android.R.drawable.ic_dialog_alert);
            dlg.show();
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                bLocation.setText(place.getName());
            }
        } else {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri uri = data.getData();
                    final InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap b = BitmapFactory.decodeStream(is);
                    int mx = Math.max(b.getWidth(), b.getHeight()) / 300;
                    int nw = b.getWidth() / mx;
                    int nh = b.getHeight() / mx;
                    b = Bitmap.createScaledBitmap(b, nw, nh, false);
                    event.setImage(b);
                    if (event.image == null)
                        Log.e("EVENT", "IMAGE IS NOT READ PROPERLY!!");
                    ibImage.setImageBitmap(b);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
