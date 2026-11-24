package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Respuesta de PUT api/auth/avatar: el avatar que ha quedado guardado
data class AvatarRespuesta (@SerializedName("mensaje") var mensaje: String?,
                            @SerializedName("avatarId") var avatarId: Int)
