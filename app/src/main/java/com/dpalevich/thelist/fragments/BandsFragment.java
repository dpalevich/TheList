package com.dpalevich.thelist.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dpalevich.thelist.R;

/**
 * Created by dpalevich on 12/31/15.
 */
public class BandsFragment extends BaseFragment {

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = (TextView) inflater.inflate(R.layout.temp_content, container, false);
        tv.setText("Bands Temp Content");
        return tv;
    }

}
