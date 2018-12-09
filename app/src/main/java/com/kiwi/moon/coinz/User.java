package com.kiwi.moon.coinz;

import android.support.annotation.NonNull;
import android.util.Log;

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

//*******************************************
//User class
//*******************************************
public class User{

    private static User user = new User();   //Singleton instance

    public UserListener delegate;   //Delegate used for starting map when data is loaded

    //Private constructor for using as a singleton
    private User(){
        this.delegate = null;
    }

    //Get the singleton instance
    public static User getinstance(){
        return user;
    }

    public int dayCoins;
    public int totalCoins;
    public double dayWalked; //in metres
    public double totalWalked;
    public double bankGold;

    public double shil;
    public double peny;
    public double quid;
    public double dolr;

    public int shilCoins;
    public int penyCoins;
    public int quidCoins;
    public int dolrCoins;

    public int coinsDepositedDay;

    public int ghostTime;

    public boolean loaded = false;

    public boolean ghostMode = false;
    public boolean timeTrialMode = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String TAG = "mapActivity";

    public void getUser() {

        db = FirebaseFirestore.getInstance();

        if (mAuth.getUid()!=null) {   //Prevent null ptrs
            DocumentReference docRef = db.collection("users").document(mAuth.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document!=null){   //Prevent null ptrs
                            //However, document should never return null
                            //The safeguard is still put into place to remove warnings
                            if (document.exists()) {

                                Log.d(TAG, "Document data: " + document.getData());
                                dayWalked = document.getDouble("Day Walked");
                                dayCoins = document.getLong("Day Coins").intValue();
                                bankGold = document.getDouble("Bank GOLD");
                                totalCoins = document.getLong("Total Coins").intValue();
                                totalWalked = document.getDouble("Total Walked");
                                dolr = document.getDouble("DOLR Collected");
                                shil = document.getDouble("SHIL Collected");
                                quid = document.getDouble("QUID Collected");
                                peny = document.getDouble("PENY Collected");
                                dolrCoins = document.getLong("DOLR Coins").intValue();
                                shilCoins = document.getLong("SHIL Coins").intValue();
                                quidCoins = document.getLong("QUID Coins").intValue();
                                penyCoins = document.getLong("PENY Coins").intValue();
                                coinsDepositedDay = document.getLong("Day Coins Deposited").intValue();
                                ghostTime = document.getLong("Ghost Time").intValue();
                                loaded = true;

                            } else {
                                Log.d(TAG, "No such document");
                                Log.d(TAG, "Creating the user on firebase");
                                createAccount();
                            }

                            if (delegate!=null){
                                delegate.onDataLoaded();
                            }
                        }
                        else {
                            Log.d(TAG, "document is null");
                        }

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        else {
            Log.d(TAG, "mAuth is null");
        }

    }

    public void updateUser() {

        //Save data in FireStore
        Map<String, Object> userStore = new HashMap<>();
        userStore.put("Day Coins", dayCoins);
        userStore.put("Day Walked", dayWalked);
        userStore.put("Total Coins", totalCoins);
        userStore.put("Total Walked", totalWalked);
        userStore.put("Bank GOLD", bankGold);
        userStore.put("SHIL Collected", shil);
        userStore.put("QUID Collected", quid);
        userStore.put("PENY Collected", peny);
        userStore.put("DOLR Collected", dolr);
        userStore.put("DOLR Coins", dolrCoins);
        userStore.put("SHIL Coins", shilCoins);
        userStore.put("PENY Coins", penyCoins);
        userStore.put("QUID Coins", quidCoins);
        userStore.put("Day Coins Deposited", coinsDepositedDay);
        userStore.put("Ghost Time", ghostTime);

        if (mAuth.getUid()!=null){
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
        else {
            Log.d(TAG, "mAuth is null");
        }

    }

    public void createAccount() {
        dayCoins = 0;
        dayWalked = 0;
        totalCoins = 0;
        totalWalked = 0;
        bankGold = 0;
        shil = 0;
        peny = 0;
        quid = 0;
        dolr = 0;
        shilCoins = 0;
        penyCoins = 0;
        quidCoins = 0;
        dolrCoins = 0;
        coinsDepositedDay = 0;
        ghostTime = 0;
        loaded = false;
        ghostMode = false;
        timeTrialMode = false;



        //Save data in FireStore
        Map<String, Object> userStore = new HashMap<>();
        userStore.put("Day Coins", dayCoins);
        userStore.put("Day Walked", dayWalked);
        userStore.put("Total Coins", totalCoins);
        userStore.put("Total Walked", totalWalked);
        userStore.put("Bank GOLD", bankGold);
        userStore.put("SHIL Collected", shil);
        userStore.put("QUID Collected", quid);
        userStore.put("PENY Collected", peny);
        userStore.put("DOLR Collected", dolr);
        userStore.put("DOLR Coins", dolrCoins);
        userStore.put("SHIL Coins", shilCoins);
        userStore.put("PENY Coins", penyCoins);
        userStore.put("QUID Coins", quidCoins);
        userStore.put("Day Coins Deposited", coinsDepositedDay);
        userStore.put("Ghost Time", ghostTime);

        if (mAuth.getUid()!=null){
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
        else {
            Log.d(TAG, "mAuth is null");
        }

    }


}
