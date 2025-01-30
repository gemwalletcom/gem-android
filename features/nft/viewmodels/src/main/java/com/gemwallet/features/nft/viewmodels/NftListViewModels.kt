package com.gemwallet.features.nft.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nft.GetListNftCase
import com.gemwallet.android.cases.nft.LoadNFTCase
import com.gemwallet.android.cases.nft.NftError
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ui.models.NftItemUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val collectionIdArg = "collectionId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NftListViewModels @Inject constructor(
    sessionRepository: SessionRepository,
    private val loadNft: LoadNFTCase,
    private val getNFT: GetListNftCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val loadState = MutableStateFlow(false) // TODO: Improve it. This just trigger for start synchronization.

    val isLoading = loadState.combine(sessionRepository.session()) { loadState, session ->
        Pair(loadState, session ?: return@combine null)
    }
    .filterNotNull()
    .flatMapLatest { state ->
        flow {
            if (!state.first) return@flow
            emit(true)
            loadNft.loadNFT(state.second.wallet)
            emit(false)
            loadState.update { false }
        }
    }
    .catch { error.update { NftError.LoadError } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val collectionId = savedStateHandle.getStateFlow<String?>(collectionIdArg, null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val collections = collectionId
        .onEach { loadState.emit(it == null) }
        .flatMapLatest { collectionId ->
        getNFT.getListNft(collectionId).map { nftData ->
            nftData.filter { it.assets.isNotEmpty() }.map { nftData ->
                val isSingleAsset = nftData.assets.size == 1
                if (collectionId != null || isSingleAsset) {
                    nftData.assets.map { NftItemUIModel(nftData.collection, it) }
                } else {
                    listOf(NftItemUIModel(nftData.collection, null, nftData.assets.size))
                }
            }
            .flatten()
            .sortedBy { it.name }
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val error = MutableStateFlow<NftError?>(null)

    fun refresh() {
        viewModelScope.launch { loadState.emit(true) }
    }
}