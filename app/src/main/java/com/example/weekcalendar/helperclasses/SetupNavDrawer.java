package com.example.weekcalendar.helperclasses;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.weekcalendar.FetchGoogleCalendarEvents;
import com.example.weekcalendar.R;
import com.example.weekcalendar.activities.ActivityExpensePage;
import com.example.weekcalendar.activities.ActivityLoginPage;
import com.example.weekcalendar.activities.ActivityMainCalendar;
import com.example.weekcalendar.activities.ActivityToDoListPage;
import com.example.weekcalendar.activities.ActivityUpcomingPage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class SetupNavDrawer {
    private Activity a;
    private Toolbar toolbar;

    public SetupNavDrawer(Activity a, Toolbar toolbar) {
        this.a = a;
        this.toolbar = toolbar;
    }

    public void setupNavDrawerPane() {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(a.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(a, gso);

        ((AppCompatActivity) a).setSupportActionBar(toolbar);

        // navigation drawer pane
        DrawerLayout dl = a.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle t = new ActionBarDrawerToggle(a, dl, toolbar, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        ((AppCompatActivity) a).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) a).getSupportActionBar().setHomeButtonEnabled(true);

        NavigationView nv = a.findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch(id)
            {
                case R.id.activity_upcoming_view:
                    Toast.makeText(a, "Upcoming Activities",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(a, ActivityUpcomingPage.class);
                    a.startActivity(i);
                    break;
                case R.id.activity_expenses_view:
                    Toast.makeText(a, "Expenses",Toast.LENGTH_SHORT).show();
                    Intent i2 = new Intent(a, ActivityExpensePage.class);
                    a.startActivity(i2);
                    break;
                case R.id.activity_todo_view:
                    Toast.makeText(a, "Todo List",Toast.LENGTH_SHORT).show();
                    Intent i3 = new Intent(a, ActivityToDoListPage.class);
                    a.startActivity(i3);
                    break;
                case R.id.logout_button:
                    Toast.makeText(a, "Logout",Toast.LENGTH_SHORT).show();
                    mGoogleSignInClient.signOut();
                    Intent i4 = new Intent(a, ActivityLoginPage.class);
                    a.startActivity(i4);
                    break;
                case R.id.fetch_button:
                    Toast.makeText(a, "Fetch Events",Toast.LENGTH_SHORT).show();
                    Intent i5 = new Intent(a, FetchGoogleCalendarEvents.class);
                    a.startActivity(i5);
                    break;
                case R.id.main_calendar_button:
                    Toast.makeText(a, "Main calendar",Toast.LENGTH_SHORT).show();
                    Intent i6 = new Intent(a, ActivityMainCalendar.class);
                    a.startActivity(i6);
                    break;
                default:
                    break;
            }
            return true;
        });

        View hView =  nv.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.user);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(a);
        if (acct != null) {
            String personName = acct.getDisplayName();
            nav_user.setText(personName);
        } else {
            String userID = fAuth.getCurrentUser().getUid();
            DocumentReference docRef = fStore.collection("users").document(userID);
            docRef.addSnapshotListener(a, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    // accessing via key value pairs
                    String username = documentSnapshot.getString("fName");
                    nav_user.setText(username);
                }
            });
        }


    }
}
