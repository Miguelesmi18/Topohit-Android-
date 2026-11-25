# Projecte de Jocs — Resumen del proyecto

Actualizado: 25/11/2026 · Equipo: Borja, Miguel y Daniil (ciclo **DAM**). Darell ya no está en el grupo desde el 13/11/2026.

> ⚠️ Somos de DAM: los apartados de los PDF marcados para desarrolladores web (DAW) no se hacen. Base de datos del proyecto: **SQL Server**.

> 🆕 **24/11/2026:** el juego Topohit queda **solo con el modo Infinito** y dificultades que se desbloquean por puntos
> o que bloquea el tutor desde el escritorio. Estado y pendientes en [`estado-y-pendientes.md`](estado-y-pendientes.md).

## El proyecto en 30 segundos

Hay que hacer **un juego educativo para tablets Android** pensado para niños de 5 años, **una app de escritorio en C#** para que las educadoras vean cómo juega cada niño, y **desplegar Odoo** para gestionar el propio proyecto con SCRUM. El juego no es solo para divertirse: sus datos deben ayudar a detectar niños con posibles dificultades de aprendizaje.

- **Quién lo encarga:** las alumnas del ciclo de Educación Infantil. Ellas probarán el juego y nos darán feedback.
- **Para quién es el juego:** niños de 5 años, así que botones grandes, partidas cortas y nada de frustración.
- **Quién usa los datos:** educadoras y profesores, desde la app de escritorio.
- **Qué se entrega:** el juego Android, la app de escritorio C# con base de datos SQL Server y los dos despliegues de Odoo (Windows y Linux).
- **Lo que NO puede ser:** un memory. Lo ponen como ejemplo, pero está prohibido copiarlo.

## Cómo encaja todo

```
Niños de 5 años ──juegan──> Juego Android (Kotlin)
                                 │  API JSON
                                 ▼
                        SQL Server (usuarios, roles, partidas)
                                 ▲  API JSON
Educadoras ──consultan──> App de escritorio C#

Odoo + módulo Project (SCRUM): organiza el trabajo del equipo, no toca los datos del juego.
```

## Fases del proyecto

1. **Investigación:** características y necesidades de los niños de 3 a 5 años.
2. **Desarrollo del juego:** prototipo en Figma, luego Kotlin.
3. **App de escritorio en C#:** recoger y analizar los datos de las partidas (tiempo de respuesta, aciertos, errores).
4. **Despliegue de Odoo:** Windows (monolocal) y Linux (cliente-servidor) para seguir el proyecto con SCRUM.
5. **Pruebas y evaluación:** las alumnas de Educación Infantil prueban el juego y la app y nos dan feedback.
6. **Presentación final:** presentación técnica ante profesores y compañeros, y una presentación pública.

## Los 5 retos

| Reto | Qué hay que hacer | Tecnología | Se entrega |
| --- | --- | --- | --- |
| Joc amb Educació Infantil | Juego educativo con varios niveles para tablet Android (en Topohit: las 3 dificultades de Infinito, que se desbloquean) | Figma, Kotlin, Android Studio | Prototipo Figma + juego en .zip (Classroom) |
| Gestió de dades | App de escritorio para gestionar juegos y usuarios y ver estadísticas de cada niño | C#, SQL Server, API JSON | Prototipo (papel o digital) + entrevista, luego la app completa |
| Protecció dels usuaris | Usuarios con roles y contraseñas cifradas | SQL Server compartido | Apps de escritorio y móvil (Classroom) |
| Mètriques dels jocs | Pipeline de datos y Machine Learning: ¿por qué abandonan los jugadores? | Python, Pandas, Matplotlib, Chaquopy | Informe PDF + vista «Mètriques» en la tablet (hecha) y pantalla Métricas en el escritorio |
| Desplegament d'Odoo | Odoo + módulo Project en dos máquinas virtuales | VirtualBox, Windows, Ubuntu Server 24.04 | Documentación + validación de las MV por la profesora |

### Juego Android: requisitos obligatorios

- Kotlin con Android Studio, **más de una Activity** y al menos un **RecyclerView**.
- **Login** con usuario y contraseña y **varios perfiles de jugador**: cada partida queda asociada a su niño.
- Interfaz para niños: controles grandes, feedback positivo, partidas cortas, sin enlaces externos ni compras.
- Accesible y sin sobrecarga sensorial.
- Mostrar el progreso del niño de forma visual.

### App de escritorio C#

- Gestionar juegos y usuarios.
- Estadísticas de cada niño: tiempo de respuesta, aciertos y errores.
- Implementar la **API** JSON para subir y descargar datos de SQL Server.

### Protección de usuarios: tres roles

- **Administrador de usuarios:** crea, modifica y elimina usuarios.
- **Administrador de juegos:** gestiona los juegos desde la app de escritorio.
- **Jugador:** solo juega.
- Usuario y **contraseña cifrada** en SQL Server, consultados desde las dos apps.

### Métricas: qué nos toca

