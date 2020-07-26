package com.example.weekcalendar.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.example.weekcalendar.adapters.ToDoListViewAdapter;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ActivityToDoListPage extends AppCompatActivity {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityToDoListPage.class.getSimpleName();

    /**
     * Indicates functionality to delete
     */
    public boolean canDelete = false;

    /**
     * UI variables
     */
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<CustomToDo>> mapOfToDo;
    private ExpandableListView expandableListView;
    private ToDoListViewAdapter mAdapter = new ToDoListViewAdapter(this, new ArrayList<>(), new HashMap<>());

    /**
     * Firebase information
     */
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    /**
     * Converts Strings to Dates and vice versa
     */
    @SuppressLint("SimpleDateFormat")
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_page);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        this.c = this.fStore.collection("todo");

        setupXMLItems();

        fetchToDos();
    }

    /**
     * Sets up layout for ActivityToDoListPage.
     */
    private void setupXMLItems() {
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.todo_toolbar));
        navDrawer.setupNavDrawerPane();

        FloatingActionButton createToDo = findViewById(R.id.create_to_do_button);
        createToDo.setOnClickListener(v -> createToDo());

        this.expandableListView = findViewById(R.id.expandableListView);
    }

    /**
     * Groups to do items in their respective days for display.
     * @param day day associated with to do item
     * @param customToDo to do item to be put in group
     */
    private void addToMap(CustomDay day, CustomToDo customToDo) {
        if (!this.setOfDays.contains(day)) {
            this.setOfDays.add(day);
            this.listOfDays.add(day);
            List<CustomToDo> temp = new ArrayList<>();
            temp.add(customToDo);
            this.mapOfToDo.put(day, temp);
        } else {
            Objects.requireNonNull(this.mapOfToDo.get(day)).add(customToDo);
        }
    }

    /**
     * Queries all to do documents in Firebase to do collection relevant to user.
     */
    private void fetchToDos() {
        this.listOfDays = new ArrayList<>();
        this.mapOfToDo = new HashMap<>();
        this.setOfDays = new HashSet<>();

        this.c.whereEqualTo("userID", userID)
                .orderBy("date")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            Log.d(TAG, "onSuccess: LIST NOT EMPTY");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
                            }
                            mAdapter = new ToDoListViewAdapter(ActivityToDoListPage.this, listOfDays, mapOfToDo);
                            expandableListView.setAdapter(mAdapter);
                            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                                expandableListView.expandGroup(i);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    }
                });
    }

    /**
     * Processes Firebase to do document to get details for each to do item
     * @param document with details representing 1 to do item
     */
    private void processDocument(QueryDocumentSnapshot document) {
        String ID = document.getId();
        String date = (String) document.get("date");
        String title = (String) document.get("title");
        String eventID = (String) document.get("eventID");
        boolean completed = (boolean) document.get("completed");
        Date d = null;
        try {
            d = dateFormatter.parse(Objects.requireNonNull(date));
            CustomDay day = new CustomDay(Objects.requireNonNull(d));
            CustomToDo toDo;
            if (eventID != null) {
                toDo = new CustomToDo(ID, eventID, title, date, completed);
            } else {
                toDo = new CustomToDo(ID, title, date, completed);

            }
            addToMap(day, toDo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            if (this.canDelete) {
                deleteToDo();
                item.setIcon(R.drawable.ic_baseline_delete_24_transparent);
                canDelete = false;
            } else {
                canDelete = true;
                item.setIcon(R.drawable.ic_baseline_delete_24);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Links to ActivityCreateToDoPage when button is clicked.
     */
    private void createToDo() {
        Intent intent = new Intent(this, ActivityCreateToDoPage.class);
        startActivity(intent);
    }

    /**
     * Stores details of to do item in a Map. Used to display details in view adapters.
     * @param customToDo to do item to get details from
     * @param offset for timestamp to maintain ordering of to do items
     * @return Map of details of to do item
     */
    private Map<String, Object> customToDoToMap(CustomToDo customToDo, long offset) {
        Map<String, Object> todoDetails = new HashMap<>();
        todoDetails.put("userID", userID);
        todoDetails.put("date", customToDo.getDate());
        todoDetails.put("title", customToDo.getTitle());
        todoDetails.put("completed", customToDo.getCompleted());
        todoDetails.put("timestamp", System.currentTimeMillis() + offset);
        if (customToDo.getEventID() != null) {
            todoDetails.put("eventID", customToDo.getEventID());
        }
        return todoDetails;
    }

    /**
     * Deletes selected to do items.
     */
    private void deleteToDo() {
        Set<Pair<Long, Long>> setItems = this.mAdapter.getItemsToDelete();
        List<Pair<Long, Long>> iterable = new ArrayList<>(setItems);
        Collections.sort(iterable, (p1, p2) -> (int) -(p2.first - p1.first + p2.second - p1.second));
        for (int i = iterable.size() - 1; i >= 0; i--) {
            Pair<Long, Long> pair = iterable.get(i);
            Log.d(TAG, pair.toString());
            int groupPos = (int) (long) pair.first;
            int childPos = (int) (long) pair.second;
            CustomToDo todo = this.mAdapter.getChild(groupPos, childPos);
            this.mAdapter.remove(groupPos, childPos);
            this.c.document(todo.getID())
                    .delete()
                    .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        }
        if (iterable.size() > 0) {
            this.mAdapter.resetCheckBoxes();
            setItems.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        pushToDo();
    }

    /**
     * Updates states of toggled to do items upon leaving this page.
     */
    private void pushToDo() {
        Set<Pair<Long, Long>> toggledItems = this.mAdapter.getToggledItems();
        long offset = 0;
        for (Pair<Long, Long> pair : toggledItems) {
            int groupPos = (int) (long) pair.first;
            int childPos = (int) (long) pair.second;
            CustomToDo todo = this.mAdapter.getChild(groupPos, childPos);
            this.fStore.collection("todo")
                    .document(todo.getID())
                    .set(customToDoToMap(todo, offset));
            offset++;
        }
    }

}