package com.android.calendar.year;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.R;
import com.android.datetimepicker.date.MonthView;

import java.util.HashMap;

/**
 * Adapter class used to display an entire year as a sequence of month views
 */
public class YearViewAdapter extends BaseAdapter implements View.OnTouchListener {

    /* Define configuration parameters for a month view */
    // fraction of the view occupied by the header
    private static final double MONTH_HEADER_HEIGHT = 0.25;

    // fraction of the header to use for the month title text
    private static final double MONTH_LABEL_FONT_SIZE = MONTH_HEADER_HEIGHT * 0.30;

    // fraction of the view to use as padding along one edge
    private static final double PADDING = 0.05;

    // fraction of the view to use for text
    private static final double MONTH_DAY_TEXT_SIZE = (1 - (2 * PADDING)) * (0.5)  / 7;

    // The padding b/w the week rows. The container height sans that taken up by the header
    // is equally distributed among six rows
    private static final double ROW_HEIGHT = (1 - MONTH_HEADER_HEIGHT) / 6;

    private static final int NUMBER_OF_MONTHS = 12;

    /* Instance Variables */
    private final Context mContext;
    private CalendarController mController;
    private int mYear;
    private int mCurrentMonth = -1;

    private int mColumns;       // number of columns the months will be dispersed into
    private int mWidth;         // dimensions of the container housing the entire year view
    private int mHeight;
    private int mMonthWidth;    // dimensions of each month inside the year view
    private int mMonthHeight;
    private int mCurrentMonthBgColor;
    private int mMonthTitleColor;
    private int mMonthClickColor;
    private int mCurrentDayColor;

    private View mClickedView;
    private long mClickTimestamp;
    private long mClickAnimDuration = ViewConfiguration.getTapTimeout() + 100;
    private int mTouchSlop;     // threshold for click to scroll transition
    private float mClickX;      // initial ACTION_DOWN coordinates
    private float mClickY;

    public YearViewAdapter(Context context, int year, int columns) {
        mContext = context;
        mController = CalendarController.getInstance(mContext);
        mYear = year;
        mColumns = columns;

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mMonthTitleColor = mContext.getResources().getColor(R.color.year_view_month_title_color);
        mMonthClickColor = mContext.getResources().getColor(R.color.year_view_month_click_color);
        mCurrentDayColor = mContext.getResources().getColor(R.color.year_view_current_day_color);
        mCurrentMonthBgColor =
                mContext.getResources().getColor(R.color.year_view_current_month_bg_color);
    }

    /**
     * Sets the container bounds that the entire year view can occupy.
     * Drawing parameters are calculated based off these bounds
     *
     * @param width max width of the parent container
     * @param height max height of the parent container
     */
    public void setBounds(int width, int height) {
        mWidth = width;
        mHeight = height;
        // calculate the dimensions of each of the months
        mMonthWidth = mWidth / mColumns;
        mMonthHeight = mMonthWidth;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_MONTHS;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Generates a month view
     *
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int month = position;
        // each month view is nested inside a linear layout
        GreedyLinearLayout linearLayout = new GreedyLinearLayout(mContext);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mMonthHeight);
        linearLayout.setLayoutParams(params);
        linearLayout.setId(month);
        linearLayout.setOnTouchListener(this);
        Time now = new Time();
        now.setToNow();
        if (now.month == month && now.year == mYear) {
            mCurrentMonth = month;
            linearLayout.setBackgroundColor(mCurrentMonthBgColor);
        }

        // the view that actually draws the calendar month
        MonthViewImpl monthView = new MonthViewImpl(mContext);

        /* set view configuration parmeters */
        HashMap<String, Integer> configureParams = new HashMap<String, Integer>();
        configureParams.put(MonthView.VIEW_PARAMS_YEAR, mYear);
        configureParams.put(MonthView.VIEW_PARAMS_MONTH, month);
        monthView.setMonthParams(configureParams);

        /* Customize view drawing parameters */
        // The drawing parameters are calculated based on the actual container dimensions and the
        // statically defined fraction components
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
        // padding in pixels ; applies to the left and right edges
        drawingParams.put(MonthView.CONFIG_EDGE_PADDING, (int) (mMonthWidth * PADDING));
        // add Month title header height
        drawingParams.put(MonthView.CONFIG_HEADER_SIZE, (int) (mMonthHeight * MONTH_HEADER_HEIGHT));
        // add Month label font size
        drawingParams.put(MonthView.CONFIG_MONTH_LABEL_TEXT_SIZE,
                (int) (mMonthHeight * MONTH_LABEL_FONT_SIZE));

