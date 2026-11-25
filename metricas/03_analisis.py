# =====================================================================
#  TOPOHIT · Métricas · 3. Análisis, modelo de predicción e informe
#  Lee las sesiones (datos/sesiones_simuladas.csv) y saca:
#   1. Las métricas derivadas (player_count, returning_player, retention_rate, churn_rate,
#      DAU/MAU, average_session_length...).
#   2. Las gráficas de las distribuciones, la actividad diaria y el abandono (Matplotlib).
#   3. Un árbol de decisión que predice si un niño volverá a jugar (returning_player) con
#      los datos de su PRIMERA partida, con su matriz de confusión, curva ROC y accuracy,
#      precision, recall y ROC-AUC.
#
#  generar_informe(ruta_csv) devuelve todo en un diccionario con las gráficas en base64:
#  es lo que llamará la pestaña «Mètriques» de la tablet (Chaquopy).
#  Si se ejecuta el script, además guarda las gráficas en graficas/, los números en
#  graficas/metricas.json y el informe en informe_metricas.pdf.
# =====================================================================

import os
import json

# Paso 1: Pandas - Cargar y preparar los datos
import pandas as pd
import numpy as np

# Columnas de la primera partida que usa el modelo para predecir si el niño vuelve
FEATURES = ["session_length", "errors_made", "final_score", "hits", "moles_escaped",
            "avg_reaction_ms", "help_clicks", "abandoned", "errores_por_minuto"]


def cargar(ruta_csv):
    df = pd.read_csv(ruta_csv)
    # Las fechas de la API pueden venir con milisegundos o sin ellos: ISO8601 acepta las dos
    df["date_time"] = pd.to_datetime(df["date_time"], format="ISO8601")
    df["dia"] = df["date_time"].dt.normalize()
    df["mes"] = df["date_time"].dt.to_period("M")
    df["errores_por_minuto"] = df["errors_made"] / (df["session_length"] / 60)
    # Sin aciertos no hay tiempo de reacción: se pone la media para que el modelo pueda usar la fila
    df["avg_reaction_ms"] = df["avg_reaction_ms"].fillna(df["avg_reaction_ms"].mean())
    df["abandoned"] = df["abandoned"].astype(int)
    return df


# Una fila por jugador: los datos de su primera partida y si ha vuelto a jugar
def tabla_jugadores(df):
    df = df.sort_values("date_time")
    partidas = df.groupby("user_id").size()
    jugadores = df.groupby("user_id").first()
    jugadores["sesiones"] = partidas
    jugadores["returning_player"] = (partidas >= 2).astype(int)
    return jugadores


# Paso 2: Pandas - Métricas derivadas
def metricas_derivadas(df, jugadores):
    dau = df.groupby("dia")["user_id"].nunique()
    mau = df.groupby("mes")["user_id"].nunique()
    player_count = len(jugadores)
    vuelven = int(jugadores["returning_player"].sum())
    return {
        "sesiones": int(len(df)),
        "player_count": int(player_count),
        "returning_players": vuelven,
        "retention_rate": round(vuelven * 100 / player_count, 2),
        "churn_rate": round((player_count - vuelven) * 100 / player_count, 2),
        "average_session_length": round(float(df["session_length"].mean()), 1),
        "dau_medio": round(float(dau.mean()), 2),
        "mau_medio": round(float(mau.mean()), 2),
        "dau_mau": round(float(dau.mean() / mau.mean()), 3),
        "sesiones_por_jugador": round(len(df) / player_count, 2),
        "partidas_abandonadas": round(float(df["abandoned"].mean() * 100), 2),
        "ayudas_por_partida": round(float(df["help_clicks"].mean()), 2),
    }


# Paso 3: Scikit-learn - Árbol de decisión: ¿volverá a jugar?
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score

# Esta parte es de información externa que no sale en los vídeos
# (precision, recall, ROC-AUC, curva ROC y matriz de confusión: el PDF de Métricas los pide
#  y en el vídeo de la optativa solo sale accuracy_score)
from sklearn.metrics import precision_score, recall_score, roc_auc_score, roc_curve, confusion_matrix


def entrenar_modelo(jugadores):
    X = jugadores[FEATURES]
    y = jugadores["returning_player"]
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    modelo = DecisionTreeClassifier(max_depth=4, random_state=42)
    modelo.fit(X_train, y_train)

    y_pred = modelo.predict(X_test)
    probabilidades = modelo.predict_proba(X_test)[:, 1]   # probabilidad de que vuelva

    importancias = pd.DataFrame({
        "feature": X.columns,
        "importancia": modelo.feature_importances_
    }).sort_values("importancia", ascending=False)

    falsos_positivos, verdaderos_positivos, _ = roc_curve(y_test, probabilidades)
    return {
        "modelo": modelo,
        "X": X,
        "accuracy": round(float(accuracy_score(y_test, y_pred)), 3),
        "precision": round(float(precision_score(y_test, y_pred)), 3),
        "recall": round(float(recall_score(y_test, y_pred)), 3),
        "roc_auc": round(float(roc_auc_score(y_test, probabilidades)), 3),
        "matriz": confusion_matrix(y_test, y_pred),
        "fpr": falsos_positivos,
        "tpr": verdaderos_positivos,
        "importancias": importancias,
        "jugadores_entrenamiento": int(len(X_train)),
        "jugadores_prueba": int(len(X_test)),
    }


# Paso 4: Matplotlib - Gráficas
import matplotlib

# Esta parte es de información externa que no sale en los vídeos
# (backend Agg: dibujar sin ventana, que es lo que hace falta en la tablet y para guardar el PDF)
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from sklearn.tree import plot_tree

DIFICULTADES = ["Fácil", "Normal", "Difícil"]
COLORES = {"Fácil": "#5BAA3A", "Normal": "#F2A93B", "Difícil": "#D9534F"}


