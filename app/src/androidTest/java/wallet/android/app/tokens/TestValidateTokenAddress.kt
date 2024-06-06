package wallet.android.app.tokens

import com.gemwallet.android.blockchain.clients.ethereum.EvmGetTokenClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTokenClient
import com.gemwallet.android.blockchain.clients.sui.SuiGetTokenClient
import com.gemwallet.android.blockchain.clients.ton.TonGetTokenClient
import com.gemwallet.android.blockchain.clients.tron.TronGetTokenClient
import org.junit.Test

class TestValidateTokenAddress {

    @Test
    fun testEVMToken() {
        EvmGetTokenClient.isTokenAddress("0xdAC17F958D2ee523a2206206994597C13D831ec7")
    }

    @Test
    fun testSolanaToken() {
        SolanaTokenClient.isTokenAddress("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
    }

    @Test
    fun testSuiToken() {
        SuiGetTokenClient.isTokenAddress("0x5d4b302506645c37ff133b98c4b50a5ae14841659738d6d733d59d0d217a93bf::coin::COIN")
    }

    @Test
    fun testTonToken() {
        TonGetTokenClient.isTokenAddress("EQAxQydO5Npp5-YYu0hciOW57EJCTBuJ8hKYGW43Xy1m0xdP")
    }

    @Test
    fun testTronToken() {
        TronGetTokenClient.isTokenAddress("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
    }
}