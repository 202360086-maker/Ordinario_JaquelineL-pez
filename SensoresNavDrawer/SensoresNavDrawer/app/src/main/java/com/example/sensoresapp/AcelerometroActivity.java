package com.example.sensoresapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * DEMO DEL ACELERÓMETRO
 * - Muestra la aceleración en X, Y, Z (m/s²).
 * - Mueve una bolita según la inclinación (nivel de burbuja).
 * - Detecta cuando se agita el teléfono.
 */
public class AcelerometroActivity extends BaseActivity implements SensorEventListener {

    // Si la aceleración total pasa de 15 m/s² lo contamos como "agitación" (quieto ≈ 9.81)
    private static final float UMBRAL_AGITACION = 15f;
    private static final long TIEMPO_ENTRE_AGITACIONES = 1000; // milisegundos

    private SensorManager sensorManager;
    private Sensor acelerometro;

    private TextView tvX, tvY, tvZ, tvMagnitud, tvEstado, tvContador;
    private FrameLayout zona; // Caja donde se mueve la bolita
    private View bola;

    private int contadorAgitaciones = 0;
    private long ultimaAgitacion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvX = findViewById(R.id.tvX);
        tvY = findViewById(R.id.tvY);
        tvZ = findViewById(R.id.tvZ);
        tvMagnitud = findViewById(R.id.tvMagnitud);
        tvEstado = findViewById(R.id.tvEstado);
        tvContador = findViewById(R.id.tvContador);
        zona = findViewById(R.id.zona);
        bola = findViewById(R.id.bola);

        // 1) Obtener el SensorManager  2) Pedir el acelerómetro (null si no existe)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (acelerometro == null) {
            tvEstado.setText("Este dispositivo no tiene acelerómetro");
        }
    }

    // 3) REGISTRAR el sensor cuando la pantalla está visible
    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // 4) QUITAR el registro al salir, para ahorrar batería
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /** Se llama cada vez que el sensor manda un nuevo dato */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        tvX.setText(String.format(Locale.getDefault(), "X: %6.2f m/s²", x));
        tvY.setText(String.format(Locale.getDefault(), "Y: %6.2f m/s²", y));
        tvZ.setText(String.format(Locale.getDefault(), "Z: %6.2f m/s²", z));

        // Magnitud = raíz de (x² + y² + z²)  → Pitágoras en 3D
        double magnitud = Math.sqrt(x * x + y * y + z * z);
        tvMagnitud.setText(String.format(Locale.getDefault(), "Total: %6.2f m/s²", magnitud));

        moverBola(x, y);
        detectarAgitacion(magnitud);
    }

    /** Mueve la bolita según la inclinación (dividimos entre 9.81 para tener -1 a 1) */
    private void moverBola(float x, float y) {
        float maxX = (zona.getWidth() - bola.getWidth()) / 2f;
        float maxY = (zona.getHeight() - bola.getHeight()) / 2f;

        float posX = -x / SensorManager.GRAVITY_EARTH * maxX;
        float posY = y / SensorManager.GRAVITY_EARTH * maxY;

        bola.setTranslationX(limitar(posX, maxX));
        bola.setTranslationY(limitar(posY, maxY));
    }

    /** Evita que la bolita se salga de la caja */
    private float limitar(float valor, float max) {
        return Math.max(-max, Math.min(max, valor));
    }

    /** Si la aceleración es muy fuerte, cuenta una agitación */
    private void detectarAgitacion(double magnitud) {
        long ahora = System.currentTimeMillis();
        if (magnitud > UMBRAL_AGITACION && ahora - ultimaAgitacion > TIEMPO_ENTRE_AGITACIONES) {
            ultimaAgitacion = ahora;
            contadorAgitaciones++;
            tvEstado.setText("¡Agitaste el teléfono!");
            tvContador.setText("Agitaciones: " + contadorAgitaciones);
        } else if (ahora - ultimaAgitacion > 1500) {
            tvEstado.setText("Agita el teléfono...");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override protected int getLayoutContenido() { return R.layout.activity_acelerometro; }
    @Override protected String getTituloPantalla() { return "Acelerómetro"; }
    @Override protected int getIdMenu() { return R.id.nav_acelerometro; }
}
