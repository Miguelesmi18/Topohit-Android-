# =====================================================================
#  TOPOHIT · Métricas · Modelo de abandono (pestaña «Mètriques» de la tablet)
#  Un árbol de decisión que predice si un niño volverá a jugar (returning_player) con los
#  datos de su PRIMERA partida. Es el mismo modelo que metricas/03_analisis.py (escritorio):
#  las mismas features, max_depth=4, 80 % entrenamiento / 20 % prueba y random_state=42.
#  Se entrena con las sesiones simuladas (sesiones_simuladas.csv, en la carpeta de la app),
#  igual que el escritorio mientras haya pocos jugadores reales.
#  Devuelve en JSON accuracy, precision, recall y ROC-AUC, y las gráficas del modelo en
#  base64 (matriz de confusión, curva ROC e importancia de cada variable).
#  Sin textos de conclusiones (lo pide el PDF de Métricas).
# =====================================================================

import os
import json

# Paso 1: Pandas - Cargar las sesiones simuladas y hacer una fila por jugador
import pandas as pd
import numpy as np

FEATURES = ["session_length", "errors_made", "final_score", "hits", "moles_escaped",
            "avg_reaction_ms", "help_clicks", "abandoned", "errores_por_minuto"]
RUTA_CSV = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sesiones_simuladas.csv")


def cargar(ruta_csv):
    df = pd.read_csv(ruta_csv)
    df["date_time"] = pd.to_datetime(df["date_time"], format="ISO8601")
    df["errores_por_minuto"] = df["errors_made"] / (df["session_length"] / 60)
    # Sin aciertos no hay tiempo de reacción: se pone la media para que el modelo pueda usar la fila
    df["avg_reaction_ms"] = df["avg_reaction_ms"].fillna(df["avg_reaction_ms"].mean())
    df["abandoned"] = df["abandoned"].astype(int)
    return df


def tabla_jugadores(df):
    df = df.sort_values("date_time")
    partidas = df.groupby("user_id").size()
    jugadores = df.groupby("user_id").first()
    jugadores["returning_player"] = (partidas >= 2).astype(int)
    return jugadores


# Paso 2: Numpy - Separar entrenamiento (80 %) y prueba (20 %), con la misma proporción de
# niños que vuelven y que no en las dos partes (stratify=y)

# Esta parte es de información externa que no sale en los vídeos
# (Chaquopy no tiene scikit-learn para Python 3.11 en 32 bits, así que train_test_split, el árbol de decisión
#  y las métricas del modelo se hacen aquí con numpy, siguiendo los pasos de scikit-learn.
#  Con random_state=42 sale la misma separación que en el escritorio y los mismos números)

# Cuántas filas de cada clase tocan (como _approximate_mode de scikit-learn)
def repartir(por_clase, cuantas, aleatorio):
    continuo = por_clase / por_clase.sum() * cuantas
    resultado = np.floor(continuo)
    faltan = int(cuantas - resultado.sum())
    if faltan > 0:
        resto = continuo - resultado
        valores = np.sort(np.unique(resto))[::-1]
        i = 0
        while faltan > 0 and i < len(valores):
            posiciones = np.where(resto == valores[i])[0]
            ahora = min(len(posiciones), faltan)
            elegidas = aleatorio.choice(posiciones, size=ahora, replace=False)
            resultado[elegidas] = resultado[elegidas] + 1
            faltan = faltan - ahora
            i = i + 1
    return resultado.astype(int)


def separar(X, y, test_size, random_state):
    aleatorio = np.random.RandomState(random_state)
    n_prueba = int(np.ceil(test_size * len(y)))
    n_entrenamiento = len(y) - n_prueba
    clases, y_indices = np.unique(y, return_inverse=True)
    por_clase = np.bincount(y_indices)
    filas_clase = np.split(np.argsort(y_indices, kind="mergesort"), np.cumsum(por_clase)[:-1])

    n_i = repartir(por_clase, n_entrenamiento, aleatorio)
    t_i = repartir(por_clase - n_i, n_prueba, aleatorio)
    entrenamiento = []
    prueba = []
    for i in range(len(clases)):
        orden = aleatorio.permutation(por_clase[i])
        filas = filas_clase[i].take(orden, mode="clip")
        entrenamiento.extend(filas[:n_i[i]])
        prueba.extend(filas[n_i[i]:n_i[i] + t_i[i]])
    entrenamiento = aleatorio.permutation(entrenamiento)
    prueba = aleatorio.permutation(prueba)
    return X[entrenamiento], X[prueba], y[entrenamiento], y[prueba]


