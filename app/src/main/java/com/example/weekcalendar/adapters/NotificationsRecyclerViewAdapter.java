package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomNotification;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<CustomNotification> mDataset;
    private Activity a;
    private View eachDayView;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public Button date;
        public TextView message;

        public MyViewHolder(View v) {
            super(v);
            this.message = itemView.findViewById(R.id.Notification_text);
            this.date = itemView.findViewById(R.id.Notification_date);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsRecyclerViewAdapter(ArrayList<CustomNotification> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        eachDayView  = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_view, parent, false);
        MyViewHolder vh = new MyViewHolder(eachDayView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final CustomNotification notif = this.mDataset.get(position);
        holder.message.setText(notif.getMessage());
        holder.date.setText(notif.getDate());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}