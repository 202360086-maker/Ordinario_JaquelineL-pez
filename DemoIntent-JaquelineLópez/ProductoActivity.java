package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProductoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        // Cada tipo de Extra se lee con su método get y un valor por defecto
        Intent intent = getIntent();
        String producto = intent.getStringExtra("EXTRA_PRODUCTO");
        int cantidad = intent.getIntExtra("EXTRA_CANTIDAD", 0);
        double precio = intent.getDoubleExtra("EXTRA_PRECIO", 0.0);
        boolean envioGratis = intent.getBooleanExtra("EXTRA_ENVIO_GRATIS", false);
        double total = cantidad * precio;

        String detalle = "Producto: " + producto + "\n"
                + "Cantidad: " + cantidad + "\n"
                + String.format(Locale.US, "Precio unitario: $%.2f\n", precio)
                + String.format(Locale.US, "Total: $%.2f\n", total)
                + "Envío gratis: " + (envioGratis ? "Sí" : "No");

        TextView tvDetalle = findViewById(R.id.tvDetalle);
        tvDetalle.setText(detalle);

        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> finish());
    }
}
