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

import android.content.Context;
import android.support.annotation.NonNull;
import android.test.InstrumentationTestCase;

import java.io.IOException;
import java.text.ParseException;

/**
 * Unit test class for {@link Parser}
 *
 * Created by dpalevich on 2/20/16.
 */
public class ParserTest extends InstrumentationTestCase {

    static class ParserFileEntry {
        public final String testFileName;
        public final int length;
        public final int firstEventIndex;
        public final int eventCount;

        public ParserFileEntry(String testFileName, int length, int firstEventIndex, int eventCount) {
            this.testFileName = testFileName;
            this.length = length;
            this.firstEventIndex = firstEventIndex;
            this.eventCount = eventCount;
        }
    }

    static class TestParser extends Parser {
        public int mFirstEventIndex;

        @Override
        protected int getFirstEventIndex(@NonNull String data) throws ParseException {
            return mFirstEventIndex = super.getFirstEventIndex(data);
        }
    }

    private ParserFileEntry[] TEST_DATA = new ParserFileEntry[] {
        new ParserFileEntry("test_file_001.txt", 111429, 0x1f2, 979)
    };

    public void testParsing() {
        Context context = getInstrumentation().getContext();

        for (ParserFileEntry entry : TEST_DATA) {
            try {
                String data = TestUtils.getTestFileData(context, entry.testFileName);
                assertEquals(entry.length, data.length());

                TestParser parser = new TestParser();
                parser.parse(data);
                assertEquals(entry.firstEventIndex, parser.mFirstEventIndex);
                assertEquals(entry.eventCount, parser.mEventList.size());
            } catch (IOException e) {
                e.printStackTrace();
                assertNull(e);
            } catch (ParseException e) {
                e.printStackTrace();
                assertNull(e);
            }
        }
    }
}