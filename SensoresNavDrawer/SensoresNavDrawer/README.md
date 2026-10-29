# Sensores y NavDrawer en Android

Actividad de Aplicaciones Móviles — Jaqueline López Alvarez

App en **Java (Android Studio)** que muestra una demo de 4 sensores, cada uno en su propia Activity, conectadas con un **NavDrawer** (menú lateral) y con **SplashScreen** al iniciar.

## Funciones
| Pantalla | Qué hace |
|---|---|
| **Inicio (Bento)** | Resumen en vivo de los 4 sensores: brújula, barras del acelerómetro, velocidad del giroscopio y coordenadas GPS |
| **Acelerómetro** | Valores X, Y, Z en m/s², nivel de burbuja y detector de agitación |
| **Giroscopio** | Velocidad de giro en rad/s y °/s, y rotación acumulada (integración) |
| **Magnetómetro** | Campo magnético en µT, aviso de interferencia y brújula |
| **GPS** | Latitud, longitud, altitud, precisión, velocidad y botón para abrir Google Maps |

## Conceptos usados
- `SensorManager`, `Sensor`, `SensorEventListener`, `SensorEvent`
- Registro en `onResume()` y liberación en `onPause()` para ahorrar batería
- `LocationManager` + `LocationListener` y permisos en tiempo de ejecución
- `DrawerLayout`, `NavigationView` y `ActionBarDrawerToggle`
- `BaseActivity` abstracta (herencia) para no repetir el menú
- API oficial **SplashScreen** (`androidx.core:core-splashscreen`)

## Cómo probar en el emulador
⋮ → **Extended Controls** → **Virtual sensors → Device Pose** (mover el teléfono) y **Location** (cambiar ubicación).
