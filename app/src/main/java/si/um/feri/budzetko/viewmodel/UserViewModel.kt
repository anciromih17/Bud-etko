package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.UserEntity
import si.um.feri.budzetko.data.repository.AuthRepository
import si.um.feri.budzetko.data.repository.UserRepository

class UserViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository = AuthRepository()
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

    private val _profileEditState = MutableStateFlow(ProfileEditState())
    val profileEditState: StateFlow<ProfileEditState> = _profileEditState.asStateFlow()

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

    fun clearProfileEditMessage() {
        _profileEditState.value = ProfileEditState()
    }

    fun updateProfile(
        username: String,
        email: String,
        currentPassword: String,
        newPassword: String
    ) {
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()
        val cleanPassword = currentPassword.trim()
        val cleanNewPassword = newPassword.trim()

        when {
            cleanUsername.isBlank() -> {
                _profileEditState.value = ProfileEditState(errorMessage = "Uporabniško ime ne sme biti prazno.")
                return
            }
            cleanEmail.isBlank() -> {
                _profileEditState.value = ProfileEditState(errorMessage = "E-pošta ne sme biti prazna.")
                return
            }
            cleanPassword.isBlank() -> {
                _profileEditState.value = ProfileEditState(errorMessage = "Za spremembo profila vnesi trenutno geslo.")
                return
            }
        }

        viewModelScope.launch {
            _profileEditState.value = ProfileEditState(isSaving = true)

            val reauthResult = authRepository.reauthenticateCurrentUser(cleanPassword)
            if (reauthResult.isFailure) {
                _profileEditState.value = ProfileEditState(
                    errorMessage = reauthResult.exceptionOrNull()?.message ?: "Preverjanje gesla ni uspelo."
                )
                return@launch
            }

            if (cleanEmail != uiState.value.email) {
                val emailResult = authRepository.updateCurrentUserEmail(cleanEmail)
                if (emailResult.isFailure) {
                    _profileEditState.value = ProfileEditState(
                        errorMessage = emailResult.exceptionOrNull()?.message ?: "E-pošte ni bilo mogoče posodobiti."
                    )
                    return@launch
                }
            }

            if (cleanNewPassword.isNotBlank()) {
                val passwordResult = authRepository.updateCurrentUserPassword(cleanNewPassword)
                if (passwordResult.isFailure) {
                    _profileEditState.value = ProfileEditState(
                        errorMessage = passwordResult.exceptionOrNull()?.message ?: "Gesla ni bilo mogoče posodobiti."
                    )
                    return@launch
                }
            }

            userRepository.upsertFirebaseUser(
                UserEntity(
                    userId = currentUserId,
                    email = cleanEmail,
                    username = cleanUsername
                )
            )

            _profileEditState.value = ProfileEditState(successMessage = "Profil je posodobljen.")
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val authRepository: AuthRepository = AuthRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(userRepository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
