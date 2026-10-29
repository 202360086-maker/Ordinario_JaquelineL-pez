package com.example.sensoresapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * BaseActivity = CLASE PADRE (abstracta) de todas las pantallas.
 * Aquí se programa UNA SOLA VEZ: el NavDrawer, la Toolbar y el botón Regresar.
 * Las demás Activities heredan (extends BaseActivity) → HERENCIA (POO).
 */
public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;     // Contenedor que permite el menú lateral
    protected NavigationView navigationView; // El menú lateral
    protected MaterialToolbar toolbar;       // La barra de arriba

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // La app se dibuja detrás de la barra de estado; íconos de la barra en claro
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));

        setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Usar nuestra Toolbar como barra de la app y ponerle el título
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getTituloPantalla());
        }

        // Meter el diseño propio de la pantalla hija dentro del "contenedor"
        FrameLayout contenedor = findViewById(R.id.contenedor);
        getLayoutInflater().inflate(getLayoutContenido(), contenedor, true);

        // ===== BOTÓN ☰ (ActionBarDrawerToggle) =====
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.abrir_menu, R.string.cerrar_menu);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.blanco));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // REFUERZO: al tocar ☰ abrimos/cerramos el menú directamente
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Esta clase recibe los clics de las opciones del menú
        navigationView.setNavigationItemSelectedListener(this);

        // ===== BOTÓN FLOTANTE "REGRESAR" =====
        ExtendedFloatingActionButton btnRegresar = findViewById(R.id.btnRegresar);
        if (this instanceof MainActivity) {
            // En Inicio no tiene sentido regresar, lo ocultamos
            btnRegresar.setVisibility(View.GONE);
        } else {
            // Espacio abajo para que el botón no tape la última tarjeta
            contenedor.setPadding(0, 0, 0, dpAPx(88));
            btnRegresar.setOnClickListener(v -> regresarAlInicio());
        }

        // ===== ESPACIO PARA LAS BARRAS DEL SISTEMA =====
        View contenidoPrincipal = findViewById(R.id.contenido_principal);
        View area = findViewById(R.id.area_contenido);
        ViewCompat.setOnApplyWindowInsetsListener(contenidoPrincipal, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, barras.top, 0, 0);       // espacio arriba (barra de estado)
            area.setPadding(0, 0, 0, barras.bottom); // espacio abajo (barra de navegación)
            return insets;
        });

        // ===== BOTÓN ATRÁS DEL TELÉFONO =====
        // Si el menú está abierto, primero lo cierra; si no, regresa normal
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Marca en el menú la opción de la pantalla actual
        navigationView.setCheckedItem(getIdMenu());
    }

    /** Se ejecuta al tocar una opción del menú lateral */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START); // Cerrar el menú

        // Si elige Inicio, regresamos limpiando las pantallas abiertas
        if (id == R.id.nav_inicio) {
            if (!(this instanceof MainActivity)) {
                regresarAlInicio();
            }
            return true;
        }

        // Se usa if/else (no switch) porque en Android Studio nuevo los R.id no son constantes
        Class<?> destino = null;
        if (id == R.id.nav_acelerometro) {
            destino = AcelerometroActivity.class;
        } else if (id == R.id.nav_giroscopio) {
            destino = GiroscopioActivity.class;
        } else if (id == R.id.nav_magnetometro) {
            destino = MagnetometroActivity.class;
        } else if (id == R.id.nav_gps) {
            destino = GpsActivity.class;
        }

        // Solo abre otra pantalla si no es la actual
        if (destino != null && destino != getClass()) {
            abrirPantalla(destino);
        }
        return true;
    }

    /**
     * Abre otra Activity. REORDER_TO_FRONT: si ya estaba abierta,
     * la trae al frente en vez de crear otra copia.
     */
    protected void abrirPantalla(Class<?> pantalla) {
        Intent intent = new Intent(this, pantalla);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    /**
     * Regresa a Inicio.
     * CLEAR_TOP = cierra todas las pantallas que estén encima de Inicio.
     * SINGLE_TOP = reutiliza la de Inicio en vez de crear otra (y no repite el splash).
     */
    protected void regresarAlInicio() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /** Convierte dp (medida de diseño) a píxeles reales de la pantalla */
    private int dpAPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ===== Métodos que CADA hija está obligada a escribir =====
    protected abstract int getLayoutContenido();   // Su layout XML
    protected abstract String getTituloPantalla(); // Su título en la Toolbar
    protected abstract int getIdMenu();            // Su opción del menú
}
