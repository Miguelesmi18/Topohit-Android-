# Topohit — Decisiones de diseño del juego

Actualizado: 24/11/2026 · Equipo: Borja, Miguel y Daniil (DAM) · **Estado: cerrado** (cambio del 24/11: solo Infinito)

> Documento de referencia para programar el juego en Android Studio (Kotlin). Recoge todo lo decidido al definir el juego y hacer el prototipo en Figma.

## Concepto

- **Nombre:** Topohit.
- **Tipo:** juego de golpear topos (estilo feria). El jugador toca los topos que salen de los agujeros para sumar puntos.
- **Objetivo del juego:** reflejos y motricidad.
- **Plataforma:** tablet Android, **horizontal**, diseño base **1280×800**.
- **Nota del profesor:** hay margen; no hace falta que sea tan infantil como pide el PDF (niños de 5 años).

## Cuenta y perfiles

- **Login** con usuario y contraseña, y **registro** (usuario, contraseña, repetir contraseña y elegir avatar). **Sin email:** los PDF solo piden `username` y contraseña cifrada (cambio del 05/11). En la app de escritorio tampoco hay email: el educador pone una contraseña temporal.
- **Avatar:** se puede elegir entre **12 avatares de las 3 temáticas** (4 por temática: Normal, bruja/vampiro/fantasma de Halloween, Papá Noel/reno/bufanda de Navidad), **juegues con la temática que juegues**. El avatar no cambia al cambiar de temática.
- La contraseña se guarda **cifrada en SQL Server** y se consulta mediante la API JSON (requisito del reto de Protección de usuarios).
- Varios perfiles de jugador: **cada partida queda asociada a su usuario**.
- Rol en la app móvil: **jugador** (los roles de administrador están en la app de escritorio C#).

## Temáticas

- 3 temáticas: **Normal**, **Halloween** y **Navidad**.
- Se eligen con **3 botones en el menú principal** (también están en Ajustes). Se guardan en el perfil.
- Al cambiar de temática cambian: fondo, botones, topos, bomba, agujeros, decoración y música. **La jugabilidad no cambia.**

| Temática | Color principal | Color secundario | Panel | Texto |
| --- | --- | --- | --- | --- |
| Normal | #F28C28 naranja | #5DAA3A verde | #FFF4DC crema | #3B2314 |
| Halloween | #FF7A1A calabaza | #7B3FC4 morado | #2B1A40 morado oscuro | #FFE9C7 |
| Navidad | #D7263D rojo | #1E7B45 verde | #F4FAFF blanco nieve | #3B1E14 |

- **Tipografías:** Lilita One para títulos y botones, Fredoka para textos.
- **Botones:** borde marrón oscuro #2B1A10 de 4 px y sombra sólida desplazada hacia abajo (estilo cartoon).

## Modos de juego

> ⚠️ **Cambio del 24/11/2026 (decisión del equipo): el juego solo tiene el modo Infinito.**
> Historia y Contrarreloj se han quitado de la tablet, de la API y, el 25/11, también de la base de datos (sus tablas y partidas se borraron con `10_quitar_historia_contrarreloj.sql`).
> ¡JUGAR! lleva directo a elegir la dificultad de Infinito. **Normal** se abre al conseguir **500 puntos en Fácil** y
> **Difícil** al conseguir **800 en Normal** (valores iniciales; se cambian en el escritorio, pantalla *Dificultades*).
> Mientras tanto salen con un **candado** y «Consigue X puntos en <anterior> para desbloquearla». El **tutor** puede
> bloquear una dificultad a un niño concreto desde su ficha del escritorio; entonces sale su motivo o
> «Tu tutor ha bloqueado esta dificultad». Las 3 dificultades que se desbloquean son los «varios niveles» del reto.
> Lo que sigue de Historia y Contrarreloj queda como **histórico del diseño**. Estado completo: `estado-y-pendientes.md`.

### Historia

10 niveles en 3 zonas. Cada nivel da **1–3 estrellas**: 1★ = 45 %, 2★ = 70 % y 3★ = 100 % de la puntuación objetivo. Antes de jugar se abre el overlay **Inicio de nivel** con el objetivo y la novedad.

| Nivel | Zona | Agujeros | Objetivo | Tiempo | Novedad |
| --- | --- | --- | --- | --- | --- |
| 1 | Campo | 3 | 150 | 45 s | Solo topos normales |
| 2 | Campo | 3 | 200 | 45 s | — |
| 3 | Campo | 3 | 250 | 45 s | Aparece el **topo dorado** |
| 4 | Huerto | 6 | 300 | 45 s | Entra el **topo enfadado** |
| 5 | Huerto | 6 | 400 | 45 s | — |
| 6 | Huerto | 6 | 450 | 45 s | Entran las **bombas** |
| 7 | Huerto | 6 | 500 | 50 s | — |
| 8 | Mina | 9 | 600 | 50 s | 9 agujeros |
| 9 | Mina | 9 | 700 | 50 s | Velocidad de Difícil |
| 10 | Mina | 9 | — | 60 s | **JEFE**: topo gigante que necesita **10 golpes** y salta de agujero en agujero |

### Contrarreloj

| Dificultad | Agujeros | Tiempo | Objetivo |
| --- | --- | --- | --- |
| Fácil | 3 (1×3) | 60 s | 300 pts |
| Medio | 6 (2×3) | 60 s | 500 pts |
| Difícil | 9 (3×3) | 60 s | 800 pts |

- Hay que llegar a la puntuación objetivo antes de que acabe el tiempo.
- Las bombas aparecen a partir de Medio.

### Infinito

| Dificultad | Agujeros | Vidas |
| --- | --- | --- |
| Fácil | 3 | 5 |
| Normal | 6 | 3 |
| Difícil | 9 | 3 + más bombas |

- No hay tiempo; la partida termina al perder todas las vidas.
- **Pierdes 1 vida** al golpear una bomba **y también cada vez que un topo se esconde sin golpearlo**.
- Empieza a la velocidad de Fácil y **cada 20 s va un 10 % más rápido** (mínimo 0,5 s visible).

## Velocidad de los topos

| Dificultad | Tiempo visible | Sale uno cada | Topos a la vez |
| --- | --- | --- | --- |
| Fácil | 1,5 s | 1,2 s | 1 |
| Medio | 1,1 s | 0,9 s | hasta 2 |
| Difícil | 0,8 s | 0,6 s | hasta 3 |

Son los valores de partida. Se ajustan en las variables **«⚙ Ajustes del juego»** de Figma (ver más abajo).

## Topos y objetos

| Elemento | Golpes | Efecto |
| --- | --- | --- |
| Topo normal | 1 | +10 puntos |
| Topo enfadado | 2 | +25 puntos (tras el 1.er golpe lleva una tirita) |
| Topo dorado | 1 | +50 puntos; en Infinito además **+1 vida** (sin pasar de las vidas iniciales). Sale 1 de cada 20 topos y solo 0,7 s |
| Bomba | — | NO hay que tocarla: −20 puntos (en Infinito, −1 vida). Cuenta como error |

Cuántos de cada tipo salen se define **de cada 20 apariciones** (por ejemplo, Contrarreloj Medio: 4 enfadados, 3 bombas y 1 dorado de cada 20; el resto, topos normales).

**Al golpear un topo** desaparece y en su agujero sale un **estallido rojo con los puntos ganados en el centro** (`golpe_estallido`, igual en las 3 temáticas). Se cambió el 02/11 porque, en las pruebas, el topo mareado con la lengua fuera parecía seguir vivo y los jugadores lo volvían a tocar.

## Reglas de puntuación

- **Fallo** (tocar un agujero vacío): **no resta puntos**, pero cuenta como error en `errors_made`. Tocar una bomba también cuenta como error.
- **Combo:** con **5 aciertos seguidos** (sin fallar ni dejar escapar ningún topo) los puntos valen **x2** hasta el siguiente fallo o escape. Se muestra "¡COMBO!" en el HUD.

## Ayuda, sonido y ranking

- **Botón ? de ayuda:** **pausa la partida** (igual que el botón de pausa) y muestra la leyenda de topos y bomba con la etiqueta «Partida en pausa» y el botón **Continuar**. Cada pulsación suma 1 a `help_clicks`.
- **Reiniciar / Repetir / Reintentar** (Pausa, Victoria, Tiempo agotado y Fin de partida) vuelven a empezar **la misma partida** que se estaba jugando (modo, dificultad o nivel).
- **Sonido:**
  - Una **música alegre en bucle por temática** (feria, Halloween, villancico chiptune).
  - **Efectos:** golpe, topo enfadado, explosión, escape, combo, victoria y derrota.
  - Todo con licencia libre (por ejemplo, Pixabay o Kenney). Volumen independiente en Ajustes.
- **Ranking global:** top 10 de todos los jugadores por modo y dificultad, leído de SQL Server mediante la API. La fila del jugador se resalta aunque no esté en el top.

## Figma

Archivo: https://www.figma.com/design/5Rcgc2CQGTvKkJzfNmcxAG/Topohit?node-id=21-184&t=zlS6XIou2qUk47dH-1 (equipo de estudiante).

- **📘 GDD:** 8 láminas con el documento de diseño.
- **⚙ Ajustes del juego:** guía de cómo cambiar los números del juego.
- **📐 Wireframe:** 21 pantallas en gris con notas y prototipo conectado (flujo "Topohit · Wireframe", empieza en Login).
- **🎨 Arte · Temáticas:** pantallas con el arte final de las 3 temáticas y prototipo conectado, con un flujo por temática. Las partidas son **jugables y leen los ajustes en vivo**: puntos, vidas, combo, cuenta atrás con barra y estadísticas reales. Infinito tiene una pantalla por tramo de velocidad.
- **🧩 Assets:** las imágenes como componentes (topos de las 3 temáticas, objetos, estallido de golpe, logos y fondos).

### Pantallas

1. **Acceso:** Login, Registro.
2. **Menú y selección:**
   - Menú principal, con los 3 botones de temática.
   - Selección de modo.
   - Dificultad de Contrarreloj y dificultad de Infinito.
   - Mapa Historia.
   - Inicio de nivel (overlay).
3. **Partida:**
   - Partida Fácil, Medio y Difícil.
   - Partida Infinito (por tramos) y Partida Historia.
   - Pausa y Ayuda (overlays; los dos pausan la partida).
   - Victoria, Fin de partida y Tiempo agotado.
4. **Otros:** Ranking, Perfil (con historial de partidas), Ajustes, Cómo jugar.

**Barra superior de la partida (HUD):**

- Botón de pausa.
- Puntos.
- Tiempo, o las vidas en Infinito.
- Objetivo, o el récord en Infinito.
- Combo.
- Botón **?** de ayuda.

**Requisitos del PDF cubiertos:**

- Más de una Activity.
- RecyclerView en Ranking, en el historial del Perfil y en el Mapa Historia.
- Login y varios perfiles.

## Ajustes del juego (en Figma)

Todos los números del juego están en la colección de variables **«⚙ Ajustes del juego»** del archivo de Figma (101 valores, agrupados en Contrarreloj, Infinito, Historia, Puntos y Combo):

- tiempo visible de cada tipo de topo y cada cuánto sale uno;
- duración, objetivo, vidas y aceleración de Infinito;
- cuántos enfadados, bombas y dorados salen de cada 20;
- puntos de cada topo, lo que resta la bomba y el combo.

**Cómo se cambian:** panel Variables → «⚙ Ajustes del juego» → cambiar el número → **Present**. El prototipo lee los valores al empezar cada partida; **no hace falta ningún plugin**. Cada variable indica en su descripción el valor del GDD.

**Fijo en el prototipo:** las animaciones (golpe 0,5 s, explosión 0,6 s), los tramos de Infinito (cada 20 s, 5 tramos) y la precisión de 0,1 s en los tiempos visibles (entre 0,3 y 3 s).

**Variable `partidaActual`** (colección «Partida (prototipo)»): cada botón que empieza una partida guarda cuál es (`CR Fácil`, `INF Normal`, `Nivel 3`…). Reiniciar, Repetir y Reintentar la leen para volver a la misma partida. Si se abre el prototipo directamente en una partida, está vacía y reinicia Contrarreloj Medio (o Infinito Normal en Fin de partida).

**Herramientas opcionales** (carpeta `topohit-config-plugin`):

- **Plugin «Topohit · Configurador del juego»:** las mismas variables en tablas, un simulador que mide el tiempo de reacción y el % de escapados y sugiere ajustes, y exportación a JSON, CSV y **`GameConfig.kt`**.
- **Versión web compartida (Configurador Topohit):** el mismo simulador con enlace, sin instalar nada. Guarda configuración y partidas en común para el equipo.

## Datos que guarda cada partida (para la API / SQL Server)

- **Obligatorios (PDF):** `username`, `session_id`, `date_time`, `session_length`, `errors_made` (fallos y bombas), `final_score`.
- **Propios:**
  - `mode`, `difficulty` o `level_reached`, `n_attempts`.
  - `help_clicks`, `moles_escaped`, `hits`, `accuracy`, `max_combo`.
  - `golden_hits`, `abandoned` (si se salió desde Pausa).
- `abandoned`, `errors_made` y `help_clicks` son útiles para el informe de abandono de Métricas.

## Estilo artístico

- Basado en el **topo final**: cartoon retro, contorno negro grueso, colores planos, topo marrón con nariz rosa y dientes grandes.
- **Sprites del topo** (8 por temática, fondo transparente, mismo tamaño y alineados por abajo):
  - **Normal:** asomando orejas, asomando a media altura, feliz, golpeado, enfadado, enfadado con tirita, burlón escondiéndose, dorado.
  - **Halloween:**
    - Topo brujo: feliz y golpeado.
    - Vampiro enfadado: normal y con tirita.
    - Fantasma burlón y brujo dorado.
    - Bomba-calabaza y su explosión.
  - **Navidad:**
    - Topo Papá Noel: feliz y golpeado.
    - Reno enfadado: normal y con tirita.
    - Burlón con bufanda y dorado navideño.
    - Regalo-bomba y su explosión.
- **Objetos comunes:** bomba clásica, explosión, **estallido de golpe** (rojo, con los puntos en el centro), agujero de tierra, martillo de feria, corazón de vida lleno y vacío, estrella llena y vacía.
- **Fondos de partida** (horizontal, cielo arriba y suelo despejado abajo para los agujeros): campo (Normal), noche con luna y calabazas (Halloween), paisaje nevado (Navidad).
- **Logos:** TOPOHIT con el topo y el martillo, uno por temática.
- **Animación básica:** asomar → salir → (estallido con puntos | burlarse y esconderse). El topo enfadado tiene dos estados para sus 2 golpes. El sprite «golpeado» ya no se usa en la partida.
- Imágenes generadas con ChatGPT y recortadas con fondo transparente. Están en `topohit_sprites.zip` (topos) y `topohit_assets.zip` (objetos, fondos y logos).
- **Pendiente de arte:** el topo jefe gigante del nivel 10. Se puede usar el sprite enfadado a doble tamaño con una corona.
