package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.NftAssetIdAction
import com.gemwallet.android.ui.models.actions.NftCollectionIdAction
import com.gemwallet.features.nft.presents.NFTDetailsScene
import com.gemwallet.features.nft.presents.NftListScene
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

val nftRoute = "nft"

@Serializable
data class NftCollectionRoute(val collectionId: String)

@Serializable
data class NftAssetRoute(val assetId: String)

fun NavController.navigateToNftCollection(collectionId: String) {
    navigate(NftCollectionRoute(collectionId), navOptions { launchSingleTop = true })
}

fun NavController.navigateToNftAsset(assetId: String) {
    navigate(NftAssetRoute(assetId), navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.nftCollection(
    cancelAction: CancelAction,
    onRecipient: (AssetId, String) -> Unit,
    collectionIdAction: NftCollectionIdAction,
    assetIdAction: NftAssetIdAction,
) {
    composable<NftCollectionRoute> {
        NftListScene(cancelAction, collectionIdAction, assetIdAction)
    }

    composable<NftAssetRoute> {
        NFTDetailsScene(cancelAction, onRecipient)
    }
}