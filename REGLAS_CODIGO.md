# Reglas y pautas de código · Topohit Android

Qué hay que seguir **siempre** al tocar la app. Salen de lo que enseñan en clase (TheLibrary,
módulo 0489, fichas `REGLA-0489-xxx`) y de cómo lo aplicamos en Proyectada.
Si algo de aquí choca con un PDF de los retos, **manda el PDF**.

Actualizado: 25/11/2026.

## 0. Lo que no puede fallar

1. **Compila sin errores** (`gradlew assembleDebug`) antes de dar nada por terminado.
2. **Solo existe el modo Infinito.** Ni Historia ni Contrarreloj: ni pantallas, ni textos, ni constantes.
3. **Cada partida se envía a la API** (`POST api/sesiones`) con `modeId = 3`, la `difficulty` **con tilde**
   tal como la devuelve la API (`Fácil`, `Normal`, `Difícil`). Ya no se envía `levelReached`
   (era de Historia y la API lo ha quitado el 24/11).
   `nAttempts = hits + errorsMade`. `avgReactionMs = null` si no ha golpeado ningún topo.
   `helpClicks` = veces que ha pulsado la ayuda (?) en la partida.
4. **Una dificultad cerrada no se puede jugar.** Se pinta con el candado y debajo:
   - si le faltan puntos: «Consigue X puntos en <anterior> para desbloquearla»;
   - si la ha bloqueado el tutor: el motivo del tutor o, si no hay, «Tu tutor ha bloqueado esta dificultad».
   El estado sale **siempre** de la API (`GET api/sesiones/dificultades`), nunca se calcula solo en la tablet.
5. **La contraseña nunca viaja en claro**: se envía su hash bcrypt (`Cifrado.hash`), igual que el escritorio.
6. **Nada de rastro de IA** en el código, los comentarios, los commits ni la documentación del equipo.
7. **Todo lo que no sale en los vídeos lleva encima el comentario de código externo** (apartado 9).
8. Todo en **castellano** (textos, comentarios y nombres propios del juego).

## 1. Estructura

| Paquete | Qué va | Ficha |
| --- | --- | --- |
| `activities` | una Activity por pantalla (`XxxActivity`) y `PantallaBase` | 058 |
| `adapters` | adapters de `RecyclerView` (`XxxAdapter`) | 058 |
| `datamodel` | `data class` del JSON de la API y clases del juego | 058, 070 |
| `api` | una interfaz `Api…` y un `object Retrofit…Client` **por llamada** | 068 |
| raíz | `SharedData`, `GestorSesion`, `GestorAjustes`, `Avisos`, `Tematicas`, `Avatares`, `Sonidos`, `Cifrado`, `ReglasJuego` | 058 (la raíz como `FilesManager`) |

- **Cada clase en su archivo** (094). Nombres de paquete en minúscula.
- La URL de la API **solo** en `SharedData.PROJECT_ROOT` (071). Para una tablet real se cambia ahí.

## 2. Activities

- Se crean a mano: layout + clase que hereda de `PantallaBase` (que hereda de `AppCompatActivity()`) + alta
  en el `AndroidManifest.xml` con `android:screenOrientation="sensorLandscape"` (039, 040, 041).
- Las claves de los extras van en un `object` de constantes dentro de la Activity que las **recibe**,
  en minúscula + `Constants` (`PartidaActivity.partidaConstants.DIFICULTAD`) (042).
- Un objeto propio que viaja en un `Intent` implementa `Serializable` y se recoge con
  `getSerializableExtra(clave) as Tipo` (043).
- Para volver: `finish()` (045).

## 3. Vistas y layouts

- **Sin ViewBinding** (decisión del equipo, camino C): cada vista que se usa va en una `val` con
  `findViewById<Tipo>(R.id.…)` dentro de `onCreate`, o en una propiedad `lateinit var` si la usan varias
  funciones (010). Una vista que solo se pinta una vez (fondo, colores de la temática) se puede buscar
  dentro de la función auxiliar que la pinta (`pintarTema()`), como hace Proyectada.
