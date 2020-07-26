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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityLoginPage extends AppCompatActivity {
    private static final String TAG = ActivityLoginPage.class.getSimpleName();

    private EditText mEmail, mPassword;
    private Button mLoginButton;
    private ProgressBar progressBar;
    private TextView mRegister;

    private FirebaseAuth fAuth;
    private static final int RC_SIGN_IN = 1;

    // Google sign in
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"), new Scope("https://www.googleapis.com/auth/calendar.events"))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupXMLItems();
    }

    private void setupXMLItems() {
        this.signInButton = findViewById(R.id.sign_in_button);
        this.signInButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        this.mEmail = findViewById(R.id.email);
        this.mPassword = findViewById(R.id.password);
        this.mLoginButton = findViewById(R.id.login_button);
        this.mRegister = findViewById(R.id.no_account);
        this.progressBar = findViewById(R.id.loading_login);

        this.mRegister.setOnClickListener(v -> toRegister());

        // to log in with firebase
        this.mLoginButton.setOnClickListener(v -> {
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
                            Intent i = new Intent(getApplicationContext(), ActivityMainCalendar.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityLoginPage.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {

        }
        super.onStart();
    }

    public void toRegister() {
        startActivity(new Intent(this, ActivityRegisterPage.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String authCode = account.getServerAuthCode();
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        this.fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fAuth.getCurrentUser();
                            String id = user.getUid();

                            DocumentReference docRef = fStore.collection("users").document(id);
                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("fName", user.getDisplayName());
                            userDetails.put("email", user.getEmail());
                            docRef.set(userDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "OnSuccess: user Profile is created for " + id);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });

                            Toast.makeText(ActivityLoginPage.this, "Logging in as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), ActivityMainCalendar.class);
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(ActivityLoginPage.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}