package com.gemwallet.features.swap.views.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.views.components.SwapProviderItemView
import uniffi.gemstone.SwapperProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProviderListDialog(
    isShow: MutableState<Boolean>,
    isUpdated: Boolean,
    currentProvider: SwapperProvider?,
    providers: List<SwapProviderItem>,
    onProviderSelect: (SwapperProvider) -> Unit,
) {
    if (!isShow.value) {
        return
    }

    ModalBottomSheet(onDismissRequest = { isShow.value = false }, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        if (isUpdated) {
            Box(modifier = Modifier.fillMaxWidth().defaultPadding()) {
                CircularProgressIndicator20(modifier = Modifier.align(Alignment.Center))
            }
            return@ModalBottomSheet
        }
        LazyColumn {
            itemsIndexed(providers) { index, item ->
                SwapProviderItemView(
                    swapProvider = item,
                    listPosition = ListPosition.getPosition(index, providers.size),
                    isSelected = item.swapProvider.id == currentProvider,
                    onProviderSelect = {
                        isShow.value = false
                        onProviderSelect(it)
                    }
                )
            }
        }
    }
}

