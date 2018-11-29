package com.kiwi.moon.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class loginActivity extends AppCompatActivity {

    private static final String TAG = createAccActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.enterEmail);
        passwordInput = (EditText) findViewById(R.id.enterPassword);


        final CardView cardView = findViewById(R.id.cardView);

        //If login card is pressed
        cardView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to create an account
                signIn(emailInput.getText().toString(), passwordInput.getText().toString());
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        //Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void signIn(String email, String password) {

        //Sign in user with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success and take to map screen
                            Log.d(TAG, "sign in success");
                            Toast.makeText(getApplicationContext(), "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Take to map
                            Intent myIntent = new Intent(loginActivity.this, mapActivity.class);
                            startActivity(myIntent);
                        }
                        else {
                            //If sign in fails, display error
                            Log.w(TAG, "sign in failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                            //Update UI
                        }
                    }
                });
    }


}
