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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dpalevich.thelist.R;
import com.dpalevich.thelist.model.Model;
import com.dpalevich.thelist.model.UniqueDateInfo;

import java.util.ArrayList;

/**
 * Created by dpalevich on 12/31/15.
 */
public class CalendarFragment extends BaseFragment implements View.OnClickListener {

    private static final int ITEM_TYPE_MASK     = 0xF0000000;
    private static final int ITEM_DATE_MASK     = 0x0FFF0000;
    private static final int ITEM_EVENT_MASK    = 0x0000FFFF;
    private static final int ITEM_TYPE_DATE     = 0x10000000;
    private static final int ITEM_TYPE_EVENT    = 0x20000000;

    private static final int ITEM_DATE_SHIFT    = 16;

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(@NonNull Model model, int item);
    }

    static class DateViewHolder extends BaseViewHolder {
        final TextView date;

        public DateViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(listener);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        @Override
        public void bind(@NonNull Model model, int item) {
            date.setText(model.dates.get((item & ITEM_DATE_MASK) >> ITEM_DATE_SHIFT).dateString);
        }
    }

    static class EventViewHolder extends BaseViewHolder {
        final TextView event;

        public EventViewHolder(View itemView) {
            super(itemView);
            event = (TextView) itemView.findViewById(R.id.date);
        }

        @Override
        public void bind(@NonNull Model model, int item) {
            int index = item & ITEM_EVENT_MASK;
            String eventString = model.convertedEvents.get(index);
            if (null == eventString) {
                UniqueDateInfo info = model.dates.get((item & ITEM_DATE_MASK) >> ITEM_DATE_SHIFT);
                eventString = model.convertEvent(info.dateString, index);
            }
            event.setText(eventString);
        }
    }

    static class DatesAdapter extends RecyclerView.Adapter<BaseViewHolder> {
        private Model mModel;
        private ArrayList<UniqueDateInfo> mUniqueDateInfo;
        private int mSize;
        private ArrayList<Integer> mItems;
        private final View.OnClickListener mOnClickListener;

        DatesAdapter(View.OnClickListener onClickListener) {
            mOnClickListener = onClickListener;
        }

        public void setModel(Model model) {
            if (null != model) {
                mModel = model;
                mUniqueDateInfo = mModel.dates;
                mSize = mUniqueDateInfo.size();
                createItems();
            } else {
                mModel = null;
                mUniqueDateInfo = null;
                mSize = 0;
                mItems = null;
            }
        }

        private void createItems() {
            mItems = new ArrayList<>(mSize);
            int index = 0;
            for (UniqueDateInfo info : mUniqueDateInfo) {
                mItems.add(ITEM_TYPE_DATE | (index++ << ITEM_DATE_SHIFT));
            }
        }

        public Model getModel() {
            return mModel;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            BaseViewHolder holder;
            if (ITEM_TYPE_DATE == viewType) {
                holder = new DateViewHolder(inflater.inflate(R.layout.calendar_item_normal, parent, false), mOnClickListener);
            } else {
                holder = new EventViewHolder(inflater.inflate(R.layout.calendar_item_normal, parent, false));
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.bind(mModel, mItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mSize;
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position) & ITEM_TYPE_MASK;
        }

        public void onDateItemClick(int index) {
            int item = mItems.get(index);
            int nextIndex = index + 1;
            boolean expanded;
            if (index < mSize - 1) {
                int nextItem = mItems.get(nextIndex);
                expanded = ITEM_TYPE_EVENT == (nextItem & ITEM_TYPE_MASK);
            } else {
                expanded = false;
            }
            int eventId = item & ITEM_DATE_MASK;
            UniqueDateInfo info = mUniqueDateInfo.get(eventId >> ITEM_DATE_SHIFT);
            if (expanded) {
                mItems.subList(nextIndex, nextIndex + info.eventCount).clear();
                mSize = mItems.size();
                notifyItemRangeRemoved(nextIndex, info.eventCount);
            } else {
                int firstEventIndex = info.firstEventIndex;
                ArrayList<Integer> newItems = new ArrayList<>(info.eventCount);
                for (int i=0; i<info.eventCount; i++) {
                    newItems.add(ITEM_TYPE_EVENT | eventId | (firstEventIndex + i));
                }
                mItems.addAll(nextIndex, newItems);
                mSize = mItems.size();
                notifyItemRangeInserted(nextIndex, info.eventCount);
            }
        }
    }

    private RecyclerView mRecyclerView;
    private DatesAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateModel();
        }
    };
    private IntentFilter mFilter = new IntentFilter(Model.INTENT_ACTION_MODEL_AVAILABLE);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DatesAdapter(this);
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

    @Override
    public void onClick(View v) {
        int index = mRecyclerView.getChildAdapterPosition(v);
        mAdapter.onDateItemClick(index);
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
