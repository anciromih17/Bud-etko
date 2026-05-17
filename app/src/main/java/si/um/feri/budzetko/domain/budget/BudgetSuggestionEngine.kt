package si.um.feri.budzetko.domain.budget

import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.viewmodel.BudgetLimitDraft

object BudgetSuggestionEngine {
    fun suggestLimits(
        categories: List<CategoryEntity>,
        income: Double
    ): List<BudgetLimitDraft> {
        if (categories.isEmpty()) return emptyList()

        val weights = categories.associateWith { budgetRoleWeight(it.budgetRole, categories) }
        val weightSum = weights.values.sum().takeIf { it > 0.0 } ?: categories.size.toDouble()
        val percentByCategoryId = allocatePercentagesByLargestRemainder(categories, weights, weightSum)
        return categories.map { category ->
            val percent = percentByCategoryId.getValue(category.id)
            BudgetLimitDraft(
                category = category,
                percent = percent,
                limitAmount = income * percent / 100.0
            )
        }
    }

    fun redistributeAfterManualChange(
        drafts: List<BudgetLimitDraft>,
        baselinePercents: Map<Long, Int>,
        changedCategoryId: Long,
        changedPercent: Int,
        income: Double
    ): List<BudgetLimitDraft> {
        if (drafts.isEmpty()) return drafts
        if (drafts.size == 1) {
            return drafts.map {
                it.copy(
                    percent = 100,
                    limitAmount = income,
                    isEditing = it.category.id == changedCategoryId && it.isEditing
                )
            }
        }

        val changedDraft = drafts.firstOrNull { it.category.id == changedCategoryId } ?: return drafts
        val otherDrafts = drafts.filterNot { it.category.id == changedCategoryId }
        val remainingPercent = 100 - changedPercent
        val otherTotal = otherDrafts.sumOf { baselinePercents[it.category.id] ?: it.percent }

        val redistributedOthers = if (remainingPercent == 0) {
            otherDrafts.map { it.copy(percent = 0, limitAmount = 0.0) }
        } else if (otherTotal == 0) {
            distributeEvenly(otherDrafts, remainingPercent, income)
        } else {
            distributeProportionally(otherDrafts, baselinePercents, otherTotal, remainingPercent, income)
        }

        val changed = changedDraft.copy(
            percent = changedPercent,
            limitAmount = income * changedPercent / 100.0
        )
        val byCategoryId = (redistributedOthers + changed).associateBy { it.category.id }
        return drafts.map { byCategoryId.getValue(it.category.id) }
    }

    fun redistributeAfterLimitAmountChange(
        drafts: List<BudgetLimitDraft>,
        baselinePercents: Map<Long, Int>,
        changedCategoryId: Long,
        changedLimitAmount: Double,
        income: Double
    ): List<BudgetLimitDraft> {
        val changedPercent = ((changedLimitAmount / income) * 100.0).toInt().coerceIn(0, 100)
        return redistributeAfterManualChange(
            drafts = drafts,
            baselinePercents = baselinePercents,
            changedCategoryId = changedCategoryId,
            changedPercent = changedPercent,
            income = income
        )
    }

    fun redistributeAfterExactLimitAmountChange(
        drafts: List<BudgetLimitDraft>,
        changedCategoryId: Long,
        changedLimitAmount: Double,
        income: Double
    ): List<BudgetLimitDraft> {
        if (drafts.isEmpty()) return drafts
        if (drafts.size == 1) {
            val exactLimit = changedLimitAmount.coerceIn(0.0, income)
            return drafts.map {
                it.copy(
                    percent = percentOf(exactLimit, income),
                    limitAmount = exactLimit
                )
            }
        }

        val changedDraft = drafts.firstOrNull { it.category.id == changedCategoryId } ?: return drafts
        val otherDrafts = drafts.filterNot { it.category.id == changedCategoryId }
        val exactChangedLimit = changedLimitAmount.coerceIn(0.0, income)
        val remainingAmount = income - exactChangedLimit
        val otherTotal = otherDrafts.sumOf { it.limitAmount }

        val redistributedOthers = when {
            remainingAmount <= 0.0 -> otherDrafts.map {
                it.copy(percent = 0, limitAmount = 0.0)
            }

            otherTotal <= 0.0 -> distributeAmountEvenly(otherDrafts, remainingAmount, income)

            else -> distributeAmountProportionally(otherDrafts, otherTotal, remainingAmount, income)
        }

        val changed = changedDraft.copy(
            percent = percentOf(exactChangedLimit, income),
            limitAmount = exactChangedLimit
        )
        val byCategoryId = (redistributedOthers + changed).associateBy { it.category.id }
        return drafts.map { byCategoryId.getValue(it.category.id) }
    }

    fun redistributeAfterCategoryRemoval(
        remainingDrafts: List<BudgetLimitDraft>,
        income: Double
    ): List<BudgetLimitDraft> {
        if (remainingDrafts.isEmpty()) return emptyList()
        if (income <= 0.0) {
            return remainingDrafts.map {
                it.copy(percent = 0, limitAmount = 0.0)
            }
        }

        val currentTotal = remainingDrafts.sumOf { it.limitAmount }
        return if (currentTotal <= 0.0) {
            distributeAmountEvenly(remainingDrafts, income, income)
        } else {
            distributeAmountProportionally(remainingDrafts, currentTotal, income, income)
        }
    }

