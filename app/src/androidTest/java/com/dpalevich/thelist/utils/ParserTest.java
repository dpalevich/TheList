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

import com.dpalevich.thelist.model.UniqueDateInfo;

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
        public final String uniqueDateInfoFilename;

        public ParserFileEntry(String testFileName, int length, int firstEventIndex, int eventCount, String uniqueDateInfoFilename) {
            this.testFileName = testFileName;
            this.length = length;
            this.firstEventIndex = firstEventIndex;
            this.eventCount = eventCount;
            this.uniqueDateInfoFilename = uniqueDateInfoFilename;
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
        new ParserFileEntry("test_file_001.txt", 111429, 0x1f2, 979, "unique_date_info_001.txt")
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
                verifyUniqueDateInfo(context, parser, entry.uniqueDateInfoFilename);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                assertNull(e);
            }
        }
    }

    /**
     * Verify parser dates against validation file
     *
     * @param context The test context
     * @param parser The parser
     * @param validationFileName The validation file name
     */
    private void verifyUniqueDateInfo(@NonNull Context context, @NonNull TestParser parser, @NonNull String validationFileName) {
        try {
            String[] lines = TestUtils.getTestFileLines(context, validationFileName);
            assertNotNull(lines);
            assertEquals(lines.length, parser.mDates.size());

            for (int i=0; i<lines.length; i++) {
                String[] split = lines[i].split(",");
                assertEquals(3, split.length);
                // strip quotes
                String dateString = split[0].substring(1, split[0].length() - 1);
                int index = Integer.parseInt(split[1]);
                int count = Integer.parseInt(split[2]);
                UniqueDateInfo info = new UniqueDateInfo(dateString, index, count);
                assertEquals(info, parser.mDates.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertNull(e);
        }
    }

    /*
    private void dumpDateInfo(TestParser parser) {
        StringBuilder sb = new StringBuilder();
        for (UniqueDateInfo info : parser.mDates) {
            sb.setLength(0);
            sb.append('\"');
            sb.append(info.dateString);
            sb.append('\"');
            sb.append(',');
            sb.append(info.firstEventIndex);
            sb.append(',');
            sb.append(info.eventCount);
            System.out.println(sb.toString());
        }
    }
    */
}