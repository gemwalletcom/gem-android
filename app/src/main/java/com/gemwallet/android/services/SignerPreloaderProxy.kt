package com.gemwallet.android.services

import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ScanAddressTarget
import com.wallet.core.primitives.ScanTransaction
import com.wallet.core.primitives.ScanTransactionPayload
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SignerPreloaderProxy(
    private val gemApiClient: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val nativeTransferClients: List<NativeTransferPreloader>,
    private val tokenTransferClients: List<TokenTransferPreloader>,
    private val stakeTransactionClients: List<StakeTransactionPreloader>,
    private val swapTransactionClients: List<SwapTransactionPreloader>,
    private val approvalTransactionClients: List<ApprovalTransactionPreloader>,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader,
    SwapTransactionPreloader, ApprovalTransactionPreloader {

    suspend fun preload(params: ConfirmParams): SignerParams = withContext(Dispatchers.IO) {

        val isValidTransactionJob = async {
            isValidTransaction(
                getScanTransactionPayload(
                    params,
                    sessionRepository.getSession()?.wallet ?: throw IllegalStateException("Session isn't available"),
                    getDeviceIdCase.getDeviceId()
                )
            )
        }

        val preloadJob = async {
            when (params) {
                is ConfirmParams.Stake -> preloadStake(params)
                is ConfirmParams.SwapParams -> preloadSwap(params)
                is ConfirmParams.TokenApprovalParams -> preloadApproval(params)
                is ConfirmParams.TransferParams.Native -> preloadNativeTransfer(params)
                is ConfirmParams.TransferParams.Token -> preloadTokenTransfer(params)
            }
        }

        val isValidTransaction = isValidTransactionJob.await()
        val preload = preloadJob.await()
        preload.copy(scanTransaction = isValidTransaction)
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

    private suspend fun isValidTransaction(payload: ScanTransactionPayload): ScanTransaction? {
        return try {
            gemApiClient.getScanTransaction(payload).getOrNull()?.data
        } catch (_: Throwable) {
            null
        }
    }

    private fun getScanTransactionPayload(params: ConfirmParams, wallet: Wallet, deviceId: String): ScanTransactionPayload {
        val chain = params.assetId.chain
        val origin = ScanAddressTarget(
            chain = chain,
            address = params.from.address,
        )
        val target = when (params) {
            is ConfirmParams.SwapParams -> ScanAddressTarget(
                params.toAssetId.chain,
                address = wallet.getAccount(params.toAssetId.chain)?.address ?: throw IllegalArgumentException("Account isn't available")
            )
            else -> ScanAddressTarget(chain, params.destination()?.address ?: throw IllegalArgumentException("Destination isn't available"))
        }

        return ScanTransactionPayload(
            deviceId = deviceId,
            walletIndex = wallet.index.toUInt(),
            origin = origin,
            target = target,
            type = params.getTxType(),
        )
    }
}