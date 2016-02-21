package com.dpalevich.thelist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by dpalevich on 12/31/15.
 */
public class BaseFragment extends Fragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println(getClass().getName() + "onViewCreated, savedInstanceState=" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }
}
