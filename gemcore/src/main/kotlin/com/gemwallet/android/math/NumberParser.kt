package com.gemwallet.android.math

import java.math.BigDecimal


fun String.parseNumber(): BigDecimal {
    val parts = trim().replace(",", ".")
        .replace(" ", "")
        .split(".")
    val number = List(parts.size) { i ->
        "${parts[i]}${if (i + 1 == parts.size - 1) "." else ""}"
    }.joinToString("")
    return BigDecimal(number.trim().replace("\uFEFF", ""))
}

fun String.parseNumberOrNull(): BigDecimal? {
    return try {
        parseNumber()
    } catch (_: Throwable) {
        null
    }
}