def crear_graficas(df, jugadores, resultado):
    graficas = []

    # 1. Distribución de la duración de las partidas
    fig = plt.figure(figsize=(8, 5))
    plt.hist(df["session_length"], bins=30, color="skyblue", edgecolor="white")
    plt.xlabel("Duración de la partida (segundos)")
    plt.ylabel("Partidas")
    plt.title("Duración de las partidas")
    graficas.append(("duracion", fig))

    # 2. Distribución de las puntuaciones por dificultad
    fig = plt.figure(figsize=(8, 5))
    for dificultad in DIFICULTADES:
        puntos = df[df["difficulty"] == dificultad]["final_score"]
        plt.hist(puntos, bins=20, alpha=0.6, color=COLORES[dificultad], label=dificultad)
    plt.xlabel("Puntuación final")
    plt.ylabel("Partidas")
    plt.title("Puntuaciones por dificultad")
    plt.legend()
    graficas.append(("puntuaciones", fig))

    # 3. Errores y partidas abandonadas por dificultad
    por_dificultad = df.groupby("difficulty").agg(errores=("errors_made", "mean"),
                                                  abandonadas=("abandoned", "mean"),
                                                  partidas=("session_id", "count")).reindex(DIFICULTADES)
    fig, ejes = plt.subplots(1, 2, figsize=(10, 4.5))
    colores = [COLORES[d] for d in DIFICULTADES]
    ejes[0].bar(DIFICULTADES, por_dificultad["errores"], color=colores)
    ejes[0].set_ylabel("Errores por partida (media)")
    ejes[0].set_title("Errores por dificultad")
    ejes[1].bar(DIFICULTADES, por_dificultad["abandonadas"] * 100, color=colores)
    ejes[1].set_ylabel("% de partidas abandonadas")
    ejes[1].set_title("Partidas abandonadas por dificultad")
    graficas.append(("errores_dificultad", fig))

    # 4. Relación entre intentos y puntuación
    fig = plt.figure(figsize=(8, 5))
    for dificultad in DIFICULTADES:
        filas = df[df["difficulty"] == dificultad]
        plt.scatter(filas["n_attempts"], filas["final_score"], alpha=0.5,
                    color=COLORES[dificultad], label=dificultad)
    plt.xlabel("Intentos (toques)")
    plt.ylabel("Puntuación final")
    plt.title("Intentos y puntuación")
    plt.legend()
    graficas.append(("intentos_puntuacion", fig))

    # 5. Actividad diaria: jugadores activos (DAU) y partidas
    dau = df.groupby("dia")["user_id"].nunique()
    partidas_dia = df.groupby("dia").size()
    fig = plt.figure(figsize=(10, 4.5))
    plt.plot(dau.index, dau.values, marker="o", color="#3B7DD8", label="Jugadores activos (DAU)")
    plt.plot(partidas_dia.index, partidas_dia.values, color="#B0B0B0", label="Partidas")
    plt.xlabel("Día")
    plt.ylabel("Cantidad")
    plt.title("Actividad diaria")
    plt.legend()
    fig.autofmt_xdate()
    graficas.append(("actividad_diaria", fig))

    # 6. Abandono del juego según la primera partida: ayudas pedidas
    abandono_ayudas = (1 - jugadores.groupby("help_clicks")["returning_player"].mean()) * 100
    fig = plt.figure(figsize=(8, 5))
    plt.bar(abandono_ayudas.index.astype(str), abandono_ayudas.values, color="#D9534F")
    plt.xlabel("Ayudas pedidas en la primera partida")
    plt.ylabel("% de niños que no vuelven")
    plt.title("Abandono según las ayudas pedidas")
    graficas.append(("abandono_ayudas", fig))

    # 7. Abandono del juego según la primera partida: errores por minuto
    tramos = pd.cut(jugadores["errores_por_minuto"], bins=[0, 2, 3, 4, 6, 100],
                    labels=["0-2", "2-3", "3-4", "4-6", "6 o más"], include_lowest=True)
    abandono_errores = (1 - jugadores.groupby(tramos, observed=False)["returning_player"].mean()) * 100
    fig = plt.figure(figsize=(8, 5))
    plt.bar(abandono_errores.index.astype(str), abandono_errores.values, color="#F2A93B")
    plt.xlabel("Errores por minuto en la primera partida")
    plt.ylabel("% de niños que no vuelven")
    plt.title("Abandono según los errores por minuto")
    graficas.append(("abandono_errores", fig))

    # 8. Modelo: matriz de confusión
    matriz = resultado["matriz"]
    fig = plt.figure(figsize=(5.5, 5))
    plt.imshow(matriz, cmap="Blues")
    for fila in range(2):
        for columna in range(2):
            color = "black"
            if matriz[fila][columna] > matriz.max() / 2:
                color = "white"          # en las casillas oscuras el número va en blanco
            plt.text(columna, fila, str(matriz[fila][columna]), ha="center", va="center",
                     fontsize=16, color=color)
    plt.xticks([0, 1], ["No vuelve", "Vuelve"])
    plt.yticks([0, 1], ["No vuelve", "Vuelve"])
    plt.xlabel("Predicción del modelo")
    plt.ylabel("Realidad")
    plt.title("Matriz de confusión")
    graficas.append(("matriz_confusion", fig))

    # 9. Modelo: curva ROC
    fig = plt.figure(figsize=(6, 5.5))
    plt.plot(resultado["fpr"], resultado["tpr"], color="#3B7DD8",
             label="Árbol de decisión (AUC = " + str(resultado["roc_auc"]) + ")")
    plt.plot([0, 1], [0, 1], linestyle="--", color="#B0B0B0", label="Al azar")
    plt.xlabel("Falsos positivos")
    plt.ylabel("Verdaderos positivos")
    plt.title("Curva ROC")
    plt.legend()
    graficas.append(("curva_roc", fig))

    # 10. Modelo: importancia de cada variable
    importancias = resultado["importancias"].sort_values("importancia")
    fig = plt.figure(figsize=(8, 5))
    plt.barh(importancias["feature"], importancias["importancia"], color="#5BAA3A")
    plt.xlabel("Importancia (0 a 1)")
    plt.title("Qué variables usa el modelo")
    graficas.append(("importancias", fig))

    # 11. Modelo: el árbol entrenado
    fig = plt.figure(figsize=(20, 10))
    plot_tree(resultado["modelo"], feature_names=resultado["X"].columns,
              class_names=["No vuelve", "Vuelve"], filled=True, rounded=True, fontsize=8)
    plt.title("Árbol de decisión entrenado")
    graficas.append(("arbol", fig))

    return graficas


# Paso 5: Informe para la pestaña «Mètriques»
import io

# Esta parte es de información externa que no sale en los vídeos
# (base64: el PDF de Métricas pide pasar las gráficas a la app como imágenes en base64)
import base64


def a_base64(fig):
    memoria = io.BytesIO()
    fig.savefig(memoria, format="png", dpi=100, bbox_inches="tight")
    return base64.b64encode(memoria.getvalue()).decode("ascii")


def analizar(ruta_csv):
    df = cargar(ruta_csv)
    jugadores = tabla_jugadores(df)
    metricas = metricas_derivadas(df, jugadores)
    resultado = entrenar_modelo(jugadores)
    graficas = crear_graficas(df, jugadores, resultado)
    return metricas, resultado, graficas


