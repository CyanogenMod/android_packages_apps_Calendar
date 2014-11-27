package com.android.calendar.year;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.datetimepicker.date.MonthView;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by danesh on 11/26/14.
 */
public class YearViewAdapter extends BaseAdapter implements View.OnTouchListener {

    // percentage of the view occupied by the header
    private static final double MONTH_HEADER_HEIGHT = 0.30;

    // font size (in pixels) of the month label
    private static final double MONTH_LABEL_FONT_SIZE = MONTH_HEADER_HEIGHT * 0.30;

    // line spacing between the week rows ; expressed as a percentage of the
    private static final double MONTH_HORIZONTAL_PADDING = (1 - MONTH_HEADER_HEIGHT);

    // right and left padding of the view - as a % of the width
    private static final double PADDING = 0.05;

    // line spacing between the week columns
    private static final double MONTH_DAY_TEXT_SIZE = (1 - (2 * PADDING)) * (0.7)  / 7;

    // private static final double
    private static final double HORIZONTAL_PADDING = (1 - MONTH_HEADER_HEIGHT) / 6;

    /* Instance Variables */
    private final Context mContext;
    private CalendarController mController;
    private int mYear;
    private int mCurrentMonth;
    private int mColumns;       // number of columns in the grid view

    private int mWidth;     // dimensions of the container housing the entire year view
    private int mHeight;

    private int mMonthWidth;    // dimensions of each month inside the year view
    private int mMonthHeight;

    private View mClickedView;
    private long mClickTimestamp;
    // min duration for click animation
    private long mClickDelay = ViewConfiguration.getTapTimeout() + 100;
    private int mTouchSlop;

    // click coords
    private float mClickX;
    private float mClickY;


    public YearViewAdapter(Context context, int year, int currentMonth, int columns) {
        mContext = context;
        mController = CalendarController.getInstance(mContext);
        mYear = year;
        mCurrentMonth = currentMonth;
        mColumns = columns;

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
    }

    public void setTime(Time startTime) {
        //
        mYear = 2014;
        mCurrentMonth = 0;
    }

    public void setBounds(int width, int height) {
        mWidth = width;
        mHeight = height;
        // calculate the dimensions of each of the months
        mMonthWidth = mWidth / mColumns;
        mMonthHeight = mHeight / (12 / mColumns);

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
        // LinearLayout params should be determined at runtime based on device display type

        // month header size
        // month title text size
        // day text size
        // left and right padding

        GreedyLinearLayout layout = new GreedyLinearLayout(mContext);
        // LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 350);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mMonthHeight);
        layout.setLayoutParams(params);
        layout.setId(i);
        layout.setOnTouchListener(this);    // add a touch listener to SimpleMonthView ??
//        layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("ROHIT", "clicked on month : " + v.getId());
//            }
//        });

        MonthViewImpl view = new MonthViewImpl(mContext);
        view.setClickable(false);
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, mYear);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, i);
        // drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, 9);

        view.setMonthParams(drawingParams);
        // customize view params
        HashMap<String, Integer> viewParameters = new HashMap<String, Integer>();
        viewParameters.put(MonthView.CONFIG_DAY_LABEL_TEXT_SIZE, 7);

        // padding in pixels ; applies onto the left and right edges
        viewParameters.put(MonthView.CONFIG_EDGE_PADDING, (int)(mMonthWidth * PADDING));
        // add Month title header height
        viewParameters.put(MonthView.CONFIG_HEADER_SIZE, (int)(mMonthHeight * MONTH_HEADER_HEIGHT));
        // add Month label font size
        viewParameters.put(MonthView.CONFIG_MONTH_LABEL_TEXT_SIZE, (int)(mMonthHeight * MONTH_LABEL_FONT_SIZE));

        // add text size for month days
        double textSize = Math.min(mMonthHeight * MONTH_DAY_TEXT_SIZE,
                mMonthWidth * MONTH_DAY_TEXT_SIZE);
        viewParameters.put(MonthView.CONFIG_MONTH_DAY_TEXT_SIZE, (int)textSize);

        // header label flag
        viewParameters.put(MonthView.CONFIG_MONTH_HEADER_LABEL_FLAGS, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
                | DateUtils.FORMAT_NO_MONTH_DAY);

        // add horizontal padding
        viewParameters.put(MonthView.CONFIG_MONTH_ROW_HEIGHT, (int)(mMonthHeight * HORIZONTAL_PADDING));
        viewParameters.put(MonthView.CONFIG_FILL_PARENT_CONTAINER, 1);

        // viewParameters.put(MonthView.CONFIG_HEADER_TITLE_OFFSET, (int)(mMonthWidth * PADDING) + ((int)(mMonthHeight * HORIZONTAL_PADDING)/2));

        viewParameters.put(MonthView.CONFIG_HEADER_TITLE_COLOR, Color.rgb(19,155,234));

        view.customizeViewParameters(viewParameters);


