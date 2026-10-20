package com.example.layouts;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class FrameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Muestra el diseño activity_frame.xml
        setContentView(R.layout.activity_frame);
    }
}