def numeros_modelo(resultado):
    return {"accuracy": resultado["accuracy"], "precision": resultado["precision"],
            "recall": resultado["recall"], "roc_auc": resultado["roc_auc"],
            "jugadores_entrenamiento": resultado["jugadores_entrenamiento"],
            "jugadores_prueba": resultado["jugadores_prueba"]}


# Lo que llamará la tablet: los números y las gráficas en base64 (sin textos de conclusiones)
def generar_informe(ruta_csv):
    metricas, resultado, graficas = analizar(ruta_csv)
    imagenes = {}
    for nombre, fig in graficas:
        imagenes[nombre] = a_base64(fig)
        plt.close(fig)
    return {"metricas": metricas, "modelo": numeros_modelo(resultado), "graficas": imagenes}


# Paso 6: Matplotlib - Guardar las gráficas y el informe PDF (v2 del informe, 25/11/2026)
# Lo que pide el PDF de Mètriques: detalle de las métricas, visualización de los datos y resultados
# del modelo. Sin textos de conclusiones (esas van en la presentación técnica).
# Esta parte es de información externa que no sale en los vídeos
# (PdfPages: juntar en un PDF varias hojas hechas con Matplotlib)
from matplotlib.backends.backend_pdf import PdfPages
import textwrap

VERSION = "versión 2 · 25/11/2026"
A4 = (8.27, 11.69)
MARRON = "#3B2314"

# Métricas de cada partida (tabla SESION) y métricas derivadas, con lo que mide cada una
DESCRIPCIONES = [
    ("session_length", "Partida", "Duración de la partida en segundos"),
    ("errors_made", "Partida", "Toques en un agujero vacío o en una bomba"),
    ("final_score", "Partida", "Puntuación final de la partida"),
    ("n_attempts", "Partida", "Todos los toques: hits + errors_made"),
    ("hits", "Partida", "Topos golpeados"),
    ("moles_escaped", "Partida", "Topos que se han escapado sin golpear"),
    ("avg_reaction_ms", "Partida", "Tiempo medio hasta golpear un topo (ms)"),
    ("help_clicks", "Partida", "Veces que se ha pulsado la ayuda (?)"),
    ("abandoned", "Partida", "Si ha salido a medias desde la pausa"),
    ("difficulty", "Partida", "Dificultad de Infinito: Fácil, Normal o Difícil"),
    ("player_count", "Derivada", "Jugadores distintos"),
    ("returning_player", "Derivada", "El jugador ha jugado 2 partidas o más"),
    ("retention_rate", "Derivada", "% de jugadores que vuelven a jugar"),
    ("churn_rate", "Derivada", "% de jugadores que solo juegan una partida"),
    ("DAU / MAU", "Derivada", "Jugadores activos por día / por mes, y su proporción"),
    ("average_session_length", "Derivada", "Duración media de las partidas (s)"),
]

NOMBRES_VALORES = {
    "sesiones": "Sesiones", "player_count": "player_count", "returning_players": "Jugadores que vuelven",
    "retention_rate": "retention_rate (%)", "churn_rate": "churn_rate (%)",
    "average_session_length": "average_session_length (s)", "dau_medio": "DAU medio", "mau_medio": "MAU medio",
    "dau_mau": "DAU / MAU", "sesiones_por_jugador": "Sesiones por jugador",
    "partidas_abandonadas": "Partidas abandonadas (%)", "ayudas_por_partida": "Ayudas por partida",
}

# Qué enseña cada gráfica (solo la descripción, sin interpretarla)
PIES = {
    "duracion": "Cuántas partidas duran cada tiempo. Cada barra es un tramo de segundos; cuanto más alta, más partidas duran eso.",
    "puntuaciones": "Cuántas partidas acaban con cada puntuación, un color por dificultad. Si se solapan, se ven los dos colores.",
    "errores_dificultad": "Izquierda: errores medios por partida. Derecha: de cada 100 partidas, cuántas se dejan a medias.",
    "intentos_puntuacion": "Cada punto es una partida: a la derecha, más toques; arriba, más puntos. Enseña cómo van juntos.",
    "actividad_diaria": "Cada día: niños distintos que juegan (DAU, línea azul) y partidas jugadas (línea gris).",
    "abandono_ayudas": "Niños agrupados por las ayudas (?) de su primera partida: % de cada grupo que no vuelve a jugar.",
    "abandono_errores": "Niños agrupados por los errores por minuto de su primera partida: % de cada grupo que no vuelve.",
    "matriz_confusion": "Filas: lo que pasó de verdad. Columnas: lo que dijo el modelo. La diagonal son los aciertos.",
    "curva_roc": "Cuanto más se acerca la curva a la esquina de arriba a la izquierda, mejor. La diagonal es el azar.",
    "importancias": "Cuánto usa el árbol cada variable para separar a los niños. Suman 1 entre todas.",
    "arbol": "Cada caja es una pregunta sobre la primera partida; se baja por la izquierda si se cumple. Azul: vuelve; naranja: no vuelve.",
}

# Anexo: los archivos de Métricas y las librerías que usa cada uno
ARCHIVOS = [
    ("Topohit-Android-/metricas/01_playtest.py", "urllib, json, pandas"),
    ("Topohit-Android-/metricas/02_simulacion.py", "pandas, numpy"),
    ("Topohit-Android-/metricas/03_analisis.py", "pandas, numpy, scikit-learn, matplotlib"),
    ("Topohit-Android-/app/.../python/estadisticas_jugador.py", "pandas, matplotlib"),
    ("Topohit-Android-/app/.../python/modelo_abandono.py", "pandas, numpy, matplotlib"),
    ("Topohit-Android-/app/.../activities/MetricasActivity.kt", "Chaquopy, Retrofit, Gson"),
    ("GolpeaTopos-Escritorio/.../Python/metricas_escritorio.py", "pandas, scikit-learn, matplotlib"),
    ("GolpeaTopos-Escritorio/.../Python/analisis.py", "copia de 03_analisis.py"),
    ("GolpeaTopos-Escritorio/.../Pantallas (pantalla Métricas)", "C#: ejecuta Python y enseña los PNG"),
]


def nueva_hoja(titulo):
    fig = plt.figure(figsize=A4)
    fig.text(0.08, 0.95, titulo, fontsize=17, weight="bold", color=MARRON)
    fig.text(0.92, 0.025, "Topohit · Informe de métricas · " + VERSION, fontsize=7, color="gray", ha="right")
    return fig


def texto(fig, y, lineas, tam=9.5, separacion=0.021):
    for linea in lineas:
        fig.text(0.08, y, linea, fontsize=tam, color=MARRON)
        y = y - separacion
    return y


