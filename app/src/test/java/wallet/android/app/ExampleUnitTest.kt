package wallet.android.app

import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testConfirmParamsPack() {
        val params: ConfirmParams = ConfirmParams.DelegateParams(AssetId(Chain.Cosmos), amount = BigInteger.TEN, validatorId = "cosmosaddress")
        val pack = params.pack()
        val unpack = ConfirmParams.unpack(ConfirmParams.DelegateParams::class.java, pack!!)!!
        assertEquals("cosmosaddress", unpack.validatorId)
    }
}