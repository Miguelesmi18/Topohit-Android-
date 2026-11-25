# Topohit · Métricas (reto «Mètriques dels jocs»)

Pipeline en Python que intenta responder a **por qué dejan de jugar los niños**. Solo usa el modo Infinito.

Se ejecuta desde esta carpeta, en este orden:

| Script | Qué hace | Salida |
| --- | --- | --- |
| `01_playtest.py` | Descarga las partidas reales de Infinito de la API (`GET api/sesiones/todas` con el usuario `profe`) y saca el mínimo, la media y el máximo de cada variable por dificultad | `datos/playtest.csv`, `datos/rangos.csv` |
| `02_simulacion.py` | Genera **400 sesiones** con esos rangos y con las reglas de frustración | `datos/sesiones_simuladas.csv` |
| `03_analisis.py` | Calcula las métricas derivadas, hace las 11 gráficas (Matplotlib) y entrena un árbol de decisión que predice `returning_player` | `informe_metricas.pdf` (v2 del 25/11, 11 páginas), `graficas/*.png`, `graficas/metricas.json` |

```
pip install -r requirements.txt
python 01_playtest.py      # con la API arrancada
python 02_simulacion.py
python 03_analisis.py
```

Las **conclusiones** para la presentación técnica están en [`CONCLUSIONES.md`](CONCLUSIONES.md).

## Reglas de la simulación

- **Habilidad:** cada niño tiene una habilidad entre 0 y 1. Cuanta más tiene, más dura la partida, más puntos y aciertos
  consigue, antes reacciona y menos ayudas pide.
- **Frustración (la regla del PDF):** si en la primera partida hace **4 o más errores por minuto** o pide **2 o más ayudas**,
  la probabilidad de no volver sube un 75 % (de 0,20 a 0,95). Si abandona la primera partida a medias, sube otro 15 %.
- **Los que vuelven:** juegan de 1 a 8 partidas más y mejoran un poco en cada una. Abren Normal y Difícil con los puntos
  del juego: 500 en Fácil y 800 en Normal.
- **Ayudas:** las partidas reales son de antes del 24/11 y tienen `help_clicks = 0`. Por eso las ayudas se simulan
  de 0 a 4. Cuando haya partidas con ayudas, se vuelve a ejecutar `01_playtest.py`.

## Modelo

`DecisionTreeClassifier(max_depth=4, random_state=42)`. Usa estas features de la **primera partida** del jugador:

- duración, errores, puntos, aciertos y topos escapados;
- tiempo de reacción;
- ayudas;
- si la abandonó;
- errores por minuto.

Los datos se reparten 80 % para entrenar y 20 % para probar, con `train_test_split` y `random_state=42`.
Resultado con la semilla 42: accuracy 0,913, precision 0,875, recall 1,0 y ROC-AUC 0,865.
Las variables más importantes son los errores por minuto (0,67) y las ayudas (0,18): el modelo **descubre las reglas
de frustración** que se programaron.

## Pestaña «Mètriques» (tablet): hecha

Al final se ha hecho como pidió el equipo: **en la tablet, las estadísticas propias del niño**.
- El script es `app/src/main/python/estadisticas_jugador.py`, con el mismo estilo que `03_analisis.py`.
- Recibe las partidas de `GET api/sesiones/mias` desde Kotlin y devuelve los números y 7 gráficas en base64.
- **El modelo también va en la tablet (25/11)**, porque el PDF pide sus resultados en la vista «Mètriques».
  Chaquopy usa Python 3.11 (por la tablet de 32 bits) y ahí no tiene scikit-learn para 32 bits, así que
  `app/src/main/python/modelo_abandono.py` hace con numpy el mismo árbol de decisión que `03_analisis.py`
  (mismas features, `max_depth=4`, 80/20 estratificado, `random_state=42`), con las sesiones simuladas
  (`app/src/main/python/sesiones_simuladas.csv`, copia de `datos/`). Sale **lo mismo que en el escritorio**:
  accuracy 0,913, precision 0,875, recall 1,0 y ROC-AUC 0,865, con la misma matriz de confusión
  (comprobado contra scikit-learn). La importancia de las variables cambia un poco porque scikit-learn
  desempata al azar entre dos cortes igual de buenos.
- Si se vuelve a generar `datos/sesiones_simuladas.csv`, hay que copiarlo también a `app/src/main/python/`
  y a `GolpeaTopos-Escritorio/TopohitEscritorio/Python/`.

`03_analisis.py` (todos los jugadores, con el modelo de abandono) es para el **escritorio** y el informe PDF:

## generar_informe() (escritorio)

`generar_informe(ruta_csv)` de `03_analisis.py` devuelve un diccionario con tres partes:
- `metricas`: los números;
- `modelo`: accuracy, precision, recall y ROC-AUC;
- `graficas`: cada gráfica en PNG y base64.

No lleva textos de conclusiones, como pide el PDF. En el escritorio se enseñarán los PNG de `graficas/` y los
números de `graficas/metricas.json`, con las partidas de `GET api/sesiones/todas`.

## Código externo

Lo que no sale en el vídeo de la optativa lleva encima `# Esta parte es de información externa que no sale en los vídeos`:
- `urllib`;
- `precision_score`, `recall_score`, `roc_auc_score`, `roc_curve` y `confusion_matrix`;
- el backend `Agg`, `base64`, `PdfPages` y `__main__`.

El resto sigue el estilo del vídeo de RA1: pasos `# Paso N: Librería - Qué hace`, `pd`/`np`/`plt`, `df`, `X`/`y` y
`train_test_split(test_size=0.2, random_state=42)`.
