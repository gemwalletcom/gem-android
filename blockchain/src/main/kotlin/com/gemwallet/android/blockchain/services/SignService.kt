package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemChainSigner
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemTransactionLoadInput
import java.math.BigInteger

class SignService : SignClient {

    override suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return GemChainSigner(chain.string).signMessage(input, privateKey).toByteArray()
    }

    override suspend fun signTypedMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return GemChainSigner(chain.string).signMessage(input, privateKey).toByteArray()
    }

    override suspend fun signActivate(
        params: ConfirmParams.Activate,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(
            getSigner(params).signAccountAction(data, privateKey).toByteArray()
        )
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signFreeze(
        params: ConfirmParams.Stake.Freeze,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signGenericTransfer(
        params: ConfirmParams.TransferParams.Generic,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(getSigner(params).signTransfer(data, privateKey).toByteArray())
    }

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(
            getSigner(params).signTransfer(data, privateKey).toByteArray()
        )
    }

    override suspend fun signNft(
        params: ConfirmParams.NftParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(
            getSigner(params).signNftTransfer(data, privateKey).toByteArray()
        )
    }

    override suspend fun signPerpetualClose(
        params: ConfirmParams.PerpetualParams.Close,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signPerpetual(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signPerpetualModify(
        params: ConfirmParams.PerpetualParams.Modify,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signPerpetual(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signPerpetualOpen(
        params: ConfirmParams.PerpetualParams.Open,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signPerpetual(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signRedelegate(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signRewards(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signSwap(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signTokenApproval(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(getSigner(params).signTokenApproval(data, privateKey).toByteArray())
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(getSigner(params).signTokenTransfer(data, privateKey).toByteArray())
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signUnfreeze(
        params: ConfirmParams.Stake.Unfreeze,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return getSigner(params).signStake(data, privateKey).map { it.toByteArray() }
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = getLoadInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
        )
        return listOf(getSigner(params).signWithdrawal(data, privateKey).toByteArray())
    }
    
    override fun supported(chain: Chain): Boolean {
        return when(chain) {
            Chain.HyperCore,
            Chain.Aptos,
            Chain.Sui -> true
            Chain.Bitcoin,
            Chain.BitcoinCash,
            Chain.Litecoin,
            Chain.Ethereum,
            Chain.SmartChain,
            Chain.Solana,
            Chain.Polygon,
            Chain.Thorchain,
            Chain.Cosmos,
            Chain.Osmosis,
            Chain.Arbitrum,
            Chain.Ton,
            Chain.Tron,
            Chain.Doge,
            Chain.Zcash,
            Chain.Optimism,
            Chain.Base,
            Chain.AvalancheC,
            Chain.Xrp,
            Chain.OpBNB,
            Chain.Fantom,
            Chain.Gnosis,
            Chain.Celestia,
            Chain.Injective,
            Chain.Sei,
            Chain.Manta,
            Chain.Blast,
            Chain.Noble,
            Chain.ZkSync,
            Chain.Linea,
            Chain.Mantle,
            Chain.Celo,
            Chain.Near,
            Chain.World,
            Chain.Stellar,
            Chain.Sonic,
            Chain.Algorand,
            Chain.Polkadot,
            Chain.Plasma,
            Chain.Cardano,
            Chain.Abstract,
            Chain.Berachain,
            Chain.Ink,
            Chain.Unichain,
            Chain.Hyperliquid,
            Chain.Monad,
            Chain.XLayer,
            Chain.Stable -> false
        } 
    }

    private fun getLoadInput(
        params: ConfirmParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
    ) = GemTransactionLoadInput(
        inputType = params.toDto(),
        senderAddress = params.from.address,
        destinationAddress = params.destination()?.address ?: "",
        value = finalAmount.toString(),
        gasPrice = when (fee) {
            is Fee.Eip1559 -> GemGasPriceType.Eip1559(
                gasPrice = fee.maxGasPrice.toString(),
                priorityFee = fee.minerFee.toString(),
            )
            is Fee.Plain -> GemGasPriceType.Regular(
                gasPrice = fee.amount.toString(),
            )
            is Fee.Regular -> GemGasPriceType.Regular(
                gasPrice = fee.amount.toString(),
            )
            is Fee.Solana -> GemGasPriceType.Solana(
                gasPrice = fee.amount.toString(),
                priorityFee = fee.minerFee.toString(),
                unitPrice = fee.unitFee.toString(),
            )
        },
        memo = params.memo(),
        isMaxValue = params.useMaxAmount,
        metadata = chainData.toDto(),
    )

    private fun getSigner(params: ConfirmParams) = GemChainSigner(params.asset.chain.string)
}