package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que se envía a POST api/auth/registro: usuario, hash de la contraseña y avatar elegido (1 a 12)
data class RegistroPeticion (@SerializedName("username") var username: String,
                             @SerializedName("passwordHash") var passwordHash: String,
                             @SerializedName("avatarId") var avatarId: Int)
