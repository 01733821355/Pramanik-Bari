package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AgeCalculator {

    fun toBengaliDigits(num: Int): String {
        val bnDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { char ->
            if (char in '0'..'9') bnDigits[char - '0'] else char
        }.joinToString("")
    }

    private fun parseYear(dateStr: String): Int? {
        if (dateStr.isBlank()) return null
        val trimmed = dateStr.trim()
        val yearPattern = Regex("""\b(18\d\d|19\d\d|20\d\d)\b""")
        val match = yearPattern.find(trimmed)
        return match?.value?.toIntOrNull()
    }

    /**
     * Calculates age either currently (if living) or at the time of death (if deceased).
     * Returns a structured pair of (Bengali formatted label, English formatted label) or null if DOB is not provided.
     */
    fun calculateAge(dobStr: String, dodStr: String, isDeceased: Boolean): Pair<String, String>? {
        val birthYear = parseYear(dobStr) ?: return null
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val deathYear = if (isDeceased) parseYear(dodStr) else null
        val endYear = deathYear ?: currentYear

        val age = (endYear - birthYear).coerceAtLeast(0)
        val bnAge = toBengaliDigits(age)

        return if (isDeceased) {
            Pair(
                "$bnAge বছর বয়সে প্রয়াত",
                "Passed away at age $age"
            )
        } else {
            Pair(
                "বয়স: $bnAge বছর",
                "Age: $age yrs"
            )
        }
    }

    /**
     * Short label for cards and tree nodes
     */
    fun getCompactAgeBadge(dobStr: String, dodStr: String, isDeceased: Boolean, isBn: Boolean): String {
        val res = calculateAge(dobStr, dodStr, isDeceased) ?: return ""
        val birthYear = parseYear(dobStr) ?: return ""
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val deathYear = if (isDeceased) parseYear(dodStr) else null
        val endYear = deathYear ?: currentYear
        val age = (endYear - birthYear).coerceAtLeast(0)

        return if (isBn) {
            val bnAge = toBengaliDigits(age)
            if (isDeceased) "প্রয়াত ($bnAge বছর)" else "$bnAge বছর"
        } else {
            if (isDeceased) "Late ($age yrs)" else "$age yrs"
        }
    }
}
