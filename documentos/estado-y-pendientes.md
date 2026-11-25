# Topohit · Estado y pendientes

Actualizado: 25/11/2026 · Equipo: Borja, Miguel y Daniil (DAM)

## Qué se hizo el 25/11/2026 (tarde-noche, 2) · tablet de 32 bits, icono, música e imágenes

| Qué | Detalle |
| --- | --- |
| **Tablet de 32 bits** | La tablet de clase es una **Samsung Galaxy Tab A de 32 bits** (`armeabi-v7a`). Chaquopy solo tiene Python 3.11 o anterior para 32 bits, así que la app pasa de **Python 3.13 a 3.11** (pandas 2.1.3, numpy 1.26.2 y matplotlib 3.6.0) y se compila también para `armeabi-v7a`. Los scripts de «Mis estadísticas» se probaron con esas versiones exactas y dan los mismos números; en el emulador funciona. El APK pesa unos 141 MB. **Falta probarlo en la Tab A de verdad.** Para compilar hace falta Python 3.11 (`instalar_librerias.bat` ya lo instala) |
| **Icono nuevo** | Cielo con rayos de sol, colina, el topo saliendo del agujero, el martillo y estrellas (`ic_launcher_fondo.png` y `ic_launcher_frente.png`), dentro de la zona visible del icono adaptable. También el de Android 7 (`mipmap-xxhdpi/ic_launcher.png`) |
| **Botón de música en el menú** | Botón redondo con el altavoz (🔊 / 🔇) junto a «?» y Ajustes: pone o quita la música con un toque y lo guarda (lo mismo que el de Ajustes) |
| **Música de fondo nueva** | `res/raw/musica_fondo.mp3`: instrumental alegre (xilófono, marimba, ukelele) de 2:57, sin el silencio del final para que el bucle no tenga hueco y con el volumen igualado a la anterior |
| **Imágenes con restos blancos** | Quitada la mancha blanca junto al martillo de `logo_halloween.png`, el blanco dentro de la mecha de `topo_navidad_07_bomba_regalo.png` y los puntitos sueltos alrededor de `topo_normal_09_dorado_golpeado.png`. El resto del dibujo no cambia |

## Qué se hizo el 25/11/2026 (noche)

| Qué | Commit | Detalle |
| --- | --- | --- |
| **Modelo de abandono en la tablet** | Android `366e0ac` | El PDF de Métricas pide que la vista «Mètriques» enseñe el resultado del modelo. En **Perfil → Mis estadísticas** sale ahora la fila «Modelo: ¿volverá a jugar?» (accuracy, precision, recall y ROC-AUC) y 3 gráficas más (matriz de confusión, curva ROC e importancia de cada variable). `modelo_abandono.py` hace con numpy el mismo árbol que el escritorio, porque Chaquopy no tiene scikit-learn para Python 3.13. Da **los mismos números que el escritorio** (0,913 / 0,875 / 1,0 / 0,865), comprobado contra scikit-learn y en el emulador |
| **Título legible en Halloween** | Android `366e0ac` | El título de 7 pantallas (Ranking, Perfil, Ajustes, Cómo jugar, Dificultades, Crear cuenta y Mis estadísticas) sale del color de la temática (`colorTitulo`): crema con sombra marrón en Halloween; en Normal y Navidad, igual que antes. Probado en el emulador |
| **Contraseña nueva de la BD** | API `2039d24` | Se cambió la contraseña del login `jocs_dam02` (`ALTER LOGIN`). **Decisión del equipo:** `appsettings.json` **se sube a git** (proyecto de clase en un entorno cerrado); ya no hay que crearlo a mano. Fuera `appsettings.ejemplo.json`. Probado: la API arranca y lee la BD. **Proyectada usa el mismo login: hay que ponerle la contraseña nueva** |
| **`instalar_librerias.bat`** | Android `366e0ac`, escritorio `56d522c` | Doble clic: instala Python 3.11 con winget si falta, las librerías (`requirements.txt`) y comprueba que se cargan. En el Android, Python 3.13 (Chaquopy) y las de `metricas/`; en el escritorio, las de la pantalla Métricas. Probados los dos |
| **Figma** | — | Texto marrón en los botones naranjas y verdes del arte y títulos como en la app (ver «Cuando se pueda») |
| Documentos | API `7c482a5` y este | README de los 3 repos, `REGLAS_CODIGO.md`, `metricas/README.md`, `CAMBIOS_18-11.md` (histórico), `resumen-del-proyecto.md` (checklist revisado) y `topohit-diseno-del-juego.md` |

