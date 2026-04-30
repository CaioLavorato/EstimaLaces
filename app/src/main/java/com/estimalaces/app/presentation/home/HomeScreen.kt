package com.estimalaces.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.asMoney

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val summary by viewModel.summary.collectAsState(
        initial = com.estimalaces.app.domain.model.DashboardSummary()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("EstimaLaces", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Seu lucro começa no preço certo.", style = MaterialTheme.typography.titleMedium)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Lucro do mês", color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    summary.profit.asMoney(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallMetric("Vendido", summary.totalSold.asMoney(), Modifier.weight(1f))
            SmallMetric("Gasto", summary.totalSpent.asMoney(), Modifier.weight(1f))
        }

        SectionCard("Meta mensal") {
            Text("Meta: ${summary.monthlyGoal.asMoney()}")
            if (summary.isGoalReached) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null)
                    Text(
                        "Meta batida! Pode liberar brinde de até ${summary.maxGiftAfterGoal.asMoney()}",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text("Falta ${summary.remainingToGoal.asMoney()} para bater a meta")
            }
        }

        if (summary.lowStockMessages.isNotEmpty()) {
            SectionCard("Estoque baixo") {
                summary.lowStockMessages.take(5).forEach { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionCard("Atalhos") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrendingUp, contentDescription = null)
                Text("Cadastre produtos, registre vendas e exporte sua planilha quando quiser.")
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