def tabla(fig, y_arriba, alto, filas, cabecera, anchos):
    ax = fig.add_axes([0.08, y_arriba - alto, 0.84, alto])
    ax.axis("off")
    t = ax.table(cellText=filas, colLabels=cabecera, colWidths=anchos, loc="upper left", cellLoc="left")
    t.auto_set_font_size(False)
    t.set_fontsize(8.5)
    t.scale(1, 1.35)
    for (fila, _), celda in t.get_celld().items():
        celda.set_edgecolor("#D8C9B0")
        if fila == 0:
            celda.set_facecolor("#F6E2B8")
            celda.set_text_props(weight="bold", color=MARRON)
    return ax


# Pone la imagen de una gráfica en una hoja A4, con su pie debajo
def poner_grafica(fig, figura, caja, pie):
    memoria = io.BytesIO()
    figura.savefig(memoria, format="png", dpi=150, bbox_inches="tight")
    memoria.seek(0)
    imagen = plt.imread(memoria)
    ax = fig.add_axes(caja)
    ax.imshow(imagen)
    ax.axis("off")
    # El pie se parte en líneas según el ancho de la gráfica, para que no se monte con la de al lado
    lineas = textwrap.wrap(pie, int(caja[2] * 112))
    for i in range(len(lineas)):
        fig.text(caja[0], caja[1] - 0.015 - i * 0.015, lineas[i], fontsize=8.5, color="dimgray", style="italic")


def hoja_portada(pdf):
    fig = plt.figure(figsize=A4)
    fig.text(0.5, 0.70, "Topohit", fontsize=40, weight="bold", ha="center", color="#F2A93B")
    fig.text(0.5, 0.64, "Informe de métricas", fontsize=24, ha="center", color=MARRON)
    fig.text(0.5, 0.60, "Reto «Mètriques dels jocs» · " + VERSION, fontsize=11, ha="center", color="dimgray")
    fig.text(0.5, 0.57, "Equipo: Borja, Miguel y Daniil (DAM)", fontsize=11, ha="center", color="dimgray")
    indice = ["1. Métricas capturadas y derivadas", "2. Datos: playtest real y simulación",
              "3. Visualización de los datos", "4. Resultados del modelo de predicción",
              "5. Dónde se ven las métricas", "Anexo: archivos y librerías"]
    y = 0.45
    for linea in indice:
        fig.text(0.30, y, linea, fontsize=12, color=MARRON)
        y = y - 0.035
    pdf.savefig(fig)
    plt.close(fig)


def hoja_metricas(pdf, metricas):
    fig = nueva_hoja("1. Métricas capturadas y derivadas")
    texto(fig, 0.91, [
        "Las métricas «de partida» las mide la tablet mientras se juega y las guarda al acabar (tabla SESION).",
        "Las «derivadas» no se guardan: se calculan con pandas juntando todas las partidas.",
    ], tam=9)
    filas = []
    for nombre, origen, descripcion in DESCRIPCIONES:
        filas.append([nombre, origen, descripcion])
    tabla(fig, 0.86, 0.44, filas, ["Métrica", "Tipo", "Qué mide"], [0.27, 0.13, 0.60])
    fig.text(0.08, 0.405, "Valores (sesiones simuladas a partir del playtest real)", fontsize=12,
             weight="bold", color=MARRON)
    texto(fig, 0.38, [
        "Cómo se calculan: un niño «vuelve» (returning_player) si tiene 2 partidas o más.",
        "retention_rate = niños que vuelven / niños × 100 · churn_rate = 100 − retention_rate.",
        "DAU = niños distintos que juegan un día (media de los días) · MAU = lo mismo por mes · DAU/MAU = DAU ÷ MAU.",
    ], tam=8.5, separacion=0.018)
    filas = []
    for nombre in metricas:
        filas.append([NOMBRES_VALORES.get(nombre, nombre), str(metricas[nombre])])
    tabla(fig, 0.315, 0.27, filas, ["Métrica", "Valor"], [0.5, 0.5])
    pdf.savefig(fig)
    plt.close(fig)


def hoja_datos(pdf, df, jugadores):
    fig = nueva_hoja("2. Datos: playtest real y simulación")
    reales = pd.read_csv("datos/playtest.csv")
    fig.text(0.08, 0.90, "Playtest real", fontsize=12, weight="bold", color=MARRON)
    y = texto(fig, 0.875, [str(len(reales)) + " partidas reales de Infinito jugadas en la tablet (tabla SESION, a través de la API).",
                          "Medias por dificultad (de aquí salen los rangos de la simulación):"])
    resumen = reales.groupby("difficulty").agg(partidas=("session_id", "count"),
                                               duracion=("session_length", "mean"),
                                               puntos=("final_score", "mean"),
                                               errores=("errors_made", "mean"),
                                               reaccion=("avg_reaction_ms", "mean")).reindex(DIFICULTADES)
    filas = []
    for dificultad in DIFICULTADES:
        r = resumen.loc[dificultad]
        filas.append([dificultad, str(int(r["partidas"])), str(round(r["duracion"], 1)), str(round(r["puntos"], 1)),
                      str(round(r["errores"], 1)), str(round(r["reaccion"], 1))])
    tabla(fig, y - 0.005, 0.10, filas, ["Dificultad", "Partidas", "Duración (s)", "Puntos", "Errores", "Reacción (ms)"],
          [0.18, 0.13, 0.17, 0.15, 0.14, 0.23])
    fig.text(0.08, 0.66, "Simulación (data augmentation)", fontsize=12, weight="bold", color=MARRON)
    texto(fig, 0.635, [
        str(len(df)) + " sesiones simuladas de " + str(len(jugadores)) + " niños (02_simulacion.py, numpy con random seed 42),",
        "con los rangos del playtest real y las mismas columnas que la tabla SESION.",
        "",
        "Reglas que se programan (el modelo tiene que descubrirlas):",
        "  · Cada niño tiene una habilidad entre 0 y 1: con más habilidad, partidas más largas,",
        "    más puntos, más aciertos, reacciona antes y pide menos ayudas.",
        "  · Regla de frustración (la del PDF del reto): si en su primera partida hace 4 errores",
        "    por minuto o más, o pide 2 ayudas o más, la probabilidad de no volver a jugar sube",
        "    un 75 % (de 0,20 a 0,95).",
        "  · Si abandona la primera partida a medias, sube otro 15 %.",
        "  · Los que vuelven juegan de 1 a 8 partidas más, mejoran un poco en cada una y abren",
        "    Normal (500 puntos en Fácil) y Difícil (800 puntos en Normal), como en el juego.",
        "",
        "Supuesto: las partidas reales no tienen ayudas (help_clicks se añadió el 24/11);",
        "en la simulación las ayudas van de 0 a 4, más cuanto menos habilidad tiene el niño.",
    ])
    pdf.savefig(fig)
    plt.close(fig)