## Decisiones del 25/11/2026 (tarde)

- **Métricas:** se quedan como están, sencillas. No se programan las propuestas de `CONCLUSIONES.md` (primera partida más
  amable, ayuda automática, «Cómo jugar» automático, transición a Normal): quedan solo como ideas para la presentación.
- **Rueda del ratón del escritorio:** la prueba el equipo con un ratón de verdad.
- **Diapositivas de la presentación técnica:** más adelante.
- **Base de datos comprobada el 25/11:** `jocs_dam02` coincide con `topohit_completo.sql` (scripts 07, 08 y 10 aplicados,
  sin tablas de Historia ni Contrarreloj, solo el modo 3, Normal a 500 y Difícil a 800 puntos). 30 partidas, ninguna de
  prueba, y ningún usuario de prueba.

## Qué se hizo el 25/11/2026 (tarde) · Figma y accesibilidad

**Figma** (solo tiene que verse bien; el prototipo no tiene que funcionar del todo):

- Escritorio: nueva pantalla **Métricas** (sección «4 · Estadísticas», con su nota): 15 tarjetas, gráficas y
  «Calcular otra vez», como en la app. La barra lateral (componente `Esc/Barra lateral`) tiene ahora la entrada
  **Métricas**, que sale en todas las pantallas del escritorio.
- Tablet, Perfil (3 temáticas): el botón «Mis estadísticas» se salía del panel; recolocado. Se quitó su enlace,
  que llevaba al Login (no hay pantalla «Mis estadísticas» en el arte).
- Probado con Present en las 3 temáticas: login → menú → cambio de temática → dificultades → partida → «¡Se acabaron
  las vidas!» → Repetir / Menú / pausa. Los enlaces van a la temática correcta.
- **No se ha tocado** (no hace falta que funcione): en el prototipo salen casi solo bombas, también en Fácil (el estado
  «Elige tipo» del componente «Agujero en vivo» solo distingue dorado / bomba); las dificultades con candado se pueden
  jugar; la partida sigue detrás de la pausa; en el Resultado, «¡Te faltan 760!» y los 3 corazones son fijos.

**Accesibilidad (G14.5), revisada en el código de Android:**

- Sin enlaces externos, compras, anuncios ni vibración. El único permiso es `INTERNET` (la API).
- Sin sobrecarga sensorial: música y efectos se pueden quitar en Ajustes, no hay parpadeos y las animaciones son cortas
  (el topo sale en 120 ms).
- Textos grandes (mínimo 13 sp; los botones, 26 sp en negrita) y mensajes cortos y en positivo.
- **Contraste (WCAG):** el texto blanco sobre el naranja (2,45:1) y el verde (2,89:1) de la temática Normal y sobre el
  naranja de Halloween (2,61:1) **no llega** al 3:1 que piden los textos grandes (tienen sombra marrón, que ayuda).
  El resto pasa: marrón sobre crema 13,4:1, blanco sobre morado 6,3:1, sobre rojo 5,0:1, sobre verde Navidad 5,3:1.
  **Arreglado el 25/11 (Android `7041bd7`)**: texto **marrón sobre naranja** (6,0:1) en el botón principal de Normal y
  Halloween, en los chips naranjas («Tu récord allí», racha, «¡NUEVO RÉCORD!»), en los números de «Cómo jugar» y en
  Música/Efectos de Ajustes. La temática tiene un campo nuevo, `colorTextoBoton`; en Navidad sigue el blanco sobre rojo.
  Probado en el emulador en las 3 temáticas. **También el verde** (Android `babc5cd`): marrón sobre el verde de Normal
  (5,2:1) en los botones secundarios (Ranking, Perfil, Continuar, Menú…), las pestañas del Ranking y el botón de la
  temática Normal; campo `colorTextoBotonSecundario` (blanco en Halloween y Navidad). **El título sobre el cielo
  morado de Halloween** se arregló el 25/11 por la noche (Android `366e0ac`): crema con sombra marrón.
  El arte de Figma se puso igual el 25/11 por la noche.

