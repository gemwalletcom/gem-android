package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.SearchBar
import com.gemwallet.android.ui.components.filters.model.FilterType
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.TransactionTypeFilter
import com.gemwallet.android.ui.theme.listItemIconSize
import com.gemwallet.android.ui.theme.paddingDefault
import com.wallet.core.primitives.Chain

@Composable
fun TransactionsFilter(
    availableChains: List<Chain>,
    chainsFilter: List<Chain>,
    typesFilter: List<TransactionTypeFilter>,
    onDismissRequest: () -> Unit,
    onChainFilter: (Chain) -> Unit,
    onTypeFilter: (TransactionTypeFilter) -> Unit,
    onClearChainsFilter: () -> Unit,
    onClearTypesFilter: () -> Unit,
) {
    var showedSubFilter by remember { mutableStateOf<FilterType?>(null) }

    FilterDialog(
        onDismissRequest = onDismissRequest,
        onClearFilters = {
            onClearChainsFilter()
            onClearTypesFilter()
        }.takeIf { typesFilter.isNotEmpty() || chainsFilter.isNotEmpty() },
    ) {
        LazyColumn {
            item {
                PropertyItem(
                    modifier = Modifier.clickable { showedSubFilter = FilterType.ByChains },
                    title = {
                        PropertyTitleText(
                            text = R.string.settings_networks_title,
                            trailing = {
                                Image(
                                    modifier = Modifier.size(listItemIconSize),
                                    painter = painterResource(R.drawable.settings_networks),
                                    contentDescription = "networks filter"
                                )
                            }
                        )
                    },
                    data = {
                        PropertyDataText(
                            text = when {
                                chainsFilter.isEmpty() -> stringResource(R.string.common_all)
                                chainsFilter.size == 1 -> chainsFilter.firstOrNull()?.asset()?.name ?: ""
                                else -> "${chainsFilter.size}"
                            },
                            badge = { IconWithBadge(null) }
                        )
                    }
                )
            }
            item {
                PropertyItem(
                    modifier = Modifier.clickable { showedSubFilter = FilterType.ByTypes },
                    title = {
                        PropertyTitleText(
                            text = R.string.filter_types,
                            trailing = {
                                Icon(
                                    modifier = Modifier.size(listItemIconSize),
                                    imageVector = Icons.AutoMirrored.Default.Article,
                                    contentDescription = "networks filter"
                                )
                            }
                        )
                    },
                    data = {
                        PropertyDataText(
                            text = when {
                                typesFilter.isEmpty() -> stringResource(R.string.common_all)
                                typesFilter.size == 1 -> typesFilter.firstOrNull()?.getLabel()
                                    ?.let { stringResource(it) } ?: ""

                                else -> "${typesFilter.size}"
                            },
                            badge = { IconWithBadge(null) }
                        )
                    }
                )
            }
        }
    }

    when (showedSubFilter) {
        FilterType.ByChains -> FilterDialog(
            fullScreen = true,
            onDismissRequest = { showedSubFilter = null },
            onClearFilters = onClearChainsFilter.takeIf { chainsFilter.isNotEmpty() },
        ) {
            val query = rememberTextFieldState()
            SearchBar(query, Modifier.Companion.padding(horizontal = paddingDefault))
            LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
                selectFilterChain(availableChains, chainsFilter, query.text.toString(), onChainFilter)
            }
        }
        FilterType.ByTypes -> FilterDialog(
            onDismissRequest = { showedSubFilter = null },
            onClearFilters = onClearTypesFilter.takeIf { typesFilter.isNotEmpty() },
        ) {
            LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
                selectFilterTransactionType(typesFilter, onTypeFilter)
            }
        }
        null -> {}
    }
}