def hoja_graficas(pdf, titulo, pares, intro=None):
    fig = nueva_hoja(titulo)
    cajas = [[0.08, 0.52, 0.84, 0.39], [0.08, 0.07, 0.84, 0.39]]
    if intro != None:
        texto(fig, 0.91, intro, tam=9, separacion=0.018)
        cajas = [[0.08, 0.50, 0.84, 0.35], [0.08, 0.06, 0.84, 0.37]]
    for i in range(len(pares)):
        nombre, figura = pares[i]
        poner_grafica(fig, figura, cajas[i], PIES[nombre])
    pdf.savefig(fig)
    plt.close(fig)


def hoja_modelo(pdf, resultado, graficas):
    fig = nueva_hoja("4. Resultados del modelo de predicción")
    texto(fig, 0.91, ["Un modelo de predicción aprende de ejemplos para adivinar algo nuevo. Aquí adivina si un niño",
                      "volverá a jugar mirando solo su primera partida. Cómo leer cada resultado: página siguiente."],
          tam=9, separacion=0.018)
    y = texto(fig, 0.86, [
        "Árbol de decisión (scikit-learn, DecisionTreeClassifier, max_depth = 4, random_state = 42).",
        "y: returning_player (1 si el niño juega 2 partidas o más).",
        "X: los datos de su primera partida:",
        "   " + ", ".join(FEATURES[:5]) + ",",
        "   " + ", ".join(FEATURES[5:]) + ".",
        "Separación: 80 % entrenamiento / 20 % prueba, con la misma proporción de cada clase (stratify).",
    ], tam=9)
    modelo = numeros_modelo(resultado)
    filas = [["accuracy", str(modelo["accuracy"]), "De todos los niños de prueba, cuántos acierta"],
             ["precision", str(modelo["precision"]), "Cuando dice «vuelve», cuántas veces es verdad"],
             ["recall", str(modelo["recall"]), "De los que vuelven de verdad, cuántos detecta"],
             ["ROC-AUC", str(modelo["roc_auc"]), "Lo bien que separa los dos grupos (0,5 azar; 1 perfecto)"],
             ["Jugadores de entrenamiento", str(modelo["jugadores_entrenamiento"]), "Con los que aprende (80 %)"],
             ["Jugadores de prueba", str(modelo["jugadores_prueba"]), "Con los que se evalúa (20 %, no los ha visto)"]]
    tabla(fig, y - 0.01, 0.15, filas, ["Indicador", "Valor", "Qué mide"], [0.30, 0.10, 0.60])
    poner_grafica(fig, graficas["matriz_confusion"], [0.08, 0.27, 0.40, 0.34], PIES["matriz_confusion"])
    poner_grafica(fig, graficas["curva_roc"], [0.52, 0.27, 0.40, 0.34], PIES["curva_roc"])
    pdf.savefig(fig)
    plt.close(fig)

    fig = nueva_hoja("4. Resultados del modelo (continuación)")
    poner_grafica(fig, graficas["importancias"], [0.16, 0.64, 0.68, 0.28], PIES["importancias"])
    poner_grafica(fig, graficas["arbol"], [0.01, 0.08, 0.98, 0.52], PIES["arbol"])
    pdf.savefig(fig)
    plt.close(fig)
    hoja_explicacion_modelo(pdf, resultado)


# Explica qué mide cada resultado del modelo y cómo se calcula, con los números de este informe
def hoja_explicacion_modelo(pdf, resultado):
    matriz = resultado["matriz"]
    vn = int(matriz[0][0])      # dijo «no vuelve» y no volvió
    fp = int(matriz[0][1])      # dijo «vuelve» y no volvió
    fn = int(matriz[1][0])      # dijo «no vuelve» y sí volvió
    vp = int(matriz[1][1])      # dijo «vuelve» y volvió
    total = vn + fp + fn + vp
    importancias = resultado["importancias"].sort_values("importancia", ascending=False)
    primeras = []
    for i in range(3):
        fila = importancias.iloc[i]
        primeras.append(fila["feature"] + " (" + str(round(float(fila["importancia"]), 2)) + ")")

    fig = nueva_hoja("4. Cómo leer los resultados del modelo")
    y = 0.91
    bloques = [
        ("Matriz de confusión", [
            "Cuenta los aciertos y los fallos del modelo con los " + str(total) + " niños de prueba, en 4 casillas:",
            "  · " + str(vp) + " verdaderos positivos (VP): dijo «vuelve» y volvió.",
            "  · " + str(vn) + " verdaderos negativos (VN): dijo «no vuelve» y no volvió.",
            "  · " + str(fp) + " falsos positivos (FP): dijo «vuelve» y no volvió.",
            "  · " + str(fn) + " falsos negativos (FN): dijo «no vuelve» y sí volvió.",
            "Todos los indicadores de abajo salen de estas cuatro casillas.",
        ]),
        ("accuracy (exactitud)", [
            "Qué mide: de todos los niños, cuántos acierta.",
            "Cómo: (VP + VN) / total = (" + str(vp) + " + " + str(vn) + ") / " + str(total) + " = " + str(resultado["accuracy"]) + ".",
            "Es decir, acierta " + str(vp + vn) + " de cada " + str(total) + " niños.",
        ]),
        ("precision (precisión)", [
            "Qué mide: cuando el modelo dice «vuelve», cuántas veces tiene razón.",
            "Cómo: VP / (VP + FP) = " + str(vp) + " / " + str(vp + fp) + " = " + str(resultado["precision"]) + ".",
            "De los " + str(vp + fp) + " niños que predijo que volverían, volvieron " + str(vp) + ".",
        ]),
        ("recall (sensibilidad)", [
            "Qué mide: de los niños que vuelven de verdad, cuántos encuentra el modelo.",
            "Cómo: VP / (VP + FN) = " + str(vp) + " / " + str(vp + fn) + " = " + str(resultado["recall"]) + ".",
            "Encontró a " + str(vp) + " de los " + str(vp + fn) + " que volvieron.",
        ]),
        ("Curva ROC y ROC-AUC", [
            "El árbol no solo dice sí o no: da una probabilidad de que vuelva. La curva ROC prueba todos los umbrales",
            "(«vuelve si la probabilidad pasa de X») y dibuja, para cada uno, los falsos positivos (eje X) frente a los",
            "verdaderos positivos (eje Y). AUC es el área bajo esa curva: 0,5 es tirar una moneda y 1 es perfecto.",
            "Aquí vale " + str(resultado["roc_auc"]) + ".",
        ]),
        ("Importancia de las variables", [
            "Cuánto ayuda cada dato de la primera partida a separar a los que vuelven de los que no.",
            "Suman 1 entre todas. Las tres que más usa el árbol: " + ", ".join(primeras) + ".",
        ]),
        ("El árbol de decisión", [
            "Una serie de preguntas de sí o no sobre la primera partida. Como es lo más difícil de entender,",
            "tiene dos hojas propias a continuación: «El árbol de decisión, paso a paso».",
        ]),
        ("Por qué se separa en entrenamiento y prueba", [
            "El modelo aprende con el 80 % de los niños y se evalúa con el otro 20 %, que no ha visto nunca: así los",
            "resultados dicen cómo funcionaría con niños nuevos. Con " + str(total) + " niños de prueba, cada fallo",
            "cambia la accuracy en unos " + str(round(100 / total, 1)) + " puntos.",
        ]),
    ]
    for titulo, lineas in bloques:
        fig.text(0.08, y, titulo, fontsize=10.5, weight="bold", color="#E07B12")
        y = texto(fig, y - 0.021, lineas, tam=8.6, separacion=0.0175) - 0.008
    pdf.savefig(fig)
    plt.close(fig)


