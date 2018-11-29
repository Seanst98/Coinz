package com.kiwi.moon.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;

public class OptionalFeaturesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optional_features);

        //If Ghost card is pressed
        final CardView cardGhost = findViewById(R.id.cardGhost);
        cardGhost.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to activate ghost mode
                activateGhost();
            }
        });

        final CardView cardTimeTrial = findViewById(R.id.cardTimeTrial);
        cardTimeTrial.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to activate Time Trial
                activateTimeTrial();
            }
        });
    }

    public void activateTimeTrial(){

    }

    public void activateGhost(){

    }
}
