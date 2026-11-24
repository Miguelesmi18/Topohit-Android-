package com.groundstudio.topohit.datamodel

// Quién ha entrado en la tablet. No viene tal cual de la API: se guarda en sesion.txt
// (GestorSesion) para no tener que escribir la contraseña cada vez que se abre la app.
data class SesionUsuario (var token: String,
                          var idUsuario: Int,
                          var username: String,
                          var rol: String,
                          var avatarId: Int)