def hoja_vistas(pdf):
    fig = nueva_hoja("5. Dónde se ven las métricas")
    texto(fig, 0.90, [
        "Tablet (Android, Chaquopy) · Perfil → Mis estadísticas (la vista «Mètriques»):",
        "  · Las partidas del niño que ha entrado (GET api/sesiones/mias): 7 números (partidas, récord,",
        "    puntos de media, precisión, segundos por partida, días jugados y ayudas) y 7 gráficas.",
        "  · El modelo de abandono: accuracy, precision, recall y ROC-AUC, matriz de confusión, curva ROC",
        "    e importancia de cada variable. Chaquopy no tiene scikit-learn para Python 3.11 en 32 bits,",
        "    así que el árbol está hecho con numpy y da los mismos números que este informe.",
        "  · Python va dentro de la app; las gráficas llegan a Kotlin en base64.",
        "",
        "Escritorio (C#) · pantalla Métricas:",
        "  · Todas las partidas (GET api/sesiones/todas): 15 tarjetas con las métricas derivadas y",
        "    las del modelo, y las 11 gráficas de este informe.",
        "  · El escritorio ejecuta Python/metricas_escritorio.py y enseña los PNG que genera.",
        "    Mientras haya menos de 50 jugadores reales, el modelo se entrena con las sesiones simuladas.",
        "",
        "Las dos vistas leen los datos a través de la API, nunca directamente de la base de datos,",
        "y no llevan textos de conclusiones (van en la presentación técnica).",
    ])
    pdf.savefig(fig)
    plt.close(fig)


def hoja_anexo(pdf):
    fig = nueva_hoja("Anexo: archivos y librerías")
    filas = []
    for ruta, librerias in ARCHIVOS:
        filas.append([ruta, librerias])
    tabla(fig, 0.91, 0.36, filas, ["Archivo", "Librerías"], [0.58, 0.42])
    texto(fig, 0.66, [
        "Versiones:",
        "  · PC (scripts de metricas/ y escritorio): Python 3.11 o superior con pandas, numpy, matplotlib",
        "    y scikit-learn (requirements.txt; instalar_librerias.bat lo deja listo).",
        "  · Tablet (Chaquopy 17.2): Python 3.11 con pandas 2.1.3, numpy 1.26.2 y matplotlib 3.6.0,",
        "    para armeabi-v7a (Samsung Galaxy Tab A, 32 bits), arm64-v8a y x86_64 (emulador).",
    ])
    pdf.savefig(fig)
    plt.close(fig)


# ---------- El árbol de decisión, paso a paso (lo más difícil de entender del informe)
NOMBRES_FEATURES = {
    "errores_por_minuto": "errores por minuto", "help_clicks": "ayudas pedidas",
    "avg_reaction_ms": "tiempo de reacción (ms)", "session_length": "duración (s)",
    "final_score": "puntos", "hits": "aciertos", "moles_escaped": "topos escapados",
    "errors_made": "errores", "abandoned": "abandonó (0 = no, 1 = sí)",
}
AZUL = "#9CC3F0"
NARANJA = "#F5B57A"


def coma(numero):
    texto_numero = str(round(float(numero), 2))
    if texto_numero.endswith(".0"):
        texto_numero = texto_numero[:-2]
    return texto_numero.replace(".", ",")


# Lo que hay en un nodo del árbol: cuántos niños llegan y cuántos vuelven
def datos_nodo(arbol, nodo):
    total = int(arbol.n_node_samples[nodo])
    fracciones = arbol.value[nodo][0]
    vuelven = int(round(float(fracciones[1]) / float(fracciones.sum()) * total))
    return total, vuelven


def pregunta(arbol, nodo):
    nombre = NOMBRES_FEATURES.get(FEATURES[arbol.feature[nodo]], FEATURES[arbol.feature[nodo]])
    return "¿" + nombre + " ≤ " + coma(arbol.threshold[nodo]) + "?"


def caja_nodo(ax, x, y, arbol, nodo, ancho=22):
    total, vuelven = datos_nodo(arbol, nodo)
    porcentaje = round(vuelven * 100 / total)
    color = AZUL
    if vuelven * 2 < total:
        color = NARANJA
    lineas = []
    if arbol.children_left[nodo] != -1:
        lineas.append(pregunta(arbol, nodo))
    lineas.append(str(total) + " niños · vuelven " + str(vuelven) + " (" + str(porcentaje) + " %)")
    ax.text(x, y, "\n".join(lineas), ha="center", va="center", fontsize=8.3, color=MARRON,
            bbox=dict(boxstyle="round,pad=0.6", facecolor=color, edgecolor=MARRON, linewidth=1.2))


def flecha(ax, x1, y1, x2, y2, etiqueta):
    ax.annotate("", xy=(x2, y2 + 6), xytext=(x1, y1 - 6),
                arrowprops=dict(arrowstyle="->", color=MARRON, linewidth=1.3))
    ax.text((x1 + x2) / 2 + (2 if x2 > x1 else -2), (y1 + y2) / 2, etiqueta, fontsize=9, weight="bold",
            color="#E07B12", ha="center")


