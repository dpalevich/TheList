/*
 * Copyright (c) 2016 Daniel Palevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dpalevich.thelist.utils;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.dpalevich.thelist.model.EventMetadata;
import com.dpalevich.thelist.model.UniqueDateInfo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Parser class for the list data
 *
 * Created by dpalevich on 2/20/16.
 */
public class Parser {

    private static final char EOL = 0x0a;
    private static HashSet<String> MONTHS;
    private static HashSet<String> DAYS;
    private static HashMap<String, String> MISSPELLED_DAYS;
    private StringBuilder mSb = new StringBuilder();

    @VisibleForTesting
    protected ArrayList<String> mEventList = new ArrayList<>();
    @VisibleForTesting
    protected ArrayList<UniqueDateInfo> mDates = new ArrayList<>();

    public Parser() {
        if (null != MONTHS) {
            return;
        }
        MONTHS = new HashSet<>(12);
        MONTHS.add("jan");
        MONTHS.add("feb");
        MONTHS.add("mar");
        MONTHS.add("apr");
        MONTHS.add("may");
        MONTHS.add("jun");
        MONTHS.add("jul");
        MONTHS.add("aug");
        MONTHS.add("sep");
        MONTHS.add("oct");
        MONTHS.add("nov");
        MONTHS.add("dec");

        DAYS = new HashSet<>(7);
        DAYS.add("sun");
        DAYS.add("mon");
        DAYS.add("tue");
        DAYS.add("wed");
        DAYS.add("thr");
        DAYS.add("fri");
        DAYS.add("sat");

        MISSPELLED_DAYS = new HashMap<>();
        MISSPELLED_DAYS.put("fir", "fri");
    }

    public void parse(@NonNull String data) throws ParseException {
        int eventStart = getFirstEventIndex(data);
        int eventEnd;

        int uniqueDateIndex = 0;
        int dateEventCount = 0;
        int dateIndex = 0;
        String dateString = null;
        boolean dateStringEndsWithDay = false;

        do {
            int lineEnd = eventEnd = data.indexOf(EOL, eventStart);
            if (lineEnd < 0) {
                throw new ParseException("Failed to find EOL", eventStart);
            }

            // Events start with a month on the first line. If it has more than one line, the
            // additional lines start with a space.
            while (' ' == data.charAt(lineEnd + 1)) {
                lineEnd = eventEnd = data.indexOf(EOL, lineEnd + 1);
            }
            if (lineEnd < 0) {
                throw new ParseException("Failed to find EOL", eventStart);
            }
            String event = data.substring(eventStart, eventEnd);
            mEventList.add(event);

            boolean dateChange;
            if (null == dateString) {
                dateChange = true;
            } else if (!event.startsWith(dateString)) {
                dateChange = true;
                if (dateStringEndsWithDay) {
                    int length = dateString.length();
                    if (event.regionMatches(0, dateString, 0, length - 3)) {
                        dateChange = !MISSPELLED_DAYS.containsKey(event.substring(length - 3, length));
                    }
                }
            } else {
                int dateStringLength = dateString.length();
                if (' ' != event.charAt(dateStringLength)) {
                    // this attempts to handle the case where date changed but still started with the
                    // string of the previous date, such as going from "may 27/28" to "may 27/28/29".
                    dateChange = true;
                } else if (!dateStringEndsWithDay) {
                    // This attempts to handle the case where we go from the day name being mistakenly
                    // omitted to the case where it is now present again, such as going from
                    // "feb 28" to "feb 28 sun"
                    int nextWordIdx = dateStringLength + 1;
                    dateChange = DAYS.contains(event.substring(nextWordIdx, nextWordIdx + 3));
                } else {
                    dateChange = false;
                }
            }
            if (dateChange) {
                String prevDateString = dateString;

                //date string examples
                //apr 18 mon
                //apr 17/18/19
                //may  5 thr
                //may  5/6
                //mar 31/ apr 1
                int i = 4;
                //find start of day (or range)
                while (' ' == event.charAt(i)) i++;
                while (' ' != event.charAt(i)) i++;
                String day = event.substring(7, 10);
                String correctedDay = MISSPELLED_DAYS.get(day);
                if (null != correctedDay) {
                    day = correctedDay;
                    mSb.setLength(0);
                    mSb.append(event, 0, 7);
                    mSb.append(day);
                    mSb.append(event, 10, event.length());
                    event = mSb.toString();
                }
                if (6 == i && DAYS.contains(day)) {
                    dateString = event.substring(0, 10);
                    dateStringEndsWithDay = true;
                } else {
                    dateString = event.substring(0, i);
                    dateStringEndsWithDay = false;
                    if (dateString.endsWith("/")) {
                        //mar 31/ apr 1
                        while (' ' == event.charAt(i)) i++;
                        if (MONTHS.contains(event.substring(i, i+3))) {
                            i += 3;
                            while (' ' == event.charAt(i)) i++;
                            while (' ' != event.charAt(i)) i++;
                            dateString = event.substring(0, i);
                        }
                    }
                }

                if (null != prevDateString) {
                    UniqueDateInfo info = new UniqueDateInfo(prevDateString, uniqueDateIndex, dateEventCount);
                    mDates.add(info);
                }

                dateEventCount = 0;
                uniqueDateIndex = dateIndex;
            }

            dateIndex++;
            dateEventCount++;

            eventStart = eventEnd + 1;
        } while (EOL != data.charAt(eventStart));

        // add last date
        UniqueDateInfo info = new UniqueDateInfo(dateString, uniqueDateIndex, dateEventCount);
        mDates.add(info);
    }

