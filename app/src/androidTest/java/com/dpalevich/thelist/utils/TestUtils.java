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
import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for Unit Tests
 *
 * Created by dpalevich on 2/20/16.
 */
public class TestUtils {

    /**
     * Reads a test text file from the assets directory and returns a String of the text
     *
     * @param context The test context
     * @param testFileName The name of the file in the assets directory
     * @return The text contained in the file
     * @throws IOException
     */
    public static String getTestFileData(@NonNull Context context, @NonNull String testFileName) throws IOException {

        InputStream is = null;
        ByteArrayOutputStream bos = null;

        try {
            is = context.getAssets().open(testFileName, AssetManager.ACCESS_BUFFER);
            bos = new ByteArrayOutputStream(12 * 1024);
            byte[] buffer = new byte[4 * 1024];

            int index = 0;
            int count;
            while (-1 != (count = is.read(buffer, index, buffer.length))) {
                bos.write(buffer, 0, count);
            }
            return bos.toString("UTF-8");
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads a test text file from the assets directory and returns a String array of the text lines
     *
     * @param context The test context
     * @param testFileName The name of the file in the assets directory
     * @return A String array containing the lines of text of the test file
     * @throws IOException
     */
    public static String[] getTestFileLines(@NonNull Context context, @NonNull String testFileName) throws IOException {
        String data = getTestFileData(context, testFileName);
        return data.split("\n");
    }
}
