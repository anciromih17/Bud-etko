package si.um.feri.budzetko.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun register(
        email: String,
        password: String
    ): Result<String> {
        return try {
            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<String> {
        return try {
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}