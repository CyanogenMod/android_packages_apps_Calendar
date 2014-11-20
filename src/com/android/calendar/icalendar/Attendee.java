/**
 * Copyright (C) 2014 Cyanogen, Inc.
 */

package com.android.calendar.icalendar;

import java.util.HashMap;

/**
 * @author Rohit Yengisetty
 */
public class Attendee {

    // property strings
    public static String CN = "CN";                 // Attendee Name
    public static String PARTSTAT = "PARTSTAT";     // Participant Status (Attending , Declined .. )
    public static String RSVP = "RSVP";
    public static String ROLE = "ROLE";
    public static String CUTYPE = "CUTYPE";

    private static HashMap<String, Integer> sPropertyList = new HashMap<String, Integer>();
    // initialize the approved list of mProperties for a calendar event
    static {
        sPropertyList.put(CN,1);
        sPropertyList.put(PARTSTAT, 1);
        sPropertyList.put(RSVP, 1);
        sPropertyList.put(ROLE, 1);
        sPropertyList.put(CUTYPE, 1);
    }

    public HashMap<String, String> mProperties;     // stores (property, value) pairs
    public String mEmail;

    public Attendee() {
        mProperties = new HashMap<String, String>();
    }

    /**
     * Add Attendee properties
     * @param property
     * @param value
     * @return
     */
    public boolean addProperty(String property, String value) {
        // only unary-pro
        if (sPropertyList.containsKey(property) && sPropertyList.get(property) == 1) {
            mProperties.put(property, value);
            return true;
        }
        return false;
    }

    /**
     * Ensure that the line lengths are less than 70 chars
     * @return
     */
    public String getICalFormattedString() {
        StringBuilder output = new StringBuilder();

        // Add Event mProperties
        output.append("ATTENDEE;");
        for (String property : mProperties.keySet() ) {
            // append properties in the following format: attribute=value;
            output.append(property + "=" + mProperties.get(property) + ";");
        }
        output.append("X-NUM-GUESTS=0:mailto:" + mEmail);

        output = ICalendarUtils.enforceICalLineLength(output);

        output.append("\n");
        return output.toString();
    }

}
