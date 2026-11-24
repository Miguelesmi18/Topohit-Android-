package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que se envía a POST api/auth/login.
// La contraseña no viaja tal cual: se envía su hash bcrypt (ver Cifrado.kt).
data class LoginPeticion (@SerializedName("username") var username: String,
                          @SerializedName("passwordHash") var passwordHash: String)
