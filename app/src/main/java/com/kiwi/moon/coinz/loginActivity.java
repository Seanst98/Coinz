package com.kiwi.moon.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class loginActivity extends AppCompatActivity {

    private static final String TAG = createAccActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        //Get the text entered into the edit text fields
        emailInput = findViewById(R.id.enterEmail);
        passwordInput = findViewById(R.id.enterPassword);


        final CardView cardView = findViewById(R.id.cardView);

        //If login card is pressed
        cardView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate email and password
                emailPasswordVal();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        //Check if user is signed in (non-null)
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //*******************************************
    //Validate against empty text field for email
    //and password
    //*******************************************
    public void emailPasswordVal() {

        if (emailInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter An Email" , Toast.LENGTH_SHORT).show();
        }
        else if (emailInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter An Email", Toast.LENGTH_SHORT).show();
        }
        else if (passwordInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter A Password" , Toast.LENGTH_SHORT).show();
        }
        else if (passwordInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Password", Toast.LENGTH_SHORT).show();
        }
        else {
            signIn(emailInput.getText().toString(), passwordInput.getText().toString());
        }
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
                            //Take to map
                            Intent myIntent = new Intent(loginActivity.this, mapActivity.class);
                            startActivity(myIntent);
                        }
                        else {
                            //If sign in fails, display error
                            Log.w(TAG, "sign in failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
