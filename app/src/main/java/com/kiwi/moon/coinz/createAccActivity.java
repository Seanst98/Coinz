package com.kiwi.moon.coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.support.annotation.NonNull;
import android.util.Log;

public class createAccActivity extends AppCompatActivity {

    private static final String TAG = createAccActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;
    private EditText passwordConfInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        mAuth = FirebaseAuth.getInstance();

        //Get text entered into edit text fields
        emailInput = findViewById(R.id.enterEmail);
        passwordInput = findViewById(R.id.enterPassword);
        passwordConfInput = findViewById(R.id.enterPassword2);


        final CardView cardView = findViewById(R.id.cardView);

        //If create account card is pressed
        cardView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to create an account
                emailPasswordVal();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is signed in (non-null)
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //This can be updated to possibly allow for ability to skip login if already logged in

    }

    //*******************************************
    //Validate against empty text field for email
    //and passwords
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
        else if (passwordConfInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter A Confirmation Password" , Toast.LENGTH_SHORT).show();
        }
        else if (passwordConfInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Confirmation Password", Toast.LENGTH_SHORT).show();
        }
        else {
            createAcc(emailInput.getText().toString(), passwordInput.getText().toString(),
                    passwordConfInput.getText().toString());
        }
    }

    public void createAcc(String email, String password, String passwordConf){

        //If password and confirmation password match
        if (password.equals(passwordConf)){

            //Create a new user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Account creation success
                                Log.d(TAG, "createUserWithEmail:success");
                                //Start Activity to the map activity
                                Intent myIntent = new Intent(createAccActivity.this, mapActivity.class);
                                startActivity(myIntent);
                            } else {

                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Creation Failed. Try A Different Email/Password",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "Password and confirmation password not matching", Toast.LENGTH_LONG).show();
        }

    }



}
