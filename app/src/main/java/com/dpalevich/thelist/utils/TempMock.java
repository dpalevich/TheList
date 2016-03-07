package com.dpalevich.thelist.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v4.content.LocalBroadcastManager;

import com.dpalevich.thelist.model.Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by dpalevich on 3/6/16.
 */
public class TempMock implements Runnable {

    final Context mContext;

    public TempMock(Context context) {
        mContext = context.getApplicationContext();
        new Thread(this).start();
    }

    @Override
    public void run() {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String data;

        try {
            is = mContext.getAssets().open("mock_list.txt", AssetManager.ACCESS_BUFFER);
            bos = new ByteArrayOutputStream(12 * 1024);
            byte[] buffer = new byte[4 * 1024];

            int index = 0;
            int count;
            while (-1 != (count = is.read(buffer, index, buffer.length))) {
                bos.write(buffer, 0, count);
            }
            data = bos.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }

        Parser parser = new Parser();
        try {
            parser.parse(data);
            parser.getDatesPerBand();
            Model.sCurrentModel = new Model(parser.mEventList, parser.mDates, parser.mBandsToEventsMap, parser.mBadDatesIndices);
            Intent intent = new Intent(Model.INTENT_ACTION_MODEL_AVAILABLE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } catch (ParseException e) {
            e.printStackTrace(System.out);
        }
    }
}
