package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que devuelve el script de Python modelo_abandono.py (en JSON): los números del árbol de decisión
// que predice si un niño volverá a jugar y sus gráficas (matriz de confusión, curva ROC e importancias)
data class ModeloAbandono (@SerializedName("accuracy") var accuracy: Double,
                           @SerializedName("precision") var precision: Double,
                           @SerializedName("recall") var recall: Double,
                           @SerializedName("roc_auc") var rocAuc: Double,
                           @SerializedName("jugadores_entrenamiento") var jugadoresEntrenamiento: Int,
                           @SerializedName("jugadores_prueba") var jugadoresPrueba: Int,
                           @SerializedName("graficas") var graficas: List<Grafica>)