## Qué se hizo el 25/11/2026 · Historia y Contrarreloj fuera de la base de datos

Se terminó lo que quedó a medias el 24/11: **Historia y Contrarreloj ya no existen en ningún sitio**.

| Parte | Commit | Qué cambia |
| --- | --- | --- |
| Base de datos | (en la API) `BaseDeDatos/10_quitar_historia_contrarreloj.sql` | **Ejecutado en `jocs_dam02` el 25/11.** Borró las 116 partidas de Historia (84) y Contrarreloj (32) **sin respaldo** (decisión de Borja), la columna `SESION.level_reached`, las tablas `NIVEL_HISTORIA` y `DIFICULTAD_CONTRARRELOJ` y los modos 1 y 2. Nueva regla `CK_SESION_INFINITO`. Quedan 30 partidas, todas de Infinito. Las tablas de Proyectada no se tocaron |
| API | `934e858` | Fuera `api/modos/historia`, `api/modos/contrarreloj` y `api/sesiones/progreso`, y `levelReached` de las partidas. `topohit_completo.sql` y `topohit_datos_prueba.sql` ya no los crean |
| Escritorio | `41c33f2` | Fuera `NivelHistoria`, `DificultadContrarreloj` y `LevelReached` |
| Android | `00cda44` | `SesionNueva` ya no lleva `levelReached` |

Los README tienen la sección «Cambios del 24/11/2026 (4)» (commit aparte de documentación).

**Importante:** hay que actualizar la API y el escritorio **a la vez**. El escritorio antiguo no carga con la API nueva
(pedía `api/modos/historia`). La tablet antigua sí funciona: la API ignora el `levelReached` que envía.

**Pruebas hechas el 25/11 contra la API real:**

- API: login de `marc` y `profe`, `POST api/sesiones` de Infinito (201), con `modeId` 1 o 2 (400), las rutas quitadas
  (404), ranking por dificultad y total, `mias` y `todas` (solo Infinito). Las partidas de prueba se borraron.
- Tablet (emulador, APK nuevo): login, dificultades con candado, una partida en Fácil guardada y ranking.
- Escritorio: Jugadores, Estadísticas (General y Seguimiento) y Métricas (30 partidas, 15 tarjetas y las gráficas).

## Qué se hizo el 24/11/2026

**Decisión del equipo:** el juego solo tiene el modo **Infinito**. Las dificultades se abren **en cadena**:

- Fácil está siempre abierta.
- Normal se abre con X puntos en Fácil (500 por defecto).
- Difícil se abre con Y puntos en Normal (800 por defecto).

El **tutor** puede bloquear para siempre una dificultad a un niño concreto desde el escritorio.

| Parte | Repositorio (rama `main`) | Commit | Qué cambia |
| --- | --- | --- | --- |
| Base de datos | (en la API) `BaseDeDatos/07_solo_infinito.sql` | — | `DIFICULTAD_INFINITO.orden` y `.puntos_desbloqueo`, y la tabla `BLOQUEO_DIFICULTAD`. **Ya ejecutado en `jocs_dam02`.** Solo añadía: Historia y Contrarreloj se quitaron después con el script 10 (25/11) |
| API | `TankistyT80/ASP.NET-Core-Web-API-Topohit` | `4f41157` | `GET api/sesiones/dificultades`, `GET/PUT/DELETE api/jugadores/{id}/bloqueos/{dificultad}`, `PUT api/modos/infinito/{nombre}`, ranking por dificultad, `POST api/sesiones` solo con `modeId` 3 (403 si la dificultad está cerrada) y avatar (login, registro y `PUT api/auth/avatar`) |
| Escritorio | `Miguelesmi18/GolpeaTopos-Escritorio` | `fa68f7d` | La pantalla **Dificultades** (vidas, aceleración y puntos para abrir) y el botón **Bloquear dificultades** en la ficha del jugador (`FormBloqueos`) |
| Android | `Miguelesmi18/Topohit-Android-` | `835f61d` | Rehecho entero siguiendo las fichas 0489 (camino C, ver `REGLAS_CODIGO.md`): dificultades con candado, ranking top 10, perfil, 6 sprites nuevos de topos golpeados y un candado, y el topo que se burla al escaparse |

