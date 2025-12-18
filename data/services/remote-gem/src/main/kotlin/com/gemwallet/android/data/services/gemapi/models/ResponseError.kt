package com.gemwallet.android.data.services.gemapi.models

import kotlinx.serialization.Serializable

@Serializable
class ResponseError(val error: ErrorDescription) {
    @Serializable
    class ErrorDescription(val message: String)
}