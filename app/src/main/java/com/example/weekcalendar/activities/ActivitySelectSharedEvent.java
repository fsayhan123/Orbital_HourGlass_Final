package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.PendingSharedEventsRecyclerViewAdapter;
import com.example.weekcalendar.adapters.ResponseRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomPendingShared;
import com.example.weekcalendar.customclasses.CustomResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mortbay.jetty.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivitySelectSharedEvent extends AppCompatActivity {

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    //Local Variables
    private String responseID;
    private TextView title;

    //Recycler View Variables
    private ArrayList<CustomResponse> CustomResponseArrayList;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ResponseRecyclerViewAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shared_event);

        System.out.println("Hello 1");
        this.title = findViewById(R.id.select_shared_event_title);

        Intent intent = getIntent();
        this.responseID = intent.getStringExtra("responseID");

        //Setup firebase Variables
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        //Recycler View Setup
        this.mRecyclerView = findViewById(R.id.selecting_shared_event_date);
        this.mRecyclerView.setHasFixedSize(true);
        this.layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);

        System.out.println("Hello 2");
        this.CustomResponseArrayList = new ArrayList<>();
        this.fStore.collection("responses")
                .document(this.responseID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ActivitySelectSharedEvent.this.title.setText(documentSnapshot.get("title").toString());

                        //Get the responses, order by array length, then put into recycler view
                        HashMap<String, ArrayList<String>> hashMap = (HashMap<String, ArrayList<String>>) documentSnapshot.get("responses");
                        for (Map.Entry<String, ArrayList<String>> entry : hashMap.entrySet()) {
                            String date = entry.getKey();
                            int count = entry.getValue().size();
                            CustomResponse response = new CustomResponse(date, count);
                            ActivitySelectSharedEvent.this.CustomResponseArrayList.add(response);
                        }

                        mAdapter = new ResponseRecyclerViewAdapter(ActivitySelectSharedEvent.this.CustomResponseArrayList);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }
}