# Topohit · App Android

Juego de golpear topos para tablet Android (en horizontal), hecho en **Kotlin + layouts XML** con lo visto en clase.
Las pantallas y los dibujos salen del Figma del equipo (página «Arte · Temáticas» y página «Assets»).

## Cómo abrirlo

1. Android Studio → **Open** → esta carpeta (`Android`). Espera a que Gradle sincronice.
2. Arranca la API (perfil **http**, puerto 5110).
3. Ejecuta la app en un emulador de **tablet en horizontal** (por ejemplo *Pixel Tablet* o *Medium Tablet*).
   - Desde el emulador la API está en `http://10.0.2.2:5110/` (ya viene puesta).
   - En una tablet de verdad: cambiad la IP en `api/RetrofitClient.kt` (`URL`, por ejemplo `http://192.168.1.20:5110/`) y volved a instalar la app.
     La tablet y el PC tienen que estar en la misma wifi y el cortafuegos de Windows tiene que dejar pasar el puerto 5110.
4. Usuarios de prueba (de `topohit_datos_prueba.sql`): `marc` / `marc1234`, `ona` / `ona1234`…
   También se puede **Crear cuenta** (eligiendo avatar).

## Pantallas

| Pantalla | Activity | Qué hace |
| --- | --- | --- |
| Login | `LoginActivity` | `POST api/auth/login`. Si ya había entrado, va directo al menú |
| Registro | `RegistroActivity` | `POST api/auth/registro` con el avatar elegido (`avatarId`, 1 a 12) |
| Menú principal | `MenuActivity` | Jugar, Récords, Perfil y los 3 botones de temática. Pide `GET api/sesiones/progreso` |
| Selección de modo | `ModoActivity` | Historia, Contrarreloj, Infinito |
| Dificultad | `DificultadActivity` | Fácil / Medio / Difícil (Contrarreloj) o Fácil / Normal / Difícil (Infinito), con el récord |
| Mapa Historia | `MapaHistoriaActivity` | 10 niveles en 3 zonas, estrellas y panel «Inicio de nivel» |
| Partida | `PartidaActivity` | El juego: barra de arriba, tablero de 3, 6 o 9 agujeros, pausa y ayuda |
| Resultado | `ResultadoActivity` | Victoria, ¡Se acabó el tiempo! o ¡Se acabaron las vidas! |
| Perfil | `PerfilActivity` | Resumen, historial (`GET api/sesiones/mias`) y cambiar avatar (`PUT api/auth/avatar`) |
| Mis récords | `RecordsActivity` | Récord por modo y dificultad y estrellas por nivel |
| Ajustes | `AjustesActivity` | Temática, música y efectos, y cerrar sesión |
| Cómo jugar | `ComoJugarActivity` | Los 3 pasos del Figma |

## Paquetes

- `activities` — una Activity por pantalla y `Constantes` (claves de los `putExtra`).
- `adapters` — `HistorialAdapter` y `RecordAdapter` (RecyclerView + ViewHolder).
- `api` — `ApiTopohit` (interfaz de Retrofit) y `RetrofitClient` (singleton).
- `data_model` — clases de datos de la API, `Tematica` (Normal, Halloween, Navidad) y `Agujero`.
- `util` — `Juego` (datos compartidos: usuario, configuración de modos, progreso), `Almacen` (archivos de texto) y `Avatares`.

## Reglas del juego (Figma, página «Ajustes del juego»)

| Elemento | Golpes | Puntos |
| --- | --- | --- |
| Topo normal | 1 | +10 |
| Topo enfadado | 2 (al primero se pone la tirita) | +25 |
| Topo dorado | 1 | +50 (en Infinito, además +1 vida) |
| Bomba | no tocar | −20 (en Infinito, −1 vida) |
| Jefe (nivel 10) | 10 | +10 por golpe y +100 al vencerlo |

- 5 aciertos seguidos → **COMBO x2** hasta el siguiente fallo o topo escapado.
- Tocar un agujero vacío no resta puntos, pero cuenta como fallo y rompe la racha.
- Infinito: se pierde una vida cuando se escapa un topo o se toca una bomba. Cada segundo va un 1 / 2 / 3 % más rápido.
- Historia: estrellas al 45 %, 70 % y 100 % del objetivo (igual que la API). Con 1 estrella se abre el nivel siguiente.
- Los números de cada modo (agujeros, tiempo, objetivo, velocidad, vidas) se piden a la API al arrancar
  (`api/modos/historia`, `contrarreloj`, `infinito`); si no hay conexión se usan los del GDD (en `Juego.kt`).