# Paso 3: Numpy - Árbol de decisión (gini, max_depth=4)
def gini(y):
    resultado = 0.0
    if len(y) > 0:
        p = y.mean()
        resultado = 1 - p * p - (1 - p) * (1 - p)
    return resultado


# La mejor pregunta «¿feature <= umbral?» para separar las filas: la que más baja el gini
def mejor_corte(X, y):
    mejor = None
    mejor_ganancia = 0.0
    gini_nodo = gini(y)
    for columna in range(X.shape[1]):
        valores = np.unique(X[:, columna])
        for i in range(len(valores) - 1):
            umbral = (valores[i] + valores[i + 1]) / 2
            izquierda = X[:, columna] <= umbral
            n_izq = izquierda.sum()
            n_der = len(y) - n_izq
            gini_hijos = (n_izq * gini(y[izquierda]) + n_der * gini(y[~izquierda])) / len(y)
            ganancia = gini_nodo - gini_hijos
            if ganancia > mejor_ganancia + 1e-12:
                mejor_ganancia = ganancia
                mejor = (columna, umbral)
    return mejor, mejor_ganancia


# Cada nodo es un diccionario: una hoja guarda la probabilidad de que vuelva;
# un nodo con pregunta guarda la columna, el umbral y sus dos hijos
def crear_nodo(X, y, profundidad, max_depth, importancias, total):
    nodo = {"probabilidad": float(y.mean())}
    if profundidad < max_depth and gini(y) > 0:
        corte, ganancia = mejor_corte(X, y)
        if corte is not None:
            columna, umbral = corte
            importancias[columna] = importancias[columna] + ganancia * len(y) / total
            izquierda = X[:, columna] <= umbral
            nodo["columna"] = columna
            nodo["umbral"] = umbral
            nodo["izquierda"] = crear_nodo(X[izquierda], y[izquierda], profundidad + 1,
                                           max_depth, importancias, total)
            nodo["derecha"] = crear_nodo(X[~izquierda], y[~izquierda], profundidad + 1,
                                         max_depth, importancias, total)
    return nodo


def probabilidad(nodo, fila):
    while "columna" in nodo:
        if fila[nodo["columna"]] <= nodo["umbral"]:
            nodo = nodo["izquierda"]
        else:
            nodo = nodo["derecha"]
    return nodo["probabilidad"]


# Paso 4: Numpy - Métricas del modelo (accuracy, precision, recall, ROC-AUC y curva ROC)
def curva_roc(y, probabilidades):
    umbrales = np.unique(probabilidades)[::-1]
    positivos = max(int(y.sum()), 1)
    negativos = max(int(len(y) - y.sum()), 1)
    fpr = [0.0]
    tpr = [0.0]
    for umbral in umbrales:
        prediccion = probabilidades >= umbral
        tpr.append(float((prediccion & (y == 1)).sum() / positivos))
        fpr.append(float((prediccion & (y == 0)).sum() / negativos))
    return np.array(fpr), np.array(tpr)


# Área bajo la curva ROC, sumando trapecios (como roc_auc_score)
def area_roc(fpr, tpr):
    area = 0.0
    for i in range(1, len(fpr)):
        area = area + (fpr[i] - fpr[i - 1]) * (tpr[i] + tpr[i - 1]) / 2
    return area


