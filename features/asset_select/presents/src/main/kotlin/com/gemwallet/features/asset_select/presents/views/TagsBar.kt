package com.gemwallet.features.asset_select.presents.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.wallet.core.primitives.AssetTag

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagsBar(
    selected: AssetTag?,
    tags: List<AssetTag?>,
    onSelect: (AssetTag?) -> Unit,
) {
    if (tags.isEmpty()) {
        return
    }
    Row(
        Modifier.padding(horizontal = paddingDefault),
        horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall),
    ) {
        tags.forEachIndexed { index, item ->
            ToggleButton(
                modifier = Modifier.semantics { role = Role.RadioButton },
                checked = item == selected,
                onCheckedChange = { onSelect(item) },
                colors = ToggleButtonDefaults.toggleButtonColors().copy(containerColor = MaterialTheme.colorScheme.background),
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            .copy(checkedShape = ButtonGroupDefaults.connectedLeadingButtonShape)
                        tags.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            .copy(checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape)
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            .copy(checkedShape = ShapeDefaults.Small)
                    },
            ) {
                Text(
                    stringResource(
                        when (item) {
                            AssetTag.Trending -> R.string.assets_tags_trending
                            AssetTag.TrendingFiatPurchase -> R.string.assets_tags_trending
                            AssetTag.Gainers -> R.string.assets_tags_gainers
                            AssetTag.Losers -> R.string.assets_tags_losers
                            AssetTag.New -> R.string.assets_tags_new
                            AssetTag.Stablecoins -> R.string.assets_tags_stablecoins
                            null -> R.string.common_all
                        }
                    ),
                )
            }
        }
    }
}