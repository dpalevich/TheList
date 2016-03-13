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
import com.dpalevich.thelist.widgets.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by dpalevich on 12/31/15.
 */
public class CalendarFragment extends BaseFragment {

    static class DateViewHolder extends RecyclerView.ViewHolder {
        final TextView date;

        public DateViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }

    static class DatesAdapter extends RecyclerView.Adapter<DateViewHolder> {
        private Model mModel;
        private ArrayList<UniqueDateInfo> mUniqueDateInfo;
        private int mSize;

        public void setModel(Model model) {
            if (null != model) {
                mModel = model;
                mUniqueDateInfo = mModel.dates;
                mSize = mUniqueDateInfo.size();
            } else {
                mModel = null;
                mUniqueDateInfo = null;
                mSize = 0;
            }
        }

        public Model getModel() {
            return mModel;
        }

        @Override
        public DateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            DateViewHolder holder = new DateViewHolder(inflater.inflate(R.layout.calendar_item_normal, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(DateViewHolder holder, int position) {
            holder.date.setText(mUniqueDateInfo.get(position).dateString);
        }

        @Override
        public int getItemCount() {
            return mSize;
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
        mAdapter = new DatesAdapter();
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bands, container, false);
        Context context = container.getContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
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
