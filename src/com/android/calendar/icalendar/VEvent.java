/**
 * Copyright (C) 2014 The CyanogenMod Project
 */

package com.android.calendar.icalendar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Models the Event/VEvent component of the iCalendar format
 */
public class VEvent {

    // valid property identifiers for an event component
    // TODO: only a partial list of attributes has been implemented, implement the rest
    public static String CLASS = "CLASS";
    public static String CREATED = "CREATED";
    public static String LOCATION = "LOCATION";
    public static String ORGANIZER = "ORGANIZER";
    public static String PRIORITY = "PRIORITY";
    public static String SEQ = "SEQ";
    public static String STATUS = "STATUS";
    public static String UID = "UID";
    public static String URL = "URL";
    public static String DTSTART = "DTSTART";
    public static String DTEND = "DTEND";
    public static String DURATION = "DURATION";
    public static String DTSTAMP = "DTSTAMP";
    public static String SUMMARY = "SUMMARY";
    public static String DESCRIPTION = "DESCRIPTION";
    public static String ATTENDEE = "ATTENDEE";
    public static String CATEGORIES = "CATEGORIES";

    // stores the -arity of the attributes that this component can have
    private static HashMap<String, Integer> sPropertyList = new HashMap<String, Integer>();

    // initialize the approved list of mProperties for a calendar event
    static {
        sPropertyList.put(CLASS,1);
        sPropertyList.put(CREATED,1);
        sPropertyList.put(LOCATION,1);
        sPropertyList.put(ORGANIZER,1);
        sPropertyList.put(PRIORITY,1);
        sPropertyList.put(SEQ,1);
        sPropertyList.put(STATUS,1);
        sPropertyList.put(UID,1);
        sPropertyList.put(URL,1);
        sPropertyList.put(DTSTART,1);
        sPropertyList.put(DTEND,1);
        sPropertyList.put(DURATION, 1);
        sPropertyList.put(DTSTAMP,1);
        sPropertyList.put(SUMMARY,1);
        sPropertyList.put(DESCRIPTION,1);

        sPropertyList.put(ATTENDEE, Integer.MAX_VALUE);
        sPropertyList.put(CATEGORIES, Integer.MAX_VALUE);
        sPropertyList.put(CATEGORIES, Integer.MAX_VALUE);
    }

    // stores attributes and their corresponding values belonging to the Event component
    public HashMap<String, String> mProperties;

    public LinkedList<Attendee> mAttendees;
    public Organizer mOrganizer;

    /**
     * Constructor
     */
    public VEvent() {
        mProperties = new HashMap<String, String>();
        mAttendees = new LinkedList<Attendee>();

        // generate and add a unique identifier to this event - ical requisite
        addProperty(UID , UUID.randomUUID().toString() + "@cyanogenmod.com");
        addTimeStamp();
    }

    /**
     * For adding unary properties. For adding other property attributes , use the respective
     * component methods to create and add these special components.
     * @param property
     * @param value
     * @return
     */
    public boolean addProperty(String property, String value) {
        // only unary-properties for now
        if (sPropertyList.containsKey(property) && sPropertyList.get(property) == 1 &&
                value != null) {
            mProperties.put(property, IcalendarUtils.cleanseString(value));
            return true;
        }
        return false;
    }

    /**
     * returns the value of the requested event property or null if there isn't one
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }

    /**
     * Add attendees to the event
     * @param attendee
     */
    public void addAttendee(Attendee attendee) {
        if(attendee != null) mAttendees.add(attendee);
    }

    /**
     * Add an Organizer to the Event
     * @param organizer
     */
    public void addOrganizer(Organizer organizer) {
        if (organizer != null) mOrganizer = organizer;
    }

    /**
     * Add an start date-time to the event
     */
    public void addEventStart(long startMillis, String timeZone) {
        if (startMillis < 0) return;

        String formattedDateTime = IcalendarUtils.getICalFormattedDateTime(startMillis, timeZone);
        addProperty(DTSTART, formattedDateTime);
    }

    /**
     * Add an end date-time for event
     */
    public void addEventEnd(long endMillis, String timeZone) {
        if (endMillis < 0) return;

        String formattedDateTime = IcalendarUtils.getICalFormattedDateTime(endMillis, timeZone);
        addProperty(DTEND, formattedDateTime);
    }

    /**
     * Timestamps the events with the current date-time
     */
    private void addTimeStamp() {
        String formattedDateTime = IcalendarUtils.getICalFormattedDateTime(
                System.currentTimeMillis(), "UTC");
        addProperty(DTSTAMP, formattedDateTime);
    }

    /**
     * Returns the iCal representation of the Event component
     */
    public String getICalFormattedString() {
        StringBuilder sb = new StringBuilder();

        // Add Event properties
        sb.append("BEGIN:VEVENT\n");
        for (String property : mProperties.keySet() ) {
            sb.append(property + ":" + mProperties.get(property) + "\n");
        }

        // Enforce line length requirements
        sb = IcalendarUtils.enforceICalLineLength(sb);

        sb.append(mOrganizer.getICalFormattedString());

        // add event Attendees
        for (Attendee attendee : mAttendees) {
            sb.append(attendee.getICalFormattedString());
        }

        sb.append("END:VEVENT\n");

        return sb.toString();
    }

}
