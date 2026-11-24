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


# Paso 6: Guardar las gráficas y el informe PDF (v1 del informe)
# Esta parte es de información externa que no sale en los vídeos
# (PdfPages: juntar todas las gráficas de Matplotlib en un PDF)
from matplotlib.backends.backend_pdf import PdfPages

DESCRIPCIONES = [
    ("session_length", "Duración de la partida en segundos"),
    ("errors_made", "Toques en un agujero vacío o en una bomba (vidas perdidas)"),
    ("final_score", "Puntuación final de la partida"),
    ("n_attempts", "Todos los toques: hits + errors_made"),
    ("hits", "Topos golpeados"),
    ("moles_escaped", "Topos que se han escapado sin golpear"),
    ("avg_reaction_ms", "Tiempo medio desde que sale un topo hasta que se golpea (ms)"),
    ("help_clicks", "Veces que se ha pulsado la ayuda (?) durante la partida"),
    ("abandoned", "Si ha salido a medias desde la pausa"),
    ("player_count", "Jugadores distintos"),
    ("returning_player", "El jugador ha jugado 2 partidas o más"),
    ("retention_rate", "% de jugadores que vuelven a jugar"),
    ("churn_rate", "% de jugadores que solo juegan una partida"),
    ("DAU / MAU", "Jugadores activos por día / por mes, y su proporción"),
    ("average_session_length", "Duración media de las partidas (s)"),
]


def pagina_texto(pdf, titulo, lineas):
    fig = plt.figure(figsize=(8.27, 11.69))     # tamaño A4
    fig.text(0.08, 0.94, titulo, fontsize=18, weight="bold")
    y = 0.89
    for linea in lineas:
        fig.text(0.08, y, linea, fontsize=9, family="monospace")
        y = y - 0.019
    pdf.savefig(fig)
    plt.close(fig)


def guardar_todo(ruta_csv):
    metricas, resultado, graficas = analizar(ruta_csv)
    os.makedirs("graficas", exist_ok=True)

    with PdfPages("informe_metricas.pdf") as pdf:
        lineas = ["Métricas capturadas en cada partida y métricas derivadas:", ""]
        for nombre, descripcion in DESCRIPCIONES:
            lineas.append(f"{nombre:<24}{descripcion}")
        lineas.append("")
        lineas.append("Valores (sesiones simuladas a partir del playtest real):")
        lineas.append("")
        for nombre in metricas:
            lineas.append(f"{nombre:<24}{metricas[nombre]}")
        lineas.append("")
        lineas.append("Modelo de predicción: árbol de decisión (max_depth=4), target returning_player,")
        lineas.append("features de la primera partida del jugador, 80 % entrenamiento / 20 % prueba.")
        lineas.append("")
        modelo = numeros_modelo(resultado)
        for nombre in modelo:
            lineas.append(f"{nombre:<24}{modelo[nombre]}")
        pagina_texto(pdf, "Topohit · Informe de métricas", lineas)

        for nombre, fig in graficas:
            fig.savefig("graficas/" + nombre + ".png", dpi=100, bbox_inches="tight")
            pdf.savefig(fig, bbox_inches="tight")
            plt.close(fig)

    datos = {"metricas": metricas, "modelo": numeros_modelo(resultado)}
    with open("graficas/metricas.json", "w", encoding="utf-8") as archivo:
        json.dump(datos, archivo, ensure_ascii=False, indent=2)

    print(json.dumps(datos, ensure_ascii=False, indent=2))
    print(resultado["importancias"])
    print("Guardado: informe_metricas.pdf, graficas/*.png y graficas/metricas.json")


# Esta parte es de información externa que no sale en los vídeos
# (__main__: solo se guarda todo al ejecutar el script; la tablet importa generar_informe)
if __name__ == "__main__":
    guardar_todo("datos/sesiones_simuladas.csv")
