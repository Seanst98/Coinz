package com.kiwi.moon.coinz;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class BankActivity extends AppCompatActivity {

    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String TAG = "mapActivity";
    private mapActivity.Rates rates;

    String dolr;
    String peny;
    String shil;
    String quid;

    User user;

    private EditText depositInput;
    //private EditText passwordInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        depositInput = (EditText) findViewById(R.id.depositCoinstxt);
        //passwordInput = (EditText) findViewById(R.id.enterPassword);


        user = getUser();

        //If deposit card is pressed
        final CardView cardDeposit = findViewById(R.id.cardDeposit);
        cardDeposit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to deposit money
                depositVal();
            }
        });

    }

    public void depositVal() {

        if (Integer.parseInt(depositInput.getText().toString())< 0){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Greater Than 0",
                    Toast.LENGTH_LONG).show();
        }
        else if (Integer.parseInt(depositInput.getText().toString())>25){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Less Than 26",
                    Toast.LENGTH_LONG).show();
        }
        else {
            if (user == null){
                Toast.makeText(getApplicationContext(), "User is Null",
                        Toast.LENGTH_LONG).show();

            }
            else {
                deposit();
            }
        }
    }

    public void deposit() {

        double gold = user.shil / Double.parseDouble(shil);


    }

    public void updateFireBaseUser(){
        //Save data in FireStore
        Map<String, Object> userStore = new HashMap<>();
        userStore.put("Day Coins" , user.dayCoins);
        userStore.put("Day Walked", user.dayWalked);
        userStore.put("Total Coins", user.totalCoins);
        userStore.put("Total Walked", user.totalWalked);
        userStore.put("Bank GOLD", user.bankGold);
        userStore.put("SHIL Collected", user.shil);
        userStore.put("QUID Collected", user.quid);
        userStore.put("PENY Collected", user.peny);
        userStore.put("DOLR Collected", user.dolr);

        db.collection("users").document(mAuth.getUid())
                .set(userStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document Snapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });


    }

    public User getUser(){

        User usr = new User();
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        usr.dayCoins = document.getLong("Day Coins").intValue();
                        Log.d(TAG, "Day Coins: " + document.getLong("Day Coins"));
                        usr.dayWalked = document.getLong("Day Walked");
                        usr.bankGold = document.getLong("Bank GOLD");
                        usr.totalCoins = document.getLong("Total Coins").intValue();
                        Log.d(TAG, "Total Coins: " + document.getLong("Total Coins"));
                        usr.totalWalked = document.getLong("Total Walked");
                        usr.dolr = document.getLong("DOLR Collected");
                        usr.shil = document.getLong("SHIL Collected");
                        usr.quid = document.getLong("QUID Collected");
                        usr.peny = document.getLong("PENY Collected");

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return usr;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            dolr = extras.getString("DOLR");
            peny = extras.getString("PENY");
            shil = extras.getString("SHIL");
            quid = extras.getString("QUID");

            Toast.makeText(getApplicationContext(), dolr, Toast.LENGTH_LONG).show();

        }

    }
}
