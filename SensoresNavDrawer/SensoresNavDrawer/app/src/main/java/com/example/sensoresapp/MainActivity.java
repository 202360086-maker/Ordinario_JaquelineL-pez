package com.example.sensoresapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * PANTALLA DE INICIO (estilo Bento).
 * 1) Muestra la SplashScreen.
 * 2) Escucha los 3 sensores + GPS para mostrar un RESUMEN EN VIVO en cada tarjeta.
 * 3) Cada tarjeta abre la pantalla de detalle de su sensor.
 *
 * UN solo listener (this) recibe datos de los 3 sensores
 * y los distinguimos con event.sensor.getType().
 */
public class MainActivity extends BaseActivity implements SensorEventListener {

    // Puntos cardinales abreviados (cada uno ocupa 45°)
    private static final String[] PUNTOS = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};
    private static final float ALFA = 0.9f; // Filtro paso bajo para suavizar la brújula

    private boolean cargando = true; // Controla cuánto dura el splash

    private SensorManager sensorManager;
    private Sensor acelerometro, giroscopio, magnetometro;
    private LocationManager locationManager;

    // Vistas de las tarjetas
    private ImageView imgBrujulaMini;
    private TextView tvRumboMini, tvMagMini, tvAcelMini, tvGiroMini, tvGpsMini, tvDisponibles;
    private LinearProgressIndicator progresoGiro;

    // Mini gráfica de barras: 5 barras con los últimos 5 valores
    private final View[] barras = new View[5];
    private final float[] historial = new float[5];
    private long ultimaBarra = 0;

    // Datos para calcular la brújula
    private final float[] gravedad = new float[3];
    private final float[] geomagnetico = new float[3];
    private boolean hayGravedad = false, hayMagnetico = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ===== SPLASH (siempre ANTES de super.onCreate) =====
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> cargando);
        new Handler(Looper.getMainLooper()).postDelayed(() -> cargando = false, 2000);

        // ===== Conectar vistas =====
        imgBrujulaMini = findViewById(R.id.imgBrujulaMini);
        tvRumboMini = findViewById(R.id.tvRumboMini);
        tvMagMini = findViewById(R.id.tvMagMini);
        tvAcelMini = findViewById(R.id.tvAcelMini);
        tvGiroMini = findViewById(R.id.tvGiroMini);
        tvGpsMini = findViewById(R.id.tvGpsMini);
        tvDisponibles = findViewById(R.id.tvDisponibles);
        progresoGiro = findViewById(R.id.progresoGiro);
        barras[0] = findViewById(R.id.barra1);
        barras[1] = findViewById(R.id.barra2);
        barras[2] = findViewById(R.id.barra3);
        barras[3] = findViewById(R.id.barra4);
        barras[4] = findViewById(R.id.barra5);

        // ===== Obtener sensores (null = el teléfono no lo tiene) =====
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // ===== Contar cuántos sensores hay =====
        int disponibles = 0;
        if (acelerometro != null) disponibles++; else tvAcelMini.setText("No disponible");
        if (giroscopio != null) disponibles++; else tvGiroMini.setText("--");
        if (magnetometro != null) disponibles++; else tvMagMini.setText("No disponible");
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            disponibles++;
        } else {
            tvGpsMini.setText("No disponible");
        }
        tvDisponibles.setText(disponibles + " de 4 sensores disponibles");

        // ===== Cada tarjeta abre su pantalla de detalle =====
        findViewById(R.id.cardAcel).setOnClickListener(v -> abrirPantalla(AcelerometroActivity.class));
        findViewById(R.id.cardGiro).setOnClickListener(v -> abrirPantalla(GiroscopioActivity.class));
        findViewById(R.id.cardMag).setOnClickListener(v -> abrirPantalla(MagnetometroActivity.class));
        findViewById(R.id.cardGps).setOnClickListener(v -> abrirPantalla(GpsActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar los 3 sensores con el MISMO listener (this)
        if (acelerometro != null) sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
        if (giroscopio != null) sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_UI);
        if (magnetometro != null) sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_UI);
        iniciarGpsMini();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Al salir de Inicio dejamos de escuchar todo (ahorro de batería)
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(listenerGps);
    }

    /** Llega un dato de CUALQUIERA de los 3 sensores; revisamos de cuál es */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int tipo = event.sensor.getType();

        if (tipo == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0], y = event.values[1], z = event.values[2];
            float magnitud = (float) Math.sqrt(x * x + y * y + z * z);
            tvAcelMini.setText(String.format(Locale.getDefault(), "%.2f m/s²", magnitud));
            actualizarBarras(magnitud);

            // También lo guardamos para la brújula
            filtrar(event.values, gravedad, hayGravedad);
            hayGravedad = true;

        } else if (tipo == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0], y = event.values[1], z = event.values[2];
            float omega = (float) Math.sqrt(x * x + y * y + z * z); // Velocidad total de giro
            tvGiroMini.setText(String.format(Locale.getDefault(), "%.2f", omega));
            int porcentaje = (int) Math.min(100, omega / 5f * 100); // 5 rad/s = barra llena
            progresoGiro.setProgressCompat(porcentaje, true);

        } else if (tipo == Sensor.TYPE_MAGNETIC_FIELD) {
            float x = event.values[0], y = event.values[1], z = event.values[2];
            float magnitud = (float) Math.sqrt(x * x + y * y + z * z);
            // Campo de la Tierra: 25 a 65 µT. Fuera de eso hay interferencia
            String estado = (magnitud > 70 || magnitud < 20) ? "interferencia" : "normal";
            tvMagMini.setText(String.format(Locale.getDefault(), "%.0f µT · %s", magnitud, estado));

            filtrar(event.values, geomagnetico, hayMagnetico);
            hayMagnetico = true;
        }

        // Con acelerómetro + magnetómetro listos, calculamos la brújula
        if (hayGravedad && hayMagnetico) {
            actualizarBrujula();
        }
    }

    /**
     * Mini gráfica: recorre los valores a la izquierda y agrega el nuevo al final.
     * Se actualiza cada 150 ms.
     */
    private void actualizarBarras(float magnitud) {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaBarra < 150) return;
        ultimaBarra = ahora;

        System.arraycopy(historial, 1, historial, 0, historial.length - 1);
        historial[historial.length - 1] = magnitud;

        float alturaMax = 44 * getResources().getDisplayMetrics().density; // 44dp en píxeles
        for (int i = 0; i < barras.length; i++) {
            // 20 m/s² = barra llena. Mínimo 15% para que siempre se vea algo
            float proporcion = Math.max(0.15f, Math.min(1f, historial[i] / 20f));
            ViewGroup.LayoutParams parametros = barras[i].getLayoutParams();
            parametros.height = Math.round(proporcion * alturaMax);
            barras[i].setLayoutParams(parametros);
        }
    }

    /** Filtro paso bajo: suaviza los datos para que la brújula no tiemble */
    private void filtrar(float[] nuevos, float[] guardados, boolean yaHabia) {
        for (int i = 0; i < 3; i++) {
            guardados[i] = yaHabia ? ALFA * guardados[i] + (1 - ALFA) * nuevos[i] : nuevos[i];
        }
    }

    /** Calcula hacia dónde apunta el teléfono y gira la brújula */
    private void actualizarBrujula() {
        float[] rotacion = new float[9];
        float[] inclinacion = new float[9];
        if (SensorManager.getRotationMatrix(rotacion, inclinacion, gravedad, geomagnetico)) {
            float[] orientacion = new float[3];
            SensorManager.getOrientation(rotacion, orientacion);
            // orientacion[0] = azimut en radianes → lo pasamos a 0°–360°
            float azimut = ((float) Math.toDegrees(orientacion[0]) + 360) % 360;
            imgBrujulaMini.setRotation(-azimut);
            int indice = Math.round(azimut / 45f) % 8;
            tvRumboMini.setText(String.format(Locale.getDefault(), "%.0f° %s", azimut, PUNTOS[indice]));
        }
    }

    // ================== GPS (resumen) ==================

    private final LocationListener listenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mostrarGpsMini(location);
        }
        @Override public void onProviderEnabled(@NonNull String provider) { }
        @Override public void onProviderDisabled(@NonNull String provider) {
            tvGpsMini.setText("Ubicación apagada");
        }
        // Necesario en Android viejos
        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    /**
     * En Inicio NO pedimos el permiso (eso lo hace la pantalla GPS).
     * Si ya lo tenemos, mostramos las coordenadas; si no, invitamos a tocar la tarjeta.
     */
    @SuppressLint("MissingPermission") // El permiso se revisa en la primera línea
    private void iniciarGpsMini() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            tvGpsMini.setText("Toca para activar");
            return;
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvGpsMini.setText("Ubicación apagada");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, listenerGps);
        Location ultima = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (ultima != null) mostrarGpsMini(ultima);
    }

    private void mostrarGpsMini(Location location) {
        // Locale.US para que los decimales siempre usen punto
        tvGpsMini.setText(String.format(Locale.US, "%.4f, %.4f",
                location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // ===== Datos que pide BaseActivity =====
    @Override protected int getLayoutContenido() { return R.layout.activity_main; }
    @Override protected String getTituloPantalla() { return "Inicio"; }
    @Override protected int getIdMenu() { return R.id.nav_inicio; }
}
