package com.estimalaces.app.presentation.report

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estimalaces.app.domain.model.ReportSummary
import com.estimalaces.app.export.XlsxExportService
import com.estimalaces.app.presentation.PrimaryAction
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.asMoney

@Composable
fun ReportScreen(viewModel: ReportViewModel, context: Context) {
    val filter by viewModel.filter.collectAsState()
    val report by viewModel.report.collectAsState(initial = ReportSummary())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Relatórios", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SectionCard("Período") {
            ReportFilter.entries.forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { viewModel.setFilter(item) },
                    label = {
                        Text(
                            when (item) {
                                ReportFilter.TODAY -> "Hoje"
                                ReportFilter.WEEK -> "Semana"
                                ReportFilter.MONTH -> "Mês"
                            }
                        )
                    }
                )
            }
        }
        SectionCard("Resumo") {
            Text("Quantidade vendida: ${report.quantitySold}")
            Text("Total vendido: ${report.totalSold.asMoney()}")
            Text("Total gasto: ${report.totalSpent.asMoney()}")
            Text("Lucro: ${report.profit.asMoney()}", fontWeight = FontWeight.Bold)
            Text("Melhor produto: ${report.bestProduct}")
            Text("Clientes recorrentes: ${report.recurringClients}")
            Text("Brindes liberados: ${report.giftsReleased}")
            Text(if (report.goalReached) "Meta atingida" else "Meta ainda não atingida")
        }
        PrimaryAction("EXPORTAR PLANILHA") {
            viewModel.export { rows ->
                XlsxExportService(context).shareSales(rows)
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}
