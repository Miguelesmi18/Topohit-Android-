package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que devuelve la API si el login o el registro van bien
data class LoginRespuesta (@SerializedName("token") var token: String,
                           @SerializedName("expira") var expira: String,
                           @SerializedName("idUsuario") var idUsuario: Int,
                           @SerializedName("username") var username: String,
                           @SerializedName("rol") var rol: String,
                           @SerializedName("avatarId") var avatarId: Int)
