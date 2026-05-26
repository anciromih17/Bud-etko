package si.um.feri.budzetko.domain.ai

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import si.um.feri.budzetko.viewmodel.DashboardUiState

class GeminiAiRecommendationClient(
    private val apiKey: String,
    private val modelName: String = "gemini-2.5-flash"
) {
    suspend fun generate(uiState: DashboardUiState): String? {
        if (apiKey.isBlank()) return null

        return withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(endpointUrl()).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15_000
                connection.readTimeout = 20_000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody(uiState).toString())
                }

                val responseCode = connection.responseCode
                val responseText = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }
                connection.disconnect()

                if (responseCode !in 200..299) return@runCatching null
                parseSummary(responseText)
            }.getOrNull()
        }
    }

    private fun endpointUrl(): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
    }

    private fun requestBody(uiState: DashboardUiState): JSONObject {
        return JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt(uiState)))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.35)
                    .put("maxOutputTokens", 900)
                    .put(
                        "thinkingConfig",
                        JSONObject().put("thinkingBudget", 0)
                    )
            )
    }

    private fun prompt(uiState: DashboardUiState): String {
        val categories = uiState.categorySpending.joinToString(separator = "\n") { category ->
            val limit = category.limitAmount?.let { "${it.formatMoney()}€" } ?: "brez limita"
            "- ${category.categoryName}: poraba ${category.spentAmount.formatMoney()}€, limit $limit"
        }

        return """
            Deluješ kot kratek finančni pomočnik za slovensko aplikacijo Budžetko.
            Vrni samo tri vrstice besedila, brez uvoda:
            Povzetek: kratek povzetek mesečne porabe.
            Opozorilo: kategorija z največjo porabo ali najpomembnejše opozorilo.
            Priporočilo: konkreten nasvet za uporabnico.
            Ton naj bo prijazen, konkreten in uporaben. Ne omenjaj, da si AI model.
            Ne uporabljaj markdown znakov, zvezdic, oštevilčenja ali razlage postopka.
            Vsaka vrstica naj bo dolga največ 20 besed.

            Mesec: ${uiState.month}/${uiState.year}
            Mesečni proračun: ${uiState.totalBudget.formatMoney()}€
            Skupna poraba: ${uiState.totalSpent.formatMoney()}€
            Preostanek: ${uiState.available.formatMoney()}€

            Poraba po kategorijah:
            $categories
        """.trimIndent()
    }

    private fun parseSummary(responseText: String): String? {
        val root = JSONObject(responseText)
        val candidates = root.optJSONArray("candidates") ?: return null
        val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        return parts.optJSONObject(0)?.optString("text")?.trim()?.takeIf { it.isNotBlank() }
    }
}

private fun Double.formatMoney(): String = "%.2f".format(this)