- **`id` solo en lo que usa el código** (008) y con **prefijo de tipo en mayúscula + pantalla + nombre**
  para que no se repitan entre layouts (008, 044):
  `Txt` EditText · `Btn` botón (también un TextView que hace de botón) · `Lbl` TextView con datos ·
  `Img` ImageView · `Lst` RecyclerView · `Lay` contenedor · `Prg` ProgressBar · `Vw` View.
  Ejemplos: `TxtLoginUsuario`, `BtnMenuJugar`, `LblPartidaPuntos`. En las filas de una lista va `List`
  detrás del prefijo: `LblListRankingNombre`.
- La variable de Kotlin se llama igual que el `id` pero empezando en minúscula (`btnMenuJugar`).
- Los textos fijos van en `strings.xml`, las medidas repetidas en `dimens.xml` y los colores en
  `colors.xml` (015). **En el código Kotlin tampoco se escriben textos para el usuario**: `getString(R.string.…)`.
- En un `EditText`, `hint` en vez de una etiqueta al lado (009), e `inputType` según lo que se escribe (036).
- Imágenes propias en `res/drawable-nodpi` y `setImageResource` (034).

## 4. Estilo de Kotlin

- **Llave `{` en su propia línea** en clases, funciones, `if`/`else`, `for`, `while`, `when`, `try`/`catch`
  y lambdas (`setOnClickListener()` + salto + `{`) (014). **Excepción**: en `build.gradle.kts` va al lado.
- Sin `;` (013). `val` si no se reasigna, `var` solo si cambia (011). Propiedades, no getters (`.text`) (012).
- En un `if`, primero el caso correcto y en el `else` el error (016).
- `when` con flecha en lugar de `switch` (023).
- Listas: `listOf` si no cambia, `mutableListOf` si cambia (084). Recorrer: `for(x in lista)` o
  `for(i in 0..lista.size - 1)` (085).
- Si algo puede ser nulo, `?.` (048) o un `if(x != null)`. **Evitar `!!` y `?:`** (no salen en los vídeos):
  se escribe con `if`.
- Parámetros que no se usan: `_` (028).
- Los campos de un `data class` de la API van con `var`, como en el vídeo 35 (070); las vistas de un
  `ViewHolder` o de un `Agujero` se buscan una vez en `init` y son `val` (no se reasignan, 011).
- Archivos: el `close()` va en un `finally` (con la variable declarada fuera del `try`), para que se cierren
  aunque falle la lectura o la escritura.
- Se intenta **no salir de un bucle a la fuerza** (`break`, `continue`, `return` dentro del bucle): se usa la
  condición del `while` o una variable (regla del equipo, igual que en Proyectada).
- Función que solo devuelve un valor: `= valor` (053), sobre todo en `getItemCount()`.

## 5. Mensajes al usuario

- Errores con `Toast.LENGTH_LONG` (017), desde `Avisos` (así se escriben una sola vez).
- Los textos son para niños de 5 años: cortos, en positivo, sin culpar («¡Casi! Inténtalo otra vez»).

## 6. Listas (RecyclerView)

- `<androidx.recyclerview.widget.RecyclerView` con `id` `Lst…` (051).
- Adapter: recibe `context` y la lista, `private val layout = R.layout.…`, `ViewHolder` interno que busca
  las vistas **una vez** en `init`, y los tres métodos (`onCreateViewHolder`, `onBindViewHolder`,
  `getItemCount`); los datos se ponen en una función `bindXxx(holder, x)` (052).
- `layoutManager` antes del adapter (054).
- Los clics los gestiona el adapter: implementa `View.OnClickListener`, guarda el listener en una propiedad
  anulable y la Activity hace `adapter.setOnClickListener() { … }` con
  `lista[lstXxx.getChildAdapterPosition(it)]` (055, 056).

## 7. API (Retrofit)

- Permiso `INTERNET`; dependencias Gson, Retrofit y converter-gson (067).
- Por cada llamada, una pareja `ApiXxx` (interfaz con `companion object { val BASE_URL = SharedData.PROJECT_ROOT }`)
  y `RetrofitXxxClient` (`object` con `getInstance()`) (068). Nombre con el verbo: `ApiGetRanking`,
  `ApiAddSesion`, `ApiSetAvatar`.
- La llamada dentro de `GlobalScope.launch()` con `call.execute()` y `response.body()`; todo lo que toca la
  pantalla dentro de `runOnUiThread()`, con `this@XxxActivity` como contexto (069).
