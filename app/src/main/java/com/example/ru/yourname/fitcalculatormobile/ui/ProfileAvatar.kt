package com.example.ru.yourname.fitcalculatormobile.ui

// Stable identifiers are stored in the account; image files stay inside the app.
object ProfileAvatar {
    const val DEFAULT = "default"
    val ids = listOf(DEFAULT, "boy_brunette", "boy_blond", "girl_brunette", "girl_blond")
    fun normalize(id: String?) = id?.takeIf { it in ids } ?: DEFAULT
}
