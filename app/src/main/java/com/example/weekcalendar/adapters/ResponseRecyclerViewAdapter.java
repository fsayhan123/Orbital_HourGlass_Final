package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomResponse;
import com.example.weekcalendar.helperclasses.HelperMethods;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ResponseRecyclerViewAdapter extends RecyclerView.Adapter<ResponseRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<CustomResponse> mDataset;
    private Activity a;
    private View eachDayView;
    private OnDateCheckListener onDateCheckListener;

    public interface OnDateCheckListener {
        void onDateCheck(String date);
        void onDateUncheck(String date);
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox eventDate;
        public TextView count;

        public MyViewHolder(View v) {
            super(v);
            this.eventDate = itemView.findViewById(R.id.shared_event_date_option);
            this.eventDate.setClickable(false);
            this.count = itemView.findViewById(R.id.shared_event_date_count);
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public ResponseRecyclerViewAdapter(ArrayList<CustomResponse> myDataset, OnDateCheckListener onDateCheckListener) {
        this.mDataset = myDataset;
        this.onDateCheckListener = onDateCheckListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ResponseRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                  int viewType) {
        // create a new view
        eachDayView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_event_date_selection, parent, false);
        MyViewHolder vh = new MyViewHolder(eachDayView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final CustomResponse response = this.mDataset.get(position);
        holder.eventDate.setText(HelperMethods.formatDateForView(response.getDate()));
        String count = String.format("%s users can attend", response.getCount());
        holder.count.setText(count);
        ((MyViewHolder) holder).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MyViewHolder) holder).eventDate.setChecked(
                        !((MyViewHolder) holder).eventDate.isChecked());
                if (((MyViewHolder) holder).eventDate.isChecked()) {
                    System.out.println(holder.eventDate.getText().toString());
                    onDateCheckListener.onDateCheck(holder.eventDate.getText().toString());;
                } else {
                    System.out.println(holder.eventDate.getText().toString());
                    onDateCheckListener.onDateUncheck(holder.eventDate.getText().toString());
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}