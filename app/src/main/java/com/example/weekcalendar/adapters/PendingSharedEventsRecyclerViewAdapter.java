package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomNotification;
import com.example.weekcalendar.customclasses.CustomPendingShared;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PendingSharedEventsRecyclerViewAdapter extends RecyclerView.Adapter<PendingSharedEventsRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<CustomPendingShared> mDataset;
    private Activity a;
    private View eachDayView;
    private OnSharedEventListener mOnSharedEventListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        OnSharedEventListener onSharedEventListener;

        public MyViewHolder(View v, OnSharedEventListener onSharedEventListener) {
            super(v);
            this.title = itemView.findViewById(R.id.shared_event_title);
            this.onSharedEventListener = onSharedEventListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSharedEventListener.onEventClick(getAdapterPosition());
        }
    }

    public interface OnSharedEventListener {
        void onEventClick(int position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PendingSharedEventsRecyclerViewAdapter(ArrayList<CustomPendingShared> myDataset, OnSharedEventListener mOnNotificationListener) {
        this.mDataset = myDataset;
        this.mOnSharedEventListener = mOnNotificationListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PendingSharedEventsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                            int viewType) {
        // create a new view
        eachDayView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_shared_event_message, parent, false);
        MyViewHolder vh = new MyViewHolder(eachDayView, this.mOnSharedEventListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String title = this.mDataset.get(position).getTitle();
        holder.title.setText(title);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}