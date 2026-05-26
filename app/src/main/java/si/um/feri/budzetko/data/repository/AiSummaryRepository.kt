package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.AiSummaryDao
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.entity.SyncStatus

class AiSummaryRepository(
    private val aiSummaryDao: AiSummaryDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    fun observeSummary(userId: String, month: Int, year: Int): Flow<AiSummaryEntity?> {
        return aiSummaryDao.observeSummary(userId, month, year)
    }

    fun observeSummaries(userId: String): Flow<List<AiSummaryEntity>> {
        return aiSummaryDao.observeSummaries(userId)
    }

    suspend fun saveSummary(
        userId: String,
        month: Int,
        year: Int,
        summary: String,
        source: AiSummarySource = AiSummarySource.FALLBACK
    ) {
        val existingSummary = aiSummaryDao.getSummary(userId, month, year)
        val savedSummary = if (existingSummary == null) {
            val newSummary = AiSummaryEntity(
                month = month,
                year = year,
                summary = summary,
                source = source,
                syncStatus = SyncStatus.PENDING,
                userId = userId
            )
            val localId = aiSummaryDao.insertSummary(newSummary)
            newSummary.copy(id = localId)
        } else {
            existingSummary.copy(
                summary = summary,
                source = source,
                syncStatus = SyncStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
                .also { aiSummaryDao.updateSummary(it) }
        }

        runCatching {
            firestoreRepository.saveAiSummary(savedSummary)
        }.onSuccess {
            aiSummaryDao.updateSummary(savedSummary.copy(syncStatus = SyncStatus.SYNCED))
        }.onFailure {
            aiSummaryDao.updateSummary(savedSummary.copy(syncStatus = SyncStatus.FAILED))
        }
    }
}
