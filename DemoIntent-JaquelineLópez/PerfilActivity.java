package com.example.demointent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Recuperamos el Extra que mandó MainActivity
        String nombre = getIntent().getStringExtra("EXTRA_NOMBRE");
        if (nombre == null) nombre = "Sin nombre";

        TextView tvSaludo = findViewById(R.id.tvSaludo);
        tvSaludo.setText("¡Hola, " + nombre + "!\nBienvenid@ a tu perfil.");

        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> finish());
    }
}
