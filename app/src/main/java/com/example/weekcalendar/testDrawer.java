package com.example.weekcalendar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class testDrawer extends AppCompatActivity {

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        dl = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
//            switch(id)
//            {
//                case R.id.account:
//                    Toast.makeText(testDrawer.this, "My Account",Toast.LENGTH_SHORT).show();break;
//                case R.id.settings:
//                    Toast.makeText(testDrawer.this, "Settings",Toast.LENGTH_SHORT).show();break;
//                case R.id.mycart:
//                    Toast.makeText(testDrawer.this, "My Cart",Toast.LENGTH_SHORT).show();break;
//                default:
//                    return true;
//            }
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}
