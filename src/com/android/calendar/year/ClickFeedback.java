package com.android.calendar.year;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.android.calendar.R;

import java.util.HashMap;

/**
 * Provides click acknowledgement in the MonthViews comprising the YearView. See
 * {@link YearViewPagerFragment}. Draws circular ripples, concentric circles, over the day selected
 */
public class ClickFeedback extends View {

    public static final String CONFIG_RIPPLE_RADIUS = "ripple_radius";
    public static final String CONFIG_DAY_TEXT_COLOR = "day_text_color";
    public static final String CONFIG_DAY_TEXT_SIZE = "day_text_size";

    private Context mContext;
    private boolean mEnabled;               // dictates the drawing status
    private float mCenterX;                 // coordinates of the ripple
    private float mCenterY;
    private int mClickedDay;                // the day that was selected
    private float mPrimaryCircleRadius;
    private float mSecondaryCircleRadius;
    private Paint mPrimaryCirclePaint;
    private Paint mSecondaryCirclePaint;
    private Paint mSelectedDayPaint;

    private int mDayTextSize;
    private int mDayTextColor;
    private float mRatio;                   // dictates the ratio of secondary to primary circle

    public ClickFeedback(Context context) {
        super(context);
        mContext = context;
    }

    public void initialize(HashMap<String, Integer> params) {
        if (params == null) return;
        for (String key : params.keySet()) {

            int paramValue = params.get(key);
            if (key.equals(CONFIG_RIPPLE_RADIUS)) {
                mPrimaryCircleRadius = paramValue;
            } else if (key.equals(CONFIG_DAY_TEXT_COLOR)) {
                mDayTextColor = paramValue;
            } else if (key.equals(CONFIG_DAY_TEXT_SIZE)) {
                mDayTextSize = paramValue;
            }
        }

        initializePaints();
    }

    private void initializePaints() {
        mPrimaryCirclePaint = new Paint();
        mPrimaryCirclePaint.setAntiAlias(true);
        mPrimaryCirclePaint.setStyle(Paint.Style.FILL);
        mPrimaryCirclePaint.setAlpha(30);
        mPrimaryCirclePaint.setColor(
                mContext.getResources().getColor(R.color.year_view_ripple_primary));

        mSecondaryCirclePaint = new Paint();
        mSecondaryCirclePaint.setAntiAlias(true);
        mSecondaryCirclePaint.setStyle(Paint.Style.FILL);
        mSecondaryCirclePaint.setAlpha(100);
        mSecondaryCirclePaint.setColor(
                mContext.getResources().getColor(R.color.year_view_ripple_secondary));

        mSelectedDayPaint = new Paint();
        mSelectedDayPaint.setAntiAlias(true);
        mSelectedDayPaint.setTextSize(mDayTextSize);
        mSelectedDayPaint.setStyle(Paint.Style.FILL);
        mSelectedDayPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedDayPaint.setFakeBoldText(false);
        mSelectedDayPaint.setColor(mDayTextColor);
    }

    /**
     * set up the view to draw the click feedback
     *
     * @param x x-coordinate of the click
     * @param y y-coordinate of the click
     * @param day the clicked day
     */
    public void initializeClickFeedback(float x, float y, int day) {
        mEnabled = true;
        mCenterX = x;
        mCenterY = y;
        mClickedDay = day;
    }

    // setter and getter for ObjectAnimator
    public void setRatio(float ratio) {
        mRatio = ratio;
        invalidate();
    }

    public float getRatio() {
        return mRatio;
    }

    public void disable() {
        mEnabled = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mEnabled) {
            mSecondaryCircleRadius = mRatio * mPrimaryCircleRadius;
            canvas.drawCircle(mCenterX, mCenterY - (mDayTextSize / 3), mPrimaryCircleRadius,
                    mPrimaryCirclePaint);
            canvas.drawCircle(mCenterX, mCenterY - (mDayTextSize / 3), mSecondaryCircleRadius,
                    mSecondaryCirclePaint);
            canvas.drawText(String.format("%d", mClickedDay), mCenterX, mCenterY, mSelectedDayPaint);
        }
    }
}
