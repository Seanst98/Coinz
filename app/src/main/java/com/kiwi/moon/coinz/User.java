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
import java.util.List;
import java.util.Map;

public class User {
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

    public List<Coin> coins;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String TAG = "mapActivity";

    public void getUser() {

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        dayCoins = document.getLong("Day Coins").intValue();
                        dayWalked = (Double) document.getData().get("Day Walked");
                        bankGold = (Double) document.getData().get("Bank GOLD");
                        totalCoins = document.getLong("Total Coins").intValue();
                        totalWalked = (Double) document.getData().get("Total Walked");
                        dolr = (Double) document.getData().get("DOLR Collected");
                        shil = (Double) document.getData().get("SHIL Collected");
                        quid = (Double) document.getData().get("QUID Collected");
                        peny = (Double) document.getData().get("PENY Collected");
                        dolrCoins = document.getLong("DOLR Coins").intValue();
                        shilCoins = document.getLong("SHIL Coins").intValue();
                        quidCoins = document.getLong("QUID Coins").intValue();
                        penyCoins = document.getLong("PENY Coins").intValue();
                        coinsDepositedDay = document.getLong("Day Coins Deposited").intValue();

                    } else {
                        Log.d(TAG, "No such document");
                    }

                    if (listener!=null){
                        Log.d(TAG, "On data loaded is called from user");
                        listener.onDataLoaded();
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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

        db.collection("users").document(mAuth.getUid())
                .set(userStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document Snapshot successfully written!");
                        Log.d(TAG, "Updating user with: " + bankGold);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });

    }

    public interface myCustomObjectListener {

        public void onDataLoaded();
    }

    public User() {
        this.listener = null;
        getUser();
    }

    private myCustomObjectListener listener = null;

    public void setCustomObjectListener(myCustomObjectListener listener) {
        this.listener = listener;
    }


}
