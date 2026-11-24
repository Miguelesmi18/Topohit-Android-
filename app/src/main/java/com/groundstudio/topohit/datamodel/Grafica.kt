package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Una gráfica de «Mis estadísticas»: su título y la imagen PNG en base64 que hace Matplotlib
data class Grafica (@SerializedName("titulo") var titulo: String,
                    @SerializedName("imagen") var imagen: String)
