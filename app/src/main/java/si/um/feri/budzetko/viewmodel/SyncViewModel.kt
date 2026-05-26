package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
                message = "Uporabnik ni prijavljen."
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                syncRepository.getLocalSyncSummary(userId)
            }.onSuccess { summary ->
                _uiState.value = SyncUiState(
                    status = if (summary.hasIssues) SyncStatusUi.WARNING else SyncStatusUi.SUCCESS,
                    message = summary.toUserMessage()
                )
            }.onFailure { error ->
                _uiState.value = SyncUiState(
                    status = SyncStatusUi.ERROR,
                    message = error.message ?: "Stanja sinhronizacije ni bilo mogoče preveriti."
                )
            }
        }
    }

    fun syncNow() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = SyncUiState(
                status = SyncStatusUi.ERROR,
                message = "Uporabnik ni prijavljen."
            )
            return
        }

        syncUserData(
            userId = userId,
            loadingMessage = "Sinhroniziram podatke..."
        )
    }

    fun syncOnStartup(userId: String) {
        if (!startupSyncedUserIds.add(userId)) return

        syncUserData(
            userId = userId,
            loadingMessage = "Preverjam lokalne podatke za Firestore sync..."
        )
    }

    private fun syncUserData(userId: String, loadingMessage: String) {
        viewModelScope.launch {
            _uiState.value = SyncUiState(
                status = SyncStatusUi.SYNCING,
                message = loadingMessage
            )

            syncRepository.pushAllLocalData(userId)
                .onSuccess { report ->
                    _uiState.value = SyncUiState(
                        status = SyncStatusUi.SUCCESS,
                        message = report.toUserMessage()
                    )
                }
                .onFailure { error ->
                    _uiState.value = SyncUiState(
                        status = SyncStatusUi.ERROR,
                        message = error.message ?: "Sinhronizacija ni uspela."
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
    val message: String = "Lokalna baza je pripravljena za Firestore sync."
)

enum class SyncStatusUi {
    IDLE,
    SYNCING,
    WARNING,
    SUCCESS,
    ERROR
}
