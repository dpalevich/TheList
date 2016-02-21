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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Parser class for the list data
 *
 * Created by dpalevich on 2/20/16.
 */
public class Parser {

    private static final char EOL = 0x0a;
    private HashSet<String> MONTHS;

    @VisibleForTesting
    protected ArrayList<String> mEventList = new ArrayList<>();

    public Parser() {
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
    }

    public void parse(@NonNull String data) throws ParseException {
        int eventStart = getFirstEventIndex(data);
        int eventEnd;

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
            mEventList.add(data.substring(eventStart, eventEnd));
            eventStart = eventEnd + 1;
        } while (EOL != data.charAt(eventStart));
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
}
