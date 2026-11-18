package com.groundstudio.topohit.util

import org.mindrot.jbcrypt.BCrypt

// Cifra la contraseña antes de enviarla a la API (reto "Protecció dels usuaris").
// La contraseña nunca viaja tal cual: se envía su hash bcrypt y la API lo compara
// con USUARIO.password_hash.
//
// [No visto en clase] jBCrypt (org.mindrot:jbcrypt). Usamos la MISMA sal fija que el
// escritorio (Servicios/Cifrado.cs): así "marc1234" da el mismo hash en la tablet,
// en el escritorio y en la base de datos.
object Cifrado {

    // El símbolo $ en Kotlin va con \ delante para que no lo tome como una variable
    private const val SAL = "\$2a\$10\$3s5iPBlBV39O4ZeUPrG23e"

    fun hash(contrasena: String): String {
        return BCrypt.hashpw(contrasena, SAL)
    }
}
