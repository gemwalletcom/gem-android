package com.gemwallet.android.cases.security

import com.gemwallet.android.model.AuthRequest

interface AuthRequester {
    fun requestAuth(auth: AuthRequest, onSuccess: () -> Unit)
}