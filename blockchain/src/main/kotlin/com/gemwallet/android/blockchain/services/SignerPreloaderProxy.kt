package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.ActivationTransactionPreloader
import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.GenericTransferPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.NftTransactionPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.algorand.toChainData
import com.gemwallet.android.blockchain.clients.aptos.toChainData
import com.gemwallet.android.blockchain.clients.bitcoin.toChainData
import com.gemwallet.android.blockchain.clients.cardano.toChainData
import com.gemwallet.android.blockchain.clients.cosmos.toChainData
import com.gemwallet.android.blockchain.clients.ethereum.toChainData
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.blockchain.clients.near.toChainData
import com.gemwallet.android.blockchain.clients.polkadot.toChainData
import com.gemwallet.android.blockchain.clients.solana.toChainData
import com.gemwallet.android.blockchain.clients.stellar.toChainData
import com.gemwallet.android.blockchain.clients.sui.toChainData
import com.gemwallet.android.blockchain.clients.ton.toChainData
import com.gemwallet.android.blockchain.clients.tron.toChainData
import com.gemwallet.android.blockchain.clients.xrp.toChainData
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import uniffi.gemstone.GemTransactionPreloadInput
import uniffi.gemstone.SwapperException

class SignerPreloaderProxy(
    private val gateway: GemGateway,
    private val nativeTransferClients: List<NativeTransferPreloader>,
    private val tokenTransferClients: List<TokenTransferPreloader>,
    private val stakeTransactionClients: List<StakeTransactionPreloader>,
    private val swapTransactionClients: List<SwapTransactionPreloader>,
    private val approvalTransactionClients: List<ApprovalTransactionPreloader>,
    private val activatePreloaderClients: List<ActivationTransactionPreloader>,
    private val genericPreloaderClients: List<GenericTransferPreloader>,
    private val nftPreloadClients: List<NftTransactionPreloader>,
) : GenericTransferPreloader, NativeTransferPreloader, TokenTransferPreloader,
    StakeTransactionPreloader,
    SwapTransactionPreloader, ApprovalTransactionPreloader, ActivationTransactionPreloader,
    NftTransactionPreloader {

    suspend fun preload(params: ConfirmParams): SignerParams = withContext(Dispatchers.IO) {
        val preloadJob = async {
            when (params) {
                is ConfirmParams.Stake -> preloadStake(params)
                is ConfirmParams.SwapParams -> preloadSwap(params)
                is ConfirmParams.TokenApprovalParams -> preloadApproval(params)
                is ConfirmParams.TransferParams.Native -> preloadNativeTransfer(params)
                is ConfirmParams.TransferParams.Token -> preloadTokenTransfer(params)
                is ConfirmParams.Activate -> preloadActivate(params)
                is ConfirmParams.TransferParams.Generic -> preloadGeneric(params)
                is ConfirmParams.NftParams -> preloadNft(params)
            }
        }

        preloadJob.await()
    }

    override fun supported(chain: Chain): Boolean {
        return (nativeTransferClients
                + tokenTransferClients
                + stakeTransactionClients
                + swapTransactionClients
                + approvalTransactionClients).getClient(chain) != null
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        when (params.assetId.chain) {
            Chain.Solana -> {
                val gemAsset = params.asset.toGem()
                val assetId = params.assetId
                val chain = assetId.chain
                val gemChain = assetId.chain.string
                return try {
                    val metadata = gateway.getTransactionPreload(
                        chain = gemChain,
                        input = GemTransactionPreloadInput(
                            inputType = GemTransactionInputType.Transfer(gemAsset),
                            senderAddress = params.from.address,
                            destinationAddress = params.destination.address,
                        )
                    )
                    val feeRates = gateway.getFeeRates(
                        chain = gemChain,
                        input = GemTransactionInputType.Transfer(gemAsset),
                    )

                    val fees = feeRates.map { feeRate ->
                        val feeRegular = feeRate.gasPriceType
                        val result = gateway.getTransactionLoad(
                            chain = gemChain,
                            input = GemTransactionLoadInput(
                                inputType = GemTransactionInputType.Transfer(gemAsset),
                                senderAddress = params.from.address,
                                destinationAddress = params.destination.address,
                                value = params.amount.toString(),
                                gasPrice = feeRegular,
                                memo = params.memo,
                                isMaxValue = params.isMax(),
                                metadata = metadata,
                            ),
                            provider = object : GemGatewayEstimateFee {
                                override suspend fun getFee(
                                    chain: uniffi.gemstone.Chain,
                                    input: GemTransactionLoadInput
                                ): GemTransactionLoadFee? = null

                                override suspend fun getFeeData(
                                    chain: uniffi.gemstone.Chain,
                                    input: GemTransactionLoadInput
                                ): String? = null
                            }
                        )
                        GasFee(
                            feeAssetId = AssetId(chain),
                            priority = FeePriority.entries.firstOrNull { it.string == feeRate.priority } ?: return@map null,
                            limit = result.fee.gasLimit.toBigInteger(),
                            maxGasPrice = result.fee.fee.toBigInteger(),
                            options = result.fee.options.options.mapKeys { it.key.name }.mapValues { it.value.toBigInteger() }
                        )
                    }.filterNotNull()
                    val chainData = when (metadata) {
                        is GemTransactionLoadMetadata.Algorand -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Aptos -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Bitcoin -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Cardano -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Cosmos -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Evm -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Near -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Polkadot -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Solana -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Stellar -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Sui -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Ton -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Tron -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Xrp -> metadata.toChainData()
                        is GemTransactionLoadMetadata.Hyperliquid -> throw SwapperException.NotSupportedChain()// metadata.toChainData()
                        GemTransactionLoadMetadata.None -> throw SwapperException.NotSupportedChain()
                    }
                    SignerParams(
                        input = params,
                        chainData = chainData,
                        fee = fees
                    )
                } catch (err: Throwable) {
                    throw err
                }
            }
            else -> {}
        }
        return nativeTransferClients.getClient(params.from.chain)?.preloadNativeTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return tokenTransferClients.getClient(params.from.chain)?.preloadTokenTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        return stakeTransactionClients.getClient(params.from.chain)?.preloadStake(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        return swapTransactionClients.getClient(params.from.chain)?.preloadSwap(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadApproval(params: ConfirmParams.TokenApprovalParams): SignerParams {
        return approvalTransactionClients.getClient(params.from.chain)?.preloadApproval(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadActivate(params: ConfirmParams.Activate): SignerParams {
        return activatePreloaderClients.getClient(params.from.chain)?.preloadActivate(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadGeneric(params: ConfirmParams.TransferParams.Generic): SignerParams {
        return genericPreloaderClients.getClient(params.from.chain)?.preloadGeneric(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadNft(params: ConfirmParams.NftParams): SignerParams {
        return nftPreloadClients.getClient(params.from.chain)?.preloadNft(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }
}