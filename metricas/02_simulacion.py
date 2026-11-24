# =====================================================================
#  TOPOHIT · Métricas · 2. Simulación de sesiones (data augmentation)
#  Con los rangos del playtest real (datos/rangos.csv) genera entre 200 y 500
#  partidas de Infinito simuladas, con las mismas columnas que la tabla SESION.
#
#  Reglas que se programan (el modelo de 03_analisis.py tiene que descubrirlas):
#   - Cada niño tiene una habilidad entre 0 y 1. Con más habilidad: partidas más largas,
#     más puntos, más aciertos, reacciona antes y pide menos ayudas.
#   - REGLA DE FRUSTRACIÓN (la del PDF): si en su primera partida comete muchos errores
#     en poco tiempo (4 o más por minuto) o pide 2 o más ayudas, la probabilidad de que
#     NO vuelva a jugar sube un 75 % (de 0,20 a 0,95).
#   - Si abandona la primera partida a medias, sube otro 15 %.
#   - Los que vuelven juegan de 1 a 8 partidas más, mejoran un poco en cada una y van
#     abriendo Normal (500 puntos en Fácil) y Difícil (800 puntos en Normal), como en el juego.
#
#  Supuesto: las partidas reales son de antes del 24/11 y no tienen ayudas (help_clicks = 0).
#  Las ayudas se simulan de 0 a 4, más cuanto menos habilidad tiene el niño.
#
#  Salida: datos/sesiones_simuladas.csv
# =====================================================================

# Paso 1: Pandas - Cargar los rangos del playtest
import pandas as pd

rangos = pd.read_csv("datos/rangos.csv", header=[0, 1], index_col=0)
print(rangos.T)

# Paso 2: NumPy - Reglas del juego y de la simulación
import numpy as np

np.random.seed(42)

VIDAS = {"Fácil": 5, "Normal": 3, "Difícil": 3}
PUNTOS_DESBLOQUEO = {"Normal": 500, "Difícil": 800}  # puntos en la dificultad anterior
OBJETIVO_SESIONES = 400                               # el PDF pide de 200 a 500
PROB_NO_VUELVE = 0.20
SUBIDA_FRUSTRACION = 0.75
SUBIDA_ABANDONO = 0.15
ERRORES_POR_MINUTO_ALTO = 4
AYUDAS_ALTAS = 2
PRIMER_DIA = pd.Timestamp("2026-10-01")
DIAS_DE_ALTAS = 40                                    # los niños empiezan a jugar en estos días


# Un valor entre el mínimo y el máximo del playtest según la habilidad (con un poco de ruido)
def valor(dificultad, variable, nivel):
    minimo = rangos.loc[dificultad, (variable, "min")]
    maximo = rangos.loc[dificultad, (variable, "max")]
    posicion = np.clip(nivel + np.random.normal(0, 0.15), 0, 1)
    return minimo + (maximo - minimo) * posicion


def crear_partida(usuario, fecha, dificultad, habilidad):
    abandonada = np.random.random() < 0.05 + 0.20 * (1 - habilidad)

    duracion = valor(dificultad, "session_length", habilidad)
    aciertos = valor(dificultad, "hits", habilidad)
    # Puntos por acierto de cada dificultad (sale de las medias del playtest)
    puntos_por_acierto = (rangos.loc[dificultad, ("final_score", "mean")] /
                          rangos.loc[dificultad, ("hits", "mean")])
    if abandonada:
        # Sale a medias: juega solo una parte de la partida y no pierde todas las vidas
        parte = np.random.uniform(0.1, 0.6)
        duracion = duracion * parte
        aciertos = aciertos * parte
        errores = np.random.randint(0, VIDAS[dificultad])
    else:
        errores = VIDAS[dificultad]      # en Infinito la partida acaba al perder las vidas

    duracion = max(10, int(round(duracion)))
    aciertos = int(round(aciertos))
    puntos = int(round(aciertos * puntos_por_acierto * np.random.uniform(0.9, 1.1)))
    escapados = int(round(valor(dificultad, "moles_escaped", 1 - habilidad)))
    racha = min(aciertos, int(round(valor(dificultad, "max_combo", habilidad))))
    dorados = min(aciertos, np.random.binomial(aciertos, 0.04))
    ayudas = min(4, np.random.poisson(2.2 * (1 - habilidad)))

    reaccion = None
    if aciertos > 0:
        reaccion = int(round(valor(dificultad, "avg_reaction_ms", 1 - habilidad)))

    intentos = aciertos + errores
    precision = None
    if intentos > 0:
        precision = round(aciertos * 100 / intentos, 2)

    return {"user_id": usuario, "date_time": fecha, "difficulty": dificultad,
            "session_length": duracion, "errors_made": errores, "final_score": puntos,
            "n_attempts": intentos, "hits": aciertos, "accuracy": precision,
            "moles_escaped": escapados, "max_combo": racha, "golden_hits": dorados,
            "abandoned": abandonada, "avg_reaction_ms": reaccion, "help_clicks": ayudas}


