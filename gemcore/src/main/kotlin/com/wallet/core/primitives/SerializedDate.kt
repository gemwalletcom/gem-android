package com.wallet.core.primitives

import com.gemwallet.android.serializer.DateSerializer
import kotlinx.serialization.Serializable

typealias SerializedDate = @Serializable(with = DateSerializer::class) Long