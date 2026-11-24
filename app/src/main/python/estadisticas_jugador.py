# =====================================================================
#  TOPOHIT · Métricas · Estadísticas propias del jugador (pestaña de la tablet)
#  La tablet le pasa las partidas del niño que ha entrado (GET api/sesiones/mias, en JSON)
#  y este script devuelve, también en JSON:
#   - los números (partidas, récord, duración media, precisión, ayudas...);
#   - las gráficas de Matplotlib en base64, para que Kotlin las pinte.
#  Sin textos de conclusiones (lo pide el PDF de Métricas).
#  Es el mismo estilo que metricas/03_analisis.py; ese es el de todos los jugadores (escritorio).
# =====================================================================

import json

# Paso 1: Pandas - Cargar las partidas que manda la tablet
import pandas as pd

DIFICULTADES = ["Fácil", "Normal", "Difícil"]
COLORES = {"Fácil": "#5BAA3A", "Normal": "#F2A93B", "Difícil": "#D9534F"}
ULTIMAS = 20          # en las gráficas por partida solo salen las últimas (si no, no se leen)


def cargar(json_partidas):
    df = pd.DataFrame(json.loads(json_partidas))
    if len(df) > 0:
        df = df[df["modo"] == "Infinito"]           # las partidas antiguas de otros modos no cuentan
    if len(df) > 0:
        # Las fechas de la API pueden venir con milisegundos o sin ellos: ISO8601 acepta las dos
        df["dateTime"] = pd.to_datetime(df["dateTime"], format="ISO8601")
        df = df.sort_values("dateTime").reset_index(drop=True)
        df["numero"] = range(1, len(df) + 1)           # número de partida: 1, 2, 3...
        df["dia"] = df["dateTime"].dt.normalize()
    return df