**Revisión del Android:** se revisó en 3 rondas y las correcciones de la última ya están aplicadas.

**Pruebas hechas:** emulador de tablet (2560×1600) contra la API real:

- login y registro;
- candado por puntos y por tutor;
- partida en Fácil y en Normal;
- resultado guardado;
- abandono desde la pausa;
- ranking, perfil y avatar.

El aviso de desbloqueo («¡Has desbloqueado Normal!») se probó de verdad el 25/11 (ver «Probar el desbloqueo real» más abajo).

## Para que funcione en otro ordenador

1. `git pull` en los tres repositorios.
2. **API**: .NET 8, perfil `http`, puerto 5110. `appsettings.json` ya viene en el repositorio (desde el 25/11). Si solo tenéis .NET 9 o superior, arranca igual (desde el 25/11 el `.csproj` lleva `RollForward=Major`), también desde Visual Studio.
3. **Android Studio**: *Settings → Build Tools → Gradle → Gradle JDK* = **JDK 17 o 21**. Gradle 8.9 no funciona con Java 25, que es el que trae el Android Studio nuevo.
4. **Python**: doble clic en `instalar_librerias.bat` del Android (Python 3.11 para compilar) y del escritorio
   (librerías de la pantalla Métricas).
5. **Emulador**: una tablet en horizontal. La URL de la API está en `SharedData.PROJECT_ROOT` (`http://10.0.2.2:5110/api/`). En una tablet real se pone la IP del PC.
6. **Usuarios de prueba**: `marc` / `marc1234` (jugador) y `profe` / `profe1234` (educador).

## Pendiente

### Importante

- [x] **Cifrado (0490)** (revisado el 25/11): el PDF «Protecció dels usuaris» solo pide guardar el usuario y la contraseña
  **cifrada** en la BD y consultarlos desde el escritorio y la tablet. Ya se hace (bcrypt con sal fija). No se cambia:
  si algún profesor pide sal aleatoria, la opción sería que la API haga `hashpw` y `checkpw`.
- [x] **Contraseña de la BD en el repositorio de la API** (código hecho el 24/11, commit `d716cf6`): la cadena de conexión
  ya no está en `BD.cs`; la API la lee de `appsettings.json`, que no se sube. Al repositorio va `appsettings.ejemplo.json`.
  **Cada compañero tiene que crear su `appsettings.json`** con la contraseña, o la API no arranca.
  **25/11: la contraseña del servidor se ha cambiado** y, por decisión del equipo, `appsettings.json` **se sube a git**
  (commit API `2039d24`). Hay que poner la nueva también en Proyectada, que usa el mismo login.
  La **clave de los tokens** (`Jwt:Clave`) también ha pasado a `appsettings.json` (commit `afb714b`), con una clave nueva.
- [x] **Escritorio, estadísticas** (hecho el 24/11):
  - solo Infinito: General y el resumen van por dificultad, Seguimiento tiene la columna «Dificultad» y la gráfica
    del progreso por dificultad, y la ficha del jugador filtra por dificultad;
  - se ha quitado el aviso «atascado»;
  - probado en la app.
- [x] **Escritorio, avatares** (arreglado el 25/11, en la API): en Jugadores y en Seguimiento todos salían con el avatar 1
  porque `GET api/usuarios` no devolvía `avatarId`. Ahora lo devuelve, y `POST`/`PUT api/usuarios` lo guardan (el avatar
  que se elige en el escritorio antes se perdía). Probado contra la API real y en el escritorio (cada jugador con su avatar).