        // text size is chosen so that there isn't an overlap in the horizontal or vertical
        // directions
        double textSize = Math.min(mMonthHeight * MONTH_DAY_TEXT_SIZE,
                mMonthWidth * MONTH_DAY_TEXT_SIZE);
        // add text size for month days
        drawingParams.put(MonthView.CONFIG_MONTH_DAY_TEXT_SIZE, (int) textSize);

        // header label flag
        drawingParams.put(MonthView.CONFIG_MONTH_HEADER_LABEL_FLAGS,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR |
                DateUtils.FORMAT_NO_MONTH_DAY);

        // add horizontal padding
        drawingParams.put(MonthView.CONFIG_MONTH_ROW_HEIGHT,
                (int) (mMonthHeight * ROW_HEIGHT));
        drawingParams.put(MonthView.CONFIG_FILL_PARENT_CONTAINER, 1);
        drawingParams.put(MonthView.CONFIG_HEADER_TITLE_COLOR, mMonthTitleColor);
        drawingParams.put(MonthView.CONFIG_DAY_SELECTED_CIRCLE_COLOR, mMonthTitleColor);
        drawingParams.put(MonthView.CONFIG_DAY_SELECTED_CIRCLE_ALPHA, 255);
        drawingParams.put(MonthView.CONFIG_CURRENT_DAY_COLOR, mCurrentDayColor);

        monthView.customizeViewParameters(drawingParams);

        ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(monthView, viewParams);

        return linearLayout;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mClickTimestamp = System.currentTimeMillis();
                mClickX = event.getX();
                mClickY = event.getY();
                mClickedView = v;
                // delay click acknowledgement to avoid view highlighting during scroll
                mClickedView.postDelayed(mHighlightView, ViewConfiguration.getTapTimeout());
                break;
            case MotionEvent.ACTION_UP:
                if (v == mClickedView) {
                    // avoid abrupt transitions in the UI by ensuring that the click animation
                    // persists for the minimum defined duration
                    long tapDuration = System.currentTimeMillis() - mClickTimestamp;
                    long delay = (tapDuration > mClickAnimDuration) ?
                            0 : mClickAnimDuration - tapDuration;
                    v.postDelayed(mPerformClick, delay);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // clear view highlighting when the gesture becomes a scroll
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

    /**
     * Undoes click acknowledgement (view highlight)
     */
    private void clearClickedView(View v) {
        v.removeCallbacks(mHighlightView);
        synchronized (v) {
            // restore the right bg color
            if (v.getId() == mCurrentMonth) {
                v.setBackgroundColor(mCurrentMonthBgColor);
            } else {
                v.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        mClickedView = null;
    }

    private final Runnable mHighlightView = new Runnable() {
        @Override
        public void run() {
            mClickedView.setBackgroundColor(mMonthClickColor);
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

    /**
     * Customized MonthView dictating how the days in a month are drawn onto the canvas
     */
    public static class MonthViewImpl extends MonthView {
        public MonthViewImpl(Context context) {
            super(context);
        }

        /**
         * Tasked with drawing a day in the month onto the canvas
         *
         * @param canvas  The canvas to draw on
         * @param year  The year of this month day
         * @param month  The month of this month day
         * @param day  The day number of this month day
         * @param x  The default x position to draw the day number
         * @param y  The default y position to draw the day number
         * @param startX  The left boundary of the day number rect
         * @param stopX  The right boundary of the day number rect
         * @param startY  The top boundary of the day number rect
         * @param stopY  The bottom boundary of the day number rect
         */
        @Override
        public void drawMonthDay(Canvas canvas, int year, int month, int day,
                                 int x, int y, int startX, int stopX, int startY, int stopY) {

            if (mHasToday && mToday == day) {
                int radius = mRowHeight / 2;
                canvas.drawCircle(x , y - (mMiniDayNumberTextSize / 3), radius,
                        mSelectedCirclePaint);
                mMonthNumPaint.setColor(mTodayNumberColor);
            } else {
                mMonthNumPaint.setColor(mDayTextColor);
            }
            canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
        }
    }
}
