package com.example.weekcalendar;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class SetupNavDrawer {
    private Activity a;
    private Toolbar toolbar;
    // navigation drawer pane
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    public SetupNavDrawer(Activity a, Toolbar toolbar) {
        this.a = a;
        this.toolbar = toolbar;
    }

    public void setupNavDrawerPane() {
        ((AppCompatActivity) a).setSupportActionBar(toolbar);

        dl = a.findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(a, dl, toolbar, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        ((AppCompatActivity) a).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) a).getSupportActionBar().setHomeButtonEnabled(true);

        nv = a.findViewById(R.id.nv);
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
                default:
                    break;
            }
            return true;
        });
    }
}
