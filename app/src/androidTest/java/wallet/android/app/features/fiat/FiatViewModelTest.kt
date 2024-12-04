package wallet.android.app.features.fiat

import app.cash.turbine.test
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.features.buy.viewmodels.FiatSceneState
import com.gemwallet.android.features.buy.viewmodels.FiatViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FiatViewModelTest {

    private lateinit var viewModel: FiatViewModel
    private val sessionRepository: SessionRepository = mockk()
    private val assetsRepository: AssetsRepository = mockk()
    private val buyRepository: BuyRepository = mockk()

    @Before
    fun setup() {
        viewModel = FiatViewModel(
            sessionRepository = sessionRepository,
            assetsRepository = assetsRepository,
            buyRepository = buyRepository,
            savedStateHandle = mockk(relaxed = true)
        )
    }

    @Test
    fun testInitialState() = runTest {
        viewModel.state.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun testDefaultAmountText() = runTest {
        viewModel.amount.test {
            assertEquals(viewModel.defaultAmount, awaitItem())
        }
    }

    @Test
    fun testUpdateAmount() = runTest {
        viewModel.amount.test {
            viewModel.updateAmount("150")
            skipItems(1)
            assertEquals("150", awaitItem())
        }
    }

    @Test
    fun testMinimumAmountErrorState() = runTest {
        viewModel.state.test {
            viewModel.updateAmount((FiatViewModel.MIN_FIAT_AMOUNT - 1).toString())
            skipItems(1)
            assert(awaitItem() is FiatSceneState.Error)
        }
    }

    @Test
    fun testValidAmountSetsLoadingState() = runTest {
        viewModel.state.test {
            viewModel.updateAmount("100")
            skipItems(1)
            assert(awaitItem() is FiatSceneState.Loading)
        }
    }
}