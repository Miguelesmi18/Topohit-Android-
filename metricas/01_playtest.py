# =====================================================================
#  TOPOHIT · Métricas · 1. Playtest real
#  Descarga las partidas reales de Infinito de la API y saca sus rangos
#  (mínimo, media y máximo de cada variable por dificultad).
#  Con estos rangos se hace la simulación (02_simulacion.py).
#
#  Salida: datos/playtest.csv (las partidas) y datos/rangos.csv (los rangos)
#  Hace falta la API arrancada (http://localhost:5110).
# =====================================================================

import os

# Paso 1: API - Descargar las partidas
import json

# Esta parte es de información externa que no sale en los vídeos
# (urllib: pedir los datos a la API de Topohit, que necesita el token del login)
import urllib.request

URL_API = "http://localhost:5110/api"
# Usuario educador de prueba (profe / profe1234): la contraseña va cifrada con bcrypt, como en las apps
USUARIO = "profe"
PASSWORD_HASH = "$2a$10$3s5iPBlBV39O4ZeUPrG23es3kJQDuchOWR9z0xhstkU1al9f9xRua"


def pedir(metodo, ruta, cuerpo, token):
    datos = None
    if cuerpo is not None:
        datos = json.dumps(cuerpo).encode("utf-8")
    peticion = urllib.request.Request(URL_API + ruta, data=datos, method=metodo)
    peticion.add_header("Content-Type", "application/json")
    if token is not None:
        peticion.add_header("Authorization", "Bearer " + token)
    respuesta = urllib.request.urlopen(peticion)
    return json.loads(respuesta.read().decode("utf-8"))


login = pedir("POST", "/auth/login", {"username": USUARIO, "passwordHash": PASSWORD_HASH}, None)
partidas = pedir("GET", "/sesiones/todas", None, login["token"])

# Paso 2: Pandas - Limpiar los datos
import pandas as pd

df = pd.DataFrame(partidas)
print(f"Partidas descargadas: {df.shape}")
print(df.head())

# Solo el modo Infinito (modeId 3): Historia y Contrarreloj ya no existen en el juego
df = df[df["modeId"] == 3]

# Mismos nombres que las columnas de la tabla SESION
df = df.rename(columns={"sessionId": "session_id", "userId": "user_id", "dateTime": "date_time",
                        "sessionLength": "session_length", "errorsMade": "errors_made",
                        "finalScore": "final_score", "nAttempts": "n_attempts",
                        "molesEscaped": "moles_escaped", "maxCombo": "max_combo",
                        "goldenHits": "golden_hits", "avgReactionMs": "avg_reaction_ms",
                        "helpClicks": "help_clicks"})
columnas = ["session_id", "user_id", "date_time", "difficulty", "session_length", "errors_made",
            "final_score", "n_attempts", "hits", "accuracy", "moles_escaped", "max_combo",
            "golden_hits", "abandoned", "avg_reaction_ms", "help_clicks"]
df = df[columnas]
print(f"Partidas de Infinito: {df.shape}")

# Paso 3: Pandas - Rangos por dificultad
variables = ["session_length", "errors_made", "final_score", "hits", "moles_escaped",
             "max_combo", "golden_hits", "avg_reaction_ms", "help_clicks"]
rangos = df.groupby("difficulty")[variables].agg(["min", "mean", "max"]).round(1)
print(rangos.T)

os.makedirs("datos", exist_ok=True)
df.to_csv("datos/playtest.csv", index=False)
rangos.to_csv("datos/rangos.csv")
print("Guardado: datos/playtest.csv y datos/rangos.csv")
