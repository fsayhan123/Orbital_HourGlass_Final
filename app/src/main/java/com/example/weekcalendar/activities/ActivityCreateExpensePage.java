package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.weekcalendar.helperclasses.DigitsInputFilter;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.adapters.NothingSelectedSpinnerAdapter;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Sets up ActivityCreateExpensePage. Handles events on this page, which are relevant to creating a CustomExpense,
 * then pushing to Firebase.
 */
public class ActivityCreateExpensePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MyDateDialog.MyDateDialogEventListener {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityCreateExpensePage.class.getSimpleName();

    /**
     * Firebase information
     */
    private FirebaseFirestore db;
    private String userID;

    /**
     * XML variables
     */
    private Spinner s;
    private ArrayAdapter<CharSequence> adapter;
    private Button date;
    private Button addExpenditure;
    private EditText expenditure;
    private EditText cost;
    private TextInputLayout spinnerLayout;
    private TextInputLayout dateLayout;
    private Button dateDialog;

    /**
     * DateFormat to format Dates and Strings
     */
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Sets up ActivityCreateExpensePage when it is opened.
     * First, sets up Firebase information.
     * Then, sets up layout items by calling setupXMLItems();
     * Depending whether we are updating an expense or creating a new one, sets up interface
     * accordingly.
     * @param savedInstanceState saved state of current page, if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense_page);

        this.db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        setupXMLItems();

        Intent i = getIntent();
        String num = i.getStringExtra("expense ID");
        if (num != null) {
            setupUpdateInterface(num);
        } else {
            setupCreateInterface();
        }
    }

    /**
     * Sets up layout for ActivityCreateExpensePage.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.create_todo_toolbar);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityExpensePage.class));
        });

        this.s = findViewById(R.id.category_spinner);
        this.adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        this.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.date = findViewById(R.id.select_date);
        this.date.setOnClickListener(v -> openSelectDateDialog(date));

        this.addExpenditure = findViewById(R.id.submit_expenditure);

        this.expenditure = findViewById(R.id.expenditure);

        this.cost = findViewById(R.id.cost);
        this.cost.setFilters(new InputFilter[] {new DigitsInputFilter(Integer.MAX_VALUE, 2, Double.MAX_VALUE)});

        this.spinnerLayout = findViewById(R.id.spinner_layout);

        this.dateLayout = findViewById(R.id.date_layout);
    }

    /**
     * Sets up interface for updating CustomExpense by populating input fields with relevant data
     * if a document ID was passed via Intent.
     * Retrieves required document from Firebase expense collection.
     * @param num Firebase document ID of individual CustomExpense.
     */
    @SuppressLint("SetTextI18n")
    private void setupUpdateInterface(String num) {
        DocumentReference docRef = this.db.collection("expense").document(num);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "Document exists");
                        String category = Objects.requireNonNull(document.get("Category")).toString();
                        s.setAdapter(adapter);
                        s.setSelection(getIndex(s, category));

                        String date = Objects.requireNonNull(document.get("Date")).toString();
                        dateDialog = findViewById(R.id.select_date);
                        try {
                            Date d = stringToDate.parse(date);
                            CustomDay myDay = new CustomDay(d);
                            dateDialog.setText(myDay.getFullDateForView());
                        } catch (ParseException e) {
                            Log.d(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                        }

                        String name = Objects.requireNonNull(document.get("Name")).toString();
                        expenditure.setText(name);

                        String amount = Objects.requireNonNull(document.get("Amount")).toString();
                        cost.setText(amount);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "Get failed with ", task.getException());
                }
            }
        });
        this.addExpenditure.setOnClickListener(v -> updateExpense(num));
        this.addExpenditure.setText("Update Expenditure");
    }

    /**
     * Sets up interface for creating new CustomExpense.
     */
    private void setupCreateInterface() {
        this.s.setAdapter(new NothingSelectedSpinnerAdapter(this.adapter, R.layout.spinner_nothing_selected, this));
        this.addExpenditure.setOnClickListener(v -> addExpense());
    }

    /**
     * Finds the index of the category to display.
     * @param spinner reference to drop down Spinner in XML
     * @param category String of category to display
     * @return index of category
     */
    private int getIndex(Spinner spinner, String category){
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).equals(category)){
                index = i;
            }
        }
        return index;
    }

    /**
     * Checks if necessary fields are sufficiently filled in by user.
     * @return boolean indicating if all necessary fields are sufficiently filled in.
     */
    private boolean checkValidInput() {
        String expenseName = ((EditText) findViewById(R.id.expenditure)).getText().toString();
        if (expenseName.equals("")) {
            this.expenditure.setError("Please insert expense name!");
        } else if (cost.getText().toString().equals("")) {
            this.cost.setError("Please insert expense cost!");
        } else if (date.getText().toString().equals("Select Date")) {
            this.dateLayout.setError("Please choose date!");
        } else if (s.getSelectedItem() == null) {
            this.spinnerLayout.setError("Please choose a category!");
        } else {
            return true;
        }
        return false;
    }

    /**
     * Checks for valid input from user first, before populating a HashMap with input data by calling
     * getExpenseDetails() method.
     * HashMap is to be pushed to Firebase expense collection as a Firebase collection document.
     */
    private void addExpense() {
        if (checkValidInput()) {
            Map<String, Object> expense = getExpenseDetails();

            this.db.collection("expense")
                    .add(expense)
                    .addOnSuccessListener(v -> {
                        Log.d("Log", "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(ActivityCreateExpensePage.this, ActivityExpensePage.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Log.w("Log", "Error writing document", e));
        }
    }

    /**
     *
     * @param ID document ID to be updated with updated details from user
     */
    private void updateExpense(String ID) {
        if (checkValidInput()) {
            Map<String, Object> expense = getExpenseDetails();

            this.db.collection("expense")
                    .document(ID)
                    .set(expense)
                    .addOnSuccessListener(v -> {
                        Log.d("Log", "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(ActivityCreateExpensePage.this, ActivityExpensePage.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Log.w("Log", "Error writing document", e));
        }
    }

    /**
     * Populates a Map where key-value pairs are keys are Strings and values are details of the
     * CustomExpense.
     * @return a String-Object Map which stores the details of the CustomExpense
     */
    private Map<String, Object> getExpenseDetails() {
        Map<String, Object> expense = new HashMap<>();
        String datePreEdited = date.getText().toString();
        String editedDate = HelperMethods.formatDateWithDash(datePreEdited);

        expense.put("userID", this.userID);
        expense.put("Date", editedDate);
        expense.put("Category", s.getSelectedItem().toString());
        expense.put("Amount", cost.getText().toString());
        expense.put("Name", ((EditText) findViewById(R.id.expenditure)).getText().toString());

        return expense;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, "selected " + text, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private void openSelectDateDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);
        int month = c.get(java.util.Calendar.MONTH);
        int year = c.get(java.util.Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityCreateExpensePage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                b.setText(String.format("%02d", dayOfMonth) + " " + HelperMethods.numToStringMonth[month + 1].substring(0, 3) + " " + year);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getFullDateForView());
    }
}