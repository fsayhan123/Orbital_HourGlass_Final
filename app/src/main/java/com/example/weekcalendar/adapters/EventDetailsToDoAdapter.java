package com.example.weekcalendar.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomToDo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventDetailsToDoAdapter extends RecyclerView.Adapter<EventDetailsToDoAdapter.MyViewHolder> {
    private List<CustomToDo> todos;
    private View eachItemView;

    private Set<CustomToDo> myToggledToDos = new HashSet<>();


    public EventDetailsToDoAdapter(List<CustomToDo> todos) {
        this.todos = todos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        eachItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_todo_event_details, parent, false);
        EventDetailsToDoAdapter.MyViewHolder holder = new EventDetailsToDoAdapter.MyViewHolder(eachItemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final CustomToDo todo = this.todos.get(position);
        holder.listChild.setText(todo.getTitle());
        holder.listChild.setChecked(todo.getCompleted());
        holder.listChild.setOnClickListener(v -> {
            todo.toggleComplete();
            if (myToggledToDos.contains(todo)) {
                myToggledToDos.remove(todo);
            } else {
                myToggledToDos.add(todo);
            }
        });
    }

    public Set<CustomToDo> getMyToggledToDos() {
        return this.myToggledToDos;
    }

    @Override
    public int getItemCount() {
        return this.todos == null ? 0 : this.todos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox listChild;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.listChild = itemView.findViewById(R.id.list_child);
        }
    }
}