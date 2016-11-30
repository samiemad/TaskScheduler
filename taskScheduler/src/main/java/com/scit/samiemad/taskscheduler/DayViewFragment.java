package com.scit.samiemad.taskscheduler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.scit.samiemad.taskscheduler.database.DbHelperSingleton;
import com.scit.samiemad.taskscheduler.database.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Sami on 12/8/2015.
 */
public class DayViewFragment extends Fragment {

    int numDays = 1;

    public DayViewFragment() {
    }

    private WeekView v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_day, container, false);
        v = (WeekView) rootView.findViewById(R.id.weekView);
        v.setNumberOfVisibleDays(numDays);
        v.setMonthChangeListener(new WeekView.MonthChangeListener() {
            @Override
            public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                SQLiteDatabase db = DbHelperSingleton.getInstance(getActivity()).getReadableDatabase();
                ArrayList<WeekViewEvent> events = Event.getEventsForMonth(db, newMonth);
                for (WeekViewEvent i : events) {
                    Log.d("Events", i.getName() + " from " + dd(i.getStartTime()) + " to " + dd(i.getEndTime()) + " col: " + i.getColor());
                }
                return events;
            }

            private String getEventTitle(Calendar time) {
                return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
            }

            private String dd(Calendar time) {
                return time.get(Calendar.YEAR) + "/" + (time.get(Calendar.MONTH) + 1) + "/" + time.get(Calendar.DAY_OF_MONTH)
                        + ", " + time.get(Calendar.HOUR_OF_DAY) + ":" + time.get(Calendar.MINUTE);
            }
        });
        v.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Intent i = new Intent(getActivity(), NewTaskActivity.class);
                i.putExtra(Event.EXTRA_EVENT_ID, event.getId());
                startActivity(i);
//                Snackbar.make(rootView, event.getName(), Snackbar.LENGTH_LONG).show();
            }
        });
        v.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(final WeekViewEvent event, RectF eventRect) {
                AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
                dlg.setTitle("Delete " + event.getName() + ", are you sure?");
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            SQLiteDatabase db = DbHelperSingleton.getInstance(getActivity()).getWritableDatabase();
                            Event.delete(db, event.getId());
                            v.notifyDatasetChanged();
                        }
                        dialog.dismiss();
                    }
                };
                dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), listener);
                dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), listener);
                dlg.show();
            }
        });
        v.setDefaultEventColor(getActivity().getResources().getColor(R.color.event_color_01));
        return rootView;
    }

    public void setNumDays(int days) {
        numDays = days;
    }

    @Override
    public void onResume() {
        super.onResume();
        v.notifyDatasetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        ((MainActivity) activity).onSectionAttached(getArguments().getInt("SECTION_NUMBER"));
    }
}