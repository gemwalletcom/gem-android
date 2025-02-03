package com.gemwallet.android.ui.components.cells

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.designsystem.trailingIcon20
import com.gemwallet.android.ui.components.image.AsyncImage
import com.wallet.core.primitives.Chain

@Composable
fun cellNetwork(chain: Chain, onOpenNetwork: ((Chain) -> Unit)? = null): CellEntity<String> {
    val asset = chain.asset()
    return CellEntity(
        label = stringResource(id = R.string.transfer_network),
        data = asset.name,
        trailing = { AsyncImage(asset, trailingIcon20) },
        action = onOpenNetwork?.let { { onOpenNetwork(chain) } },
    )
}