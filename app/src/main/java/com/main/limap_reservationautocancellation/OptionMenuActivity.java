package com.main.limap_reservationautocancellation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class OptionMenuActivity extends AppCompatActivity {

    private static final String TAG = "OptionMenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.header_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                Log.d(TAG, "logout");
                return true;
            case R.id.profile:
                Log.d(TAG, "profile");
                return true;
            case R.id.map:
                Log.d(TAG, "map");
                return true;
            case R.id.parkinghistory:
                Log.d(TAG, "parkinghistory");
                return true;
            case R.id.paymentinfo:
                Log.d(TAG, "paymentinfo");
                return true;
            case R.id.feedback:
                Log.d(TAG, "feedback");
                return true;
            case android.R.id.home:
                Log.d(TAG, "home");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}