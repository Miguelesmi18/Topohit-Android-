package com.groundstudio.topohit

import org.mindrot.jbcrypt.BCrypt

// Cifra la contraseña antes de enviarla a la API (reto «Protecció dels usuaris»).
// La contraseña nunca viaja tal cual: se envía su hash bcrypt y la API lo compara con
// USUARIO.password_hash.
//
// Esta parte es de información externa que no sale en los vídeos
// (jBCrypt: el profesor de 0490 da jbcrypt.jar en el Classroom, pero ningún vídeo lo programa).
// Se usa la MISMA sal fija que el escritorio (Servicios/Cifrado.cs): así "marc1234" da el mismo
// hash en la tablet, en el escritorio y en la base de datos.
object Cifrado
{
    // El símbolo $ en Kotlin va con \ delante para que no lo tome como una variable
    private val SAL = "\$2a\$10\$3s5iPBlBV39O4ZeUPrG23e"

    fun hash(contrasena: String): String
    {
        return BCrypt.hashpw(contrasena, SAL)
    }
}
