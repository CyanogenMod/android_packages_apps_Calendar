/*
 * Copyright (C) 2014 The CyanogenMod Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar.year;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.R;
import com.android.datetimepicker.date.MonthView;
import com.android.datetimepicker.date.SimpleMonthView;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Fragment encompassing a view pager to allow swiping between Years
 *
 */
public class YearViewPagerFragment extends Fragment implements CalendarController.EventHandler {

    private static final int EPOCH_TIME_YEAR_START = 1970;
    private static final int EPOCH_TIME_YEAR_END = 2036;

    private ViewPager mPager;
    private YearViewPagerAdapter mPagerAdapter;
    private int mCurrentYear;
    private int mCurrentMonth;      // the current month based on the current time
    private int mCurrentPage;
    private CalendarController mController;

    public YearViewPagerFragment() {
        this(System.currentTimeMillis());
    }

    public YearViewPagerFragment(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        mCurrentYear = calendar.get(Calendar.YEAR);
        if (mCurrentYear < EPOCH_TIME_YEAR_START || mCurrentYear > EPOCH_TIME_YEAR_END) {
            mCurrentYear = 1970;
        }
        Log.i("ROHIT", "mCurrentYear : " + mCurrentYear);
        mCurrentMonth = calendar.get(Calendar.MONTH);   // todo tie to current time based on mTimeZone
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mController = CalendarController.getInstance(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // mController.deregisterEventHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.year_by_month, container, false);
        mPager = (ViewPager) v.findViewById(R.id.pager);
        mPagerAdapter = new YearViewPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                Log.i("ROHIT", "current page : " + mCurrentPage + " year: " + (EPOCH_TIME_YEAR_START + mCurrentPage));
                // set current year and return to this
                updateTitle(EPOCH_TIME_YEAR_START + mCurrentPage);
            }
        });

        return v;
    }

    /**
     * Interacts with the Calendar controller to update the action bar title with the current
     * year
     *
     * @param year year the title should be updated to
     */
    private void updateTitle(int year) {
        Log.i("ROHIT", "mController setting title : " + year);

        Time start = new Time(Time.getCurrentTimezone());
        start.set(1, 1, year);
        long formatFlags = DateUtils.FORMAT_SHOW_YEAR;
        mController.sendEvent(getActivity(), EventType.UPDATE_TITLE, start, start, null, -1,
                CalendarController.ViewType.CURRENT, formatFlags, null, null);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("ROHIT", "pager onResume()");
        goToYear(mCurrentYear);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void goToYear(int year) {
        if (mPager != null) {
            mPager.setCurrentItem(year - EPOCH_TIME_YEAR_START);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    private View getCalendarView(int i) {
        SimpleMonthView view = new SimpleMonthView(getActivity());
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
              //drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
//        drawingParams.put(MonthView.VIEW_PARAMS_HEIGHT, 20);
              drawingParams.put(MonthView.VIEW_PARAMS_YEAR, mCurrentYear);
              drawingParams.put(MonthView.VIEW_PARAMS_MONTH, i);
               //drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
               view.setMonthParams(drawingParams);

//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH, i);
//        view.setDate(calendar.getTime());
        return view;
    }

    @Override
    public long getSupportedEventTypes() {
        return EventType.GO_TO;
    }

    @Override
    public void handleEvent(EventInfo event) {
        if (event.eventType == EventType.GO_TO) {
            Log.i("ROHIT", "goto called");
            goToYear(event.selectedTime.year);
            /*
            if (mAdapter != null) {
                Log.i("ROHIT", "current time : " + System.currentTimeMillis());
                mAdapter.setTime(event.startTime);
                // reset adapter and redraw
                mAdapter.notifyDataSetChanged();
            } */
        }
    }

    @Override
    public void eventsChanged() {

    }

    /**
     * Adapter class for the fragment view pager that displays the year views
     *
     */
    public static class YearViewPagerAdapter extends FragmentPagerAdapter {

        public YearViewPagerAdapter (FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.i("ROHIT", "adapter getItem() : " + i);
            return YearViewFragment.newInstance(i);
        }

        // returns the count of years from 1970 - 2037
        @Override
        public int getCount() {
            return 67;
        }
    }

    /**
     * Fragment showing the entire year view
     */
    public static class YearViewFragment extends Fragment {
        private GridView mGridView;
        private YearViewAdapter mAdapter;
        private int mYear;
        private CalendarController mController;
        private int mColumns;

        // instance creator
        static YearViewFragment newInstance(int yearOffset) {
            YearViewFragment f = new YearViewFragment();

            Bundle args = new Bundle();
            args.putInt("year", EPOCH_TIME_YEAR_START + yearOffset);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            int orientation = getActivity().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mColumns = 3;
            } else {
                mColumns = 4;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mYear = getArguments() != null ? getArguments().getInt("year") :
                    EPOCH_TIME_YEAR_START;
            Log.i("ROHIT", "setting YearViewFragment mYear : " + mYear);

            mController = CalendarController.getInstance(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            Log.i("ROHIT", "year view onCreateView " + mYear);
            mGridView = new GridView(getActivity());
            // set layout params ?
            mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            mGridView.setNumColumns(mColumns);

            // we need to wait till the gridview is laid out
            // the adapter drawing the year view depends on the dimensions of the grid view to
            // figure out the layout and size of each of the months and its parameters
            mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // remove listener
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    int gridWidth = mGridView.getWidth();
                    int gridHeight = mGridView.getHeight();

                    // prepare adapter
                    mAdapter = new YearViewAdapter(getActivity(), mYear, 1, mColumns);
                    mAdapter.setBounds(gridWidth, gridHeight);

                    // set adapter
                    mGridView.setAdapter(mAdapter);

                }
            });

            return mGridView;

        }
    }
}
