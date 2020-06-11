package com.example.weekcalendar;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ActivityLoginPage extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mLoginButton;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    TextView mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginButton = findViewById(R.id.login_button);
        mRegister = findViewById(R.id.no_account);
        progressBar = findViewById(R.id.loading_login);

        mRegister.setOnClickListener(v -> toRegister());

        fAuth = FirebaseAuth.getInstance();

        mLoginButton.setOnClickListener(v -> {
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

            // to authenticate user
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && password.length() >= 6) {
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ActivityLoginPage.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(mName).build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                    }
                                }
                            });*/

                            Intent i = new Intent(getApplicationContext(), ActivityUpcomingPage.class);
                            i.putExtra("user", user.getDisplayName());
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityLoginPage.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    public void toRegister() {
        startActivity(new Intent(this, ActivityRegisterPage.class));
    }
}