- [x] **Métricas (Python)** (hecho el 24/11, en `Topohit-Android-/metricas/`):
  - el playtest real: 29 partidas de Infinito;
  - la simulación de 400 sesiones con la regla de frustración;
  - 11 gráficas;
  - el árbol de decisión: accuracy 0,91 y ROC-AUC 0,87;
  - `informe_metricas.pdf` (v1).

  **Conclusiones escritas el 25/11** en `Topohit-Android-/metricas/CONCLUSIONES.md` (para la presentación técnica, como pide
  el PDF). Falta **entregar la v1** (`informe_metricas.pdf`, era para el 21/11) y, cuando haya partidas reales con ayudas,
  volver a ejecutarlo.
- [x] **Dos vistas de Métricas conectadas a la BD (a través de la API), cada una con su consulta:**
  - [x] **Android** (hecho el 24/11): **Perfil → Mis estadísticas**. Son las estadísticas **propias** del niño
    (`GET api/sesiones/mias`) con Python dentro de la app (Chaquopy): 7 números y 7 gráficas. Probado en el emulador.
    Para compilar hace falta **Python 3.11** en el PC (era 3.13 hasta el 25/11). La app pasa a `minSdk` 24 y el APK pesa unos 115 MB.
  - [x] **Escritorio** (hecho y probado el 24/11): pantalla **Métricas**
    con las estadísticas de **todos** los jugadores (`GET api/sesiones/todas`) y el modelo de abandono.
    Ejecuta `Python/metricas_escritorio.py`, que hace 11 gráficas y 15 tarjetas. El script está probado con las partidas reales.
    Mientras haya menos de 50 jugadores reales, el modelo se entrena con las sesiones simuladas.
    Hace falta Python con `pip install -r requirements.txt`. Probado de punta a punta: login, cálculo en unos 20 s, 15 tarjetas,
    11 gráficas y la ventana grande al hacer clic.
- [x] **Android, clasificación total** (hecho el 24/11): pestaña **Total** en el Ranking.
  - Suma el récord de Fácil, Normal y Difícil y enseña el desglose debajo del nombre.
  - Usa la API nueva `GET api/sesiones/ranking/total`.
  - Probado en el emulador.
- [x] **Escritorio, tamaño de la ventana** (arreglado el 25/11): si la ventana de 1440 × 900 no cabe (por ejemplo 1536 × 864
  con escalado del 125 %), se hace más baja y la pantalla activa va con barra de desplazamiento. Probado en Jugadores y Estadísticas.
  **Falta comprobar la rueda del ratón con un ratón de verdad** (la rueda simulada no llega).
- [x] **Vista «Mètriques» con las gráficas (Métricas, apartado opcional de DAM)** (hecho el 25/11 por la noche: la tablet
  enseña también el modelo, ver arriba). El PDF pide que el informe
  final se vea en una pestaña «Mètriques» de la app **Android** (no dice nada del escritorio), sin textos de conclusiones:
  - las métricas (`retention_rate`, `churn_rate`, DAU…);
  - las gráficas de Matplotlib pasadas a base64;
  - el resultado del modelo (matriz de confusión o ROC, con accuracy, precision, recall y ROC-AUC).

  Está hecho con **Chaquopy** en la tablet (las métricas del niño + el modelo) y, en el escritorio, con Python que
  guarda los PNG (todos los jugadores + el modelo). En la tablet, las métricas derivadas de todos los jugadores
  (`retention_rate`, `churn_rate`, DAU/MAU) no salen porque son del escritorio; si el profesor las quiere también en la
  tablet, se pueden sacar de las mismas sesiones simuladas.
- [x] **Probar el desbloqueo real** (hecho el 25/11 en el emulador): con un usuario de prueba y el umbral de Normal a 10 un momento,
  una partida de 25 puntos en Fácil sacó «¡Has desbloqueado Normal!» y quitó el candado. El umbral volvió a 500 y se borraron
  el usuario y sus partidas. De paso se arregló «Repetir», que se quedaba con el récord de antes (commit en Android).

