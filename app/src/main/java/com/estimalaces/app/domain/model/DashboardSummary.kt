package com.estimalaces.app.domain.model

data class DashboardSummary(
    val totalSold: Double = 0.0,
    val totalSpent: Double = 0.0,
    val profit: Double = 0.0,
    val monthlyGoal: Double = 0.0,
    val maxGiftAfterGoal: Double = 0.0,
    val lowStockMessages: List<String> = emptyList()
) {
    val remainingToGoal: Double get() = (monthlyGoal - totalSold).coerceAtLeast(0.0)
    val isGoalReached: Boolean get() = monthlyGoal > 0.0 && totalSold >= monthlyGoal
}