# Paso 2: Pandas - Métricas del jugador
def metricas_jugador(df):
    terminadas = df[df["abandoned"] == False]
    mejor = 0
    if len(terminadas) > 0:
        mejor = int(terminadas["finalScore"].max())
    intentos = int(df["hits"].sum() + df["errorsMade"].sum())
    precision = 0
    if intentos > 0:
        precision = round(int(df["hits"].sum()) * 100 / intentos)
    return {
        "partidas": int(len(df)),
        "abandonadas": int(df["abandoned"].sum()),
        "mejor_puntuacion": mejor,
        "puntuacion_media": int(round(df["finalScore"].mean())),
        "duracion_media": int(round(df["sessionLength"].mean())),
        "minutos_jugados": int(df["sessionLength"].sum() // 60),
        "precision": precision,
        "ayudas_por_partida": round(float(df["helpClicks"].mean()), 1),
        "dias_jugados": int(df["dia"].nunique()),
    }


# Paso 3: Matplotlib - Gráficas
import matplotlib

# Esta parte es de información externa que no sale en los vídeos
# (backend Agg: dibujar sin ventana, que es lo que hace falta dentro de la tablet)
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def colores_partidas(filas):
    colores = []
    for dificultad in filas["difficulty"]:
        colores.append(COLORES.get(dificultad, "#888888"))
    return colores


# Partidas en el eje X: números enteros (1, 2, 3...), sin decimales
def eje_partidas(numeros):
    if len(numeros) <= ULTIMAS:
        plt.xticks(numeros)


# El título de cada gráfica lo pinta la tablet encima de la imagen (por eso aquí no lleva plt.title)
def crear_graficas(df):
    graficas = []
    ultimas = df.tail(ULTIMAS)

    # 1. Puntos en cada partida (el color es la dificultad)
    fig = plt.figure(figsize=(8, 4.5))
    plt.plot(df["numero"], df["finalScore"], color="#B0B0B0", zorder=1)
    plt.scatter(df["numero"], df["finalScore"], c=colores_partidas(df), s=60, zorder=2)
    for dificultad in DIFICULTADES:
        plt.scatter([], [], color=COLORES[dificultad], label=dificultad)
    plt.xlabel("Partida")
    eje_partidas(df["numero"])
    plt.ylabel("Puntos")
    plt.legend()
    graficas.append(("Tus puntos en cada partida", fig))

    # 2. Récord de cada dificultad (las abandonadas no cuentan)
    terminadas = df[df["abandoned"] == False]
    records = terminadas.groupby("difficulty")["finalScore"].max().reindex(DIFICULTADES).fillna(0)
    fig = plt.figure(figsize=(8, 4.5))
    plt.bar(DIFICULTADES, records.values, color=[COLORES[d] for d in DIFICULTADES])
    plt.ylabel("Puntos")
    graficas.append(("Tu récord en cada dificultad", fig))

    # 3. Aciertos y fallos de las últimas partidas
    fig = plt.figure(figsize=(8, 4.5))
    plt.bar(ultimas["numero"] - 0.2, ultimas["hits"], width=0.4, color="#5BAA3A", label="Aciertos")
    plt.bar(ultimas["numero"] + 0.2, ultimas["errorsMade"], width=0.4, color="#D9534F", label="Fallos")
    plt.xlabel("Partida")
    eje_partidas(ultimas["numero"])
    plt.ylabel("Toques")
    plt.legend()
    graficas.append(("Aciertos y fallos", fig))

    # 4. Duración de las últimas partidas
    fig = plt.figure(figsize=(8, 4.5))
    plt.bar(ultimas["numero"], ultimas["sessionLength"], color=colores_partidas(ultimas))
    plt.xlabel("Partida")
    eje_partidas(ultimas["numero"])
    plt.ylabel("Segundos")
    graficas.append(("Cuánto duran tus partidas", fig))

    # 5. Tiempo de reacción (las partidas sin aciertos no tienen)
    reaccion = df.dropna(subset=["avgReactionMs"])
    fig = plt.figure(figsize=(8, 4.5))
    plt.plot(reaccion["numero"], reaccion["avgReactionMs"], marker="o", color="#3B7DD8")
    plt.xlabel("Partida")
    eje_partidas(reaccion["numero"])
    plt.ylabel("Milisegundos")
    graficas.append(("Lo rápido que golpeas", fig))

    # 6. Ayudas pedidas en las últimas partidas
    fig = plt.figure(figsize=(8, 4.5))
    plt.bar(ultimas["numero"], ultimas["helpClicks"], color="#F2A93B")
    plt.xlabel("Partida")
    eje_partidas(ultimas["numero"])
    plt.ylabel("Veces")
    plt.ylim(0, max(4, ultimas["helpClicks"].max() + 1))     # si no ha pedido ninguna, el eje va de 0 a 4
    graficas.append(("Ayudas pedidas", fig))

    # 7. Partidas de cada día
    por_dia = df.groupby("dia").size()
    fig = plt.figure(figsize=(8, 4.5))
    plt.bar(por_dia.index.strftime("%d/%m"), por_dia.values, color="#3B7DD8")
    plt.xlabel("Día")
    plt.ylabel("Partidas")
    fig.autofmt_xdate()
    graficas.append(("Partidas de cada día", fig))

    return graficas


# Paso 4: Resultado para la tablet
import io

# Esta parte es de información externa que no sale en los vídeos
# (base64: el PDF de Métricas pide pasar las gráficas a la app como imágenes en base64)
import base64


def a_base64(fig):
    memoria = io.BytesIO()
    fig.savefig(memoria, format="png", dpi=100, bbox_inches="tight")
    plt.close(fig)
    return base64.b64encode(memoria.getvalue()).decode("ascii")


# Lo que llama Kotlin: recibe las partidas en JSON y devuelve los números y las gráficas en JSON
def generar(json_partidas):
    df = cargar(json_partidas)
    resultado = {"partidas": 0, "graficas": []}
    if len(df) > 0:
        resultado = metricas_jugador(df)
        imagenes = []
        for titulo, fig in crear_graficas(df):
            imagenes.append({"titulo": titulo, "imagen": a_base64(fig)})
        resultado["graficas"] = imagenes
    return json.dumps(resultado)
