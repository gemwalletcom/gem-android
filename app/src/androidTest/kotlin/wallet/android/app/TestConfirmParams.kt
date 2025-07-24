package wallet.android.app

import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class TestConfirmParams {
    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }
    @Test
    fun testConfirmParamsPack() {
        val params: ConfirmParams = ConfirmParams.Stake.DelegateParams(Chain.Cosmos.asset(), amount = BigInteger.TEN, validatorId = "cosmosaddress", from = Account(Chain.Cosmos, "", "", ""))
        val pack = params.pack()
        val unpack = ConfirmParams.unpack(pack!!)
        assertEquals("cosmosaddress", (unpack as ConfirmParams.Stake.DelegateParams).validatorId)
    }
}
