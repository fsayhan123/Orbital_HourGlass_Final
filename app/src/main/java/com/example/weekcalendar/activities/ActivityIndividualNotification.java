package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityIndividualNotification extends AppCompatActivity {
    //Firebase Classes
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    //Notification Content
    private TextView title;
    private TextView content;
    private TextView links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_notification);

        //Setup Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        //Setup local activity variables
        this.title = findViewById(R.id.notification_title);
        this.content = findViewById(R.id.notification_body);
        this.links = findViewById(R.id.notification_url);

        //get the document then set the text
        Intent intent = getIntent();
        String notificationID = intent.getStringExtra("notificationID");
        this.fStore.collection("Notifications")
                .document(notificationID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String message = (String) documentSnapshot.get("Message");
                        String date = (String) documentSnapshot.get("Date");
                        String url = (String) documentSnapshot.get("url");
                        setText(date, message, url);
                    }
                });
    }

    private void setText(String title, String content, String links) {
        this.title.setText(title);
        this.content.setText(content);
        String updatedLink = "<a href='" + links + "'> HERE";
        this.links.setMovementMethod(LinkMovementMethod.getInstance());
        this.links.setText(Html.fromHtml(updatedLink));
    }
}