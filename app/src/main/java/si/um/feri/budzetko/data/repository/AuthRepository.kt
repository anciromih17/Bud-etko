package si.um.feri.budzetko.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
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

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("Uporabnik ni prijavljen."))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reauthenticateCurrentUser(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("Uporabnik ni prijavljen."))
            val email = user.email ?: return Result.failure(IllegalStateException("Email uporabnika ni na voljo."))
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurrentUserEmail(email: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("Uporabnik ni prijavljen."))
            user.updateEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurrentUserPassword(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("Uporabnik ni prijavljen."))
            user.updatePassword(password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
