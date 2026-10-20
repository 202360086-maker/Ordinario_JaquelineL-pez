package com.example.layouts;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LinearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Muestra el diseño activity_linear.xml
        setContentView(R.layout.activity_linear);
    }
}
