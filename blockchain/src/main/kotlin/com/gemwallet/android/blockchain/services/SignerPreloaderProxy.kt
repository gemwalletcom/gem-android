package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.algorand.toChainData
import com.gemwallet.android.blockchain.clients.aptos.toChainData
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinGatewayEstimateFee
import com.gemwallet.android.blockchain.clients.bitcoin.toChainData
import com.gemwallet.android.blockchain.clients.cardano.CardanoGatewayEstimateFee
import com.gemwallet.android.blockchain.clients.cardano.toChainData
import com.gemwallet.android.blockchain.clients.cosmos.toChainData
import com.gemwallet.android.blockchain.clients.ethereum.toChainData
import com.gemwallet.android.blockchain.clients.hyper.toChainData
import com.gemwallet.android.blockchain.clients.near.toChainData
import com.gemwallet.android.blockchain.clients.polkadot.PolkadotGatewayEstimateFee
import com.gemwallet.android.blockchain.clients.polkadot.toChainData
import com.gemwallet.android.blockchain.clients.solana.toChainData
import com.gemwallet.android.blockchain.clients.stellar.toChainData
import com.gemwallet.android.blockchain.clients.sui.toChainData
import com.gemwallet.android.blockchain.clients.ton.toChainData
import com.gemwallet.android.blockchain.clients.tron.toChainData
import com.gemwallet.android.blockchain.clients.xrp.toChainData
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.domains.confirm.toGem
import com.gemwallet.android.domains.stake.toGem
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.gemstone.GemApprovalData
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemStakeType
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import uniffi.gemstone.GemTransactionPreloadInput
import uniffi.gemstone.GemTransferDataExtra
import uniffi.gemstone.GemWalletConnectionSessionAppMetadata
import uniffi.gemstone.SwapperException
import uniffi.gemstone.TransferDataOutputAction
import uniffi.gemstone.TransferDataOutputType

