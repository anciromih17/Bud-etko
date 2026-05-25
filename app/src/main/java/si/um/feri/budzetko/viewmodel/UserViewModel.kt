package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.UserEntity
import si.um.feri.budzetko.data.repository.UserRepository

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    private val currentUserId: String = firebaseUser?.uid ?: "unknown-user"
    private val currentEmail: String = firebaseUser?.email ?: "uporabnik@email.com"
    private val currentUsername: String = currentEmail.substringBefore("@")

    val uiState: StateFlow<UserUiState> = userRepository.observeUser(currentUserId)
        .map { user ->
            UserUiState(
                username = user?.username ?: currentUsername,
                email = user?.email ?: currentEmail
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserUiState(
                username = currentUsername,
                email = currentEmail
            )
        )

    init {
        viewModelScope.launch {
            userRepository.upsertFirebaseUser(
                UserEntity(
                    userId = currentUserId,
                    email = currentEmail,
                    username = currentUsername
                )
            )
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