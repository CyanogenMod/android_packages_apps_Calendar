package com.android.calendar.year;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.android.datetimepicker.date.MonthView;
import com.android.datetimepicker.date.SimpleMonthView;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by danesh on 11/26/14.
 */
public class YearAdapter extends BaseAdapter {

    private final Context mContext;

    public YearAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    private View getCalendarView(int i) {
        LinearLayout layout = new LinearLayout(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 350);
        layout.setLayoutParams(params);
        SimpleMonthView view = new SimpleMonthView(mContext);
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, 2014);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, i);
        view.setMonthParams(drawingParams);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(400, 350);
        layout.addView(view, viewParams);
        return layout;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private int getRandomColor() {
        Random random = new Random();
        return Color.argb(55, random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = getCalendarView(position);
        v.setBackgroundColor(getRandomColor());
        return v;
    }
}
