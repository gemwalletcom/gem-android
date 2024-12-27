package com.gemwallet.android.blockchain.operators

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.operators.walletcore.WCCreateAccountOperator
import com.gemwallet.android.blockchain.testPhrase
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestCreateAccountOperator {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testCreate_account_solana() {
        val operator = WCCreateAccountOperator()
        val result = operator(walletType = WalletType.multicoin, data = testPhrase, Chain.Solana)
        assertEquals("4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", result.address)
        assertEquals("m/44'/501'/0'", result.derivationPath)
        assertEquals("", result.extendedPublicKey)
    }

    @Test
    fun testCreate_account_bitcoincache() {
        val operator = WCCreateAccountOperator()
        val result = operator(walletType = WalletType.multicoin, data = testPhrase, Chain.BitcoinCash)
        assertEquals("qq29xrkkd68alnrca375qlfyhwdqdkevsvmgkq9cmw", result.address)
        assertEquals("m/44'/145'/0'/0/0", result.derivationPath)
        assertEquals("xpub6Cd3LU6iyrbbhxPRYZpE5hGUdmrQVpQ79i9RYNLrs2iVrtYkKRv6swMWeTpPfomebgisrRGPrFvt1qaFiZLLuQdSFRVBWdbKD4HWnMrFsjR", result.extendedPublicKey)
    }


    @Test
    fun testCreate_account_evm() {
        val operator = WCCreateAccountOperator()
        val result = operator(walletType = WalletType.multicoin, data = testPhrase, Chain.Ethereum)
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", result.address)
        assertEquals("m/44'/60'/0'/0/0", result.derivationPath)
        assertEquals("", result.extendedPublicKey)
    }
}