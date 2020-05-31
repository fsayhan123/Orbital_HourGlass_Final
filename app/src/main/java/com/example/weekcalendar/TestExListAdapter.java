package com.example.weekcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class TestExListAdapter extends RecyclerView.Adapter<TestExListAdapter.MyListViewHolder>{
    private final List<String> listGroup;
    private final Map<String, List<String>> listItem;
    private final Context context;
    private List<String> list;

    public TestExListAdapter(List<String> list, List<String> listGroup, Map<String, List<String>> listItem, Context context) {
        this.list = list;
        this.listGroup = listGroup;
        this.listItem = listItem;
        this.context = context;
    }

    @NonNull
    @Override
    public MyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_list, parent, false);
        TestExListAdapter.MyListViewHolder holder = new TestExListAdapter.MyListViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyListViewHolder holder, int position) {
        String s = this.list.get(position);
        holder.title.setText(s);

        LinearLayoutManager manager = new LinearLayoutManager(this.context);
        ExListAdapter e = new ExListAdapter(this.context, this.listGroup, this.listItem);
        holder.testList.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.list == null ? 0 : this.list.size();
    }

    public class MyListViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ExpandableListView testList;

        public MyListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.textView2);
            this.testList = itemView.findViewById(R.id.ex_list);
        }
    }
}