# Dibujo del árbol simplificado: las dos primeras preguntas y los grupos que salen
def dibujo_arbol(fig, arbol):
    ax = fig.add_axes([0.03, 0.08, 0.94, 0.48])
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis("off")
    raiz = 0
    caja_nodo(ax, 50, 90, arbol, raiz)
    hijos = [(arbol.children_left[raiz], 25, "Sí"), (arbol.children_right[raiz], 75, "No")]
    for nodo, x, etiqueta in hijos:
        flecha(ax, 50, 90, x, 55, etiqueta)
        caja_nodo(ax, x, 55, arbol, nodo)
        if arbol.children_left[nodo] != -1:
            nietos = [(arbol.children_left[nodo], x - 12.5, "Sí"), (arbol.children_right[nodo], x + 12.5, "No")]
            for nieto, xn, etiqueta_nieto in nietos:
                flecha(ax, x, 55, xn, 17, etiqueta_nieto)
                caja_nodo(ax, xn, 17, arbol, nieto)
    ax.text(50, -1, "Azul: en ese grupo vuelve la mayoría · Naranja: no vuelve la mayoría.\n"
            "Debajo, el árbol hace hasta 2 preguntas más (dibujo completo en la hoja del modelo).",
            ha="center", va="top", fontsize=8, color="dimgray", style="italic")


# Un niño concreto bajando por el árbol: qué le pregunta y por dónde va
def recorrido(modelo, fila, real):
    arbol = modelo.tree_
    nodo = 0
    lineas = []
    paso = 1
    while arbol.children_left[nodo] != -1:
        nombre = FEATURES[arbol.feature[nodo]]
        valor = float(fila[nombre])
        cumple = valor <= arbol.threshold[nodo]
        respuesta = "No → va a la derecha"
        siguiente = arbol.children_right[nodo]
        if cumple:
            respuesta = "Sí → va a la izquierda"
            siguiente = arbol.children_left[nodo]
        lineas.append("  " + str(paso) + ". " + pregunta(arbol, nodo) + "  Tiene " + coma(valor) + "  →  " + respuesta)
        nodo = siguiente
        paso = paso + 1
    total, vuelven = datos_nodo(arbol, nodo)
    prediccion = "VUELVE"
    if vuelven * 2 < total:
        prediccion = "NO VUELVE"
    verdad = "no volvió"
    if real == 1:
        verdad = "volvió"
    lineas.append("  Llega a un grupo de " + str(total) + " niños del entrenamiento donde volvieron " + str(vuelven) +
                  "  →  predice " + prediccion + ". En realidad " + verdad + ".")
    return lineas


# Junta las condiciones de una misma variable: «más de 4,62 y hasta 8,57» → «entre 4,62 y 8,57»
def juntar_condiciones(condiciones):
    limites = {}
    orden = []
    for nombre, tipo, valor in condiciones:
        if nombre not in limites:
            limites[nombre] = [None, None]
            orden.append(nombre)
        if tipo == "más de":
            limites[nombre][0] = valor
        else:
            limites[nombre][1] = valor
    partes = []
    for nombre in orden:
        minimo, maximo = limites[nombre]
        if minimo != None and maximo != None:
            partes.append(nombre + " entre " + coma(minimo) + " y " + coma(maximo))
        elif minimo != None:
            partes.append(nombre + " más de " + coma(minimo))
        else:
            partes.append(nombre + " hasta " + coma(maximo))
    return ", ".join(partes)


# Las reglas que ha aprendido, en palabras: un camino hasta cada hoja con muchos niños
def reglas(modelo, minimo=10):
    arbol = modelo.tree_
    resultado = []
    pendientes = [(0, [])]
    while len(pendientes) > 0:
        nodo, condiciones = pendientes.pop(0)
        if arbol.children_left[nodo] == -1:
            total, vuelven = datos_nodo(arbol, nodo)
            if total >= minimo:
                prediccion = "vuelve"
                if vuelven * 2 < total:
                    prediccion = "no vuelve"
                frase = "Si " + juntar_condiciones(condiciones) + "  →  " + prediccion + " (" + str(vuelven) +                         " de " + str(total) + " volvieron)"
                partes = textwrap.wrap(frase, 100)
                for p in range(len(partes)):
                    if p == 0:
                        resultado.append("  · " + partes[p])
                    else:
                        resultado.append("     " + partes[p])
        else:
            nombre = NOMBRES_FEATURES.get(FEATURES[arbol.feature[nodo]], FEATURES[arbol.feature[nodo]])
            umbral = coma(arbol.threshold[nodo])
            pendientes.append((arbol.children_left[nodo], condiciones + [(nombre, "hasta", float(arbol.threshold[nodo]))]))
            pendientes.append((arbol.children_right[nodo], condiciones + [(nombre, "más de", float(arbol.threshold[nodo]))]))
    return resultado


