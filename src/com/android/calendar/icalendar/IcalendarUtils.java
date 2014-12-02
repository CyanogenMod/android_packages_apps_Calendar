/**
 * Copyright (C) 2014 The CyanogenMod Project
 */

package com.android.calendar.icalendar;

import android.provider.CalendarContract;
import com.android.calendar.CalendarEventModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helper class
 */
public class IcalendarUtils {

    private static int sPermittedLineLength = 75; // Line length mandated by iCalendar format

    /**
     * ensure the string conforms to the iCalendar encoding requirements
     * escape line breaks , commas and semicolons
     * @param sequence
     * @return
     */
    public static String cleanseString(CharSequence sequence) {
        if (sequence == null) return null;
        String input = sequence.toString();

        // replace new lines with the literal '\n'
        input = input.replaceAll("\\r|\\n|\\r\\n", "\\\\n");
        // escape semicolons and commas
        input = input.replace(";", "\\;");
        input = input.replace(",", "\\,");

        return input;
    }

    /**
     * Stringify VCalendar object and write to file
     * @param calendar
     * @param file
     * @return success status of the file write operation
     */
    public static boolean writeCalendarToFile(VCalendar calendar, File file) {
        if (calendar == null || file == null) return false;
        String icsFormattedString = calendar.getICalFormattedString();
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(icsFormattedString.getBytes());
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (outStream != null) outStream.close();
            } catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }

    /**
     * Formats the given input to adhere to the iCal line length and formatting requirements
     * @param input
     * @return
     */
    public static StringBuilder enforceICalLineLength(StringBuilder input) {
        if (input == null) return null;
        StringBuilder output = new StringBuilder();
        int length = input.length();

        // ensure that work needs to be done
        if (length <= sPermittedLineLength) {
            return input;
        }

        for (int i =0, currentLineLength = 0 ; i < length; i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '\n') {          // new line encountered
                output.append(currentChar);
                currentLineLength = 0;          // reset line counter

            } else if (currentChar != '\n' && currentLineLength <= sPermittedLineLength) {
                // a non-newline char that can be part of the current line
                output.append(currentChar);
                currentLineLength++;

            } else if (currentLineLength > sPermittedLineLength) {
                // need to branch out to a new line
                // add a new line and a space - iCal requirement
                output.append("\n ");
                currentLineLength = 0;
            }
        }

        return output;
    }

    /**
     * create an iCal Attendee with properties from CalendarModel attendee
     *
     * @param attendee
     * @param event
     */
    public static void addAttendeeToEvent(CalendarEventModel.Attendee attendee, VEvent event) {
        if (attendee == null || event == null) return;
        Attendee vAttendee = new Attendee();
        vAttendee.addProperty(Attendee.CN, attendee.mName);

        String participationStatus;
        switch (attendee.mStatus) {
            case CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED:
                participationStatus = "ACCEPTED";
                break;
            case CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED:
                participationStatus = "DECLINED";
                break;
            case CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE:
                participationStatus = "TENTATIVE";
                break;
            case CalendarContract.Attendees.ATTENDEE_STATUS_NONE:
            default:
                participationStatus = "NEEDS-ACTION";
                break;
        }
        vAttendee.addProperty(Attendee.PARTSTAT, participationStatus);
        vAttendee.mEmail = attendee.mEmail;

        event.addAttendee(vAttendee);
    }

    /**
     * returns iCalendar UTC formatted date time
     * ex: 20141120T120000Z for noon on Nov 20, 2014
     *
     * @param millis in epoch time
     * @return
     */
    public static String getICalFormattedDateTime(long millis) {
        if (millis < 0) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateTime = simpleDateFormat.format(new Date(millis));
        StringBuilder output = new StringBuilder(16);

        // iCal UTC date format : <yyyyMMdd>T<HHmmss>Z
        return output.append(dateTime.subSequence(0,8))
                .append("T")
                .append(dateTime.substring(8))
                .append("Z")
                .toString();
    }
}
