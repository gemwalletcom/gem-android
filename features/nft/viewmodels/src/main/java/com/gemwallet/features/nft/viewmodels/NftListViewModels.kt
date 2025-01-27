package com.gemwallet.features.nft.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nft.GetNFTCase
import com.gemwallet.android.cases.nft.LoadNFTCase
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ui.models.actions.NftAssetIdAction
import com.gemwallet.android.ui.models.actions.NftCollectionIdAction
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val collectionIdArg = "collection_id"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NftListViewModels(
    sessionRepository: SessionRepository,
    private val loadNft: LoadNFTCase,
    private val getNFT: GetNFTCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val collectionId = savedStateHandle.getStateFlow<String?>(collectionIdArg, null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val collections = collectionId.flatMapLatest { collectionId ->
        getNFT.getNft(collectionId).map { nftData ->
            nftData.filter { it.assets.isNotEmpty() }.map { nftData ->
                val isSingleAsset = nftData.assets.size == 1
                if (collectionId != null || isSingleAsset) {
                    nftData.assets.map { NftItemUIModel(nftData.collection, it) }
                } else {
                    listOf(NftItemUIModel(nftData.collection, null, nftData.assets.size))
                }
            }.flatten()
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val loadState = MutableStateFlow(true)

    val isLoading = loadState.combine(sessionRepository.session()) { loadState, session ->
        Pair(loadState, session ?: return@combine null)
    }
    .filterNotNull()
    .flatMapLatest { state ->
        flow {
            emit(true)
            viewModelScope.launch(Dispatchers.IO) { loadNft.loadNFT(state.second.wallet) }
            delay(500) // Show progress bar
            emit(false)
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun refresh() {
        loadState.update { true }
    }
}

data class NftItemUIModel(
    val collection: NFTCollection,
    val asset: NFTAsset? = null,
    val collectionSize: Int? = null,
) {
    val id: String get() = asset?.id ?: collection.id
    val imageUrl: String get() = asset?.image?.imageUrl ?: collection.image.imageUrl
    val name: String get() = asset?.name ?: collection.name

    fun onClick(collectionAction: NftCollectionIdAction, assetAction: NftAssetIdAction) {
        if (asset == null) collectionAction(collection.id) else assetAction(asset.id)
    }
}