def hoja_arbol(pdf, resultado, jugadores):
    modelo = resultado["modelo"]
    arbol = modelo.tree_
    total_raiz, vuelven_raiz = datos_nodo(arbol, 0)

    # ---- Hoja 1: qué es, cómo aprende, gini y el dibujo simplificado
    fig = nueva_hoja("4. El árbol de decisión, paso a paso (1/2)")
    y = 0.91
    bloques = [
        ("Qué es", [
            "Es como el juego «¿Quién es quién?»: para adivinar si un niño volverá a jugar, hace preguntas de sí o no",
            "sobre su primera partida («¿cuántos errores por minuto hizo?», «¿cuántas ayudas pidió?»…). Según la",
            "respuesta va por un camino o por otro, y al final llega a un grupo de niños parecidos: si en ese grupo",
            "la mayoría volvió, predice «vuelve»; si no, «no vuelve».",
        ]),
        ("Cómo aprende (con el 80 % de los niños, los de entrenamiento)", [
            "1. Prueba todas las preguntas posibles: cada variable con cada número de corte.",
            "2. Se queda con la que mejor separa a los que vuelven de los que no (la que más baja el gini).",
            "3. Repite lo mismo dentro de cada grupo nuevo, hasta hacer 4 preguntas seguidas (max_depth = 4)",
            "   o hasta que un grupo tenga a todos iguales. Nadie le dice qué preguntas hacer: las encuentra solo.",
        ]),
        ("Qué es el gini (lo «mezclado» que está un grupo)", [
            "0 = todos los niños del grupo son iguales (todos vuelven o ninguno) · 0,5 = mitad y mitad.",
            "Al empezar hay " + str(total_raiz) + " niños: " + str(vuelven_raiz) + " vuelven y " +
            str(total_raiz - vuelven_raiz) + " no → gini = " + coma(arbol.impurity[0]) + " (muy mezclado). Cada pregunta",
            "busca grupos con el gini más bajo posible, es decir, más «puros».",
        ]),
        ("Las primeras preguntas del árbol", [
            "Cada caja dice la pregunta y cuántos niños de entrenamiento llegan; las flechas son las respuestas.",
        ]),
    ]
    for titulo, lineas in bloques:
        fig.text(0.08, y, titulo, fontsize=10.5, weight="bold", color="#E07B12")
        y = texto(fig, y - 0.021, lineas, tam=8.8, separacion=0.0178) - 0.008
    dibujo_arbol(fig, arbol)
    pdf.savefig(fig)
    plt.close(fig)

    # ---- Hoja 2: leer el dibujo de scikit-learn, dos niños de ejemplo y las reglas
    fig = nueva_hoja("4. El árbol de decisión, paso a paso (2/2)")
    fig.text(0.08, 0.91, "Cómo leer cada caja del dibujo completo (el de scikit-learn)", fontsize=10.5,
             weight="bold", color="#E07B12")
    filas = [
        [FEATURES[arbol.feature[0]] + " <= " + str(round(float(arbol.threshold[0]), 3)),
         "La pregunta. Sí (True): izquierda · No (False): derecha"],
        ["gini = " + str(round(float(arbol.impurity[0]), 3)), "Lo mezclado del grupo (0 = todos iguales)"],
        ["samples = " + str(total_raiz), "Niños de entrenamiento que llegan a la caja"],
        ["value = [" + str(total_raiz - vuelven_raiz) + ", " + str(vuelven_raiz) + "]", "Cuántos [no vuelven, vuelven] hay"],
        ["class = Vuelve", "Lo que predice: lo de la mayoría de la caja"],
        ["Color", "Azul: vuelve · naranja: no vuelve (fuerte = grupo puro)"],
    ]
    tabla(fig, 0.885, 0.16, filas, ["En la caja pone…", "Significa"], [0.38, 0.62])

    X = jugadores[FEATURES]
    y_real = jugadores["returning_player"].to_numpy()
    predicciones = modelo.predict(X)
    hojas = modelo.apply(X)
    ejemplos = []
    for clase in [1, 0]:
        mejor = -1
        tam_mejor = -1
        for i in range(len(y_real)):
            if y_real[i] == clase and predicciones[i] == clase:
                tam = int(arbol.n_node_samples[hojas[i]])
                if tam > tam_mejor:
                    tam_mejor = tam
                    mejor = i
        if mejor >= 0:
            ejemplos.append(mejor)

    y = 0.68
    fig.text(0.08, y, "Dos niños de los datos bajando por el árbol", fontsize=10.5, weight="bold", color="#E07B12")
    y = y - 0.022
    letras = ["A", "B"]
    for k in range(len(ejemplos)):
        i = ejemplos[k]
        fila = X.iloc[i]
        fig.text(0.08, y, "Niño " + letras[k] + ": primera partida con " + coma(fila["errores_por_minuto"]) +
                 " errores por minuto, " + coma(fila["help_clicks"]) + " ayudas y " + coma(fila["avg_reaction_ms"]) +
                 " ms de reacción.", fontsize=8.8, color=MARRON, weight="bold")
        y = texto(fig, y - 0.019, recorrido(modelo, fila, y_real[i]), tam=8.3, separacion=0.0168) - 0.01

    fig.text(0.08, y, "Las reglas que ha aprendido (grupos de 10 niños o más)", fontsize=10.5, weight="bold", color="#E07B12")
    y = texto(fig, y - 0.021, reglas(modelo), tam=8.2, separacion=0.0168) - 0.006
    texto(fig, y, [
        "Fíjate en las dos primeras preguntas: «¿más de 3,98 errores por minuto?» y «¿más de 1,5 ayudas?». Son casi",
        "exactamente la regla de frustración que se programó en la simulación (4 errores por minuto o 2 ayudas). El",
        "árbol la ha descubierto solo, mirando los datos: eso es lo que hace un modelo de predicción.",
    ], tam=8.8, separacion=0.0178)
    pdf.savefig(fig)
    plt.close(fig)


def guardar_todo(ruta_csv):
    df = cargar(ruta_csv)
    jugadores = tabla_jugadores(df)
    metricas = metricas_derivadas(df, jugadores)
    resultado = entrenar_modelo(jugadores)
    lista = crear_graficas(df, jugadores, resultado)
    os.makedirs("graficas", exist_ok=True)
    graficas = {}
    for nombre, fig in lista:
        fig.savefig("graficas/" + nombre + ".png", dpi=100, bbox_inches="tight")
        graficas[nombre] = fig

    with PdfPages("informe_metricas.pdf") as pdf:
        hoja_portada(pdf)
        hoja_metricas(pdf, metricas)
        hoja_datos(pdf, df, jugadores)
        hoja_graficas(pdf, "3. Visualización de los datos", [("duracion", graficas["duracion"]),
                                                             ("puntuaciones", graficas["puntuaciones"])],
                      ["Gráficas hechas con Matplotlib a partir de las sesiones simuladas. Las cuatro primeras enseñan",
                       "cómo son las partidas; la de actividad, cuándo se juega; y las dos de abandono comparan a los",
                       "niños según su primera partida, que es justo lo que usa el modelo para predecir."])
        hoja_graficas(pdf, "3. Visualización de los datos (2)", [("errores_dificultad", graficas["errores_dificultad"]),
                                                                 ("intentos_puntuacion", graficas["intentos_puntuacion"])])
        hoja_graficas(pdf, "3. Visualización de los datos (3)", [("actividad_diaria", graficas["actividad_diaria"]),
                                                                 ("abandono_ayudas", graficas["abandono_ayudas"])])
        hoja_graficas(pdf, "3. Visualización de los datos (4)", [("abandono_errores", graficas["abandono_errores"])])
        hoja_modelo(pdf, resultado, graficas)
        hoja_arbol(pdf, resultado, jugadores)
        hoja_vistas(pdf)
        hoja_anexo(pdf)

    for nombre in graficas:
        plt.close(graficas[nombre])

    datos = {"metricas": metricas, "modelo": numeros_modelo(resultado)}
    with open("graficas/metricas.json", "w", encoding="utf-8") as archivo:
        json.dump(datos, archivo, ensure_ascii=False, indent=2)

    print(json.dumps(datos, ensure_ascii=False, indent=2))
    print("Guardado: informe_metricas.pdf, graficas/*.png y graficas/metricas.json")


# Esta parte es de información externa que no sale en los vídeos
# (__main__: solo se guarda todo al ejecutar el script; la tablet importa generar_informe)
if __name__ == "__main__":
    guardar_todo("datos/sesiones_simuladas.csv")
