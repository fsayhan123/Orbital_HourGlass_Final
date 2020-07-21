package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomNotification;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<CustomNotification> mDataset;
    private Activity a;
    private View eachDayView;
    private OnNotificationListener mOnNotificationListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView date;
        public TextView message;
        public TextView responseStatus;
        OnNotificationListener onNotificationListener;

        public MyViewHolder(View v, OnNotificationListener onNotificationListener) {
            super(v);
            this.message = itemView.findViewById(R.id.notification_message);
            this.date = itemView.findViewById(R.id.notification_date);
            this.responseStatus = itemView.findViewById(R.id.response_status);
            this.onNotificationListener = onNotificationListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onNotificationListener.onNotificationClick(getAdapterPosition());
        }
    }

    public interface OnNotificationListener {
        void onNotificationClick(int position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsRecyclerViewAdapter(ArrayList<CustomNotification> myDataset, OnNotificationListener mOnNotificationListener) {
        this.mDataset = myDataset;
        this.mOnNotificationListener = mOnNotificationListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        eachDayView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view, parent, false);
        MyViewHolder vh = new MyViewHolder(eachDayView, this.mOnNotificationListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final CustomNotification notif = this.mDataset.get(position);
        holder.message.setText(notif.getMessage());
        holder.date.setText(notif.getDateForView());
        holder.responseStatus.setText(notif.getResponseStatus() ? "Responded!" : "Requires response.");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}