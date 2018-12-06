package com.kiwi.moon.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class OptionalFeaturesActivity extends AppCompatActivity {

    User user = User.getinstance();
    TextView ghostText;
    TextView timeTrialText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optional_features);

        ghostText = findViewById(R.id.textGhost);
        timeTrialText = findViewById(R.id.textTimeTrial);

        //*******************************************
        //If we are already in a mode then update UI
        //*******************************************
        if (user.ghostMode){
            ghostText.setText("De-Activate Ghost Mode");
        }
        if (user.timeTrialMode){
            timeTrialText.setText("De-Activate Time Trial Mode");
        }

        //If Ghost card is pressed
        final CardView cardGhost = findViewById(R.id.cardGhost);
        cardGhost.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to activate ghost mode
                if (user.ghostMode){
                    deactivateGhost();
                }
                else {
                    activateGhost();
                }
            }
        });

        final CardView cardTimeTrial = findViewById(R.id.cardTimeTrial);
        cardTimeTrial.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to activate Time Trial
                if (user.timeTrialMode){
                    deactivateTimeTrial();
                }
                else {
                    activateTimeTrial();
                }
            }
        });
    }

    //*******************************************
    //Validate ghost mode and if passes then set
    //user ghost mode to true and update UI
    //*******************************************
    public void activateGhost(){

        if (user.dayCoins != 0){
            Toast.makeText(getApplicationContext(), "You've Already Collected Coins Today", Toast.LENGTH_SHORT).show();
        }
        else if (user.ghostMode){
            Toast.makeText(getApplicationContext(), "Ghost Mode Is Already Active", Toast.LENGTH_SHORT).show();
        }
        else if (user.timeTrialMode){
            Toast.makeText(getApplicationContext(), "You Can't Have Ghost Mode and Time Trial Activated Simultaneously", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Activated Ghost Mode", Toast.LENGTH_SHORT).show();
            ghostText.setText("De-Activate Ghost Mode");
            user.ghostMode = true;
        }
    }

    //*******************************************
    //De-Activate ghost mode
    //*******************************************
    public void deactivateGhost(){
        Toast.makeText(getApplicationContext(), "Ghost Mode Has Been De-Activated", Toast.LENGTH_SHORT).show();
        user.ghostMode=false;
        ghostText.setText("Activate Ghost Mode");

    }

    //*******************************************
    //Validate time trial mode and if passes then set
    //user time trial mode to true and update UI
    //*******************************************
    public void activateTimeTrial(){

        if (user.ghostMode){
            Toast.makeText(getApplicationContext(), "You Can't Have Ghost Mode and Time Trial Activated Simultaneously", Toast.LENGTH_SHORT).show();
        }
        if (user.timeTrialMode) {
            Toast.makeText(getApplicationContext(), "Time Trial Mode Is Already Active", Toast.LENGTH_SHORT).show();
        }
        else {

            Toast.makeText(getApplicationContext(), "Activated Time Trial Mode", Toast.LENGTH_SHORT).show();
            user.timeTrialMode = true;
            timeTrialText.setText("De-Activate Time Trial Mode");

        }
    }

    //*******************************************
    //De-Activate time trial mode
    //*******************************************
    public void deactivateTimeTrial() {
        Toast.makeText(getApplicationContext(), "Time Trial Has Been De-Activated", Toast.LENGTH_SHORT).show();
        user.timeTrialMode=false;
        timeTrialText.setText("Activate Time Trial Mode");

    }
}
