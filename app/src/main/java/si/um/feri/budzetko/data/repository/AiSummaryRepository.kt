package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.AiSummaryDao
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.AiSummarySource

class AiSummaryRepository(
    private val aiSummaryDao: AiSummaryDao
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
        if (existingSummary == null) {
            aiSummaryDao.insertSummary(
                AiSummaryEntity(
                    month = month,
                    year = year,
                    summary = summary,
                    source = source,
                    userId = userId
                )
            )
        } else {
            aiSummaryDao.updateSummary(
                existingSummary.copy(
                    summary = summary,
                    source = source,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
