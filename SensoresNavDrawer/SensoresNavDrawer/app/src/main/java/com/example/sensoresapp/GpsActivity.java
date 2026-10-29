package com.example.sensoresapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DEMO DEL GPS
 * El GPS NO usa SensorManager, usa LocationManager.
 * Además necesita PERMISO DE UBICACIÓN pedido en tiempo de ejecución.
 */
public class GpsActivity extends BaseActivity {

    private LocationManager locationManager;
    private TextView tvEstado, tvLat, tvLon, tvAlt, tvPrecision, tvVelocidad, tvHora;
    private Button btnActivarGps, btnMapa;
    private Location ultimaUbicacion; // Para abrir el mapa

    /** Lanzador moderno para PEDIR PERMISOS y recibir la respuesta */
    private final ActivityResultLauncher<String[]> lanzadorPermisos =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    resultados -> {
                        if (tienePermiso()) {
                            iniciarGps();
                        } else {
                            tvEstado.setText("Sin permiso de ubicación PRECISA no se puede usar el GPS.");
                        }
                    });

    /** LocationListener = "escucha" las ubicaciones que manda el GPS */
    private final LocationListener listenerUbicacion = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mostrarUbicacion(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            tvEstado.setText("GPS activado, buscando señal...");
            btnActivarGps.setVisibility(View.GONE);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            tvEstado.setText("El GPS se apagó.");
            btnActivarGps.setVisibility(View.VISIBLE);
        }

        // Obligatorio en Android viejos (antes de Android 10)
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvEstado = findViewById(R.id.tvEstado);
        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        tvAlt = findViewById(R.id.tvAlt);
        tvPrecision = findViewById(R.id.tvPrecision);
        tvVelocidad = findViewById(R.id.tvVelocidad);
        tvHora = findViewById(R.id.tvHora);
        btnActivarGps = findViewById(R.id.btnActivarGps);
        btnMapa = findViewById(R.id.btnMapa);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Abre los ajustes del teléfono para encender la ubicación
        btnActivarGps.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

        btnMapa.setOnClickListener(v -> abrirMapa());

        // Si no tenemos permiso, lo pedimos (aparece el cuadro de diálogo)
        if (!tienePermiso()) {
            lanzadorPermisos.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tienePermiso()) {
            iniciarGps();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dejar de pedir ubicaciones al salir (el GPS gasta mucha batería)
        locationManager.removeUpdates(listenerUbicacion);
    }

    /** Revisa si el usuario ya dio permiso de ubicación precisa */
    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Empieza a recibir ubicaciones del GPS */
    @SuppressLint("MissingPermission") // Ya revisamos el permiso con tienePermiso()
    private void iniciarGps() {
        if (!tienePermiso()) return;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvEstado.setText("El GPS está apagado. Actívalo para continuar.");
            btnActivarGps.setVisibility(View.VISIBLE);
            return;
        }

        btnActivarGps.setVisibility(View.GONE);
        tvEstado.setText("Buscando señal GPS...");

        // Proveedor GPS, cada 2000 ms o si se movió 1 metro
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000, 1, listenerUbicacion);

        Location ultima = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (ultima != null) {
            mostrarUbicacion(ultima);
        }
    }

    /** Escribe en pantalla los datos de la ubicación */
    private void mostrarUbicacion(Location location) {
        ultimaUbicacion = location;
        tvEstado.setText("Ubicación recibida");

        tvLat.setText(String.format(Locale.getDefault(), "Latitud:  %.6f", location.getLatitude()));
        tvLon.setText(String.format(Locale.getDefault(), "Longitud: %.6f", location.getLongitude()));

        if (location.hasAltitude()) {
            tvAlt.setText(String.format(Locale.getDefault(), "Altitud: %.1f m", location.getAltitude()));
        } else {
            tvAlt.setText("Altitud: no disponible");
        }

        tvPrecision.setText(String.format(Locale.getDefault(), "Precisión: ± %.1f m", location.getAccuracy()));
        // getSpeed() viene en m/s; × 3.6 = km/h
        tvVelocidad.setText(String.format(Locale.getDefault(), "Velocidad: %.1f km/h", location.getSpeed() * 3.6f));

        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        tvHora.setText("Hora del dato: " + formato.format(new Date(location.getTime())));

        btnMapa.setEnabled(true);
    }

    /** Abre Google Maps con la ubicación actual (Intent implícito) */
    private void abrirMapa() {
        if (ultimaUbicacion == null) return;
        String url = "https://www.google.com/maps/search/?api=1&query="
                + ultimaUbicacion.getLatitude() + "," + ultimaUbicacion.getLongitude();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override protected int getLayoutContenido() { return R.layout.activity_gps; }
    @Override protected String getTituloPantalla() { return "GPS"; }
    @Override protected int getIdMenu() { return R.id.nav_gps; }
}
