package com.gemwallet.android.math

import java.math.BigDecimal


fun String.numberParse(): BigDecimal {
    val parts = trim().replace(",", ".")
        .replace(" ", "")
        .split(".")
    val number = List(parts.size) { i ->
        "${parts[i]}${if (i + 1 == parts.size - 1) "." else ""}"
    }.joinToString("")
    return BigDecimal(number.trim().replace("\uFEFF", ""))
}

fun String.numberParseOrNull(): BigDecimal? {
    return try {
        numberParse()
    } catch (_: Throwable) {
        null
    }
}