def entrenar_modelo(jugadores):
    X = jugadores[FEATURES].to_numpy(dtype=float)
    y = jugadores["returning_player"].to_numpy(dtype=int)
    X_train, X_test, y_train, y_test = separar(X, y, test_size=0.2, random_state=42)

    importancias = np.zeros(len(FEATURES))
    arbol = crear_nodo(X_train, y_train, 0, 4, importancias, len(y_train))
    if importancias.sum() > 0:
        importancias = importancias / importancias.sum()

    probabilidades = np.zeros(len(y_test))
    for i in range(len(y_test)):
        probabilidades[i] = probabilidad(arbol, X_test[i])
    y_pred = (probabilidades > 0.5).astype(int)

    verdaderos_pos = int(((y_pred == 1) & (y_test == 1)).sum())
    falsos_pos = int(((y_pred == 1) & (y_test == 0)).sum())
    falsos_neg = int(((y_pred == 0) & (y_test == 1)).sum())
    verdaderos_neg = int(((y_pred == 0) & (y_test == 0)).sum())

    precision = 0.0
    if verdaderos_pos + falsos_pos > 0:
        precision = verdaderos_pos / (verdaderos_pos + falsos_pos)
    recall = 0.0
    if verdaderos_pos + falsos_neg > 0:
        recall = verdaderos_pos / (verdaderos_pos + falsos_neg)
    fpr, tpr = curva_roc(y_test, probabilidades)

    return {
        "accuracy": round(float((y_pred == y_test).mean()), 3),
        "precision": round(float(precision), 3),
        "recall": round(float(recall), 3),
        "roc_auc": round(float(area_roc(fpr, tpr)), 3),
        "matriz": np.array([[verdaderos_neg, falsos_pos], [falsos_neg, verdaderos_pos]]),
        "fpr": fpr,
        "tpr": tpr,
        "importancias": pd.DataFrame({"feature": FEATURES, "importancia": importancias}),
        "jugadores_entrenamiento": int(len(y_train)),
        "jugadores_prueba": int(len(y_test)),
    }


# Paso 5: Matplotlib - Gráficas del modelo (el título lo pinta la tablet encima de la imagen)
import matplotlib

# Esta parte es de información externa que no sale en los vídeos
# (backend Agg: dibujar sin ventana, que es lo que hace falta dentro de la tablet)
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def crear_graficas(resultado):
    graficas = []

    # 1. Matriz de confusión
    matriz = resultado["matriz"]
    fig = plt.figure(figsize=(8, 4.5))
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
    graficas.append(("Modelo: matriz de confusión", fig))

    # 2. Curva ROC
    fig = plt.figure(figsize=(8, 4.5))
    plt.plot(resultado["fpr"], resultado["tpr"], color="#3B7DD8",
             label="Árbol de decisión (AUC = " + str(resultado["roc_auc"]) + ")")
    plt.plot([0, 1], [0, 1], linestyle="--", color="#B0B0B0", label="Al azar")
    plt.xlabel("Falsos positivos")
    plt.ylabel("Verdaderos positivos")
    plt.legend()
    graficas.append(("Modelo: curva ROC", fig))

    # 3. Importancia de cada variable
    importancias = resultado["importancias"].sort_values("importancia")
    fig = plt.figure(figsize=(8, 4.5))
    plt.barh(importancias["feature"], importancias["importancia"], color="#5BAA3A")
    plt.xlabel("Importancia (0 a 1)")
    graficas.append(("Modelo: qué variables usa", fig))

    return graficas


# Paso 6: Resultado para la tablet
import io

# Esta parte es de información externa que no sale en los vídeos
# (base64: el PDF de Métricas pide pasar las gráficas a la app como imágenes en base64)
import base64


def a_base64(fig):
    memoria = io.BytesIO()
    fig.savefig(memoria, format="png", dpi=100, bbox_inches="tight")
    plt.close(fig)
    return base64.b64encode(memoria.getvalue()).decode("ascii")


# Lo que llama Kotlin: los números del modelo y sus gráficas en JSON
def generar():
    resultado = entrenar_modelo(tabla_jugadores(cargar(RUTA_CSV)))
    imagenes = []
    for titulo, fig in crear_graficas(resultado):
        imagenes.append({"titulo": titulo, "imagen": a_base64(fig)})
    return json.dumps({
        "accuracy": resultado["accuracy"],
        "precision": resultado["precision"],
        "recall": resultado["recall"],
        "roc_auc": resultado["roc_auc"],
        "jugadores_entrenamiento": resultado["jugadores_entrenamiento"],
        "jugadores_prueba": resultado["jugadores_prueba"],
        "graficas": imagenes,
    })
