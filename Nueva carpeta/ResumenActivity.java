package com.example.demointent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResumenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        // Recuperamos el Bundle completo con todos los Extras
        Bundle datos = getIntent().getExtras();
        String nombre = "-";
        int edad = 0;
        ArrayList<String> materias = new ArrayList<>();

        if (datos != null) {
            nombre = datos.getString("nombre", "-");
            edad = datos.getInt("edad", 0);
            ArrayList<String> lista = datos.getStringArrayList("materias");
            if (lista != null) materias = lista;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Alumno: ").append(nombre).append("\n");
        sb.append("Edad: ").append(edad).append("\n\nMaterias:\n");
        for (String m : materias) {
            sb.append("• ").append(m).append("\n");
        }

        TextView tvResumen = findViewById(R.id.tvResumen);
        tvResumen.setText(sb.toString());

        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> finish());
    }
}