    /**
     * Gets the index of the first event
     *
     * @param data The list text
     * @return The index of the first event
     * @throws ParseException
     */
    @VisibleForTesting
    protected int getFirstEventIndex(@NonNull String data) throws ParseException {

        int length = data.length();

        for (int i=0; i<length; i++) {
            int lineStart = i;
            int eol_idx = data.indexOf(EOL, lineStart);
            if (eol_idx < 0) {
                throw new ParseException("Failed to find EOL", i);
            } else if (lineStart == eol_idx) {
                continue;
            }
            if (MONTHS.contains(data.substring(i, i+3))) {
                return lineStart;
            } else {
                i = eol_idx;
            }
        }

        throw new ParseException("Failed to find first event", length);
    }

    @VisibleForTesting
    protected void getBands(@NonNull String event, @NonNull String date, @NonNull ArrayList<String> bands) throws ParseException {
        bands.clear();
        int length = event.length();
        int idx = date.length() + 1;

        while (' ' == event.charAt(idx)) idx++;

        int name_start_idx = idx;
        boolean past_first_line = false;
        boolean reachedLocation = false;
        boolean searchForNameStart = false;
        boolean isComma = false;
        boolean inBrackets = false;

        while (idx < length) {
            char c = event.charAt(idx);
            boolean isEOL = '\n' == c;

            // special case, where comma is at the end of the line
            if (isComma && isEOL) {
                isComma = false;
                idx++;
                continue;
            }

            // Need to not treat commas as separators if within brackets
            if (!inBrackets) {
                inBrackets = '(' == c;
            } else {
                inBrackets = ')' != c;
            }
            isComma = !inBrackets && ',' == c;

            boolean isEndOfBandName = isComma || isEOL;

            if (!isEndOfBandName && !past_first_line) {
                if (event.regionMatches(idx, " at ", 0, 4) && (-1 != event.indexOf(EOL, idx) || -1 == event.indexOf(EOL))) {
                    // on the last line (or only one line) and already at location
                    reachedLocation = isEndOfBandName = true;
                }
            }

            if (isEndOfBandName) {
                if (name_start_idx == idx) {
                    throw new ParseException("Found name that starts with delimeter", idx);
                }
                String name = event.substring(name_start_idx, idx);
                if (name.endsWith(")")) {
                    int open_idx = name.indexOf('(');
                    if (open_idx > 1) {
                        int name_end_idx = open_idx - 1;
                        while (' ' == name.charAt(name_end_idx)) name_end_idx--;
                        name = name.substring(0, name_end_idx + 1);
                    }
                }
                int at_idx = name.indexOf(" at ");
                if (at_idx > 0 && -1 == event.indexOf(EOL, name_start_idx)) {
                    // TODO FIXME detect false positives of location
                    name = name.substring(0, at_idx);
                    if (')' == name.charAt(at_idx - 1)) {
                        int start_parenthesis = name.indexOf(" (");
                        if (start_parenthesis > 0) {
                            name = name.substring(0, start_parenthesis);
                        }
                    }
                    reachedLocation = true;
                }
                bands.add(name);
                searchForNameStart = true;
                inBrackets = false;
                past_first_line |= isEOL;
                if (isEOL && idx < length - 1) {
                    if (event.regionMatches(idx, "\n      ", 0, 7)) {
                        idx += 7;
                        while (' ' == event.charAt(idx)) {
                            idx++;
                            if (idx == length) {
                                break;
                            }
                        }
                        if (event.regionMatches(idx, "at ", 0, 3)) {
                            return;
                        }
                        if (event.regionMatches(idx, "a/a", 0, 3) || event.regionMatches(idx, "21+", 0, 3)) {
                            // Handle case like this:
                            // mar 28 mon Underoath (Tampa, FL), Caspian at the Warfield, S.F.
                            // a/a $28/$30 6:30pm/7:30pm # *** @ (was at Regency Ballroom)

                            // recurse warning, the location was probably on the previous line and was
                            // incorrectly interpreted as bands. Remove this line and do it again.

                            // But only do this if previous line did not contain " at "
                            int i = event.indexOf(" at ");
                            if (i > 7 && i < idx) {
                                event = event.substring(0, idx);
                                getBands(event, date, bands);
                                return;
                            }
                        }
                    }
                }
            } else if (searchForNameStart) {
                if (' ' != c) {
                    searchForNameStart = false;
                    name_start_idx = idx;
                }
            }
            if (reachedLocation) {
                return;
            }
            idx++;
        }
    }

    @VisibleForTesting
    protected EventMetadata fixupBands(@NonNull ArrayList<String> bands) {
        int count = bands.size();
        String metaData = null;

        EventMetadata.Status status = null;

        for (int i=0; i<count; i++) {
            String band = bands.get(i);
            if(0 == i) {
                if (band.regionMatches(0, "cancelled: ", 0, 11)) {
                    status = EventMetadata.Status.CANCELLED;
                } else if (band.regionMatches(0, "postponed: ", 0, 11)) {
                    status = EventMetadata.Status.POSTPONED;
                }
                if (null != status) {
                    band = band.substring(11).trim();
                    bands.set(0, band);
                }
            }
            if (band.regionMatches(0, "host ", 0, 5)) {
                bands.set(i, band.substring(5).trim());
            }
        }
        if (null != status) {
            EventMetadata eventMetadata = new EventMetadata();
            eventMetadata.status = status;
            return eventMetadata;
        }
        return null;
    }
}
