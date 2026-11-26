package com.example.fragmentado; // usa el paquete de tu proyecto

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Button btnFragmento1, btnFragmento2, btnFragmento3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFragmento1 = findViewById(R.id.btnFragmento1);
        btnFragmento2 = findViewById(R.id.btnFragmento2);
        btnFragmento3 = findViewById(R.id.btnFragmento3);

        // Cada botón le pide al FragmentManager mostrar su fragmento
        btnFragmento1.setOnClickListener(v -> mostrarFragmento(new Fragmento1()));
        btnFragmento2.setOnClickListener(v -> mostrarFragmento(new Fragmento2()));
        btnFragmento3.setOnClickListener(v -> mostrarFragmento(new Fragmento3()));

        // Al abrir la app por primera vez, mostramos el Fragmento 1
        if (savedInstanceState == null) {
            mostrarFragmento(new Fragmento1());
        }
    }

    private void mostrarFragmento(Fragment fragmento) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) // desvanecido suave
                .replace(R.id.contenedor, fragmento)
                .commit();
    }
}