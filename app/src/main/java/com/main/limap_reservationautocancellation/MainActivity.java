package com.main.limap_reservationautocancellation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnGotoMap, btnGotoAutoCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGotoMap = findViewById(R.id.btnGotoMap);
        btnGotoMap.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MapActivity.class));
        });

        btnGotoAutoCancel = findViewById(R.id.btnGotoAutoCancel);
        btnGotoAutoCancel.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AutoCancellationActivity.class));
        });
    }
}