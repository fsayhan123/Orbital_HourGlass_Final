package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegisterPage extends AppCompatActivity {
    private static final String TAG = ActivityRegisterPage.class.getSimpleName();

    EditText mFullname, mEmail, mPassword;
    Button mRegisterButton;
    TextView mLoginButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        this.mFullname = findViewById(R.id.create_name);
        this.mEmail = findViewById(R.id.create_email);
        this.mPassword = findViewById(R.id.create_password);
        this.mRegisterButton = findViewById(R.id.register_button);
        this.mLoginButton = findViewById(R.id.already_registered);
        this.progressBar = findViewById(R.id.loading_register);

        this.mLoginButton.setOnClickListener(v -> toLogin());

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        // check if user is already logged in
        if (this.fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), ActivityUpcomingPage.class));
            finish();
        }

        this.mRegisterButton.setOnClickListener(v -> {
            String name = mFullname.getText().toString();
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                // user has not entered email value
                mEmail.setError("Email is required!");
            }

            if (TextUtils.isEmpty(password)) {
                // user has not entered password value
                mPassword.setError("Password is required!");
            }

            if (password.length() < 6) {
                // input password is too short
                mPassword.setError("Password must be >= 6 characters!");
            }

            progressBar.setVisibility(View.VISIBLE);

            // also checks if email is valid
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && password.length() >= 6) {
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // process of registering the user is called task
                        if (task.isSuccessful()) {
                            Toast.makeText(ActivityRegisterPage.this, "User created!", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            // will automatically create it if it does not exist
                            DocumentReference docRef = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName", name);
                            user.put("email", email);
                            docRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "OnSuccess: user Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), ActivityMainCalendar.class));
                        } else {
                            Toast.makeText(ActivityRegisterPage.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    public void toLogin() {
        startActivity(new Intent(this, ActivityLoginPage.class));
    }
}