package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que se envía a PUT api/auth/avatar para cambiar el avatar desde el Perfil
data class AvatarPeticion (@SerializedName("avatarId") var avatarId: Int)