- `data class` con `@SerializedName` en **cada** campo, aunque coincida el nombre (070).
- Parámetros de la URL con `@Query("nombre")` y el mismo nombre en Kotlin (072). `@GET` para leer y `@POST`
  para añadir (075).
- Las llamadas de una pantalla van **una detrás de otra**, nunca dos a la vez.
- Tras guardar algo, se recarga de la API para comprobarlo (074).
- **No se sale de una pantalla mientras se guarda una partida**: los botones se desactivan hasta que la API
  contesta (Resultado) o se cierra la pantalla cuando ha contestado (abandonar desde la pausa). Si no, la
  pantalla de dificultades podría pedir los datos antes de que la partida esté guardada.
- Si una llamada falla (sin conexión o error de la API), la pantalla **siempre** deja reintentar.

## 6b. Jugabilidad para niños

- Un toque sobre un topo que ya está golpeado **no es un fallo** (los niños tocan dos veces seguidas).
- Cada topo tiene su cara de golpeado (normal, dorado y enfadado) y al escaparse se burla un momento.
- Empatar el récord también se celebra («¡Has igualado tu récord!»).

## 8. Datos locales

- La sesión (`sesion.txt`) y los ajustes (`ajustes.txt`) en `context.filesDir`, una línea por dato, con
  `FileWriter` y `BufferedReader` + `readLine()` hasta `null` (0490: REGLA-0490-008 y 011).

## 9. Código externo (lo que no sale en los vídeos)

Encima del método, clase o bloque que lo usa (uno por bloque, no uno por línea), **literal**:

```kotlin
// Esta parte es de información externa que no sale en los vídeos
// (motivo en pocas palabras)
```

Ejemplos que ya están en la app, con su motivo:

| Qué | Motivo |
| --- | --- |
| `Handler` + `postDelayed` (bucle del juego) | el juego tiene que repetir una función cada 40 ms |
| `SystemClock.elapsedRealtime()` | medir el tiempo jugado sin contar la pausa |
| `view.animate()` | que el topo «salte» del agujero |
| `SoundPool` y `MediaPlayer` | efectos y música |
| `OnBackPressedCallback` | que el botón atrás abra la pausa |
| `@Body`, `@Header`, `@PUT` (Retrofit) | la API recibe JSON en el cuerpo y pide el token |
| `response.code()` | distinguir los errores de la API (401, 403…) |
| `GridLayoutManager` | las 3 dificultades una al lado de otra (el vídeo 19 solo lo nombra) |
| jBCrypt | el profesor de 0490 da `jbcrypt.jar`, pero ningún vídeo lo programa |
| `usesCleartextTraffic` | la API va por `http` en el ordenador de clase |
| `Intent.flags` (`CLEAR_TASK`, `CLEAR_TOP`) | cerrar sesión o volver al menú sin dejar pantallas detrás |
| Chaquopy (`Python.start`, `getModule`, `callAttr`) | el reto de Métricas pide ejecutar Python dentro de la app |
| `Base64` + `BitmapFactory` | pintar las gráficas que Python manda en base64 |
| `android:pageSizeCompat` | que numpy funcione en las tablets con páginas de 16 KB |
| `setShadowLayer` | cambiar desde el código la sombra del título según la temática (`PantallaBase.pintarTitulo()`) |
| En Python: árbol de decisión, `train_test_split` y métricas hechos con numpy (`modelo_abandono.py`) | Chaquopy no tiene scikit-learn para Python 3.11 en 32 bits |
| En Python: backend `Agg`, `base64` | dibujar sin ventana y pasar las gráficas a la app |

Para elegir agujero y tipo de topo se usa `java.util.Random`, que sí sale en clase (087).

## 10. Pendiente de decidir o preguntar

- **Cifrado** (0490), **decidido el 25/11**: se queda como está (bcrypt con **sal fija** calculado en la tablet), porque
  el PDF solo pide la contraseña cifrada en la BD. Si algún profesor pide sal aleatoria, la API haría `hashpw` y `checkpw`.
- Las partidas de Historia y Contrarreloj se borraron de la BD el 25/11 (`10_quitar_historia_contrarreloj.sql`).
- **Colores de la temática:** el texto sobre botones y títulos sale de la `Tematica` (`colorTextoBoton`,
  `colorTextoBotonSecundario`, `colorTitulo`, `colorSombraTitulo`), nunca un color fijo en el layout.
