package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText etNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNombre = findViewById(R.id.etNombre);
        Button btnPerfil = findViewById(R.id.btnPerfil);
        Button btnProducto = findViewById(R.id.btnProducto);
        Button btnResumen = findViewById(R.id.btnResumen);

        // Botón 1: Intent explícito + Extra de tipo String
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
            intent.putExtra("EXTRA_NOMBRE", obtenerNombre());
            startActivity(intent);
        });

        // Botón 2: Intent explícito + Extras de varios tipos
        btnProducto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductoActivity.class);
            intent.putExtra("EXTRA_PRODUCTO", "Audífonos inalámbricos");
            intent.putExtra("EXTRA_CANTIDAD", 2);
            intent.putExtra("EXTRA_PRECIO", 549.90);
            intent.putExtra("EXTRA_ENVIO_GRATIS", true);
            startActivity(intent);
        });

        // Botón 3: Intent explícito + Extras agrupados en un Bundle
        btnResumen.setOnClickListener(v -> {
            Bundle datos = new Bundle();
            datos.putString("nombre", obtenerNombre());
            datos.putInt("edad", 21);
            datos.putStringArrayList("materias",
                    new ArrayList<>(Arrays.asList("Móviles", "SOA", "Ciberseguridad")));

            Intent intent = new Intent(MainActivity.this, ResumenActivity.class);
            intent.putExtras(datos);
            startActivity(intent);
        });
    }

    private String obtenerNombre() {
        String nombre = etNombre.getText().toString().trim();
        return nombre.isEmpty() ? "Invitado" : nombre;
    }
}