# La dificultad más alta que tiene abierta (a veces se queda en una más fácil)
def elegir_dificultad(record):
    abiertas = ["Fácil"]
    if record["Fácil"] >= PUNTOS_DESBLOQUEO["Normal"]:
        abiertas.append("Normal")
    if record["Normal"] >= PUNTOS_DESBLOQUEO["Difícil"]:
        abiertas.append("Difícil")
    if np.random.random() < 0.7:
        return abiertas[-1]
    return abiertas[np.random.randint(0, len(abiertas))]


# Paso 3: Simulación - Un niño detrás de otro hasta llegar a las sesiones que se buscan
sesiones = []
usuario = 0
while len(sesiones) < OBJETIVO_SESIONES:
    usuario = usuario + 1
    habilidad = np.random.uniform(0, 1)
    record = {"Fácil": 0, "Normal": 0, "Difícil": 0}

    # Primera partida: siempre en Fácil, en horario de clase
    fecha = (PRIMER_DIA + pd.Timedelta(days=np.random.randint(0, DIAS_DE_ALTAS))
             + pd.Timedelta(hours=np.random.randint(9, 17), minutes=np.random.randint(0, 60)))
    primera = crear_partida(usuario, fecha, "Fácil", habilidad)
    sesiones.append(primera)
    if not primera["abandoned"]:
        record["Fácil"] = primera["final_score"]

    # Regla de frustración: ¿vuelve a jugar?
    errores_por_minuto = primera["errors_made"] / (primera["session_length"] / 60)
    frustrado = errores_por_minuto >= ERRORES_POR_MINUTO_ALTO or primera["help_clicks"] >= AYUDAS_ALTAS
    prob_no_vuelve = PROB_NO_VUELVE
    if frustrado:
        prob_no_vuelve = prob_no_vuelve + SUBIDA_FRUSTRACION
    if primera["abandoned"]:
        prob_no_vuelve = prob_no_vuelve + SUBIDA_ABANDONO
    prob_no_vuelve = min(prob_no_vuelve, 0.95)

    if np.random.random() >= prob_no_vuelve:
        mas_partidas = np.random.randint(1, 9)
        for _ in range(mas_partidas):
            habilidad = min(1, habilidad + 0.03)      # practicando mejora un poco
            fecha = (fecha.normalize() + pd.Timedelta(days=np.random.randint(0, 5))
                     + pd.Timedelta(hours=np.random.randint(9, 17), minutes=np.random.randint(0, 60)))
            dificultad = elegir_dificultad(record)
            partida = crear_partida(usuario, fecha, dificultad, habilidad)
            sesiones.append(partida)
            if not partida["abandoned"] and partida["final_score"] > record[dificultad]:
                record[dificultad] = partida["final_score"]

# Paso 4: Pandas - Guardar
df = pd.DataFrame(sesiones)
df = df.sort_values("date_time").reset_index(drop=True)
df.insert(0, "session_id", range(1, len(df) + 1))
df.to_csv("datos/sesiones_simuladas.csv", index=False)

print(df.head())
print(f"Sesiones simuladas: {df.shape[0]} de {usuario} jugadores")
print(df.groupby("difficulty").size())
