package com.gemwallet.android.features.bridge.connection.model

import com.gemwallet.android.features.bridge.model.ConnectionUI

class ConnectionSceneState(
    val error: String? = null,
    val walletName: String = "",
    val connection: ConnectionUI = ConnectionUI(),
)