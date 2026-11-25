# Topohit · App Android

Juego de golpear topos para tablet Android (en horizontal), para niños de 5 años, hecho en **Kotlin + layouts XML**
siguiendo lo que se enseña en clase (módulo 0489). Las normas de código están en [`REGLAS_CODIGO.md`](REGLAS_CODIGO.md).

## Desde el 24/11/2026: solo modo Infinito

- **¡JUGAR!** lleva directamente a elegir la dificultad de **Infinito**: Fácil, Normal o Difícil.
- Las dificultades se abren **en cadena**: Normal se abre al conseguir X puntos en Fácil y Difícil al conseguir Y puntos
  en Normal. Mientras tanto salen con un **candado** y debajo «Consigue X puntos en Fácil para desbloquearla».
- El **tutor** (escritorio) puede bloquear una dificultad a un niño concreto: sale el candado con su motivo
  (o «Tu tutor ha bloqueado esta dificultad»).
- Los puntos de desbloqueo, las vidas y la aceleración se cambian en el escritorio (pantalla **Dificultades**).
- Todo lo decide la API (`GET api/sesiones/dificultades`); la tablet solo lo pinta. La API tampoco deja guardar
  partidas de una dificultad cerrada (403).
- **Cambios del 24/11 (4):** Historia y Contrarreloj se han borrado también de la base de datos y de la API.
  `SesionNueva` ya no lleva `levelReached`. Probado en el emulador el 25/11: login, dificultades, partida guardada y ranking.

## Ranking

Tiene una pestaña por dificultad, con los 10 mejores récords, y la pestaña **Total**
(`GET api/sesiones/ranking/total`). En Total se suma el récord de cada dificultad y debajo del nombre sale el
récord de cada una, para que los niños compitan entre ellos aunque jueguen en dificultades distintas.
La fila del jugador va resaltada.

## Mis estadísticas (pestaña «Mètriques» del reto de Métricas)

Se abre desde **Perfil → Mis estadísticas** y enseña las métricas **propias** del niño que ha entrado.

1. `MetricasActivity` pide sus partidas a la API (`GET api/sesiones/mias`).
2. Se las pasa en JSON a `app/src/main/python/estadisticas_jugador.py`, que se ejecuta dentro de la app con **Chaquopy**.
3. Python devuelve los números (partidas, récord, puntos de media, precisión, segundos por partida, días jugados y
   ayudas) y 7 gráficas de Matplotlib en base64. `GraficasAdapter` las pinta de dos en dos.

Las estadísticas de todos los niños y el modelo de abandono van en el escritorio.

Lo que cambia en la app por Chaquopy:

- **Python 3.13** con pandas 2.1 y matplotlib 3.8. Solo para `arm64-v8a` (tablets) y `x86_64` (emulador).
  El APK de depuración pasa de unos 16 MB a unos 115 MB.
- **`minSdk` 24** (Android 7.0), porque esos paquetes de Chaquopy lo piden. Antes era 23.
- **`compileSdk` 36** y `android:pageSizeCompat="enabled"` en el manifest. Los emuladores y tablets nuevos
  (Android 15 o más, con páginas de memoria de 16 KB) no cargan la librería de cálculo de numpy de Chaquopy, que está
  hecha para 4 KB. Con ese atributo funciona en modo de compatibilidad. En las tablets normales no hace nada.
  Probado en el emulador `ps16k` de Android 17.

## Métricas (carpeta `metricas/`)

Contiene el pipeline en Python del reto «Mètriques dels jocs»: el playtest, la simulación de 400 sesiones, las gráficas,
el árbol de decisión y el informe PDF. Está aquí porque la pestaña «Mètriques» de la tablet lo ejecutará con Chaquopy
(`generar_informe()` devuelve las gráficas en base64). Más información en [`metricas/README.md`](metricas/README.md).

## Cómo abrirlo

1. Android Studio → **Open** → esta carpeta. Si Gradle dice que no admite la versión de Java (por ejemplo «25.0.3»),
   en *Settings → Build Tools → Gradle → Gradle JDK* elegid un **JDK 17 o 21** (Gradle 8.9 no funciona con Java 25).
