package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalActivity extends AppCompatActivity {


    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String TAG = "mapActivity";

    private EditText emailInput;
    private EditText passwordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);


        emailInput = (EditText) findViewById(R.id.updateEmailtxt);
        passwordInput = (EditText) findViewById(R.id.updatePasswordtxt);

        //If deposit card is pressed
        final CardView cardUpdateEmail = findViewById(R.id.cardUpdateEmail);
        cardUpdateEmail.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate deposit inputs
                emailVal();
            }
        });

        final CardView cardUpdatePassword = findViewById(R.id.cardUpdatePassword);
        cardUpdatePassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate deposit inputs
                passwordVal();
            }
        });

        final CardView cardLogOut = findViewById(R.id.cardLogOut);
        cardLogOut.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate deposit inputs
                logOut();
            }
        });

        final CardView cardDeleteAcc = findViewById(R.id.cardDeleteAccount);
        cardDeleteAcc.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate deposit inputs
                deleteAcc();
            }
        });
    }

    public void emailVal(){

    }

    public void passwordVal(){

    }

    public void deleteAcc(){

        //Deletes the user's firebase account and deletes shared preferenes
        //And takes the user to the main menu

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            //Once the firebase user is deleted, delete shared preferences
                            clearSharedPrefs();
                            Log.d(TAG, "User account deleted.");
                            Intent intent = new Intent(PersonalActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });

    }

    public void clearSharedPrefs(){

        String preferencesFile = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public void logOut(){

        //Logs the user out and takes them to the main menu
        mAuth.signOut();
        Intent intent = new Intent(PersonalActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
