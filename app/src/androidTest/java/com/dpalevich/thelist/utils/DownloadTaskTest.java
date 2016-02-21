package com.dpalevich.thelist.utils;

import android.app.Instrumentation;
import android.content.Context;
import android.support.v4.os.AsyncTaskCompat;
import android.test.InstrumentationTestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Created by dpalevich on 2/20/16.
 */
public class DownloadTaskTest extends InstrumentationTestCase implements DownloadTask.Listener {

    private DownloadTask.Result mResult;

    @Override
    public void publish(DownloadTask.Result result) {
        mResult = result;
    }

    static class TestDownloadTask extends DownloadTask {
        private final CountDownLatch mLatch;

        public TestDownloadTask(Context context, String outputFileName, CountDownLatch latch, DownloadTask.Listener listener) {
            super(context, outputFileName, listener);
            mLatch = latch;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            synchronized (mLatch) {
                mLatch.countDown();
            }
        }
    }

    public void testDownload() {
        Instrumentation instrumentation = getInstrumentation();
        Context context = instrumentation.getTargetContext();
        CountDownLatch latch = new CountDownLatch(1);
        final TestDownloadTask task = new TestDownloadTask(context, "downloaded.txt", latch, this);

        URL url = null;
        try {
            url = new URL("http://www.stevelist.com/list");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            assertNull(e);
        }
        final URL url2 = url;

        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AsyncTaskCompat.executeParallel(task, url2);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.out);
            assertNull(throwable);
        }

        try {
            synchronized (latch) {
                latch.wait(10000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertNull(e);
        }
        synchronized (latch) {
            assertEquals(0, latch.getCount());
        }

        assertNotNull(mResult);
        assertEquals(200, mResult.responseCode);
        assertNull(mResult.exception);
        assertNotNull(mResult.data);
        System.out.println("message:" + mResult.responseMessage);
        int index = mResult.data.indexOf("skoepke at stevelist dot com");
        System.out.println("index = " + Integer.toString(index));
        assertTrue(index > 0);
    }
}