    fun budgetRoleWeight(role: CategoryBudgetRole, allCategories: List<CategoryEntity>): Double {
        val presentRoles = allCategories.map { it.budgetRole }.toSet()
        val activeRuleWeight = presentRoles.sumOf { baseRoleWeight(it) }
        val normalizedRoleWeight = if (activeRuleWeight > 0.0) {
            baseRoleWeight(role) / activeRuleWeight * 100.0
        } else {
            100.0
        }
        val categoryCountInRole = allCategories.count { it.budgetRole == role }.coerceAtLeast(1)
        return normalizedRoleWeight / categoryCountInRole
    }

    private fun baseRoleWeight(role: CategoryBudgetRole): Double {
        return when (role) {
            CategoryBudgetRole.NEEDS -> 50.0
            CategoryBudgetRole.WANTS -> 30.0
            CategoryBudgetRole.SAVINGS -> 20.0
            CategoryBudgetRole.OTHER -> 10.0
        }
    }

    private fun distributeEvenly(
        drafts: List<BudgetLimitDraft>,
        totalPercent: Int,
        income: Double
    ): List<BudgetLimitDraft> {
        if (drafts.isEmpty()) return emptyList()
        val base = totalPercent / drafts.size
        var remainder = totalPercent % drafts.size
        return drafts.map { draft ->
            val percent = base + if (remainder > 0) 1 else 0
            if (remainder > 0) remainder -= 1
            draft.copy(
                percent = percent,
                limitAmount = income * percent / 100.0
            )
        }
    }

    private fun distributeProportionally(
        drafts: List<BudgetLimitDraft>,
        baselinePercents: Map<Long, Int>,
        oldTotalPercent: Int,
        newTotalPercent: Int,
        income: Double
    ): List<BudgetLimitDraft> {
        data class Share(
            val draft: BudgetLimitDraft,
            val basePercent: Int,
            val remainder: Double
        )

        val shares = drafts.map { draft ->
            val baselinePercent = baselinePercents[draft.category.id] ?: draft.percent
            val exactPercent = (baselinePercent.toDouble() / oldTotalPercent) * newTotalPercent
            Share(
                draft = draft,
                basePercent = exactPercent.toInt(),
                remainder = exactPercent - exactPercent.toInt()
            )
        }
        val remainderCount = newTotalPercent - shares.sumOf { it.basePercent }
        val idsReceivingRemainder = shares
            .sortedByDescending { it.remainder }
            .take(remainderCount.coerceAtLeast(0))
            .map { it.draft.category.id }
            .toSet()

        return shares.map { share ->
            val percent = share.basePercent + if (share.draft.category.id in idsReceivingRemainder) 1 else 0
            share.draft.copy(
                percent = percent,
                limitAmount = income * percent / 100.0
            )
        }
    }

    private fun distributeAmountEvenly(
        drafts: List<BudgetLimitDraft>,
        totalAmount: Double,
        income: Double
    ): List<BudgetLimitDraft> {
        if (drafts.isEmpty()) return emptyList()
        val amount = totalAmount / drafts.size
        return drafts.map {
            it.copy(
                percent = percentOf(amount, income),
                limitAmount = amount
            )
        }
    }

    private fun distributeAmountProportionally(
        drafts: List<BudgetLimitDraft>,
        oldTotalAmount: Double,
        newTotalAmount: Double,
        income: Double
    ): List<BudgetLimitDraft> {
        var usedAmount = 0.0
        return drafts.mapIndexed { index, draft ->
            val amount = if (index == drafts.lastIndex) {
                newTotalAmount - usedAmount
            } else {
                (draft.limitAmount / oldTotalAmount) * newTotalAmount
            }.coerceAtLeast(0.0)
            usedAmount += amount
            draft.copy(
                percent = percentOf(amount, income),
                limitAmount = amount
            )
        }
    }

    private fun percentOf(amount: Double, income: Double): Int {
        if (income <= 0.0) return 0
        return ((amount / income) * 100.0).toInt().coerceIn(0, 100)
    }

    private fun allocatePercentagesByLargestRemainder(
        categories: List<CategoryEntity>,
        weights: Map<CategoryEntity, Double>,
        weightSum: Double
    ): Map<Long, Int> {
        data class Share(
            val categoryId: Long,
            val basePercent: Int,
            val remainder: Double
        )

        val shares = categories.map { category ->
            val exactPercent = (weights.getValue(category) / weightSum) * 100.0
            Share(
                categoryId = category.id,
                basePercent = exactPercent.toInt(),
                remainder = exactPercent - exactPercent.toInt()
            )
        }
        val missingPercent = 100 - shares.sumOf { it.basePercent }
        val idsReceivingRemainder = shares
            .sortedByDescending { it.remainder }
            .take(missingPercent.coerceAtLeast(0))
            .map { it.categoryId }
            .toSet()

        return shares.associate { share ->
            share.categoryId to share.basePercent + if (share.categoryId in idsReceivingRemainder) 1 else 0
        }
    }
}
