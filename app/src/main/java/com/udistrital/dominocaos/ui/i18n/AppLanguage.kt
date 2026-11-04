package com.udistrital.dominocaos.ui.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    SPANISH("es", "Español", "🇪🇸"),
    ENGLISH("en", "English", "🇺🇸")
}

object LanguageManager {
    var currentLanguage by mutableStateOf(AppLanguage.SPANISH)
        private set

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        val locale = Locale(language.code)
        Locale.setDefault(locale)
    }

    fun toggleLanguage() {
        setLanguage(if (currentLanguage == AppLanguage.SPANISH) AppLanguage.ENGLISH else AppLanguage.SPANISH)
    }
}
