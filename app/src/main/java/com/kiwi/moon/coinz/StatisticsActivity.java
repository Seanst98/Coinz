package com.kiwi.moon.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class StatisticsActivity extends AppCompatActivity {

    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String TAG = "mapActivity";

    private TextView distanceWalkedDay;
    private TextView coinsCollectedDay;
    private TextView bankGold;
    private TextView distanceWalked;
    private TextView coinsCollected;
    private TextView dolr;
    private TextView shil;
    private TextView peny;
    private TextView quid;

    private User user = User.getinstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        distanceWalkedDay = (TextView) findViewById(R.id.distanceWalkedDaytxt);
        coinsCollectedDay = (TextView) findViewById(R.id.coinsCollectedDaytxt);
        bankGold = (TextView) findViewById(R.id.bankGoldtxt);
        distanceWalked = (TextView) findViewById(R.id.distanceWalkedtxt);
        coinsCollected = (TextView) findViewById(R.id.coinsCollectedtxt);
        dolr = (TextView) findViewById(R.id.dolrtxt);
        shil = (TextView) findViewById(R.id.shiltxt);
        peny = (TextView) findViewById(R.id.penytxt);
        quid = (TextView) findViewById(R.id.quidtxt);


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
