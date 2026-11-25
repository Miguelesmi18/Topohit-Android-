# Enlaces del proyecto

**Equipo:** Borja, Miguel y Daniil. Darell ya no está en el grupo desde el 13/11/2026.

## GitHub: API del juego (ASP.NET Core Web API)

- https://github.com/TankistyT80/ASP.NET-Core-Web-API-Topohit

## Figma: prototipo del juego (Topohit)

- https://www.figma.com/design/5Rcgc2CQGTvKkJzfNmcxAG/Topohit?node-id=21-184&t=zlS6XIou2qUk47dH-1
- Qué hay dentro (GDD, ajustes, wireframe, arte y assets): ver `topohit-diseno-del-juego.md`.

## Airtable: gestión de tareas (SCRUM)

- **Base «Proyecto-golpea topos»**, tabla TAREAS GOLPEA TOPOS: https://airtable.com/appiUp3vBCEsYVJ2X/tblXoWJ1A31SJHOiH/viwfew7SOoPfuKvlK?blocks=hide
- Tabla SUBTAREAS: https://airtable.com/appiUp3vBCEsYVJ2X/tblJYAevu5e1mtGSU/viwRVMfTv0tKlRU36?blocks=hide
- IDs: base `appiUp3vBCEsYVJ2X` · TAREAS `tblXoWJ1A31SJHOiH` · SUBTAREAS `tblJYAevu5e1mtGSU`
- Las dos vistas están filtradas por **Sprint 1**. Hay más sprints: Sprint 2, Increment, Sprint 3 y Fi del projecte.

### Cómo está organizado
- **TAREAS:** tareas grandes (G01, G02…) con área, sprint, asignados, prioridad, estado, fechas, horas estimadas, horas reales, dependencias, notas y subtareas.
- **SUBTAREAS:** cada tarea se divide en subtareas (G01.1, G01.2…) con ESTADO (Backlog, To do, En curso, Done), dificultad, fechas, horas estimadas y horas reales.

### Horas reales: cómo se rellenan
- Se escriben **a mano en las dos tablas** (TAREAS y SUBTAREAS), aunque el campo esté duplicado. No se calculan solas.
- Motivo: hay tareas sin subtareas, y una tarea siempre lleva pasos que no entran en sus subtareas (reuniones, revisar, juntar, entregar…). Por eso **las horas reales de una tarea nunca coinciden con la suma de sus subtareas**.
- Solo se rellenan cuando la tarea o subtarea está en **Done**. Se pone una estimación razonable: algunas coinciden con lo previsto y otras se desvían un poco.

### Horas reales rellenadas (13/11/2026)
**TAREAS (Sprint 1, todas en Done)**
| Tarea | Estimadas | Reales |
| --- | --- | --- |
| G01 · Organización del equipo y Scrum | 4,0 | 5,0 |
| G02 · Diseño del juego (GDD) | 9,0 | 10,5 |
| G03 · Definir las métricas por sesión | 3,5 | 3,5 |
| G06 · Prototipo del juego y tecnología | 6,5 | 8,0 |
| G20 · Odoo en Windows (individual, 3 personas) | 24,0 | 21,0 |

**SUBTAREAS (en Done)**
| Subtarea | Estimadas | Reales |
| --- | --- | --- |
| G20.2 · Odoo Windows — Miguel | 6,0 | 7,5 |
| G02.0 · Investigación: características y necesidades de los niños | 2,0 | 2,0 |

### Salida de Darell (13/11/2026)
- Se ha quitado la opción «Darell» del campo ASIGNADO en TAREAS y en SUBTAREAS.
- Se han borrado sus subtareas individuales de Odoo (G20.4 y G21.4). Están en la papelera de Airtable unos días por si hiciera falta recuperarlas.
- Sus subtareas en solitario se han repartido:
  - **Miguel:** G01.4, G11.2, G11.3, G11.4, G12.1, G12.2, G12.5 y G13.2.
  - **Borja:** G03.1, G03.3, G12.3, G15.2 y G15.4.
- Tareas principales: G11 pasa a Miguel; en G12 están ahora Daniil, Miguel y Borja.
- Pendiente de decidir: las HORAS ESTIMADAS de G20 (24 h) y G21 (32 h) se calcularon para 4 personas.
