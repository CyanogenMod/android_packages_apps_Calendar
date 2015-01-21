/**
 * Copyright (C) 2014 The CyanogenMod Project
 */

package com.android.calendar.icalendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Models the Calendar/VCalendar component of the iCalendar format
 */
public class VCalendar {

    // valid property identifiers of the component
    // TODO: only a partial list of attributes have been implemented, implement the rest
    public static String VERSION = "VERSION";
    public static String PRODID = "PRODID";
    public static String CALSCALE = "CALSCALE";
    public static String METHOD = "METHOD";

    public final static String PRODUCT_IDENTIFIER = "-//Cyanogen Inc//com.android.calendar";

    // stores the -arity of the attributes that this component can have
    private final static HashMap<String, Integer> sPropertyList = new HashMap<String, Integer>();

    // initialize approved list of iCal Calendar properties
    static {
        sPropertyList.put(VERSION, 1);
        sPropertyList.put(PRODID, 1);
        sPropertyList.put(CALSCALE, 1);
        sPropertyList.put(METHOD, 1);
    }

    // stores attributes and their corresponding values belonging to the Calendar object
    public HashMap<String, String> mProperties;
    public LinkedList<VEvent> mEvents;      // events that belong to this Calendar object

    /**
     * Constructor
     */
    public VCalendar() {
        mProperties = new HashMap<String, String>();
        mEvents = new LinkedList<VEvent>();
    }

    /**
     * Add specified property
     * @param property
     * @param value
     */
    public boolean addProperty(String property, String value) {
        // since all the required mProperties are unary (only one can exist) , taking a shortcut
        // here
        // TODO: when multiple attributes of a property can exist , enforce that here
        if (sPropertyList.containsKey(property) && value != null) {
            mProperties.put(property, IcalendarUtils.cleanseString(value));
            return true;
        }
        return false;
    }

    /**
     * Add Event to calendar
     * @param event
     */
    public void addEvent(VEvent event) {
        if (event != null) mEvents.add(event);
    }

    /**
     * Returns all the events that are part of this calendar
     */
    public LinkedList<VEvent> getAllEvents() {
        return mEvents;
    }

    /**
     * Returns the first event of the calendar
     */
    public VEvent getFirstEvent() {
        if (mEvents != null && mEvents.size() > 0) {
            return mEvents.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the iCal representation of the calendar and all of its inherent components
     */
    public String getICalFormattedString() {
        StringBuilder output = new StringBuilder();

        // Add Event properties
        // TODO: add the ability to specify the order in which to compose the properties
        output.append("BEGIN:VCALENDAR\n");
        for (String property : mProperties.keySet() ) {
            output.append(property + ":" + mProperties.get(property) + "\n");
        }

        // enforce line length requirements
        output = IcalendarUtils.enforceICalLineLength(output);
        // add event
        for (VEvent event : mEvents) {
            output.append(event.getICalFormattedString());
        }

        output.append("END:VCALENDAR\n");

        return output.toString();
    }

    public void populateFromString(ArrayList<String> input) {
        ListIterator<String> iter = input.listIterator();

        while (iter.hasNext()) {
            String line = iter.next();
            if (line.contains("BEGIN:VEVENT")) {
                // Go one previous, so VEvent, parses current line
                iter.previous();

                // Offload to vevent for parsing
                VEvent event = new VEvent();
                event.populateFromEntries(iter);
                mEvents.add(event);
            } else if (line.contains("END:VCALENDAR")) {
                break;
            }
        }
    }

    public String getProperty(String key) {
        return mProperties.get(key);
    }

    /**
     * TODO: Aggressive validation of VCalendar and all of its components to ensure they conform
     * to the ical specification
     * @return
     */
    private boolean validate() {
        return false;
    }
}