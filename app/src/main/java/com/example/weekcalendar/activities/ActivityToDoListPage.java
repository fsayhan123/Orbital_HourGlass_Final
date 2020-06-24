package com.example.weekcalendar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.example.weekcalendar.adapters.ToDoListViewAdapter;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityToDoListPage extends AppCompatActivity {
    private static final String TAG = ActivityToDoListPage.class.getSimpleName();
    private boolean canDelete = false;

    // ExpandableListView variables
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<CustomToDo>> mapOfToDo;
    private ExpandableListView expandableListView;
    private ToDoListViewAdapter mAdapter;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // Set up navigation drawer
    private SetupNavDrawer navDrawer;

    // To transform String to Date
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("todo");

        // Fetches data from Firebase
        fetchToDos();

        // Link to XML
        this.expandableListView = findViewById(R.id.expandableListView);

        // Set up navigation drawer
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.todo_toolbar));
        this.navDrawer.setupNavDrawerPane();
    }

    private void addToMap(CustomDay day, CustomToDo toDo) {
        if (!this.setOfDays.contains(day)) {
            this.setOfDays.add(day);
            this.listOfDays.add(day);
            List<CustomToDo> temp = new ArrayList<>();
            temp.add(toDo);
            this.mapOfToDo.put(day, temp);
        } else {
            this.mapOfToDo.get(day).add(toDo);
        }
    }

    private void processDocument(QueryDocumentSnapshot document) {
        String ID = document.getId();
        String date = (String) document.get("date");
        String title = (String) document.get("title");
        Date d = null;
        try {
            d = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(d);
        CustomToDo toDo = new CustomToDo(ID, title, date);
        addToMap(day, toDo);
    }

    private void fetchToDos() {
        this.listOfDays = new ArrayList<>();
        this.mapOfToDo = new HashMap<>();
        this.setOfDays = new HashSet<>();

        c.whereEqualTo("userID", userID)
                .orderBy("date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
                            }
                            mAdapter = new ToDoListViewAdapter(ActivityToDoListPage.this, listOfDays, mapOfToDo);
                            expandableListView.setAdapter(mAdapter);
                            // Expand all views by default
                            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                                expandableListView.expandGroup(i);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "hello!!!!!!!!!!!!!!!!!!! " + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

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

    public void createToDo(View view) {
        Intent intent = new Intent(this, ActivityCreateToDoPage.class);
        startActivity(intent);
    }

    public void deleteToDo() {
        Set<Pair<Long, Long>> setItems = this.mAdapter.getCheckedItems();
        for (Pair<Long, Long> pair : setItems) {
            int groupPos = (int) (long) pair.first;
            int childPos = (int) (long) pair.second;
            CustomToDo todo = this.mAdapter.getChild(groupPos, childPos);
            this.mAdapter.remove(groupPos, childPos);
            this.c.document(todo.getID())
                    .delete()
                    .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        }
        setItems.clear();
        this.mAdapter.resetCheckBoxes();
    }
}