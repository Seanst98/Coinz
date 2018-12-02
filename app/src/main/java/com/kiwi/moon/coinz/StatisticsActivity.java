package com.kiwi.moon.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StatisticsActivity extends AppCompatActivity {

    private User user = User.getinstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);


        //*******************************************
        //Set the text fields with the user's statistics
        //*******************************************
        TextView distanceWalkedDay;
        TextView coinsCollectedDay;
        TextView bankGold;
        TextView distanceWalked;
        TextView coinsCollected;
        TextView dolr;
        TextView shil;
        TextView peny;
        TextView quid;

        distanceWalkedDay = findViewById(R.id.distanceWalkedDaytxt);
        coinsCollectedDay = findViewById(R.id.coinsCollectedDaytxt);
        bankGold = findViewById(R.id.bankGoldtxt);
        distanceWalked = findViewById(R.id.distanceWalkedtxt);
        coinsCollected = findViewById(R.id.coinsCollectedtxt);
        dolr = findViewById(R.id.dolrtxt);
        shil = findViewById(R.id.shiltxt);
        peny = findViewById(R.id.penytxt);
        quid = findViewById(R.id.quidtxt);


        distanceWalkedDay.setText("You Have Walked " + user.dayWalked + " Metres Today");
        coinsCollectedDay.setText("You Have Collected " + user.dayCoins + " Coins Today");
        bankGold.setText("You Have " + user.bankGold + " GOLD In The Bank");
        distanceWalked.setText("In Total, You Have Walked " + user.totalWalked + " Metres");
        coinsCollected.setText("In Total, You Have Collected " + user.totalCoins + " Coins");
        dolr.setText("In Total, You Have Collected " + user.dolrCoins + " DOLR Coins Which Equals " + user.dolr + " of DOLR");
        shil.setText("In Total, You Have Collected " + user.shilCoins + " SHIL Coins Which Equals " + user.shil + " of SHIL");
        peny.setText("In Total, You Have Collected " + user.penyCoins + " PENY Coins Which Equals " + user.peny + " of PENY");
        quid.setText("In Total, You Have Collected " + user.quidCoins + " QUID Coins Which Equals " + user.quid + " of QUID");

    }
}
