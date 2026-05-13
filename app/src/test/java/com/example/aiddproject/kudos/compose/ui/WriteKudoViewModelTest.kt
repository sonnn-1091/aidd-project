package com.example.aiddproject.kudos.compose.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.data.DemoKudosRepository
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SunnerNode
import com.example.aiddproject.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * State-machine unit tests for [WriteKudoViewModel] (T040).
 *
 * Uses the real [DemoKudosRepository] as the under-test repo since it's
 * deterministic AND already plumbs the partial-failure hook the Phase 7
 * test will need. AuthRepository is a hand-rolled fake that returns a
 * configurable currentUserId.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WriteKudoViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repo: DemoKudosRepository
    private lateinit var auth: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = DemoKudosRepository()
        auth = FakeAuthRepository(currentUserId = "u-self")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newVm(prefilledRecipientId: String? = null): WriteKudoViewModel {
        val savedState =
            SavedStateHandle().apply {
                if (prefilledRecipientId != null) set(Routes.WRITE_KUDO_ARG_RECIPIENT, prefilledRecipientId)
            }
        return WriteKudoViewModel(savedState, repo as KudosRepository, auth)
    }

    // ── Prefill (US1 Sc4) ───────────────────────────────────────────

    @Test
    fun prefill_doesNotDirtyForm() =
        testScope.runTest {
            val vm = newVm(prefilledRecipientId = "u-other")
            assertEquals("u-other", vm.state.value.recipientId)
            assertFalse(vm.state.value.formDirty)
        }

    @Test
    fun noPrefill_recipientStartsNull() =
        testScope.runTest {
            val vm = newVm()
            assertNull(vm.state.value.recipientId)
            assertFalse(vm.state.value.formDirty)
        }

    // ── Field-update intents (T036) ─────────────────────────────────

    @Test
    fun onTitleChange_dirtiesForm_andClearsTitleError() =
        testScope.runTest {
            val vm = newVm()
            vm.revealErrors() // populate fieldErrors
            assertNotNull(vm.state.value.fieldErrors.title)
            vm.onTitleChange("My title")
            assertEquals("My title", vm.state.value.title)
            assertTrue(vm.state.value.formDirty)
            assertNull(vm.state.value.fieldErrors.title)
        }

    @Test
    fun onTitleChange_capsAtMaxLength() =
        testScope.runTest {
            val vm = newVm()
            vm.onTitleChange("A".repeat(150))
            assertEquals(100, vm.state.value.title.length)
        }

    @Test
    fun onRecipientChosen_setsIdNameAndClearsError() =
        testScope.runTest {
            val vm = newVm()
            vm.revealErrors()
            vm.onRecipientChosen(SunnerNode(id = "u-other", fullName = "Nguyễn Văn A"))
            assertEquals("u-other", vm.state.value.recipientId)
            assertEquals("Nguyễn Văn A", vm.state.value.recipientName)
            assertTrue(vm.state.value.formDirty)
            assertNull(vm.state.value.fieldErrors.recipient)
        }

    // ── isSubmitEnabled derived state ───────────────────────────────

    @Test
    fun isSubmitEnabled_falseWhenAnyFieldMissing() =
        testScope.runTest {
            val vm = newVm()
            assertFalse(vm.state.value.isSubmitEnabled)
            vm.onRecipientChosen(SunnerNode(id = "u-other", fullName = "X"))
            vm.onTitleChange("T")
            vm.onMessageChange(RichTextValue.ofPlainText("M"))
            assertFalse(vm.state.value.isSubmitEnabled) // hashtag missing
            vm.onHashtagAdd(Hashtag(id = "h1", tagName = "teamwork"))
            assertTrue(vm.state.value.isSubmitEnabled)
            vm.onHashtagRemove("h1")
            assertFalse(vm.state.value.isSubmitEnabled)
        }

    @Test
    fun isSubmitEnabled_falseWhenMessageOver1000() =
        testScope.runTest {
            val vm = newVm()
            vm.onRecipientChosen(SunnerNode(id = "u-other", fullName = "X"))
            vm.onTitleChange("T")
            vm.onHashtagAdd(Hashtag(id = "h1", tagName = "teamwork"))
            vm.onMessageChange(RichTextValue.ofPlainText("A".repeat(1001)))
            assertFalse(vm.state.value.isSubmitEnabled)
        }

    // ── onSendTap (US1 happy path + US3 reveal-errors) ──────────────

    @Test
    fun onSendTap_invalidForm_revealsAllFieldErrors() =
        testScope.runTest {
            val vm = newVm()
            vm.onSendTap()
            advanceUntilIdle()
            val errors = vm.state.value.fieldErrors
            assertEquals(R.string.write_kudo_error_recipient_required, errors.recipient)
            assertEquals(R.string.write_kudo_error_title_required, errors.title)
            assertEquals(R.string.write_kudo_error_message_required, errors.message)
            assertEquals(R.string.write_kudo_error_hashtags_required, errors.hashtags)
            assertFalse(vm.state.value.isSending)
        }

    @Test
    fun onSendTap_validForm_emitsSubmittedEvent() =
        testScope.runTest {
            val vm = newVm()
            vm.onRecipientChosen(SunnerNode(id = "u-other", fullName = "X"))
            vm.onTitleChange("T")
            vm.onMessageChange(RichTextValue.ofPlainText("M"))
            vm.onHashtagAdd(Hashtag(id = "h1", tagName = "teamwork"))

            vm.events.test {
                vm.onSendTap()
                advanceUntilIdle()
                assertEquals(WriteKudoEvent.Submitted, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertFalse(vm.state.value.isSending)
        }

    // ── Cancel (US2 — also tested in Phase 4 Compose UI) ────────────

    @Test
    fun onCancelTap_cleanForm_emitsNavigateBack() =
        testScope.runTest {
            val vm = newVm()
            vm.events.test {
                vm.onCancelTap()
                assertEquals(WriteKudoEvent.NavigateBack, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onCancelTap_dirtyForm_setsConfirmDialog() =
        testScope.runTest {
            val vm = newVm()
            vm.onTitleChange("X")
            vm.onCancelTap()
            assertEquals(ConfirmDialogState.UnsavedChanges, vm.state.value.confirmDialog)
        }

    @Test
    fun onConfirmDiscard_clearsDialogAndNavigatesBack() =
        testScope.runTest {
            val vm = newVm()
            vm.onTitleChange("X")
            vm.onCancelTap()
            vm.events.test {
                vm.onConfirmDiscard()
                assertEquals(WriteKudoEvent.NavigateBack, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(vm.state.value.confirmDialog)
        }

    @Test
    fun onDismissConfirmDialog_clearsDialog() =
        testScope.runTest {
            val vm = newVm()
            vm.onTitleChange("X")
            vm.onCancelTap()
            vm.onDismissConfirmDialog()
            assertNull(vm.state.value.confirmDialog)
        }

    // ── Hashtag picker (US1 / T039) ─────────────────────────────────

    @Test
    fun onHashtagAdd_atMaxLimit_setsHashtagsMaxError() =
        testScope.runTest {
            val vm = newVm()
            repeat(5) { i ->
                vm.onHashtagAdd(Hashtag(id = "h$i", tagName = "tag$i"))
            }
            vm.onHashtagAdd(Hashtag(id = "h5", tagName = "overflow"))
            assertEquals(R.string.write_kudo_error_hashtags_max, vm.state.value.fieldErrors.hashtags)
            assertEquals(5, vm.state.value.tags.size)
        }

    @Test
    fun onHashtagAdd_duplicate_isIgnored() =
        testScope.runTest {
            val vm = newVm()
            vm.onHashtagAdd(Hashtag(id = "h1", tagName = "teamwork"))
            vm.onHashtagAdd(Hashtag(id = "h1", tagName = "teamwork"))
            assertEquals(1, vm.state.value.tags.size)
        }

    // ── Fakes ───────────────────────────────────────────────────────

    private class FakeAuthRepository(private val currentUserId: String?) : AuthRepository {
        override suspend fun signInWithIdToken(token: String): Result<Unit> = Result.success(Unit)

        override suspend fun signOut() = Unit

        override fun currentUserId(): String? = currentUserId
    }

    @Suppress("unused")
    private val _depRef: Department? = null // pin Department symbol if needed by future tests
}
