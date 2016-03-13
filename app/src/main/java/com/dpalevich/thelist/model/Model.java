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

package com.dpalevich.thelist.model;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * The model for a single list file
 *
 * Created by dpalevich on 3/6/16.
 */
public class Model {
    public static final String INTENT_ACTION_MODEL_AVAILABLE = Model.class.getName() + ".ACTION_MODEL_AVAILABLE";
    public static final String INTENT_EXTRA_MODEL_ID = Model.class.getName() + ".EXTRA_MODEL_ID";
    public static final int MODEL_ID_CURRENT = 1;
    public static final int MODEL_ID_PREVIOUS = 2;

    public static Model sCurrentModel;

    public final ArrayList<String> eventList;
    public final ArrayList<UniqueDateInfo> dates;
    public final TreeMap<String, Object> bandsToEventsMap;
    public final ArrayList<String> bandsSorted;
    public final HashSet<Integer> badDatesIndices;
    public final SparseArray<String> convertedEvents;

    public Model(ArrayList<String> eventList, ArrayList<UniqueDateInfo> dates, TreeMap<String, Object> bandsToEventsMap, HashSet<Integer> badDatesIndices) {
        this.eventList = eventList;
        this.dates = dates;
        this.bandsToEventsMap = bandsToEventsMap;
        this.badDatesIndices = badDatesIndices;
        bandsSorted = new ArrayList<>(bandsToEventsMap.size());
        bandsSorted.addAll(bandsToEventsMap.keySet());
        convertedEvents = new SparseArray<>();
    }

    public String convertEvent(@NonNull String dateString, int index) {
        String converted = eventList.get(index).substring(dateString.length()).trim().replaceAll("\\s+", " ");
        convertedEvents.put(index, converted);
        return converted;
    }
}
