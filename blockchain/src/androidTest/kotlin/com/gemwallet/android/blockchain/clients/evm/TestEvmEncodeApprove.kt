package com.gemwallet.android.blockchain.clients.evm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.ethereum.encodeApprove
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.math.toHexString
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestEvmEncodeApprove {

    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testEncodeApprove() {
        assertEquals(
            "0x095ea7b30000000000000000000000009b1db81180c31b1b428572be105e209b5a6222b77fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            encodeApprove("0x9b1DB81180c31B1b428572Be105E209b5A6222b7").toHexString()
        )
    }
}