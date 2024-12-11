package wallet.android.app

import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class ConfirmParamsTest {
    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }
    @Test
    fun testConfirmParamsPack() {
        val params: ConfirmParams = ConfirmParams.Stake.DelegateParams(AssetId(Chain.Cosmos), amount = BigInteger.TEN, validatorId = "cosmosaddress", from = Account(Chain.Cosmos, "", "", ""))
        val pack = params.pack()
        val unpack = ConfirmParams.unpack(TransactionType.StakeDelegate, pack!!)
        assertEquals("cosmosaddress", (unpack as ConfirmParams.Stake.DelegateParams).validatorId)
    }
}
