package com.estimalaces.app.presentation.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.sale.SaleViewModel

@Composable
fun ClientScreen(viewModel: SaleViewModel) {
    val clients by viewModel.clients.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Clientes fiéis", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SectionCard("Compras por cliente") {
            if (clients.isEmpty()) Text("Os clientes aparecem aqui depois da primeira venda.")
            clients.forEach { client ->
                val missing = 3 - (client.purchaseCount % 3)
                val message = if (client.purchaseCount > 0 && client.purchaseCount % 3 == 0) {
                    "Brinde liberado!"
                } else {
                    "${3 - missing} de 3 compras para brinde"
                }
                Text("${client.name}: ${client.purchaseCount} compras - $message")
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}
