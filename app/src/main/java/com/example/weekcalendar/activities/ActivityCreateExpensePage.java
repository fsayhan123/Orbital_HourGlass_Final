package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.weekcalendar.helperclasses.DatabaseHelper;
import com.example.weekcalendar.helperclasses.DigitsInputFilter;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.adapters.NothingSelectedSpinnerAdapter;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityCreateExpensePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MyDateDialog.MyDateDialogEventListener {
    private Spinner s;
    private Button date;
    private EditText expenditure;
    private EditText cost;
    private Button dateDialog;
    private Button addExpenditure;
    private SingleDateAndTimePicker time_scroller;
    private DatabaseHelper myDB;
    private SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateToString = new SimpleDateFormat("dd MMMMM yyyy");
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense_page);

        //Setup Link to firebase
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        myDB = new DatabaseHelper(this);

        // sets up toolbar with working back button
        Toolbar tb = findViewById(R.id.create_todo_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityExpensePage.class));
        });

        s = findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        date = findViewById(R.id.select_date);
        date.setOnClickListener(v -> openSelectDateDialog(v, date));

        addExpenditure = findViewById(R.id.submit_expenditure);

        cost = findViewById(R.id.cost);
        cost.setFilters(new InputFilter[] {new DigitsInputFilter(Integer.MAX_VALUE, 2, Double.MAX_VALUE)});

        View inflatedView = getLayoutInflater().inflate(R.layout.select_time_dialog, null);
        time_scroller = inflatedView.findViewById(R.id.date_selector_time);

        Intent i = getIntent();
        String num = i.getStringExtra("expense ID");
        Toast.makeText(this, String.valueOf(num), Toast.LENGTH_SHORT).show();
        if (num != null) {
            System.out.println(num);
            DocumentReference docRef = db.collection("expense").document(num);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String category = document.get("Category").toString();
                            s.setAdapter(adapter);
                            s.setSelection(getIndex(s, category));

                            String date = document.get("Date").toString();
                            dateDialog = findViewById(R.id.select_date);
                            try {
                                Date d = stringToDate.parse(date);
                                CustomDay myDay = new CustomDay(d);
                                String output = dateToString.format(d);
                                dateDialog.setText(myDay.getDate());
                            } catch (ParseException e) {
                                System.out.println(e);
                            }

                            String name = document.get("Name").toString();
                            expenditure = findViewById(R.id.expenditure);
                            expenditure.setText(name);

                            String amount = document.get("Amount").toString();
                            cost.setText(amount);


                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });


            System.out.println(num);
            addExpenditure.setOnClickListener(v -> updateExpense(num));
            addExpenditure.setText("Update Expenditure");
        } else {
            s.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_nothing_selected, this));

            addExpenditure.setOnClickListener(v -> addExpense());
        }
    }

    private int getIndex(Spinner spinner, String myString){
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    private boolean checkValidInput() {
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
            return true;
        }
        return false;
    }

    //Adds a document for expenses into the firebase collection called "expense"
    private void addExpense() {
        if (checkValidInput()) {
            Map<String, Object> expense =  new HashMap<>();
            String datePreEdited = date.getText().toString();
            String editedDate = HelperMethods.formatDateWithDash(datePreEdited);

            expense.put("userID", this.userID);
            expense.put("Date", editedDate);
            expense.put("Category", s.getSelectedItem().toString());
            expense.put("Amount", cost.getText().toString());
            expense.put("Name", ((EditText) findViewById(R.id.expenditure)).getText().toString());

            db.collection("expense").add(expense)
                    .addOnSuccessListener(v -> Log.d("Log", "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w("Log", "Error writing document", e));;
            Intent intent = new Intent(this, ActivityExpensePage.class);
            startActivity(intent);
        }
    }

    private void updateExpense(String ID) {
        if (checkValidInput()) {
            Map<String, Object> expense = new HashMap<>();
            String datePreEdited = date.getText().toString();
            String editedDate = HelperMethods.formatDateWithDash(datePreEdited);
            expense.put("userID", this.userID);
            expense.put("Date", editedDate);
            expense.put("Category", s.getSelectedItem().toString());
            expense.put("Amount", cost.getText().toString());
            expense.put("Name", ((EditText) findViewById(R.id.expenditure)).getText().toString());

            db.collection("expense").document(ID).set(expense);
            Intent intent = new Intent(this, ActivityExpensePage.class);
            startActivity(intent);
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
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
    }
}
