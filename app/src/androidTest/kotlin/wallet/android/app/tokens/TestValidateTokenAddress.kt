package wallet.android.app.tokens

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestValidateTokenAddress {

    @Test
    fun testAptosToken() {
        assertFalse(AptosGetTokenClient.isTokenAddress("0xdAC17F958D2ee523a2206206994597C13D831ec7"))
        assertTrue(AptosGetTokenClient.isTokenAddress("0x111ae3e5bc816a5e63c2da97d0aa3886519e0cd5e4b046659fa35796bd11542a::amapt_token::AmnisApt"))
    }

    @Test
    fun testEVMToken() {
        assertTrue(EvmGetTokenClient.isTokenAddress("0xdAC17F958D2ee523a2206206994597C13D831ec7"))
    }

    @Test
    fun testSolanaToken() {
        assertTrue(SolanaTokenClient.isTokenAddress("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"))
    }

    @Test
    fun testSuiToken() {
        assertTrue(SuiGetTokenClient.isTokenAddress("0x5d4b302506645c37ff133b98c4b50a5ae14841659738d6d733d59d0d217a93bf::coin::COIN"))
    }

    @Test
    fun testTonToken() {
        assertTrue(TonGetTokenClient.isTokenAddress("EQAxQydO5Npp5-YYu0hciOW57EJCTBuJ8hKYGW43Xy1m0xdP"))
    }

    @Test
    fun testTronToken() {
        assertTrue(TronGetTokenClient.isTokenAddress("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"))
    }
}