# Projecte de Jocs — Retos y aplicaciones

**Equipo:** Borja, Miguel y Daniil (Darell ya no está en el grupo desde el 13/11/2026)
**Ciclo:** DAM (Desarrollo de Aplicaciones Multiplataforma)
**Base de datos del proyecto:** SQL Server (compartida por el juego Android y la app de escritorio)

> ⚠️ **Somos de DAM.** Todo apartado que en los PDF ponga explícitamente *«Per als desenvolupadors web»* o que sea para DAW **no lo hacemos** (por ejemplo: Pyodide, Laravel o pestañas HTML).

## Retos y herramientas

| Reto | Qué se hace | Aplicaciones / herramientas |
| --- | --- | --- |
| Joc col·laborant amb Educació Infantil | Juego educativo con varios niveles para tablets Android (niños de 5 años) | Figma (prototipo), Android Studio (Kotlin) |
| Gestió de dades | App de escritorio para gestionar juegos y usuarios y ver estadísticas de cada niño | C# (app de escritorio), SQL Server |
| API i comunicació | API JSON para subir y descargar datos entre el juego y la app de escritorio | C# (API), SQL Server, Android Studio (cliente) |
| Protecció dels usuaris | Login, 3 roles (admin. usuarios, admin. juegos, jugador) y contraseñas cifradas | SQL Server, app C# y juego Android |
| Mètriques dels jocs | Simulación de sesiones, análisis y modelo predictivo de abandono | Python (Pandas, Matplotlib); integración en Android con Chaquopy **opcional** (ver abajo) |
| Desplegament d'Odoo (individual) | Odoo + módulo Project con SCRUM en Windows (monolocal) y Ubuntu (cliente-servidor) | VirtualBox, Windows, Ubuntu Server 24.04, Odoo |

## Mètriques dels jocs: qué nos toca

> ✅ **Confirmado por el profesor el 28/10/2026:** el apartado para DAM es opcional y el de web/DAW no lo hacemos.

| Apartado del PDF | ¿Lo hacemos? | Contenido |
| --- | --- | --- |
| Pipeline general (captura, limpieza, gráficos, modelo, conclusiones) + informe PDF | ✅ Sí, obligatorio | Python con Pandas y Matplotlib, modelo predictivo, simulación de 200–500 sesiones |
| *Per als desenvolupadors d'aplicacions* (DAM) | 🟡 Opcional: solo si da tiempo | Chaquopy para ejecutar Python en Android, datos CSV/JSON desde el almacenamiento interno o desde Kotlin, gráficos en base64 y vista «Mètriques» con gráficos y métricas del modelo |
| *Per als desenvolupadors web* (DAW) | ❌ No, no es de nuestro ciclo | Pyodide en el navegador, datos desde Laravel, gráficos incrustados en HTML |

**Prioridad por falta de tiempo:** primero el pipeline y el informe; la integración con Chaquopy solo al final si queda margen.
