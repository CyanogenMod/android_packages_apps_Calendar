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

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.android.calendar.CalendarController;
import com.android.datetimepicker.date.MonthView;
import com.android.datetimepicker.date.SimpleMonthView;

import java.util.HashMap;
import java.util.Random;

public class YearByMonthFragment extends Fragment implements CalendarController.EventHandler {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GridView gridView = new GridView(getActivity());
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setNumColumns(2);

        YearAdapter adapter = new YearAdapter(getActivity());
        gridView.setAdapter(adapter);
//        LinearLayout rootLayout = new LinearLayout(getActivity());
//        rootLayout.setOrientation(LinearLayout.VERTICAL);
//        rootLayout.setPadding(10, 0, 10, 0);
//        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        rootLayout.setLayoutParams(rootParams);
//        int numCols = 3;
//        for (int row = 0; row < 4; row++) {
//            LinearLayout layout = new LinearLayout(getActivity());
////            layout.setBackgroundColor(getRandomColor());
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//            params.gravity = Gravity.TOP;
//            params.weight = 1;
//            for (int column = 0; column < numCols; column++) {
//                View view = getCalendarView((row * numCols) + column);
//                view.setPadding(25, 25, 25, 25);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
//                layoutParams.gravity = Gravity.TOP;
//                //layoutParams.setMargins(0, 0, 10, 0);
//                layoutParams.weight = 1;
//                layout.addView(view, layoutParams);
//            }
//            rootLayout.addView(layout, params);
//        }
        return gridView;
    }

    private View getCalendarView(int i) {
        SimpleMonthView view = new SimpleMonthView(getActivity());
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
              //drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
//        drawingParams.put(MonthView.VIEW_PARAMS_HEIGHT, 20);
              drawingParams.put(MonthView.VIEW_PARAMS_YEAR, 2014);
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
        return 0;
    }

    @Override
    public void handleEvent(CalendarController.EventInfo event) {

    }

    @Override
    public void eventsChanged() {

    }
}
