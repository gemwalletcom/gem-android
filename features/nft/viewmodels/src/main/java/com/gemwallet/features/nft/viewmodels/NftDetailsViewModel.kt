package com.gemwallet.features.nft.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nft.GetAssetNft
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTAttribute
import com.wallet.core.primitives.NFTCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

val nftAssetIdArg = "assetId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NftDetailsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getAssetNft: GetAssetNft,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assetId = savedStateHandle.getStateFlow<String?>(nftAssetIdArg, null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val nftAsset = assetId.filterNotNull()
        .flatMapLatest { getAssetNft.getAssetNft(it) }
        .catch { Log.d("NFT-DETAILS", "Error on get nft: ", it) }
        .filterNotNull()
        .map { NftAssetDetailsUIModel(it.collection, it.assets.first(), sessionRepository.getSession()?.wallet?.getAccount(it.assets.first().chain)!!) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

class NftAssetDetailsUIModel(
    val collection: NFTCollection,
    val asset: NFTAsset,
    val account: Account,
) {
    val imageUrl: String get() = asset.images.preview.url
    val assetName: String get() = asset.name
    val collectionName: String get() = collection.name
    val description: String? get() = asset.description
    val attributes: List<NFTAttribute> get() = asset.attributes
}