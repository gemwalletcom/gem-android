package com.gemwallet.android.ext

fun String.getAddressEllipsisText(
    maxLength: Int = 8,
    ellipsisChar: Char = '.',
    ellipsisLength: Int = 3,
): String {
    val half = maxLength / 2
    return if (length <= 8 + ellipsisLength) {
        this
    } else {
        val left = substring(0, half + if ("0x" == substring(0, 2)) 2 else 0)
        val right = substring(length - half)
        val ellipsis = StringBuilder()
        for (i in 0..ellipsisLength) {
            ellipsis.append(ellipsisChar)
        }
        "$left$ellipsis$right"
    }
}