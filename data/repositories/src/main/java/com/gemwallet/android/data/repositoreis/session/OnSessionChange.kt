package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.model.Session

interface OnSessionChange {
    fun onSessionChange(session: Session?)
}