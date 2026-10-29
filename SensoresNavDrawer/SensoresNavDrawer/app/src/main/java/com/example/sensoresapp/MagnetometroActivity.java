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
 * DEMO DEL MAGNETÓMETRO
 * - Muestra el campo magnético en X, Y, Z (µT).
 * - Avisa si hay un imán o metal cerca.
 * - Hace una BRÚJULA combinando magnetómetro + acelerómetro.
 */
public class MagnetometroActivity extends BaseActivity implements SensorEventListener {

    // Filtro paso bajo: 90% valor anterior + 10% valor nuevo → la aguja no tiembla
    private static final float ALFA = 0.9f;
    private static final String[] DIRECCIONES =
            {"Norte", "Noreste", "Este", "Sureste", "Sur", "Suroeste", "Oeste", "Noroeste"};

    private SensorManager sensorManager;
    private Sensor magnetometro;
    private Sensor acelerometro; // Necesario para saber la inclinación del teléfono

    private TextView tvX, tvY, tvZ, tvMagnitud, tvInterferencia, tvRumbo;
    private ImageView imgBrujula;

    private final float[] gravedad = new float[3];
    private final float[] geomagnetico = new float[3];
    private boolean hayGravedad = false;
    private boolean hayMagnetico = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvX = findViewById(R.id.tvX);
        tvY = findViewById(R.id.tvY);
        tvZ = findViewById(R.id.tvZ);
        tvMagnitud = findViewById(R.id.tvMagnitud);
        tvInterferencia = findViewById(R.id.tvInterferencia);
        tvRumbo = findViewById(R.id.tvRumbo);
        imgBrujula = findViewById(R.id.imgBrujula);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (magnetometro == null) {
            tvInterferencia.setText("Este dispositivo no tiene magnetómetro");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registramos LOS DOS sensores con el mismo listener (this)
        if (magnetometro != null) {
            sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_UI);
        }
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); // Quita los dos
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Como escuchamos 2 sensores, revisamos de cuál vino el dato
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            filtrar(event.values, gravedad, hayGravedad);
            hayGravedad = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tvX.setText(String.format(Locale.getDefault(), "X: %7.2f µT", x));
            tvY.setText(String.format(Locale.getDefault(), "Y: %7.2f µT", y));
            tvZ.setText(String.format(Locale.getDefault(), "Z: %7.2f µT", z));

            double magnitud = Math.sqrt(x * x + y * y + z * z);
            tvMagnitud.setText(String.format(Locale.getDefault(), "Total: %7.2f µT", magnitud));

            // Campo de la Tierra ≈ 25 a 65 µT. Fuera de eso hay imán o metal cerca
            if (magnitud > 70 || magnitud < 20) {
                tvInterferencia.setText("Interferencia: hay un imán o metal cerca");
            } else {
                tvInterferencia.setText("Campo magnético normal (Tierra)");
            }

            filtrar(event.values, geomagnetico, hayMagnetico);
            hayMagnetico = true;
        }

        if (hayGravedad && hayMagnetico) {
            actualizarBrujula();
        }
    }

    /** FILTRO PASO BAJO: suaviza los datos. La primera vez solo copia. */
    private void filtrar(float[] nuevos, float[] guardados, boolean yaHabia) {
        for (int i = 0; i < 3; i++) {
            guardados[i] = yaHabia ? ALFA * guardados[i] + (1 - ALFA) * nuevos[i] : nuevos[i];
        }
    }

    /**
     * getRotationMatrix: combina gravedad + campo magnético → matriz de rotación.
     * getOrientation: saca 3 ángulos; el [0] es el azimut (ángulo respecto al norte).
     */
    private void actualizarBrujula() {
        float[] matrizRotacion = new float[9];
        float[] matrizInclinacion = new float[9];

        boolean ok = SensorManager.getRotationMatrix(matrizRotacion, matrizInclinacion,
                gravedad, geomagnetico);

        if (ok) {
            float[] orientacion = new float[3];
            SensorManager.getOrientation(matrizRotacion, orientacion);

            float azimut = (float) Math.toDegrees(orientacion[0]);
            azimut = (azimut + 360) % 360;

            // Giramos la brújula al revés del teléfono para que la aguja quede al norte
            imgBrujula.setRotation(-azimut);

            int indice = Math.round(azimut / 45f) % 8; // 360 / 8 = 45° cada dirección
            tvRumbo.setText(String.format(Locale.getDefault(), "Rumbo: %.0f° %s", azimut, DIRECCIONES[indice]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override protected int getLayoutContenido() { return R.layout.activity_magnetometro; }
    @Override protected String getTituloPantalla() { return "Magnetómetro"; }
    @Override protected int getIdMenu() { return R.id.nav_magnetometro; }
}
