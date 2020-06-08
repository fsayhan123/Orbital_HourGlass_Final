package com.example.weekcalendar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class EachDayExpensesAdapter extends RecyclerView.Adapter<EachDayExpensesAdapter.MyDayExpenseViewHolder> {
    private List<String> listOfCat;
    private Map<String, List<CustomExpense>> expensesInEachCat;
    private Activity a;
    private LinearLayoutManager manager;
    private ClickPosition clickPosition;
    EachDayExpensesAdapter.MyDayExpenseViewHolder holder;

    public EachDayExpensesAdapter(List<String> listOfCat, Map<String, List<CustomExpense>> expensesInEachCat,
                                  Activity a) {
        this.listOfCat = listOfCat;
        this.expensesInEachCat = expensesInEachCat;
        this.a = a;
    }

    @NonNull
    @Override
    public EachDayExpensesAdapter.MyDayExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_category_in_day, parent, false);
        EachDayExpensesAdapter.MyDayExpenseViewHolder holder = new EachDayExpensesAdapter.MyDayExpenseViewHolder(view);
        this.holder = holder;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EachDayExpensesAdapter.MyDayExpenseViewHolder holder, int position) {
        String category = this.listOfCat.get(position);
        holder.category.setText(category);

        List<CustomExpense> expenditureInCat = this.expensesInEachCat.get(category);

        EachCatExpenseAdapter e = new EachCatExpenseAdapter(expenditureInCat, clickPosition);

        manager = new LinearLayoutManager(a);
        holder.expenseInCategory.setLayoutManager(manager);
        holder.expenseInCategory.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.listOfCat == null ? 0 : this.listOfCat.size();
    }

    public class MyDayExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView category;
        RecyclerView expenseInCategory;

        public MyDayExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            clickPosition = new ClickPosition() {
                @Override
                public void getPosition(int position) {
                    Toast.makeText(a, "" + position, Toast.LENGTH_SHORT).show();
                }
            };
            this.category = itemView.findViewById(R.id.category);
            this.expenseInCategory = itemView.findViewById(R.id.expenses_in_category);
        }
    }
}