//        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(400, 350);
        // LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
        //        ViewGroup.LayoutParams.WRAP_CONTENT, 350);
        if (i == mCurrentMonth) layout.setBackgroundColor(getRandomColor());
        ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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
        // v.setBackgroundColor(getRandomColor());
        return v;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i("ROHIT", "in ACTION_DOWN");
                mClickTimestamp = System.currentTimeMillis();
                mClickX = event.getX();
                mClickY = event.getY();
                mClickedView = v;
                mClickedView.postDelayed(mHighlightView, ViewConfiguration.getTapTimeout());
                break;
            case MotionEvent.ACTION_UP:
                Log.i("ROHIT", "in ACTION_UP");
                if (v == mClickedView) {
                    // show the animation for atleast 300 ms
                    long tapDuration = System.currentTimeMillis() - mClickTimestamp;
                    long delay = (tapDuration > mClickDelay) ? 0 : mClickDelay - tapDuration;
                    v.postDelayed(mPerformClick, delay);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if ((Math.abs(event.getX() - mClickX) > mTouchSlop) ||
                        (Math.abs(event.getY() - mClickY) > mTouchSlop)) {
                    clearClickedView(v);
                }
                break;
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_CANCEL:
                clearClickedView(v);
                break;
        }

        return true;
    }

    private void clearClickedView(View v) {
        v.removeCallbacks(mHighlightView);
        synchronized (v) {
            v.setBackgroundColor(Color.TRANSPARENT);
        }
        mClickedView = null;
    }

    private final Runnable mHighlightView = new Runnable() {
        @Override
        public void run() {
            Log.i("ROHIT", "in highlight view");
            mClickedView.setBackgroundColor(Color.parseColor("#e1bee7"));
        }
    };

    /**
     * A runnable that completes the action mandated by a touch event. Used to delay the action
     * consequence whilst the click animation is being performed.
     */
    private final Runnable mPerformClick = new Runnable() {
        @Override
        public void run() {
            Time time = new Time(Time.getCurrentTimezone());
            time.set(1, mClickedView.getId(), mYear);   // set(day, month, year)
            mController.sendEvent(mContext, EventType.GO_TO, time, time, -1,
                    CalendarController.ViewType.MONTH,
                    CalendarController.EXTRA_GOTO_BACK_TO_PREVIOUS, null, null );
        }
    };

    /**
     * A greedy LinearLayout that always intercepts the touch events from its children.
     */
    private static class GreedyLinearLayout extends LinearLayout {

        public GreedyLinearLayout(Context context) {
            super(context);
        }

        /**
         * Always intercept the touch events
         * @param ev touch event
         * @return always true indicating that the touch events won't be propagated to the children
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }


    public static class MonthViewImpl extends MonthView {
        public MonthViewImpl(Context context) {
            super(context);
        }

        @Override
        public void drawMonthDay(Canvas canvas, int year, int month, int day,
                                 int x, int y, int startX, int stopX, int startY, int stopY) {
            if (mSelectedDay == day) {
                canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), mRowHeight,
                        mSelectedCirclePaint);
            }

            // If we have a mindate or maxdate, gray out the day number if it's outside the range.
            if (isOutOfRange(year, month, day)) {
                mMonthNumPaint.setColor(mDisabledDayTextColor);
            } else if (mHasToday && mToday == day) {
                int radius = mRowHeight / 2;
                canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), radius,
                        mSelectedCirclePaint);
                mMonthNumPaint.setColor(mTodayNumberColor);
            } else {
                mMonthNumPaint.setColor(mDayTextColor);
            }
            canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
        }

    }

}
