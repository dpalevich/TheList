package com.dpalevich.thelist.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by dpalevich on 1/3/16.
 */
public class DownloadTask extends AsyncTask<URL, Integer, DownloadTask.Result> {
    public static class Result {
        public final String data;
        public final int responseCode;
        public final String responseMessage;
        public final Exception exception;

        public Result(String data, int responseCode, String responseMessage, Exception exception) {
            this.data = data;
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.exception = exception;
        }
    }

    public interface Listener {
        void publish(Result result);
    }

    private static final int FAKE_COUNT = 110636;

    public DownloadTask(Context context, String outputFileName, Listener listener) {
        mContext = context;
        mOutputFileName = outputFileName;
        mListenerRef = new WeakReference<>(listener);
    }

    private final WeakReference<Listener> mListenerRef;
    private final String mOutputFileName;
    private Context mContext;

    @Override
    protected Result doInBackground(URL... params) {
        InputStream is = null;
        OutputStream fos = null;
        ByteArrayOutputStream bos = null;
        URLConnection connection = null;
        int responseCode = 0;
        String responseMessage = null;
        String data = null;
        Exception exception = null;

        try {
            connection = params[0].openConnection();

            if (connection instanceof HttpURLConnection) {
                responseCode = ((HttpURLConnection)connection).getResponseCode();
                responseMessage = ((HttpURLConnection) connection).getResponseMessage();
            }

            int length = connection.getContentLength();

            is = connection.getInputStream();
            fos = mContext.openFileOutput(mOutputFileName, Context.MODE_PRIVATE);
            bos = new ByteArrayOutputStream(12 * 1024);

            byte buffer[] = new byte[4096];
            long total = 0;
            int count;
            while (-1 != (count = is.read(buffer))) {
                if (isCancelled()) {
                    is.close();
                    is = null;
                    return null;
                }
                total += count;
                if (length > 0) {
                    publishProgress((int) (total * 100 / length));
                } else {
                    int estimated_progress = (int) (total * 100 / FAKE_COUNT);
                    estimated_progress = Math.min(estimated_progress, 100);
                    publishProgress(estimated_progress);
                }
                fos.write(buffer, 0, count);
                bos.write(buffer, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            exception = e;
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection)connection).disconnect();
            }
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
            if (null == exception && null != bos) {
                try {
                    data = bos.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    exception = e;
                }
            }
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException ignored) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (null != exception || null == data) {
            File outputFile = new File(mContext.getFilesDir(), mOutputFileName);
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }

        return new Result(data, responseCode, responseMessage, exception);
    }

    @Override
    protected void onPostExecute(Result result) {
        Listener listener = mListenerRef.get();
        if (null != listener) {
            listener.publish(result);
        }
    }
}
