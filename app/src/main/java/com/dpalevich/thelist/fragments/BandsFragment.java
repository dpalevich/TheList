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

package com.dpalevich.thelist.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dpalevich.thelist.R;
import com.dpalevich.thelist.model.Model;

import java.util.Map;
import java.util.Set;

/**
 * Created by dpalevich on 12/31/15.
 */
public class BandsFragment extends BaseFragment {

    static class BandViewHolder extends RecyclerView.ViewHolder {
        public final TextView bandName;

        public BandViewHolder(View itemView) {
            super(itemView);
            bandName = (TextView) itemView.findViewById(R.id.band_name);
        }
    }

    static class BandsAdapter extends RecyclerView.Adapter<BandViewHolder> {
        private Model mModel;
        private Set<Map.Entry<String, Object>> mEntrySet;
        private int mSize;

        public void setModel(Model model) {
            if (null != model) {
                mModel = model;
                mEntrySet = mModel.bandsToEventsMap.entrySet();
                mSize = mEntrySet.size();
            } else {
                mModel = null;
                mEntrySet = null;
                mSize = 0;
            }
        }

        public Model getModel() {
            return mModel;
        }

        @Override
        public BandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            BandViewHolder holder = new BandViewHolder(inflater.inflate(R.layout.band_item_normal, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(BandViewHolder holder, int position) {
            holder.bandName.setText(mModel.bandsSorted.get(position));
        }

        @Override
        public int getItemCount() {
            return mSize;
        }
    }

    private RecyclerView mRecyclerView;
    private BandsAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateModel();
        }
    };
    private IntentFilter mFilter = new IntentFilter(Model.INTENT_ACTION_MODEL_AVAILABLE);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BandsAdapter();
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bands, container, false);
        Context context = container.getContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter.setModel(Model.sCurrentModel);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mBroadcastReceiver, mFilter);
        updateModel();
    }

    private void updateModel() {
        Model adapterModel = mAdapter.getModel();
        Model currentModel = Model.sCurrentModel;
        if (null != currentModel && adapterModel != currentModel) {
            mAdapter.setModel(currentModel);
            mAdapter.notifyDataSetChanged();
        }
    }
}
