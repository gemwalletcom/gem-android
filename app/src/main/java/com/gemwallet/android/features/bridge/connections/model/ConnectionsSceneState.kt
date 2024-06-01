package com.gemwallet.android.features.bridge.connections.model

import com.gemwallet.android.features.bridge.model.ConnectionUI

class ConnectionsSceneState(
    val error: String? = null,
    val pairError: String? = null,
    val connections: List<ConnectionUI> = emptyList(),
)