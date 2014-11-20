/**
 * Copyright (C) 2014 The CyanogenMod Project
 */

package com.android.calendar.icalendar;

/**
 * Event Organizer component
 * Fulfils the ORGANIZER property of an Event
 */
public class Organizer {

    public String mName;
    public String mEmail;

    public Organizer(String name, String email) {
        if (name != null) {
            mName = name;
        } else {
            mName = "UNKNOWN";
        }
        if (email != null) {
            mEmail = email;
        } else {
            mEmail = "UNKNOWN";
        }
    }

    /**
     * Returns an iCal formatted string
     */
    public String getICalFormattedString() {
        StringBuilder output = new StringBuilder();
        // add the organizer info
        output.append("ORGANIZER;CN=" + mName + ":mailto:" + mEmail);
        // enforce line length constraints
        output = IcalendarUtils.enforceICalLineLength(output);
        output.append("\n");
        return output.toString();
    }

}
