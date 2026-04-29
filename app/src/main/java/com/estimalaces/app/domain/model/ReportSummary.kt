package com.estimalaces.app.domain.model

data class ReportSummary(
    val quantitySold: Int = 0,
    val totalSold: Double = 0.0,
    val totalSpent: Double = 0.0,
    val profit: Double = 0.0,
    val bestProduct: String = "-",
    val recurringClients: Int = 0,
    val giftsReleased: Int = 0,
    val goalReached: Boolean = false
)
