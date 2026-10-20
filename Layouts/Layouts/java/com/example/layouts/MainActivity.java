package com.example.layouts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Buscamos cada botón por su id
        Button btnLinear   = findViewById(R.id.btnLinear);
        Button btnRelative = findViewById(R.id.btnRelative);
        Button btnFrame    = findViewById(R.id.btnFrame);
        Button btnTable    = findViewById(R.id.btnTable);

        // 2. Al tocar cada botón, un Intent abre la Activity correspondiente
        btnLinear.setOnClickListener(v ->
                startActivity(new Intent(this, LinearActivity.class)));

        btnRelative.setOnClickListener(v ->
                startActivity(new Intent(this, RelativeActivity.class)));

        btnFrame.setOnClickListener(v ->
                startActivity(new Intent(this, FrameActivity.class)));

        btnTable.setOnClickListener(v ->
                startActivity(new Intent(this, TableActivity.class)));
    }
}
