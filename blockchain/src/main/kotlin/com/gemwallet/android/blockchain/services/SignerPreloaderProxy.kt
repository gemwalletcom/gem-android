package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.ActivationTransactionPreloader
import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.GenericTransferPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.NftTransactionPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.blockchain.clients.solana.models.SolanaChainData
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.SolanaTokenProgramId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemSolanaTokenProgramId
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import uniffi.gemstone.GemTransactionPreloadInput

class SignerPreloaderProxy(
//    private val gemApiClient: GemApiClient,
//    private val sessionRepository: SessionRepository,
//    private val getDeviceIdCase: GetDeviceIdCase,
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
//        val isValidTransactionJob = async {
//            isValidTransaction(
//                getScanTransactionPayload(
//                    params,
//                    sessionRepository.getSession()?.wallet
//                        ?: throw IllegalStateException("Session isn't available"),
//                    getDeviceIdCase.getDeviceId()
//                )
//            )
//        }

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

//        val isValidTransaction = isValidTransactionJob.await()
        val preload = preloadJob.await()
//        preload.copy(scanTransaction = isValidTransaction)
        preload
    }

    override fun supported(chain: Chain): Boolean {
        return (nativeTransferClients
                + tokenTransferClients
                + stakeTransactionClients
                + swapTransactionClients
                + approvalTransactionClients).getClient(chain) != null
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
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
        when (params.assetId.chain) {
            Chain.Solana -> {
                val gemAsset = params.asset.toGem()
                val assetId = params.assetId
                val chain = assetId.chain
                val gemChain = assetId.chain.string
                return try {
                    val result = gateway.getTransactionPreload(
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
                        val feeRegular = feeRate.gasPriceType as GemGasPriceType.Solana
                        when (chain) {
                            Chain.Solana -> {
                                val metadata = result as? GemTransactionLoadMetadata.Solana
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
                                        metadata = metadata!!,
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
                            }
                            else -> null
                        }
                    }.filterNotNull()
                    val metadata = result as GemTransactionLoadMetadata.Solana
                    SignerParams(
                        input = params,
                        chainData = SolanaChainData(
                            blockhash = metadata.blockHash,
                            senderTokenAddress = metadata.senderTokenAddress,
                            recipientTokenAddress = metadata.recipientTokenAddress,
                            tokenProgram = when (metadata.tokenProgram) {
                                GemSolanaTokenProgramId.TOKEN -> SolanaTokenProgramId.Token
                                GemSolanaTokenProgramId.TOKEN2022 -> SolanaTokenProgramId.Token2022
                                null -> null
                            },
                            fees = fees
                        )
                    )
                } catch (err: Throwable) {
                    throw err
                }
            }
            else -> {}
        }
        return genericPreloaderClients.getClient(params.from.chain)?.preloadGeneric(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadNft(params: ConfirmParams.NftParams): SignerParams {
        return nftPreloadClients.getClient(params.from.chain)?.preloadNft(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

//    private suspend fun isValidTransaction(payload: ScanTransactionPayload): ScanTransaction? {
//        return try {
//            gemApiClient.getScanTransaction(payload).data
//        } catch (_: Throwable) {
//            null
//        }
//    }

//    private fun getScanTransactionPayload(params: ConfirmParams, wallet: Wallet, deviceId: String): ScanTransactionPayload {
//        val chain = params.assetId.chain
//        val origin = ScanAddressTarget(
//            chain = chain,
//            address = params.from.address,
//        )
//        val target = when (params) {
//            is ConfirmParams.SwapParams -> ScanAddressTarget(
//                params.toAssetId.chain,
//                address = wallet.getAccount(params.toAssetId.chain)?.address
//                    ?: throw IllegalArgumentException("Account isn't available")
//            )
//            is ConfirmParams.Stake -> ScanAddressTarget(chain, params.validatorId)
//            is ConfirmParams.TokenApprovalParams -> ScanAddressTarget(chain, params.contract)
//            is ConfirmParams.TransferParams.Native,
//            is ConfirmParams.TransferParams.Generic,
//            is ConfirmParams.TransferParams.Token -> ScanAddressTarget(
//                chain,
//                params.destination().address
//            )
//            is ConfirmParams.Activate -> ScanAddressTarget(chain, params.from.address)
//            is ConfirmParams.NftParams -> ScanAddressTarget(chain, params.from.address)
//        }
//
//        return ScanTransactionPayload(
//            deviceId = deviceId,
//            walletIndex = wallet.index.toUInt(),
//            origin = origin,
//            target = target,
//            type = params.getTxType(),
//        )
//    }
}