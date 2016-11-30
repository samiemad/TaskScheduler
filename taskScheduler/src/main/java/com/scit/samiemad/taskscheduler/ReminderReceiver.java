package com.scit.samiemad.taskscheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.scit.samiemad.taskscheduler.database.DbHelperSingleton;
import com.scit.samiemad.taskscheduler.database.Event;

/**
 * Created by Sami on 12/11/2015.
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Event.EXTRA_EVENT_ID, -1);
        Log.e("EVENT REMINDER", "Broadcast received!!! id = " + id);
        if (id == -1)
            return;
        Event event = new Event();
        SQLiteDatabase db = DbHelperSingleton.getInstance(context).getWritableDatabase();
        event.read(db, id);

        Log.e("EVENT REMINDER", "event: " + event.name + " , " + event.toString());

        Notification.Builder n = new Notification.Builder(context);
        n.setAutoCancel(true);
        n.setContentText(event.name + " in " + NewTaskActivity.Times[event.reminderTime] + " minutes!!");
        n.setContentTitle("تذكير بالمهمة");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n.setPriority(Notification.PRIORITY_MAX);
        }
        n.setDefaults(Notification.DEFAULT_ALL);
        n.setSmallIcon(R.drawable.ic_launcher);
        Intent i = new Intent(context, NewTaskActivity.class);
        i.putExtra(Event.EXTRA_EVENT_ID, id);
        i.setAction("foo"+id);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        n.setContentIntent(pi);
        Log.e("EVENT REMINDER", "nitification sent with id = " + id);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.notify((int) id, n.build());
        } else {
            manager.notify((int) id, n.getNotification());
        }
    }
}
