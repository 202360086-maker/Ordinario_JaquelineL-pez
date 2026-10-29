package com.example.sensoresapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

/**
 * DEMO DEL GIROSCOPIO
 * - Muestra la velocidad de giro en X, Y, Z (rad/s y °/s).
 * - Calcula cuántos grados giró el teléfono en el eje Z (INTEGRACIÓN).
 */
public class GiroscopioActivity extends BaseActivity implements SensorEventListener {

    // El timestamp del sensor viene en NANOsegundos; esto lo convierte a segundos
    private static final float NS_A_SEGUNDOS = 1.0f / 1_000_000_000.0f;
    private static final float UMBRAL_MOVIMIENTO = 0.5f; // rad/s (filtra el ruido)

    private SensorManager sensorManager;
    private Sensor giroscopio;

    private TextView tvX, tvY, tvZ, tvMovimiento, tvAngulo;
    private ImageView imgGiro;

    private long ultimoTimestamp = 0; // Momento del dato anterior
    private float anguloZ = 0;        // Grados acumulados en el eje Z

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvX = findViewById(R.id.tvX);
        tvY = findViewById(R.id.tvY);
        tvZ = findViewById(R.id.tvZ);
        tvMovimiento = findViewById(R.id.tvMovimiento);
        tvAngulo = findViewById(R.id.tvAngulo);
        imgGiro = findViewById(R.id.imgGiro);

        // Botón para regresar el ángulo a 0°
        findViewById(R.id.btnReiniciar).setOnClickListener(v -> {
            anguloZ = 0;
            imgGiro.setRotation(0);
            tvAngulo.setText("Rotación: 0.0°");
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (giroscopio == null) {
            tvMovimiento.setText("Este dispositivo no tiene giroscopio");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (giroscopio != null) {
            sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        ultimoTimestamp = 0; // Al volver, empezamos a medir el tiempo de nuevo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Velocidad angular en radianes por segundo
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Math.toDegrees convierte radianes a grados (1 rad ≈ 57.3°)
        tvX.setText(String.format(Locale.getDefault(), "X: %6.2f rad/s (%6.1f °/s)", x, Math.toDegrees(x)));
        tvY.setText(String.format(Locale.getDefault(), "Y: %6.2f rad/s (%6.1f °/s)", y, Math.toDegrees(y)));
        tvZ.setText(String.format(Locale.getDefault(), "Z: %6.2f rad/s (%6.1f °/s)", z, Math.toDegrees(z)));

        // ===== INTEGRACIÓN: ángulo = velocidad × tiempo =====
        if (ultimoTimestamp != 0) {
            float dt = (event.timestamp - ultimoTimestamp) * NS_A_SEGUNDOS;
            anguloZ += (float) Math.toDegrees(z * dt);
        }
        ultimoTimestamp = event.timestamp;

        // En Android, rotación positiva = horario, por eso el signo -
        imgGiro.setRotation(-anguloZ);
        tvAngulo.setText(String.format(Locale.getDefault(), "Rotación: %.1f°", anguloZ));

        describirMovimiento(x, y, z);
    }

    /** Dice en palabras hacia dónde se está girando */
    private void describirMovimiento(float x, float y, float z) {
        String texto;
        if (Math.abs(z) > UMBRAL_MOVIMIENTO) {
            texto = z > 0 ? "↺ Girando a la izquierda (eje Z)" : "↻ Girando a la derecha (eje Z)";
        } else if (Math.abs(x) > UMBRAL_MOVIMIENTO) {
            texto = "Inclinando adelante/atrás (eje X)";
        } else if (Math.abs(y) > UMBRAL_MOVIMIENTO) {
            texto = "Inclinando a los lados (eje Y)";
        } else {
            texto = "Quieto";
        }
        tvMovimiento.setText(texto);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override protected int getLayoutContenido() { return R.layout.activity_giroscopio; }
    @Override protected String getTituloPantalla() { return "Giroscopio"; }
    @Override protected int getIdMenu() { return R.id.nav_giroscopio; }
}
