package com.estimalaces.app.presentation.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estimalaces.app.data.entity.ProductEntity
import com.estimalaces.app.presentation.ChoiceChips
import com.estimalaces.app.presentation.MoneyField
import com.estimalaces.app.presentation.PrimaryAction
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.TextFieldLine
import com.estimalaces.app.presentation.asMoney
import com.estimalaces.app.presentation.toMoneyDouble

@Composable
fun SaleScreen(viewModel: SaleViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    val clients by viewModel.clients.collectAsState(initial = emptyList())
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var manualProduct by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var sale by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var giftApplied by remember { mutableStateOf(false) }
    var giftValue by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("Pix") }
    var notes by remember { mutableStateOf("") }

    val saleValue = sale.toMoneyDouble()
    val costValue = selectedProduct?.purchaseValue ?: cost.toMoneyDouble()
    val giftCost = if (giftApplied) giftValue.toMoneyDouble() else 0.0
    val alert = viewModel.alert(saleValue, costValue, giftCost)
    val purchaseCount = clients.firstOrNull { it.name.equals(client, ignoreCase = true) }?.purchaseCount
    val clientMessage = viewModel.clientMessage(client, purchaseCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Registrar venda", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        SectionCard("Produto vendido") {
            products.take(4).forEach { product ->
                FilterChip(
                    selected = selectedProduct?.id == product.id,
                    onClick = {
                        selectedProduct = product
                        manualProduct = product.name
                        cost = product.purchaseValue.toString()
                    },
                    label = { Text("${product.name} (${product.suggestedSaleValue.asMoney()})") }
                )
            }
            TextFieldLine("Produto", manualProduct, { manualProduct = it; selectedProduct = null })
            MoneyField("Custo do produto", if (selectedProduct != null) costValue.asMoney() else cost, { cost = it })
            Text("Venda ideal: ${(costValue * 2).asMoney()}", fontWeight = FontWeight.Bold)
        }

        SectionCard("Dados da venda") {
            TextFieldLine("Cliente", client, { client = it })
            clientMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            MoneyField("Valor da venda", sale, { sale = it })
            ChoiceChips(listOf("Pix", "Dinheiro", "Cartao", "Fiado"), payment, { payment = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = giftApplied, onCheckedChange = { giftApplied = it })
                Text("Brinde aplicado")
            }
            if (giftApplied) MoneyField("Valor do brinde", giftValue, { giftValue = it })
            TextFieldLine("Observacao", notes, { notes = it })
            Text("Lucro automatico: ${(saleValue - costValue - giftCost).asMoney()}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            alert?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            PrimaryAction("Salvar venda") {
                viewModel.save(selectedProduct, manualProduct, client, saleValue, costValue, giftApplied, giftCost, payment, notes)
                selectedProduct = null; manualProduct = ""; client = ""; sale = ""; cost = ""; giftApplied = false; giftValue = ""; notes = ""
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}
