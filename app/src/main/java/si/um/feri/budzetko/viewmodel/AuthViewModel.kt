package si.um.feri.budzetko.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.UserEntity
import si.um.feri.budzetko.data.repository.AuthRepository
import si.um.feri.budzetko.data.repository.UserRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository? = null
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser =
        MutableStateFlow<FirebaseUser?>(auth.currentUser)

    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val authListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->

            _currentUser.value = firebaseAuth.currentUser
        }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (!validateEmail(cleanEmail) || !validatePassword(password)) return

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null
            _message.value = null

            val result = repository.login(
                email = cleanEmail,
                password = password
            )

            result.onSuccess {
                saveCurrentUser()
            }

            result.onFailure {
                _error.value =
                    it.message ?: "Prijava ni uspela."
            }

            _currentUser.value =
                repository.getCurrentUser()

            _isLoading.value = false
        }
    }

    fun register(email: String, username: String, password: String) {
        val cleanEmail = email.trim()
        val cleanUsername = username.trim()
        if (!validateEmail(cleanEmail) || !validateUsername(cleanUsername) || !validatePassword(password)) return

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null
            _message.value = null

            val result = repository.register(
                email = cleanEmail,
                password = password
            )

            result.onSuccess {
                saveCurrentUser(username = cleanUsername)
            }

            result.onFailure {
                _error.value =
                    it.message ?: "Registracija ni uspela."
            }

            _currentUser.value =
                repository.getCurrentUser()

            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        val cleanEmail = email.trim()
        if (!validateEmail(cleanEmail)) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null

            repository.resetPassword(cleanEmail)
                .onSuccess {
                    _message.value = "Povezava za ponastavitev gesla je poslana na email."
                }
                .onFailure {
                    _error.value = it.message ?: "Ponastavitev gesla ni uspela."
                }

            _isLoading.value = false
        }
    }

    fun logout() {

        repository.logout()

        _currentUser.value = null
    }

    fun deleteAccount(password: String) {
        if (password.isBlank()) {
            _error.value = "Za brisanje računa vnesi geslo."
            return
        }

        val userId = repository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Uporabnik ni prijavljen."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null

            repository.reauthenticateCurrentUser(password)
                .onFailure {
                    _error.value = it.message ?: "Ponovna prijava ni uspela."
                    _isLoading.value = false
                    return@launch
                }

            userRepository?.deleteAccountData(userId)

            repository.deleteCurrentUser()
                .onSuccess {
                    _message.value = "Račun je izbrisan."
                    _currentUser.value = null
                }
                .onFailure {
                    _error.value = it.message ?: "Brisanje računa ni uspelo. Poskusi se ponovno prijaviti."
                }

            _isLoading.value = false
        }
    }

    override fun onCleared() {

        super.onCleared()

        auth.removeAuthStateListener(authListener)
    }

    private suspend fun saveCurrentUser(username: String? = null) {
        val firebaseUser = repository.getCurrentUser() ?: return
        val email = firebaseUser.email.orEmpty()
        val user = UserEntity(
            userId = firebaseUser.uid,
            email = email,
            username = username?.takeIf { it.isNotBlank() } ?: email.substringBefore("@", "Uporabnik")
        )

        userRepository?.upsertFirebaseUser(user)
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            _error.value = "Vnesi email."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Email ni v pravilni obliki."
            return false
        }
        return true
    }

    private fun validateUsername(username: String): Boolean {
        if (username.isBlank()) {
            _error.value = "Vnesi uporabniško ime."
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.length < 6) {
            _error.value = "Geslo mora imeti vsaj 6 znakov."
            return false
        }
        return true
    }

    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(
                    userRepository = userRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