class SignerPreloaderProxy(
    private val gateway: GemGateway,
) {

    suspend fun preload(params: ConfirmParams): SignerParams = withContext(Dispatchers.IO) {
        val assetId = params.assetId
        val chain = assetId.chain
        val feeAssetId = AssetId(chain)
        val gemChain = assetId.chain.string
        val destination = params.destination()?.address ?: throw java.lang.IllegalArgumentException()

        try {
            val inputType = getInputType(params)
            val metadata = gateway.getTransactionPreload(
                chain = gemChain,
                input = GemTransactionPreloadInput(
                    inputType = inputType,
                    senderAddress = params.from.address,
                    destinationAddress = destination
                )
            )
            val feeRates = gateway.getFeeRates(
                chain = gemChain,
                input = inputType
            )

            val transactionData = feeRates.map { feeRate ->
                val priority = FeePriority.entries.firstOrNull { it.string == feeRate.priority } ?: return@map null

                val result = gateway.getTransactionLoad(
                    chain = gemChain,
                    input = GemTransactionLoadInput(
                        inputType = inputType,
                        senderAddress = params.from.address,
                        destinationAddress = destination,
                        value = params.amount.toString(),
                        gasPrice = feeRate.gasPriceType,
                        memo = params.memo(),
                        isMaxValue = params.isMax(),
                        metadata = metadata,
                    ),
                    provider = getEstimateFee(chain)
                )
                val fee = chain.toFeeType().convertFee(feeAssetId, priority, result.fee)
                val chainData = result.metadata.toChainData()
                SignerParams.Data(chainData = chainData, fee = fee)
            }.filterNotNull()

            SignerParams(
                input = params,
                data = transactionData,
            )
        } catch (err: Throwable) {
            throw err
        }
    }

    private fun getInputType(params: ConfirmParams): GemTransactionInputType {
        val gemAsset = params.asset.toGem()
        val chain = params.assetId.chain.string

        return when (params) {
            is ConfirmParams.Stake.DelegateParams -> GemTransactionInputType.Stake(
                asset = gemAsset,
                stakeType = GemStakeType.Delegate(params.validator.toGem(chain))
            )
            is ConfirmParams.Stake.RedelegateParams -> GemTransactionInputType.Stake(
                asset = gemAsset,
                stakeType = GemStakeType.Redelegate(
                    delegation = params.delegation.toGem(chain),
                    toValidator = params.dstValidator.toGem(chain)
                )
            )
            is ConfirmParams.Stake.RewardsParams -> GemTransactionInputType.Stake(
                asset = gemAsset,
                stakeType = GemStakeType.WithdrawRewards(
                    validators = params.validators.map { it.toGem(chain) }
                )
            )
            is ConfirmParams.Stake.UndelegateParams -> GemTransactionInputType.Stake(
                asset = gemAsset,
                stakeType = GemStakeType.Undelegate(
                    delegation = params.delegation.toGem(chain),
                )
            )
            is ConfirmParams.Stake.WithdrawParams -> GemTransactionInputType.Stake(
                asset = gemAsset,
                stakeType = GemStakeType.Withdraw(params.delegation.toGem(chain))
            )
            is ConfirmParams.SwapParams -> GemTransactionInputType.Swap(
                fromAsset = params.fromAsset.toGem(),
                toAsset = params.toAsset.toGem(),
                swapData = params.toGem(),
            )
            is ConfirmParams.TokenApprovalParams -> GemTransactionInputType.TokenApprove(
                gemAsset,
                GemApprovalData(
                    params.assetId.tokenId!!,
                    spender = params.contract,
                    value = params.amount.toString(),
                )
            )
            is ConfirmParams.TransferParams.Generic -> GemTransactionInputType.Generic(
                asset = gemAsset,
                metadata = GemWalletConnectionSessionAppMetadata(
                    name = params.name,
                    description = params.description,
                    url = params.url,
                    icon = params.icon,
                ),
                extra = GemTransferDataExtra(
                    gasLimit = null,
                    gasPrice = null,
                    data = params.memo?.toByteArray(),
                    outputType = when (params.inputType) {
                        ConfirmParams.TransferParams.InputType.Signature -> TransferDataOutputType.SIGNATURE
                        ConfirmParams.TransferParams.InputType.EncodeTransaction -> TransferDataOutputType.ENCODED_TRANSACTION
                        null -> throw IllegalArgumentException("Not supported ${params.inputType}")
                    },
                    outputAction = TransferDataOutputAction.SEND
                ),
            )
            is ConfirmParams.Activate,
            is ConfirmParams.NftParams,
            is ConfirmParams.TransferParams.Native,
            is ConfirmParams.TransferParams.Token -> GemTransactionInputType.Transfer(gemAsset)
        }
    }

    private fun getEstimateFee(chain: Chain): GemGatewayEstimateFee {
        return when (chain.toChainType()) {
            ChainType.Bitcoin -> BitcoinGatewayEstimateFee()
            ChainType.Cardano -> CardanoGatewayEstimateFee()
            ChainType.Polkadot -> PolkadotGatewayEstimateFee()
            ChainType.Ethereum,
            ChainType.Solana,
            ChainType.Cosmos,
            ChainType.Ton,
            ChainType.Tron,
            ChainType.Aptos,
            ChainType.Sui,
            ChainType.Xrp,
            ChainType.Near,
            ChainType.Stellar,
            ChainType.Algorand,
            ChainType.HyperCore -> StubGatewayEstimateFee
        }
    }

    private object StubGatewayEstimateFee : GemGatewayEstimateFee {
        override suspend fun getFee(
            chain: uniffi.gemstone.Chain,
            input: GemTransactionLoadInput
        ): GemTransactionLoadFee? = null

        override suspend fun getFeeData(
            chain: uniffi.gemstone.Chain,
            input: GemTransactionLoadInput
        ): String? = null

    }
}

private sealed interface FeeType {

    fun convertFee(feeAssetId: AssetId, priority: FeePriority, gemFee: GemTransactionLoadFee): Fee

    object Plain : FeeType {

        override fun convertFee(
            feeAssetId: AssetId,
            priority: FeePriority,
            gemFee: GemTransactionLoadFee
        ): Fee {
            return Fee(
                feeAssetId = feeAssetId,
                priority = priority,
                amount = gemFee.fee.toBigInteger(),
                options = gemFee.options.options.mapKeys { it.key.name }.mapValues { it.value.toBigInteger() } // TODO: Review keys
            )
        }

    }
    object Regular : FeeType {

