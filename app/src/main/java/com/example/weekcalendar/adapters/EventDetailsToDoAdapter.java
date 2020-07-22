package com.example.weekcalendar.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomToDo;

import java.util.List;

import static android.content.ContentValues.TAG;

public class EventDetailsToDoAdapter extends RecyclerView.Adapter<EventDetailsToDoAdapter.MyViewHolder> {
    private List<CustomToDo> todos;
    private View eachItemView;

    public EventDetailsToDoAdapter(List<CustomToDo> todos) {
        this.todos = todos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        eachItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        EventDetailsToDoAdapter.MyViewHolder holder = new EventDetailsToDoAdapter.MyViewHolder(eachItemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final CustomToDo todo = this.todos.get(position);
        holder.listChild.setText(todo.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.todos == null ? 0 : this.todos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView listChild;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.listChild = itemView.findViewById(R.id.list_child);
        }
    }
}
