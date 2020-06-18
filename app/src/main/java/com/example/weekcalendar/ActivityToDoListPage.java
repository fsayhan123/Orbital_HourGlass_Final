package com.example.weekcalendar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    // ExpandableListView variables
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<String>> mapOfToDo;
    private ExpandableListView expandableListView;
    private ToDoListViewAdapter mAdapter;

    //
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    private SetupNavDrawer navDrawer;

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

        this.expandableListView = findViewById(R.id.expandableListView);

//        this.expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listOfDays.get(groupPosition) + " List Expanded.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        this.expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//            @Override
//            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listOfDays.get(groupPosition) + " List Collapsed.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        this.expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v,
//                                        int groupPosition, int childPosition, long id) {
//                Toast.makeText(
//                        getApplicationContext(),
//                        listOfDays.get(groupPosition)
//                                + " -> "
//                                + mapOfToDo.get(
//                                listOfDays.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT
//                ).show();
//                return false;
//            }
//        });

        // Set up navigation drawer
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.todo_toolbar));
        this.navDrawer.setupNavDrawerPane();
    }

    public void fetchToDos() {
        this.listOfDays = new ArrayList<>();
        this.mapOfToDo = new HashMap<>();
        this.setOfDays = new HashSet<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

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
                                String date = (String) document.get("date");
                                String title = (String) document.get("title");
                                Date d = null;
                                try {
                                    d = dateFormatter.parse(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                CustomDay day = new CustomDay(d);
                                if (!setOfDays.contains(day)) {
                                    setOfDays.add(day);
                                    listOfDays.add(day);
                                    List<String> temp = new ArrayList<>();
                                    temp.add(title);
                                    mapOfToDo.put(day, temp);
                                } else {
                                    mapOfToDo.get(day).add(title);
                                }
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

    public void createToDo(View view) {
        Intent intent = new Intent(this, ActivityCreateToDoPage.class);
        startActivity(intent);
    }

    public void deleteToDo(View view) {
//        Set<Pair<Long, Long>> setItems = this.mAdapter.getCheckedItems();
//        for (Pair<Long, Long> pair : setItems) {
//            myDB.deleteToDo(this.mAdapter.getChild((int) (long) pair.first, (int) (long) pair.second).toString(),
//                    this.mAdapter.getGroup((int) (long) pair.first).toString());
//        }
//
//        Intent intent = new Intent(this, ActivityToDoListPage.class);
//        startActivity(intent);
    }

//    public void getData() {
//        this.mapOfToDo = new HashMap<>();
//        for (CustomDay d : this.listOfDays) {
//            String day = d.getdd();
//            if (day.length() == 1) {
//                day = "0" + day;
//            }
//            String daySQL = d.getyyyy() + "-" + myDB.convertDate(d.getMMM()) + "-" + day;
//            Cursor result = myDB.getToDo(daySQL);
//            this.mapOfToDo.put(d, new ArrayList<>());
//            for (int i = 0; i < result.getCount(); i++) {
//                result.moveToNext();
//                String description = result.getString(2);
//                this.mapOfToDo.get(d).add(description);
//            }
//        }
//    }

}