package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityCreateToDoPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener{
    private Button date;
    private CustomDay selectedCustomDay;
    TextView description;

    private DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do_page);
        myDB = new DatabaseHelper(this);

        date = findViewById(R.id.date_header);
        date.setOnClickListener(v -> openSelectDateDialog(v, date));

        description = findViewById(R.id.to_do_description);
    }

    private void openSelectDateDialog(View v, Button b) {
        Toast.makeText(this, "opened date dialog", Toast.LENGTH_SHORT).show();
        MyDateDialog myDateDialog = new MyDateDialog(b);
        myDateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
        selectedCustomDay = d;
    }

    public void submitToDo(View view) {
        if (date.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
        } else if (description.getText().toString().equals("")) {
            Toast.makeText(this, "Please insert to do details", Toast.LENGTH_SHORT).show();
        } else {
            // insert SQL insertion function here
            boolean result = myDB.addToDo(date.getText().toString(), description.getText().toString(), "");
            if (result) {
                Toast.makeText(this, "added to do", Toast.LENGTH_SHORT).show();
            }
            Intent i = new Intent(this, ActivityToDoListPage.class);
            startActivity(i);
        }
    }
}