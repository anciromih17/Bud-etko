package si.um.feri.budzetko.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.repository.LocalSyncSummary
import si.um.feri.budzetko.data.repository.SyncReport
import si.um.feri.budzetko.data.repository.SyncRepository

class SyncViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState
    private val startupSyncedUserIds = mutableSetOf<String>()

    fun refreshLocalSyncStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = SyncUiState(
                status = SyncStatusUi.ERROR,
                messageResId = R.string.sync_error_not_logged_in
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                syncRepository.getLocalSyncSummary(userId)
            }.onSuccess { summary ->
                _uiState.value = SyncUiState(
                    status = if (summary.hasIssues) SyncStatusUi.WARNING else SyncStatusUi.SUCCESS,
                    messageResId = summary.messageResId(),
                    messageArgs = summary.messageArgs()
                )
            }.onFailure { error ->
                _uiState.value = SyncUiState(
                    status = SyncStatusUi.ERROR,
                    messageResId = R.string.sync_error_status_check_failed
                )
            }
        }
    }

    fun syncNow() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = SyncUiState(
                status = SyncStatusUi.ERROR,
                messageResId = R.string.sync_error_not_logged_in
            )
            return
        }

        syncUserData(
            userId = userId,
            loadingMessageResId = R.string.sync_loading
        )
    }

    fun syncOnStartup(userId: String) {
        if (!startupSyncedUserIds.add(userId)) return

        syncUserData(
            userId = userId,
            loadingMessageResId = R.string.sync_checking_local
        )
    }

    private fun syncUserData(userId: String, @StringRes loadingMessageResId: Int) {
        viewModelScope.launch {
            _uiState.value = SyncUiState(
                status = SyncStatusUi.SYNCING,
                messageResId = loadingMessageResId
            )

            syncRepository.pushAllLocalData(userId)
                .onSuccess { report ->
                    _uiState.value = SyncUiState(
                        status = SyncStatusUi.SUCCESS,
                        messageResId = report.messageResId(),
                        messageArgs = report.messageArgs()
                    )
                }
                .onFailure { error ->
                    _uiState.value = SyncUiState(
                        status = SyncStatusUi.ERROR,
                        messageResId = R.string.sync_error_failed
                    )
                }
        }
    }

    class Factory(
        private val syncRepository: SyncRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
                return SyncViewModel(syncRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SyncUiState(
    val status: SyncStatusUi = SyncStatusUi.IDLE,
    @StringRes val messageResId: Int = R.string.sync_ready,
    val messageArgs: List<Any> = emptyList()
)

enum class SyncStatusUi {
    IDLE,
    SYNCING,
    WARNING,
    SUCCESS,
    ERROR
}

@StringRes
private fun LocalSyncSummary.messageResId(): Int {
    return when {
        failedTotal > 0 -> R.string.sync_local_failed_pending
        pendingTotal > 0 -> R.string.sync_local_pending
        else -> R.string.sync_no_local_changes
    }
}

private fun LocalSyncSummary.messageArgs(): List<Any> {
    return when {
        failedTotal > 0 -> listOf(failedTotal, pendingTotal)
        pendingTotal > 0 -> listOf(pendingTotal)
        else -> emptyList()
    }
}

@StringRes
private fun SyncReport.messageResId(): Int {
    val failedTotal = failedCategories + failedExpenses + failedBudgets + failedAiSummaries
    val uploadedTotal = uploadedCategories + uploadedExpenses + uploadedBudgets + uploadedAiSummaries
    return when {
        failedTotal > 0 -> R.string.sync_report_partial
        uploadedTotal > 0 -> R.string.sync_report_uploaded
        else -> R.string.sync_report_checked_no_changes
    }
}

private fun SyncReport.messageArgs(): List<Any> {
    val failedTotal = failedCategories + failedExpenses + failedBudgets + failedAiSummaries
    val uploadedTotal = uploadedCategories + uploadedExpenses + uploadedBudgets + uploadedAiSummaries
    return when {
        failedTotal > 0 -> listOf(failedCategories, failedExpenses, failedBudgets, failedAiSummaries)
        uploadedTotal > 0 -> listOf(uploadedCategories, uploadedExpenses, uploadedBudgets, uploadedAiSummaries)
        else -> emptyList()
    }
}
