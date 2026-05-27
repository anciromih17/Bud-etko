package si.um.feri.budzetko.domain.ai

import si.um.feri.budzetko.AppCurrency
import si.um.feri.budzetko.viewmodel.DashboardUiState

class AiRecommendationService(
    private val geminiClient: GeminiAiRecommendationClient,
    private val currency: AppCurrency
) {
    suspend fun generate(uiState: DashboardUiState): AiRecommendationResult {
        val geminiSummary = geminiClient.generate(uiState, currency)
        return if (geminiSummary != null && geminiSummary.isCompleteRecommendation()) {
            AiRecommendationResult(
                summary = geminiSummary,
                source = AiRecommendationSource.GEMINI
            )
        } else {
            AiRecommendationResult(
                summary = LocalAiRecommendationEngine.generate(uiState, currency),
                source = AiRecommendationSource.FALLBACK
            )
        }
    }
}

data class AiRecommendationResult(
    val summary: String,
    val source: AiRecommendationSource
)

enum class AiRecommendationSource {
    GEMINI,
    FALLBACK
}

private fun String.isCompleteRecommendation(): Boolean {
    val cleanLines = lines().filter { it.isNotBlank() }
    val text = trim()
    return cleanLines.size >= 3 ||
        ("Povzetek:" in text && "Opozorilo:" in text && "Priporočilo:" in text) ||
        ("povzetek" in text.lowercase() && "priporoč" in text.lowercase())
}
