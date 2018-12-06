package com.kiwi.moon.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Set the buttons to take the user to the login screen or create account screen
        final Button loginButton = findViewById(R.id.loginButton);

        final Button createAcc = findViewById(R.id.createAccButton);


        //When the login button is pressed
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Start Activity to the login screen
                Intent myIntent = new Intent(MainActivity.this, loginActivity.class);
                startActivity(myIntent);
            }
        });


        //When the create account button is pressed
        createAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Start Activity to the create account screen
                Intent myIntent = new Intent(MainActivity.this, createAccActivity.class);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
