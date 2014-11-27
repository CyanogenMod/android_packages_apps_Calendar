/**
 * Copyright (C) 2014-2016 The CyanogenMod Project
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
        // check
        if (mCurrentYear < EPOCH_TIME_YEAR_START || mCurrentYear > EPOCH_TIME_YEAR_END) {
            mCurrentYear = EPOCH_TIME_YEAR_START;
        }
        mCurrentMonth = calendar.get(Calendar.MONTH);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mController = CalendarController.getInstance(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        Time start = new Time(Time.getCurrentTimezone());
        start.set(1, 0, year);                              // Set(day, month, year)
        Time end = new Time(Time.getCurrentTimezone());
        end.set(31, 11, year);
        long formatFlags = DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mController.sendEvent(getActivity(), EventType.UPDATE_TITLE, start, end, null, -1,
                CalendarController.ViewType.CURRENT, formatFlags, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        goToYear(mCurrentYear);
    }

    /**
     * moves the pager to year specified by the input parameter
     * @param year
     */
    public void goToYear(int year) {
        if (mPager != null) {
            mPager.setCurrentItem(year - EPOCH_TIME_YEAR_START);
        }
    }

    @Override
    public long getSupportedEventTypes() {
        // support action bar navigation to the current time ("current time button")
        return EventType.GO_TO;
    }

    @Override
    public void handleEvent(EventInfo event) {
        // Handle "go to current time" action bar button click
        if (event.eventType == EventType.GO_TO) {
            goToYear(event.selectedTime.year);
        }
    }

    @Override
    public void eventsChanged() {
        // Nothing to do as this view doesn't depend on the underlying events database
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
            return YearViewFragment.newInstance(i);
        }

        // returns the count of years from 1970 - 2036 (inclusive)
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
                mColumns = 3;   // the year view will be in a 4x3 configuration
            } else {
                mColumns = 4;   // the year view will be in a 3x4 configuration
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mYear = getArguments() != null ? getArguments().getInt("year") :
                    EPOCH_TIME_YEAR_START;

            mController = CalendarController.getInstance(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mGridView = new GridView(getActivity());
            // Set layout params
            mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            mGridView.setNumColumns(mColumns);

            // We need to wait till the gridview is laid out
            // The adapter drawing the year view depends on the dimensions of the grid view to
            // figure out the layout and size of each of the months and its parameters
            mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove listener
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    int gridWidth = mGridView.getWidth();
                    int gridHeight = mGridView.getHeight();

                    // [repare adapter and supply the dimensions it has to work with
                    mAdapter = new YearViewAdapter(getActivity(), mYear, mColumns);
                    mAdapter.setBounds(gridWidth, gridHeight);

                    mGridView.setAdapter(mAdapter);
                }
            });

            return mGridView;
        }
    }
}
