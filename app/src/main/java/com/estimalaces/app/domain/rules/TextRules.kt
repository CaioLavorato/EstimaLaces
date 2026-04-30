package com.estimalaces.app.domain.rules

import java.text.Normalizer

object TextRules {
    fun normalizeName(value: String): String {
        val withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return withoutAccents
            .lowercase()
            .replace("\\s+".toRegex(), " ")
    }

    fun looksSimilar(input: String, saved: String): Boolean {
        val normalizedInput = normalizeName(input)
        val normalizedSaved = normalizeName(saved)
        if (normalizedInput.length < 3) return false
        return normalizedSaved.contains(normalizedInput) || normalizedInput.contains(normalizedSaved)
    }
}
