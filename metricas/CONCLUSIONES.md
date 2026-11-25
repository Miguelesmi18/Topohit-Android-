# Topohit · Conclusiones de las métricas (para la presentación técnica)

El PDF del reto pide que las conclusiones vayan en la **presentación técnica**, no en la vista «Mètriques».
Salen de `03_analisis.py` con la semilla 42 (datos del 25/11/2026).

## De dónde salen los datos

- **Playtest real:** 29 partidas de Infinito de 8 jugadores (11 en Fácil, 13 en Normal y 5 en Difícil). Un 10 % se abandonaron.
- **Simulación:** 400 sesiones de 112 jugadores, entre el 1/10 y el 22/11, con los rangos del playtest y la regla de
  frustración del PDF (4 o más errores por minuto o 2 o más ayudas en la primera partida: +75 % de no volver).
- **Ojo:** las conclusiones sobre el abandono salen de datos **simulados**. El modelo encuentra las reglas que programamos,
  así que confirman la simulación, no a niños de verdad. Hay que repetirlo con las partidas de las pruebas con Educación Infantil.

## Qué hacen los jugadores

| Métrica | Valor |
| --- | --- |
| Jugadores (`player_count`) | 112 |
| Vuelven a jugar (`retention_rate`) | 59,8 % (67 jugadores) |
| Solo juegan una vez (`churn_rate`) | 40,2 % |
| Duración media de la partida (`average_session_length`) | 135 s |
| Partidas por jugador | 3,6 |
| DAU medio / MAU medio | 6,8 / 64 (DAU/MAU = 0,11) |
| Partidas abandonadas desde la pausa | 11 % |
| Ayudas por partida | 0,69 |

**Por dificultad:**

| | Partidas | Errores por partida | Abandonadas | Duración | Puntos | Reacción | Ayudas |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Fácil | 153 | 4,6 | 14 % | 115 s | 1041 | 738 ms | 0,88 |
| Normal | 94 | 2,7 | **16 %** | 134 s | 1734 | 554 ms | 0,70 |
| Difícil | 153 | 2,9 | 5 % | 156 s | 1945 | 544 ms | 0,50 |

- **Dónde se quedan:** 48 de los 112 jugadores no pasan de Fácil, 14 llegan como mucho a Normal y 50 llegan a Difícil.
  Casi todos los que no vuelven se quedan en Fácil: se van **en la primera partida**.
- **Fácil es donde más se falla:** 4,6 errores por partida y la reacción más lenta (738 ms). Es normal, porque es donde empiezan,
  pero es justo donde se decide si vuelven.
- **Normal es donde más se abandona a medias** (16 %). Es el salto de 3 a 6 agujeros.
- **Correlaciones:** las partidas largas dan más puntos (0,83). Los que piden más ayudas juegan menos rato y hacen menos puntos
  (−0,40 y −0,39): las ayudas las piden los que lo están pasando mal.

## Por qué dejan de jugar

| En la primera partida… | No vuelven |
| --- | --- |
| Menos de 2 errores por minuto | 11 % |
| De 2 a 4 errores por minuto | 26 % |
| De 4 a 6 errores por minuto | **94 %** |
| 6 o más errores por minuto | **93 %** |
| 0 ayudas / 1 ayuda | 24 % / 28 % |
| 2 ayudas / 3 ayudas | **77 % / 100 %** |
| No la abandonan / la abandonan | 36 % / 61 % |

**Modelo** (`DecisionTreeClassifier`, profundidad 4, 89 jugadores para entrenar y 23 para probar):
accuracy 0,91, precision 0,88, recall 1,0 y ROC-AUC 0,87. Matriz de confusión: de los 9 que no vuelven acierta 7;
de los 14 que vuelven acierta los 14.
Las variables que más pesan son los **errores por minuto (0,67)** y las **ayudas (0,18)**.

**¿El modelo ha descubierto las reglas de frustración?** Sí: las dos variables que más usa son las dos de la regla
(errores y ayudas), y el corte está cerca de los 4 errores por minuto que programamos.

## Propuestas para mejorar el juego

1. **Primera partida más amable (dificultad).** El abandono se decide en la primera partida en Fácil. Que el primer
   minuto vaya más lento (los topos más rato fuera) y que los primeros topos que se escapan no quiten vida: ahora,
   con 5 escapados en Fácil se acaba la partida.
2. **Ayuda antes de que la pidan (ayudas).** Pedir 2 ayudas ya es señal de abandono. Si un niño falla seguido o pide ayuda
   2 veces, enseñar solo la animación de «toca el topo» y bajar la velocidad un momento.
3. **Tutorial antes de la primera partida (tutoriales).** Ahora «Cómo jugar» solo sale si se pulsa el botón «?» del menú.
   Enseñarlo solo la primera vez, con un topo de práctica que no quita vidas.
4. **Un paso entre Fácil y Normal (niveles).** Normal es donde más se sale a medias: pasa de 3 a 6 agujeros y acelera más.
   Se puede empezar Normal más lento (aceleración desde el escritorio) o con menos agujeros al principio.
   El tutor ya puede bajar los 500 puntos para abrir Normal si un niño se atasca.
5. **Volver cada día (retención).** DAU/MAU de 0,11: juegan pocos días. La clasificación total y el aviso de
   «¡Has desbloqueado…!» ayudan; se podría añadir un reto del día.

## Siguiente paso

Repetir `01_playtest.py` → `02_simulacion.py` → `03_analisis.py` cuando haya partidas de las pruebas con Educación Infantil
(ahora ya se guardan las ayudas, `help_clicks`) y comprobar si las conclusiones se mantienen con niños de verdad.
