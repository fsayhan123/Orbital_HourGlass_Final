package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityIndividualNotification extends AppCompatActivity {
    //Firebase Classes
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    //Nav Drawer
    private SetupNavDrawer navDrawer;

    //Notification Content
    private TextView title;
    private TextView content;
    private TextView links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_notification);

        //Setup Nav Drawer
        //this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.notifications_toolbar));
        //this.navDrawer.setupNavDrawerPane();

        //Setup Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        //Setup local activity variables
        this.title = findViewById(R.id.notification_title);
        this.content = findViewById(R.id.notification_body);
        this.links = findViewById(R.id.notification_url);

        setText("Hello", "You are gay", "http://www.google.com");

    }

    private void setText(String title, String content, String links) {
        this.title.setText(title);
        this.content.setText(content);
        String updatedLink = "<a href='" + links + "'> HERE";
        this.links.setMovementMethod(LinkMovementMethod.getInstance());
        this.links.setText(Html.fromHtml(updatedLink));
    }
}