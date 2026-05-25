package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.repository.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser =
        MutableStateFlow<FirebaseUser?>(auth.currentUser)

    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            val result = repository.login(
                email = email.trim(),
                password = password
            )

            result.onFailure {
                _error.value =
                    it.message ?: "Prijava ni uspela."
            }

            _currentUser.value =
                repository.getCurrentUser()

            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            val result = repository.register(
                email = email.trim(),
                password = password
            )

            result.onFailure {
                _error.value =
                    it.message ?: "Registracija ni uspela."
            }

            _currentUser.value =
                repository.getCurrentUser()

            _isLoading.value = false
        }
    }

    fun logout() {

        repository.logout()

        _currentUser.value = null
    }

    override fun onCleared() {

        super.onCleared()

        auth.removeAuthStateListener(authListener)
    }
}