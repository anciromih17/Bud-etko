package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.data.repository.UserRepository.Companion.DEMO_USER_ID

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    val uiState: StateFlow<UserUiState> = userRepository.observeUser(DEMO_USER_ID)
        .map { user ->
            UserUiState(
                username = user?.username ?: "Ana",
                email = user?.email ?: "ana@budzetko.local"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserUiState()
        )

    init {
        viewModelScope.launch {
            userRepository.ensureDemoUser()
        }
    }

    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
