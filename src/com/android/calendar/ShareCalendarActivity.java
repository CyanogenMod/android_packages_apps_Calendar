package com.android.calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.calendar.agenda.AgendaFragment;
import com.android.calendar.icalendar.IcalendarUtils;
import com.android.calendar.icalendar.VCalendar;
import com.android.calendar.icalendar.VEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Handles requests for sharing calendar events
 * This activity returns a vcs formatted file
 */
public class ShareCalendarActivity extends Activity implements CalendarUtils.ShareEventListener {

    public static final String EXTRA_LIMIT_TO_ONE_EVENT = "EXTRA_LIMIT_TO_ONE_EVENT";

    private static final String TAG = "ShareCalendarActivity";

    private HashSet<CalendarUtils.Triple<Long, Long, Long>> mShareEventsList =
            new HashSet<CalendarUtils.Triple<Long, Long, Long>>();

    private ArrayList<EventInfoFragment> mEventDataFragments = new ArrayList<EventInfoFragment>();

    private ActionBar mActionBar;
    private AgendaFragment mAgendaFragment;
    private long mStartMillis;
    private ArrayList<Uri> mFileUris = new ArrayList<Uri>();
    private int mNumQueriesCompleted;
    private boolean mShouldSelectSingleEvent;
    private Resources mResources;
    private String mNoSelectionMsg;

    @Override
    public void onEventShared(CalendarUtils.Triple<Long, Long, Long> eventInfo) {
        if (eventInfo.first < 0) return;

        mShareEventsList.add(eventInfo);
        updateSubtitle();
    }

    @Override
    public void onEventRemoval(long eventId) {
        if (eventId < 0) return;

        CalendarUtils.Triple<Long, Long, Long> eventInfo =
                new CalendarUtils.Triple<Long, Long, Long>(eventId, 0l, 0l);
        mShareEventsList.remove(eventInfo);
        updateSubtitle();
    }

    @Override
    public void onResetShareList() {
        // undo
        mShareEventsList.clear();
        updateSubtitle();
    }

    public void updateSubtitle() {
        int listSize = mShareEventsList.size();
        String subtitle;
        // workaround for Android's poor pluralization treatment of 'zero' quantity in English
        if (listSize == 0) {
            subtitle = mNoSelectionMsg;
        } else {
            subtitle = mResources.getQuantityString(R.plurals.events_selected,
                    listSize, listSize);
        }
        mActionBar.setSubtitle(subtitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args != null) {
            mShouldSelectSingleEvent = args.getBoolean(EXTRA_LIMIT_TO_ONE_EVENT, false);
        }

        mStartMillis = System.currentTimeMillis();
        setContentView(R.layout.simple_frame_layout);
        mResources = getResources();

        // create agenda fragment displaying the list of calendar events
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        mAgendaFragment = new AgendaFragment(mStartMillis, false, true);
        mAgendaFragment.setShareModeOptions(this, mShouldSelectSingleEvent);
        ft.replace(R.id.main_frame, mAgendaFragment);
        ft.commit();

        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(true);
        String title = mShouldSelectSingleEvent ?
                mResources.getQuantityString(R.plurals.select_events_to_share, 1) :
                mResources.getQuantityString(R.plurals.select_events_to_share, 2) ;

        mActionBar.setTitle(title);
        mNoSelectionMsg = mResources.getString(R.string.no_events_selected);
        updateSubtitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_event_title_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case(R.id.action_done):
                generateEventData();
                evalIfComplete();
                break;

            case (R.id.action_cancel):
                setResultAndFinish(false);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    // called after the data fragment's is done loading
    public void queryComplete() {
        mNumQueriesCompleted++;
        evalIfComplete();
    }

    // used load event information for the list of selected events
    private void generateEventData() {
        for (CalendarUtils.Triple<Long, Long, Long> event : mShareEventsList) {

            long eventId = event.first;
            long eventStartMillis = event.second;
            long eventEndMillis = event.third;

            // EventInfoFragment just serves as a data fragment and is initialized with
            // default arguments for parameters that don't affect model loading
            EventInfoFragment eif = new EventInfoFragment(this, eventId, eventStartMillis,
                    eventEndMillis, CalendarContract.Attendees.ATTENDEE_STATUS_NONE, false, 0,
                    null);
            eif.launchInNonUiMode();
            eif.startQueryingData(this);
            eif.setQueryCompleteRunnable(new Runnable() {
                @Override
                public void run() {
                    // indicate model loading is complete
                    queryComplete();
                }
            });

            mEventDataFragments.add(eif);
        }

    }

    // generates the vcs files if the data for selected events has been successfully queried
    private void evalIfComplete() {
        if (mNumQueriesCompleted != 0 && mNumQueriesCompleted == mEventDataFragments.size()) {

            for(EventInfoFragment event : mEventDataFragments) {
                try {
                    // generate vcs file
                    VCalendar calendar = event.generateVCalendar();
                    // event title serves as the file name prefix
                    String filePrefix = calendar.getFirstEvent().getProperty(VEvent.SUMMARY);
                    if (filePrefix == null || filePrefix.length() < 3) {
                        // default to a generic filename if event title doesn't qualify
                        // prefix length constraint is imposed by File#createTempFile
                        filePrefix = "invite";
                    }

                    filePrefix = filePrefix.replaceAll("\\W+", " ");

                    if (!filePrefix.endsWith(" ")) {
                        filePrefix += " ";
                    }
                    File dir = getExternalCacheDir();
                    File inviteFile = IcalendarUtils.createTempFile(filePrefix, ".vcs", dir);
                    inviteFile.setReadable(true, false);     // set world-readable
                    if (IcalendarUtils.writeCalendarToFile(calendar, inviteFile)) {
                        mFileUris.add(Uri.fromFile(inviteFile));
                    }
                } catch (IOException ioe) {
                    break;
                }
            }

            setResultAndFinish(true);

        } else if (mEventDataFragments.size() < 1) {    // if no events have been selected
            setResultAndFinish(false);
        }
    }

    private void setResultAndFinish(boolean isResultAvailable) {
        if (isResultAvailable) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mFileUris);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
