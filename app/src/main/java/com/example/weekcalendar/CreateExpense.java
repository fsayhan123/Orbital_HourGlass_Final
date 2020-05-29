package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateExpense extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MyDateDialog.MyDateDialogEventListener {
    private Spinner s;
    private Button date;
    private EditText cost;
    private Button addExpenditure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);

        s = findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_nothing_selected, this));

        date = findViewById(R.id.date);
        date.setOnClickListener(v -> openSelectDateDialog(v, date));

        addExpenditure = findViewById(R.id.submit_expenditure);
        addExpenditure.setOnClickListener(v -> addExpense());

        cost = findViewById(R.id.cost);
        cost.setFilters(new InputFilter[] {new DigitsInputFilter(Integer.MAX_VALUE, 2, Double.MAX_VALUE)});
    }

    private void addExpense() {
        String expenseName = ((EditText) findViewById(R.id.expenditure)).getText().toString();
        if (expenseName.equals("")) {
            Toast.makeText(this, "Please insert expense name!", Toast.LENGTH_SHORT).show();
        } else if (cost.getText().equals("")) {
            Toast.makeText(this, "Please insert expense cost!", Toast.LENGTH_SHORT).show();
        } else if (date.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please choose date!", Toast.LENGTH_SHORT).show();
        } else if (s.getSelectedItem() == null) {
            Toast.makeText(this, "Please choose a category!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Added expense", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ExpenseHomePage.class);
            // grab data and add to db here
            startActivity(i);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, "selected " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private void openSelectDateDialog(View v, Button b) {
        Toast.makeText(this, "opened date dialog", Toast.LENGTH_SHORT).show();
        MyDateDialog myDateDialog = new MyDateDialog(b);
        myDateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    @Override
    public void applyDateText(Day d, Button b) {
        b.setText(d.getDate());
    }


}
