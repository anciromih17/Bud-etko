package si.um.feri.budzetko.data.repository

import si.um.feri.budzetko.data.dao.UserDao
import si.um.feri.budzetko.data.entity.UserEntity

class UserRepository(
    private val userDao: UserDao
) {
    suspend fun ensureDemoUser() {
        if (userDao.getUser(DEMO_USER_ID) == null) {
            userDao.upsertUser(
                UserEntity(
                    userId = DEMO_USER_ID,
                    email = "ana@budzetko.local",
                    username = "Ana"
                )
            )
        }
    }

    companion object {
        const val DEMO_USER_ID = "demo-user"
    }
}