        override fun convertFee(
            feeAssetId: AssetId,
            priority: FeePriority,
            gemFee: GemTransactionLoadFee
        ): Fee {
            return GasFee(
                feeAssetId = feeAssetId,
                priority = priority,
                maxGasPrice = (gemFee.gasPriceType as GemGasPriceType.Regular).gasPrice.toBigInteger(),
                limit = gemFee.gasLimit.toBigInteger(),
                amount = gemFee.fee.toBigInteger(),
                options = gemFee.options.options.mapKeys { it.key.name }.mapValues { it.value.toBigInteger() } // TODO: Review keys
            )
        }

    }
    object Eip1559 : FeeType {

        override fun convertFee(
            feeAssetId: AssetId,
            priority: FeePriority,
            gemFee: GemTransactionLoadFee
        ): Fee {
            return (gemFee.gasPriceType as GemGasPriceType.Eip1559).let { price ->
                GasFee(
                    feeAssetId = feeAssetId,
                    priority = priority,
                    maxGasPrice = price.gasPrice.toBigInteger(),
                    minerFee = price.priorityFee.toBigInteger(),
                    limit = gemFee.gasLimit.toBigInteger(),
                    amount = gemFee.fee.toBigInteger(),
                    options = gemFee.options.options.mapKeys { it.key.name }.mapValues { it.value.toBigInteger() } // TODO: Change keys
                )
            }
        }
    }

    object Solana : FeeType {

        override fun convertFee(
            feeAssetId: AssetId,
            priority: FeePriority,
            gemFee: GemTransactionLoadFee
        ): Fee {
            return (gemFee.gasPriceType as GemGasPriceType.Solana).let { price ->
                GasFee(
                    feeAssetId = feeAssetId,
                    priority = priority,
                    maxGasPrice = price.gasPrice.toBigInteger(),
                    minerFee = price.priorityFee.toBigInteger(),
//                  unitFee = price.unitPrice.toBigInteger(),
                    limit = gemFee.gasLimit.toBigInteger(),
                    amount = gemFee.fee.toBigInteger(),
                    options = gemFee.options.options.mapKeys { it.key.name }.mapValues { it.value.toBigInteger() } // TODO: Change keys
                )
            }
        }

    }
}

private fun Chain.toFeeType() = when (this.toChainType()) {
    ChainType.Solana -> FeeType.Solana
    ChainType.Bitcoin,
    ChainType.Cosmos,
    ChainType.Aptos -> FeeType.Regular
    ChainType.Ethereum -> FeeType.Eip1559
    ChainType.HyperCore,
    ChainType.Ton,
    ChainType.Tron,
    ChainType.Sui,
    ChainType.Xrp,
    ChainType.Near,
    ChainType.Stellar,
    ChainType.Algorand,
    ChainType.Polkadot,
    ChainType.Cardano -> FeeType.Plain
}

private fun GemTransactionLoadMetadata.toChainData() = when (this) {
    is GemTransactionLoadMetadata.Algorand -> toChainData()
    is GemTransactionLoadMetadata.Aptos -> toChainData()
    is GemTransactionLoadMetadata.Bitcoin -> toChainData()
    is GemTransactionLoadMetadata.Cardano -> toChainData()
    is GemTransactionLoadMetadata.Cosmos -> toChainData()
    is GemTransactionLoadMetadata.Evm -> toChainData()
    is GemTransactionLoadMetadata.Near -> toChainData()
    is GemTransactionLoadMetadata.Polkadot -> toChainData()
    is GemTransactionLoadMetadata.Solana -> toChainData()
    is GemTransactionLoadMetadata.Stellar -> toChainData()
    is GemTransactionLoadMetadata.Sui -> toChainData()
    is GemTransactionLoadMetadata.Ton -> toChainData()
    is GemTransactionLoadMetadata.Tron -> toChainData()
    is GemTransactionLoadMetadata.Xrp -> toChainData()
    is GemTransactionLoadMetadata.Hyperliquid -> toChainData()
    GemTransactionLoadMetadata.None -> throw SwapperException.NotSupportedChain()
}