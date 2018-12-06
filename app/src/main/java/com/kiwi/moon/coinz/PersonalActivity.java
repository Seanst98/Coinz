package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PersonalActivity extends AppCompatActivity {


    //Access a cloud firestore instance from the bank activity
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String TAG = "mapActivity";

    private EditText emailInput;
    private EditText passwordInput;
    private TextView UIDtxt;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);


        emailInput = findViewById(R.id.updateEmailtxt);
        passwordInput = findViewById(R.id.updatePasswordtxt);
        UIDtxt = findViewById(R.id.UIDtxt);
        UIDtxt.setText("Your UID is: " + mAuth.getUid());


        //*******************************************
        //Listeners for if a card is pressed
        //*******************************************
        //If deposit card is pressed
        final CardView cardUpdateEmail = findViewById(R.id.cardUpdateEmail);
        cardUpdateEmail.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate email inputs
                emailVal();
            }
        });

        final CardView cardUpdatePassword = findViewById(R.id.cardUpdatePassword);
        cardUpdatePassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate password inputs
                passwordVal();
            }
        });

        final CardView cardLogOut = findViewById(R.id.cardLogOut);
        cardLogOut.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Log the user out
                logOut();
            }
        });

        final CardView cardDeleteAcc = findViewById(R.id.cardDeleteAccount);
        cardDeleteAcc.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Check user meant to delete their account
                deleteAccDialog();
            }
        });
    }

    //*******************************************
    //Validate against empty text field for email
    //*******************************************
    public void emailVal() {

        if (emailInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter An Email" , Toast.LENGTH_SHORT).show();
        }
        else if (emailInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter An Email", Toast.LENGTH_SHORT).show();
        }
        else {
            if (user == null){
                Log.d(TAG, "User is null");
            }
            else {
                updateEmail();
            }
        }
    }

    //*******************************************
    //Validate against empty password text field
    //*******************************************
    public void passwordVal() {

        if (passwordInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter A Password" , Toast.LENGTH_SHORT).show();
        }
        else if (passwordInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Password", Toast.LENGTH_SHORT).show();
        }
        else {
            if (user == null){
                Log.d(TAG, "User is null");
            }
            else {
                updatePassword();
            }
        }
    }

    //*******************************************
    //Update the Firebase email of user
    //Display an error message or success message
    //*******************************************
    public void updateEmail(){

        String newEmail = emailInput.getText().toString();

        user.updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully Changed Email", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User email address updated.");
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Email Update Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //*******************************************
    //Update the Firebase password of user
    //Display an error message or success message
    //*******************************************
    public void updatePassword(){

        String newPassword = passwordInput.getText().toString();

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully Changed Password", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User password updated.");
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Password Update Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //*******************************************
    //Open a dialog with the user if they are sure
    //that they want to delete their account
    //*******************************************
    public void deleteAccDialog() {

        //Open a dialog with the user to confirm their account deletion
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteAcc();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        //Dialog builder / dialog display
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are You Sure You Want To Delete Your Account?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    //*******************************************
    //Delete the user's account
    //
    //Deletion of coins that have been sent as gifts
    //are not deleted as the receiving user may want
    //these or know of these coins
    //
    //Firebase data is also not deleted as it holds
    //no personal information of the user
    //The reason for this is that this data can
    //possibly be of use later on down the line
    //Perhaps for user combined statistics
    //*******************************************
    public void deleteAcc(){

        //Deletes the user's firebase account and deletes shared preferenes
        //And takes the user to the main menu

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            //Once the firebase user is deleted, delete shared preferences
                            clearSharedPrefs();
                            Log.d(TAG, "User account deleted.");
                            Toast.makeText(getApplicationContext(), "Successfully Deleted Account", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PersonalActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });

    }

    //*******************************************
    //We must clear the shared preferences too
    //*******************************************
    public void clearSharedPrefs(){

        //Find the shared preferences file and clear it

        String preferencesFile = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    //*******************************************
    //Log Out
    //*******************************************
    public void logOut(){

        //Logs the user out and takes them to the main menu
        mAuth.signOut();
        Toast.makeText(getApplicationContext(), "Successfully Logged Out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PersonalActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
