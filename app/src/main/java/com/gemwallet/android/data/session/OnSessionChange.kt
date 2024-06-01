package com.gemwallet.android.data.session

import com.gemwallet.android.model.Session

interface OnSessionChange {
    fun onSessionChange(session: Session?)
}