| Apartado del PDF | ¿Lo hacemos? |
| --- | --- |
| Pipeline general + informe PDF | ✅ Obligatorio |
| *Per als desenvolupadors d'aplicacions* (DAM): Chaquopy + vista «Mètriques» | ✅ Hecho (el profesor dijo el 28/10 que era opcional; el equipo lo ha hecho en la tablet y en el escritorio) |
| *Per als desenvolupadors web* (DAW): Pyodide, Laravel, HTML | ❌ No |

Pipeline en 5 pasos (objetivo: descubrir qué hace que los jugadores abandonen):

1. **Capturar métricas** por sesión.
2. **Cargar y limpiar** con Pandas; calcular jugadores únicos, si vuelve a jugar, retención, abandono y duración media.
3. **Visualizar** con Matplotlib: distribuciones, correlaciones, actividad diaria, nivel donde más abandonan.
4. **Modelo predictivo** (árbol de decisión, regresión logística o random forest) para predecir si un jugador volverá; evaluar con matriz de confusión, accuracy, precision y recall.
5. **Conclusiones** para mejorar dificultad, tutoriales, ayudas y niveles (en la presentación, no en la app).

Se juegan **5 a 10 partidas reales** para sacar rangos y un script Python **simula 200 a 500 sesiones**, con reglas de frustración (p. ej. muchos errores o ayudas → +75 % de probabilidad de no volver).

### Odoo: tarea individual

- **Windows (monolocal):** MV con Windows, Odoo y módulo Project.
- **Linux (cliente-servidor):** Ubuntu Server 24.04 sin entorno gráfico, SSH, «Source Install», IP fija en 192.168.100.0 y acceso remoto.
- Módulo Project con SCRUM (product backlog, sprint backlog, To Do, In Progress, In Review, Testing, Done).
- Documentar todo y **avisar a la profesora** para que valide las MV.

## Qué datos guarda cada partida

| Dato | Nombre sugerido | Ejemplo | Obligatorio |
| --- | --- | --- | --- |
| Usuario del niño | username | nen_lucia | Sí |
| Identificador de sesión | session_id | 1042 | Sí |
| Fecha y hora | date_time | 2026-11-12 10:35 | Sí |
| Duración de la sesión | session_length | 180 s | Sí |
| Errores cometidos | errors_made | 6 | Sí |
| Puntuación | final_score | 850 | Sí |
| ~~Nivel alcanzado~~ | ~~level_reached~~ | — | Era de Historia: se quitó el 24-25/11. Topohit guarda en su lugar `difficulty`, `hits`, `moles_escaped`, `max_combo`, `golden_hits`, `abandoned` y `avg_reaction_ms` |
| Intentos | n_attempts | 2 | Mínimo 4 métricas propias |
| Ayudas pedidas | help_clicks | 4 | Mínimo 4 métricas propias |

## Fechas y entregas

La única fecha de los PDF es el **21/11/2026**: primera versión del informe de Métricas. El resto hay que mirarlo en Classroom.

| Entrega | Reto | Fecha |
| --- | --- | --- |
| Informe de Métricas v1 (PDF) | Mètriques | 21/11/2026 |
| Prototipo del juego en Figma | Joc | Sin fecha en el PDF |
| Prototipo de la app de escritorio + entrevista | Gestió de dades | Sin fecha en el PDF |
| Odoo Windows + validación MV | Odoo | Sin fecha en el PDF |
| Odoo Linux + validación MV | Odoo | Sin fecha en el PDF |
| Juego final en .zip | Joc | Sin fecha en el PDF |
| App de escritorio completa | Gestió de dades | Sin fecha en el PDF |
| Pestaña «Mètriques» en la app (opcional) | Mètriques | Sin fecha en el PDF |
| Presentación técnica y pública | Todo | Sin fecha en el PDF |

## Checklist

Revisado el 25/11/2026:

- [x] El juego **no es un memory** (golpea topos) y tiene **varios niveles** (las 3 dificultades de Infinito)
- [x] Más de una Activity y al menos un RecyclerView
- [x] Login y varios perfiles de niño sin mezclar partidas
- [x] Cada partida guarda usuario, sesión, fecha y hora, duración, errores, puntuación y más de 4 métricas propias
- [x] Contraseñas cifradas en SQL Server (bcrypt) y tres roles
- [x] API JSON entre el juego y la app C#
- [x] Script de simulación de 400 sesiones con reglas de frustración
- [x] Vista «Mètriques» con gráficos y números del modelo, sin textos de conclusiones (tablet: Perfil → Mis estadísticas)
- [ ] Entregar el informe de Métricas v1 (`informe_metricas.pdf`, era para el 21/11)
- [x] Odoo por cada uno (Borja, Miguel y Daniil): Windows monolocal y Ubuntu cliente-servidor, IP fija y SSH (según el equipo)
- [x] Avisar a la profesora para validar las MV (según el equipo)
- [ ] Prototipo de la app de escritorio revisado **antes de programarla** (comprobar que consta la entrevista)
- [ ] Probar el juego con las alumnas de Educación Infantil

Fuentes: los seis PDF del proyecto + indicaciones del profesor (28/10/2026).
