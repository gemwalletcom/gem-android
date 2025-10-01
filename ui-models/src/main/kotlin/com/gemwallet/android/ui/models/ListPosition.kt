package com.gemwallet.android.ui.models

enum class ListPosition {
    First,
    Middle,
    Single,
    Last;

    companion object {
        fun getPosition(index: Int, size: Int): ListPosition = if (size == 1)
            Single
        else
            when (index) {
            0 -> First
            size - 1 -> Last
            else -> Middle
        }
    }
}

fun List<*>.getListPosition(index: Int) = ListPosition.getPosition(index, size = size)