- Los puntos y probabilidades están en `Juego.kt` y en `PartidaActivity.configurar()`.

## Datos que se envían a la API (`POST api/sesiones`)

Al acabar (o al salir desde la pausa, con `abandoned = true`): `modeId` (1 Historia, 2 Contrarreloj, 3 Infinito),
`sessionLength`, `errorsMade` (toques en vacío + bombas), `finalScore`, `difficulty` o `levelReached`,
`nAttempts` = `hits` + `errorsMade`, `molesEscaped`, `hits`, `maxCombo`, `goldenHits`, `abandoned` y `avgReactionMs`
(media de ms desde que sale el topo hasta el primer golpe).

## Cosas que NO hemos visto en clase (y cómo se han resuelto de la forma más simple)

1. **Bucle del juego con `Handler` + `postDelayed`**: para mover el juego hay que repetir una función cada 40 ms.
   El `Handler` la ejecuta en el hilo de la pantalla, así que no hace falta `runOnUiThread`. Está en `PartidaActivity`.
2. **`view.animate()`**: una animación de 0,12 s para que el topo «salte» del agujero.
3. **`@Body` y `@Header` en Retrofit**: la API recibe JSON en el cuerpo y pide el token en la cabecera `Authorization: Bearer …`.
4. **`OnBackPressedCallback`**: para que el botón atrás de la tablet abra la pausa en vez de salir.
5. **Guardar la sesión y los ajustes**: con archivos de texto línea a línea (`FileWriter` / `BufferedReader`, como en
   el vídeo «Arxius de text») dentro de `context.filesDir`. Así no hay que escribir la contraseña cada vez.
6. **Vistas que se crean desde Kotlin** (agujeros del tablero, niveles del mapa): se inflan layouts con
   `layoutInflater.inflate(...)` como en las listas personalizadas y se colocan con `translationX` / `translationY`.

## Cambios del 19/11/2026

- **Merge de git arreglado.** 16 archivos `.kt`, `activity_login.xml`, `activity_ajustes.xml`, `strings.xml` y `build.gradle.kts`
  tenían las marcas `<<<<<<< HEAD` de un merge sin terminar y la app no compilaba. Se ha dejado la versión **HEAD**
  (contraseña con bcrypt, progreso con `nivelActual`, música y efectos). Del otro commit (`6f38390`) se han descartado
  «Probar sin conexión» y el campo «Servidor de la API» en Ajustes: enviaba la contraseña sin cifrar y usaba el progreso antiguo,
  que la API ya no acepta. **Haced commit** de estos archivos para que el repositorio quede bien.
- **Avatar guardado en la API** (tabla `AVATAR`, columna `USUARIO.avatar_id`):
  - `Juego.avatar` ahora es el **id** del avatar (1 a 12), no la posición. La imagen `i` de la rejilla es el avatar `i + 1`
    (`Tematicas.AVATARES[avatarId - 1]`). A la API nunca se envía `R.drawable…`, que cambia al compilar.
  - Registro: `RegistroPeticion` envía `avatarId`. Login: el avatar llega en `LoginRespuesta.avatarId`.
  - Perfil: «Guardar» llama a `PUT api/auth/avatar` (en `GlobalScope.launch` + `runOnUiThread`, como en el vídeo 35).
  - Ya no se usa `avatar_<usuario>.txt`: el id se apunta en `sesion.txt` (línea 5) para pintarlo al abrir la app.
  - El avatar no depende de la temática: cualquier jugador puede elegir cualquiera.
- [No visto en clase] `@PUT` en Retrofit: igual que `@POST`, pero con el verbo que usa la API para modificar.

## Pendiente / ideas

- **Ranking global**: la API todavía no tiene una ruta con el top 10 de todos los jugadores; por eso la pantalla es «Mis récords».
- **API, nivel jefe**: `CalcularEstrellas` da 3 estrellas a cualquier partida no abandonada del nivel 10, aunque no se venza al jefe.
#
