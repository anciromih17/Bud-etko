package si.um.feri.budzetko.domain.ai

import si.um.feri.budzetko.viewmodel.DashboardCategorySpending
import si.um.feri.budzetko.viewmodel.DashboardUiState

object LocalAiRecommendationEngine {
    fun generate(uiState: DashboardUiState): String {
        if (uiState.totalBudget <= 0.0 && uiState.totalSpent <= 0.0) {
            return "Za priporočilo najprej nastavi mesečni proračun in dodaj nekaj stroškov.\nKo bo na voljo več podatkov, bom lahko povzela porabo po kategorijah."
        }

        val lines = mutableListOf<String>()
        val budgetUsage = if (uiState.totalBudget > 0.0) {
            ((uiState.totalSpent / uiState.totalBudget) * 100.0).coerceAtLeast(0.0)
        } else {
            0.0
        }
        lines += if (uiState.totalBudget > 0.0) {
            "Ta mesec si porabila ${uiState.totalSpent.formatMoney()}€, kar je ${budgetUsage.toInt()}% mesečnega proračuna."
        } else {
            "Ta mesec si porabila ${uiState.totalSpent.formatMoney()}€. Mesečni proračun še ni nastavljen, zato primerjava z limitom ni mogoča."
        }

        uiState.categorySpending
            .filter { it.spentAmount > 0.0 }
            .maxByOrNull { it.spentAmount }
            ?.let { topCategory ->
                lines += "Največ porabe je v kategoriji ${topCategory.categoryName}: ${topCategory.spentAmount.formatMoney()}€."
            }

        val overLimit = uiState.categorySpending.filter { it.hasLimit && it.progress >= 1f }
        val nearLimit = uiState.categorySpending.filter { it.hasLimit && it.progress >= 0.8f && it.progress < 1f }

        if (overLimit.isNotEmpty()) {
            val category = overLimit.maxBy { it.progress }
            val overAmount = category.spentAmount - (category.limitAmount ?: 0.0)
            lines += "Kategorija ${category.categoryName} je presegla limit za ${overAmount.formatMoney()}€. Predlagam, da preveriš ta limit ali začasno zmanjšaš porabo v tej kategoriji."
        } else if (nearLimit.isNotEmpty()) {
            val category = nearLimit.maxBy { it.progress }
            val remaining = ((category.limitAmount ?: 0.0) - category.spentAmount).coerceAtLeast(0.0)
            lines += "Kategorija ${category.categoryName} je blizu limita. Do limita imaš še ${remaining.formatMoney()}€, zato jo je smiselno spremljati."
        } else if (uiState.totalBudget > 0.0) {
            lines += "Trenutno ni preseženih limitov. Poraba je za zdaj pod nadzorom."
        }

        val categoriesWithoutLimit = uiState.categorySpending.filter { it.limitAmount == null && it.spentAmount > 0.0 }
        if (categoriesWithoutLimit.isNotEmpty()) {
            lines += "Nekatere kategorije imajo porabo brez nastavljenega limita. Za bolj natančna opozorila jim dodaj mesečni limit."
        }

        return lines.joinToString(separator = "\n")
    }
}

private val DashboardCategorySpending.hasLimit: Boolean
    get() = limitAmount != null && limitAmount > 0.0

private fun Double.formatMoney(): String = "%.2f".format(this)
