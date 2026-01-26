package com.gemwallet.android.data.repositoreis.wallets

import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.blockchain.operators.InvalidPhrase
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.blockchain.services.AddressStatusService
import com.gemwallet.android.cases.banners.AddBanner
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.wallet.ImportError
import com.gemwallet.android.cases.wallet.ImportWalletService
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.keyEncodingTypes
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ImportType
import com.wallet.core.primitives.AddressStatus
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EncodingType
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.jni.Base32
import wallet.core.jni.Base58
import wallet.core.jni.PrivateKey

class PhraseAddressImportWalletService(
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val sessionRepository: SessionRepository,
    private val storePhraseOperator: StorePhraseOperator,
    private val phraseValidate: ValidatePhraseOperator,
    private val addressValidate: ValidateAddressOperator,
    private val passwordStore: PasswordStore,
    private val syncSubscription: SyncSubscription,
    private val addressStatusService: AddressStatusService,
    private val addBanner: AddBanner,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : ImportWalletService {

    override suspend fun importWallet(
        importType: ImportType,
        walletName: String,
        data: String
    ): Result<Wallet> {
        val wallet = try {
            when (importType.walletType) {
                WalletType.Multicoin -> handlePhrase(importType, walletName, data, WalletSource.Import)
                WalletType.Single -> handlePhrase(importType, walletName, data, WalletSource.Import)
                WalletType.View -> handleAddress(importType.chain!!, walletName, data)
                WalletType.PrivateKey -> handlePrivateKey(importType.chain!!, walletName, data)
            }
        } catch (err: ImportError.DuplicatedWallet) {
            return Result.success(err.wallet)
        } catch (err: Throwable) {
            return Result.failure(err)
        }

        setupWallet(wallet)
        assetsRepository.importAssets(wallet)
        checkAddresses(wallet)

        return Result.success(wallet)
    }

    override suspend fun createWallet(walletName: String, data: String): Result<Wallet> =
        withContext(Dispatchers.IO) {
            val wallet = try {
                handlePhrase(ImportType(WalletType.Multicoin), walletName, data, WalletSource.Create)
            } catch (err: Throwable) {
                return@withContext Result.failure(err)
            }

            setupWallet(wallet)

            Result.success(wallet)
        }

    private suspend fun setupWallet(wallet: Wallet) {
        assetsRepository.createAssets(wallet)
        sessionRepository.setWallet(wallet)
        syncSubscription.syncSubscription(listOf(wallet), true)
    }

    private suspend fun handlePhrase(importType: ImportType, walletName: String, rawData: String, source: WalletSource): Wallet {
        val cleanedData = rawData.trim().split("\\s+".toRegex()).joinToString(" ") { it.trim() }
        val validateResult = phraseValidate(cleanedData)
        if (validateResult.isFailure || validateResult.getOrNull() != true) {
            val error = validateResult.exceptionOrNull() ?: InvalidPhrase
            throw when (error) {
                is InvalidWords -> ImportError.InvalidWords(error.words)
                else -> ImportError.InvalidationSecretPhrase
            }
        }
        val wallet = walletsRepository.addControlled(walletName, cleanedData, importType.walletType, importType.chain, source)

        val password = passwordStore.createPassword(wallet.id)
        val storeResult = storePhraseOperator(wallet, cleanedData, password)
        return if (storeResult.isSuccess) {
            wallet
        } else {
            walletsRepository.removeWallet(wallet.id)
            throw storeResult.exceptionOrNull() ?: ImportError.CreateError("Unknown error")
        }
    }

    private suspend fun handleAddress(chain: Chain, walletName: String, data: String): Wallet {
        if (addressValidate(data, chain).getOrNull() != true) {
            throw ImportError.InvalidAddress
        }
        return try {
            val wallet = walletsRepository.addWatch(walletName, data, chain)
            wallet
        } catch (err: ImportError.DuplicatedWallet) {
            err.wallet
        } catch (err: Exception) {
            throw ImportError.CreateError(err.message ?: "Unknown error")
        }
    }

    private suspend fun handlePrivateKey(chain: Chain, walletName: String, data: String): Wallet {
        val key = try {
            val data = decodePrivateKey(chain, data.trim())

            if (!PrivateKey.isValid(data, WCChainTypeProxy().invoke(chain).curve())) {
                throw Exception()
            }
            data.toHexString()
        } catch (_: Throwable) {
            throw ImportError.InvalidationPrivateKey
        }

        val wallet = try {
            walletsRepository.addControlled(walletName, key, WalletType.PrivateKey, chain, source = WalletSource.Import)
        } catch (err: ImportError.DuplicatedWallet) {
            return err.wallet
        }

        val password = passwordStore.createPassword(wallet.id)
        val storeResult = storePhraseOperator(wallet, key, password)
        return if (storeResult.isSuccess) {
            wallet
        } else {
            walletsRepository.removeWallet(wallet.id)
            throw storeResult.exceptionOrNull() ?: ImportError.CreateError("Unknown error")
        }
    }

    private suspend fun checkAddresses(wallet: Wallet) {
        wallet.accounts.forEach {
            val statuses = addressStatusService.getAddressStatus(it.chain, it.address)
            for (status in statuses) {
                if (status == AddressStatus.MultiSignature) {
                    addBanner.addBanner(
                        wallet = wallet,
                        chain = it.chain,
                        event = BannerEvent.AccountBlockedMultiSignature,
                        state = BannerState.AlwaysActive,
                    )
                }
            }
        }
    }

    companion object {

        fun decodePrivateKey(chain: Chain, data: String): ByteArray {
            chain.keyEncodingTypes.forEach { type ->
                when (type) {
                    EncodingType.Base58 -> {
                        val decoded = Base58.decodeNoCheck(data)
                            ?.takeIf { it.size % 32 == 0 }?.slice(0..< 32)
                            ?.toByteArray()
                        if (decoded != null) {
                            return decoded
                        }
                    }
                    EncodingType.Base32 -> {
                        val decoded = decodeBase32(chain, data)
                        if (decoded != null) {
                            return decoded
                        }
                    }
                    EncodingType.Hex -> return data.decodeHex()
                }
            }
            throw IllegalArgumentException("Invalid private key encoding")
        }

        fun decodeBase32(chain: Chain, data: String): ByteArray? {
            return when (chain) {
                Chain.Stellar -> {
                    if (data.length != 56 || !data.startsWith("S")) {
                        return null
                    }
                    val decoded = Base32.decode(data)
                    if (decoded.size == 35 && decoded[0] == 0x90.toByte()) {
                        decoded.slice(1 .. 32).toByteArray()
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
    }
}