2. **Instalad Python 3.13** en el ordenador. Chaquopy lo usa al compilar para meter pandas y matplotlib en la app.
   En Windows: `pymanager install 3.13` o el instalador de python.org. La primera compilación descarga unos 30 MB de paquetes.
3. Arrancad la API (perfil **http**, puerto 5110). Hace falta `07_solo_infinito.sql` en la base de datos.
4. Ejecutad la app en un emulador de **tablet en horizontal** (por ejemplo *Pixel Tablet*).
   - Desde el emulador la API está en `http://10.0.2.2:5110/api/` (`SharedData.PROJECT_ROOT`).
   - En una tablet de verdad: cambiad la IP en `SharedData.PROJECT_ROOT` (misma wifi y puerto 5110 abierto en el cortafuegos).
5. Usuarios de prueba: `marc` / `marc1234`, `ona` / `ona1234`… También se puede **Crear cuenta** eligiendo avatar.

## Pantallas

| Pantalla | Activity | Qué hace |
| --- | --- | --- |
| Login | `LoginActivity` | `POST api/auth/login`. Si ya había entrado, va directo al menú |
| Registro | `RegistroActivity` | `POST api/auth/registro` con el avatar elegido (rejilla con `RecyclerView`) |
| Menú | `MenuActivity` | ¡JUGAR!, Ranking, Perfil, Cómo jugar, Ajustes y las 3 temáticas |
| Dificultades | `DificultadActivity` | Las 3 dificultades (`RecyclerView`) con candado, motivo del tutor y récord |
| Partida | `PartidaActivity` | El juego: vidas, aceleración, combo, pausa y ayuda |
| Resultado | `ResultadoActivity` | Puntos y datos; guarda la partida y avisa si se ha desbloqueado otra dificultad |
| Perfil | `PerfilActivity` | Resumen, historial (`GET api/sesiones/mias`) y cambiar avatar (`PUT api/auth/avatar`) |
| Ranking | `RankingActivity` | Top 10 de cada dificultad (`GET api/sesiones/ranking`); tu fila resaltada |
| Ajustes | `AjustesActivity` | Temática, música, efectos y cerrar sesión |
| Cómo jugar | `ComoJugarActivity` | Los 3 pasos |

## Estructura

```
com.groundstudio.topohit
├── activities/   una Activity por pantalla y PantallaBase (música y ajustes)
├── adapters/     DificultadesAdapter, HistorialAdapter, RankingAdapter, AvataresAdapter
├── api/          una interfaz Api… y un object Retrofit…Client por llamada (7 llamadas)
├── datamodel/    data class del JSON (@SerializedName), Tematica y Agujero
└── (raíz)        SharedData, GestorSesion, GestorAjustes, Avisos, Tematicas, Avatares, Sonidos, Cifrado, ReglasJuego
```

## Reglas del juego

| Elemento | Golpes | Efecto |
| --- | --- | --- |
| Topo normal | 1 | +10 |
| Topo enfadado | 2 (al primero se pone la tirita) | +25 |
| Topo dorado | 1 | +50 y +1 vida (sin pasar de las iniciales) |
| Bomba | no tocar | −1 vida (cuenta como fallo) |

- Se pierde una vida al tocar una bomba y cuando un topo se escapa (el topo se burla un momento).
- 5 aciertos seguidos → **COMBO x2** hasta el siguiente fallo o escape.
- Tocar un agujero vacío es un fallo (no resta puntos). Tocar un topo que ya está golpeado **no** es un fallo.
- Cada topo tiene su cara de golpeado: normal, dorado y enfadado, en las 3 temáticas (sprites `_09_` y `_10_`, nuevos el 24/11).

## Datos que se envían (`POST api/sesiones`)

`modeId = 3`, `difficulty` con tilde (`Fácil`…), `sessionLength`, `errorsMade`, `finalScore`,
`nAttempts = hits + errorsMade`, `molesEscaped`, `hits`, `maxCombo`, `goldenHits`, `abandoned` (salir desde la pausa)
`avgReactionMs` (`null` si no ha golpeado ningún topo) y `helpClicks` (veces que ha pulsado la ayuda ?).
