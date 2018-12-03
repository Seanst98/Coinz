package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BankActivity extends AppCompatActivity {

    //*******************************************
    //Define variables
    //*******************************************
    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String TAG = "mapActivity";

    JsonData coinsCollectedData;

    User user = User.getinstance();

    private EditText coinsInput;
    private TextView coinsCollectedTxt;
    private TextView goldInBankTxt;

    private EditText giftNameInput;
    private EditText giftAmountInput;

    private final String preferencesFile = "MyPrefsFile";   //For storing preferences

    JsonData coinsReceived;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        //*******************************************
        //Get the text boxes from the layout
        //*******************************************
        coinsInput = (EditText) findViewById(R.id.depositCoins);
        coinsCollectedTxt = (TextView) findViewById(R.id.coinsCollectedtxt);
        goldInBankTxt = (TextView) findViewById(R.id.goldInBanktxt);

        giftNameInput = (EditText) findViewById(R.id.giftnametxt);
        giftAmountInput = (EditText) findViewById(R.id.giftamounttxt);


        //*******************************************
        //Listeners for if a card is pressed
        //*******************************************
        //If deposit card is pressed
        final CardView cardDeposit = findViewById(R.id.cardDeposit);
        cardDeposit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to validate deposit inputs
                depositVal();
            }
        });

        //If gift card is pressed
        final CardView cardGift = findViewById(R.id.cardGift);
        cardGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Call function to validate gift inputs
                giftVal();
            }
        });

        //If check for coins card is pressed
        final CardView cardRetrieve = findViewById(R.id.cardRetrieve);
        cardRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Call function to check for coins and retrieve them if there are coins
                checkForCoins();
            }
        }
        );

    }


    //*******************************************
    //Check if the player has been sent any coins
    //*******************************************
    public void checkForCoins() {

        coinsReceived = null;

        db.collection("availableCoins").whereEqualTo("ForUID", mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //If there are no documents resulting from the query
                            if (task.getResult().size() == 0) {

                                Toast.makeText(getApplicationContext(),"No One Has Sent You Any Coins", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "No Documents To Receive");
                            }

                            //*******************************************
                            //Get the coins from the query
                            //There may be multiple documents intended for
                            //this user and we handle that by looping through
                            //all the found documents
                            //*******************************************
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String json = (String) document.getData().get("Coins");

                                coinsReceived = new JsonData(json);
                                coinsCollectedData.getFeatures().addAll(coinsReceived.getFeatures());
                                Toast.makeText(getApplicationContext(),"You Have Collected " + coinsReceived.getFeatures().size() +" Coins", Toast.LENGTH_SHORT).show();

                                coinsCollectedTxt.setText("You Have " + coinsCollectedData.getFeatures().size() + " Coins To Deposit Or Gift");
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                //Delete the document so the player can't retrieve coins more than once
                                db.collection("availableCoins").document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Log.d(TAG, "Error deleting document", e);
                                            }
                                        });
                            }

                        }
                        else {

                            Log.d(TAG, "Error getting documents: " + task.getException());
                        }
                    }
                });


    }

    //*******************************************
    //Validate gifting
    //When the future team of developers come in
    //they can alter the validation of names to check
    //that the user actually exists and that you only
    //need the user's name not UID
    //*******************************************
    public void giftVal(){

        //Amount validation
        if (giftAmountInput == null) {
            Toast.makeText(getApplicationContext(), "Please Enter An Amount To Gift" , Toast.LENGTH_SHORT).show();
        }
        else if (giftNameInput == null) {
            Toast.makeText(getApplicationContext(), "Please Enter A Name To Gift To" , Toast.LENGTH_SHORT).show();
        }
        else if (giftAmountInput.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter An Amount To Gift" , Toast.LENGTH_SHORT).show();
        }
        else if (giftNameInput.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter A Name To Gift To" , Toast.LENGTH_SHORT).show();
        }
        else if (Integer.parseInt(giftAmountInput.getText().toString()) < 1){
            Toast.makeText(getApplicationContext(), "Please Enter An Amount To Gift Greater Than 0" , Toast.LENGTH_SHORT).show();
        }
        else if (Integer.parseInt(giftAmountInput.getText().toString()) > 25){
            Toast.makeText(getApplicationContext(), "Please Enter An Amount To Gift Less than 26" , Toast.LENGTH_SHORT).show();
        }
        //NAME VALIDATION
        else if (coinsCollectedData.getFeatures().size() < Integer.parseInt(giftAmountInput.getText().toString())){
            Toast.makeText(getApplicationContext(), "You Can't Gift More Coins Than You Own", Toast.LENGTH_SHORT).show();
        }
        else if (giftNameInput.getText().toString().equals(mAuth.getUid())) {
            Toast.makeText(getApplicationContext(), "You Can't Send Coins To Yourself", Toast.LENGTH_SHORT).show();
        }
        else if (coinsCollectedData.getFeatures().size() == 0){
            Toast.makeText(getApplicationContext(), "You Don't Have Any Coins To Gift! Go Collect Some", Toast.LENGTH_SHORT).show();
        }
        else {
            gift();
        }
    }

    //*******************************************
    //Gift the coin to the availableCoins collection
    //and remove it from the user's collection
    //*******************************************
    public void gift(){

        //The coins are converted to json and stored as a json string

        List<Coin> coins = new ArrayList<>();
        JsonData storable = new JsonData(coinsCollectedData.getType(), coinsCollectedData.getDate_generated(), coinsCollectedData.getTime_generated(), coinsCollectedData.getApproximate_time_remaining(), coinsCollectedData.getRates(), coins);

        for (int i = 0; i < Integer.parseInt(giftAmountInput.getText().toString()); i++){
            storable.getFeatures().add(coinsCollectedData.getFeatures().get(i));
        }

        //Save data in FireStore
        Map<String, Object> userStore = new HashMap<>();
        userStore.put("Coins" , storable.toJson());
        userStore.put("ForUID", giftNameInput.getText().toString());
        userStore.put("FromUID", mAuth.getUid());

        db.collection("availableCoins").document()
                .set(userStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Successfully Gifted", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document Snapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });

        //Remove the coins from the user's collection
        for (int i = 0; i < Integer.parseInt(giftAmountInput.getText().toString()); i++){
            coinsCollectedData.getFeatures().remove(0);
        }

        //Update UI
        coinsCollectedTxt.setText("You Have " + coinsCollectedData.getFeatures().size() + " Coins To Deposit Or Gift");

    }

    //*******************************************
    //Validate depositing
    //*******************************************
    public void depositVal() {

        if (user.coinsDepositedDay < 25){

            if (coinsInput.getText() == null){
                Toast.makeText(getApplicationContext(), "Please Enter A Value" , Toast.LENGTH_SHORT).show();
            }
            else if (coinsInput.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Please Enter A Value", Toast.LENGTH_SHORT).show();
            }
            else if (Integer.parseInt(coinsInput.getText().toString()) < 1){
                Toast.makeText(getApplicationContext(), "Please Enter A Number Greater Than 0",
                        Toast.LENGTH_LONG).show();
            }
            else if (Integer.parseInt(coinsInput.getText().toString()) > 25){
                Toast.makeText(getApplicationContext(), "Please Enter A Number Less Than 26",
                        Toast.LENGTH_LONG).show();
            }
            else if (coinsCollectedData.getFeatures().size() < Integer.parseInt(coinsInput.getText().toString())){
                Toast.makeText(getApplicationContext(), "You Can't Deposit More Coins Than You Own", Toast.LENGTH_SHORT).show();
            }
            else if ((user.coinsDepositedDay + Integer.parseInt(coinsInput.getText().toString())) > 25){
                Toast.makeText(getApplicationContext(), "You Can't Deposit More Than 25 Coins Per Day", Toast.LENGTH_SHORT).show();
            }
            if (coinsCollectedData.getFeatures().size() == 0){   //If you don't have any coins to deposit
                Toast.makeText(getApplicationContext(), "You Don't Have Any Coins To Deposit! Go Collect Some", Toast.LENGTH_SHORT).show();
            }
            else {
                if (user == null){
                    Toast.makeText(getApplicationContext(), "User is Null",
                            Toast.LENGTH_LONG).show();

                }
                else {
                    Log.d(TAG, "DEPOSITING");
                    deposit();
                }
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "You Have Already Deposited 25 Coins Today", Toast.LENGTH_SHORT).show();
        }

    }

    //*******************************************
    //Deposit the selected number of coins
    //*******************************************
    public void deposit() {

        double gold = 0;

        //For each coin, convert to GOLD and remove from user's collection
        for (int i = 0; i < Integer.parseInt(coinsInput.getText().toString()); i++){

            switch (coinsCollectedData.getFeatures().get(0).properties.currency) {

                case "DOLR":
                    gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.getFeatures().get(0).properties.value), Double.parseDouble(coinsCollectedData.getRates().DOLR));
                    break;

                case "SHIL":
                    gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.getFeatures().get(0).properties.value), Double.parseDouble(coinsCollectedData.getRates().SHIL));
                    break;

                case "PENY":
                    gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.getFeatures().get(0).properties.value), Double.parseDouble(coinsCollectedData.getRates().PENY));
                    break;

                case "QUID":
                    gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.getFeatures().get(0).properties.value), Double.parseDouble(coinsCollectedData.getRates().QUID));
                    break;
            }

            coinsCollectedData.getFeatures().remove(0);

            Log.d(TAG, "Coins Collected Data is now: " + coinsCollectedData.toJson());

            Toast.makeText(getApplicationContext(), "Deposited!", Toast.LENGTH_SHORT).show();

        }

        //Update UI and update Firestore
        Log.d(TAG, "Gold Value of coins: " + gold);
        user.bankGold = user.bankGold + gold;
        user.coinsDepositedDay = user.coinsDepositedDay + Integer.parseInt(coinsInput.getText().toString());
        user.updateUser();

        coinsCollectedTxt.setText("You Have " + coinsCollectedData.getFeatures().size() + " Coins To Deposit Or Gift");
        goldInBankTxt.setText("You Have " + user.bankGold + " GOLD In The Bank");


    }

    //*******************************************
    //Currency to GOLD conversion
    //*******************************************
    public double exchangeConversion(double amount, double rate){
        return amount/rate;
    }

    //*******************************************
    //When the activity starts
    //Get the user's coin collection and update UI
    //*******************************************
    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        String json = "";

        if(extras != null) {
            json = extras.getString("coinsCollected");
        }

        coinsCollectedData = new JsonData(json);
        coinsCollectedTxt.setText("You Have " + coinsCollectedData.getFeatures().size() + " Coins To Deposit Or Gift");
        goldInBankTxt.setText("You Have " + user.bankGold + " GOLD In The Bank");

        Log.d(TAG, "starting bank day walked is: " + user.dayWalked);

    }

    //*******************************************
    //When the activity is paused
    //The reason I do storing here is because this is
    //done before the next activity's onStart function
    //is called
    //*******************************************
    @Override
    protected void onPause() {
        super.onPause();

        //We do this in onPause as we want this effect to happen before reaching map activity to read it
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("coinsCollected", coinsCollectedData.toJson());

        //Apply the edits
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Save data in FireStore
        user.updateUser();
    }
}