### Cuando se pueda

- [x] **Figma** (hecho el 25/11): fuera Historia y Contrarreloj del wireframe, del arte de las 3 temáticas (9 pantallas por temática,
  con los enlaces del prototipo redirigidos), del GDD (páginas 1, 2, 3, 5, 7 y 8) y del escritorio. Nuevas: Dificultades con candado
  (por puntos y por el tutor), Ranking con Fácil/Normal/Difícil/Total, Resultado con «¡Has desbloqueado Normal!», Perfil con
  «Mis estadísticas» y, en el escritorio, la pantalla Dificultades y el diálogo «Bloquear dificultades». Las variables de
  Contrarreloj e Historia de «⚙ Ajustes del juego» se han dejado porque el componente «Agujero en vivo» las usa.
- [x] **Airtable** (hecho el 25/11): tareas y subtareas puestas al día con lo que hay en el código (estados y horas reales),
  las que hablaban de cosas que ya no existen reescritas (niveles → dificultades, perfiles de niño, guardar sin conexión…) y
  7 subtareas nuevas con el trabajo del 24-25/11. **Las horas reales son aproximadas** (las estimadas): revisadlas.
- [x] **`help_clicks`** (hecho el 24/11):
  - BD: `08_help_clicks.sql`, ya ejecutado en `jocs_dam02`. Las partidas antiguas quedan a 0.
  - API: `helpClicks` en `POST api/sesiones` y en todas las respuestas con partidas. Si no se envía vale 0; si es negativo, 400.
  - Android: se cuenta cada vez que se pulsa ?.
  - Probado en el emulador: una partida abandonada con 3 ayudas se guardó con `helpClicks = 3`.
- [x] **Probar en una tablet real** (hecho, según el equipo el 25/11): la misma wifi y el puerto 5110 abierto en el cortafuegos.
- [ ] **Pruebas con las alumnas de Educación Infantil** (feedback).
- [x] **Odoo** (individual, hecho según el equipo el 25/11): las máquinas virtuales de Windows y Linux, y la validación con la profesora.
- [x] **Avatar en la API** (cerrado el 25/11): `avatarId` está en el login, el registro, `PUT api/auth/avatar` y `api/usuarios`,
  que es lo que usan las apps. `api/modos/avatares` no existe ni hace falta (los avatares van dentro de cada app); el README de la
  API lo avisa en la sección del 19/11.
- [ ] **Entregar el informe de Métricas** (`Topohit-Android-/metricas/informe_metricas.pdf`, la v1 era para el 21/11).
  **v2 generada el 25/11** (11 páginas): portada, métricas capturadas y derivadas, playtest real y simulación,
  las 7 gráficas de datos, el modelo (indicadores, matriz de confusión, ROC, importancias y árbol), dónde se ve
  cada cosa (tablet y escritorio) y un anexo con los archivos y las librerías. Mismos datos y números que las apps.
  Se genera con `python 03_analisis.py` desde `metricas/`.
- [x] **Figma** (hecho el 25/11 por la noche): en «🎨 Arte · Temáticas», 69 textos de botones, pestañas y chips naranjas y verdes
  pasan a marrón sin contorno (Normal y Halloween), y los 18 títulos sobre el cielo van como en la app: crema con contorno
  marrón en Halloween y marrón con contorno blanco en Normal y Navidad.
- [ ] **Diapositivas de la presentación técnica** (con `metricas/CONCLUSIONES.md`).

## Documentos relacionados

- `Topohit-Android-/REGLAS_CODIGO.md`: las normas de código que no pueden fallar (fichas 0489 y código externo).
- `Topohit-Android-/README.md`, `GolpeaTopos-Escritorio/README.md` y `ASP.NET-Core-Web-API-Topohit/README.md`: la sección «Cambios del 24/11/2026» de cada uno.
- `topohit-diseno-del-juego.md`: el diseño original (la parte de Historia y Contrarreloj queda como histórico).
- Estos documentos están en `Topohit-Android-/documentos/` desde el 25/11 (antes, en una carpeta fuera de los repositorios).
