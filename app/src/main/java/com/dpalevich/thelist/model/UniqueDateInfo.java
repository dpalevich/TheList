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

/**
 * Created by dpalevich on 2/21/16.
 */
public class UniqueDateInfo {
    public final String dateString;
    public final int firstEventIndex;
    public final int eventCount;

    public UniqueDateInfo(String dateString, int firstEventIndex, int eventCount) {
        this.dateString = dateString;
        this.firstEventIndex = firstEventIndex;
        this.eventCount = eventCount;
    }

    // All the code below is generated

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueDateInfo that = (UniqueDateInfo) o;

        if (firstEventIndex != that.firstEventIndex) return false;
        if (eventCount != that.eventCount) return false;
        return dateString.equals(that.dateString);

    }

    @Override
    public int hashCode() {
        int result = dateString.hashCode();
        result = 31 * result + firstEventIndex;
        result = 31 * result + eventCount;
        return result;
    }

    @Override
    public String toString() {
        return "UniqueDateInfo{" +
            "dateString='" + dateString + '\'' +
            ", firstEventIndex=" + firstEventIndex +
            ", eventCount=" + eventCount +
            '}';
    }
}
