package com.estimalaces.app.presentation.product

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estimalaces.app.presentation.ChoiceChips
import com.estimalaces.app.presentation.MoneyField
import com.estimalaces.app.presentation.PrimaryAction
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.TextFieldLine
import com.estimalaces.app.presentation.asDate
import com.estimalaces.app.presentation.asMoney
import com.estimalaces.app.presentation.toMoneyDouble

@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("lace") }
    var purchase by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var minimum by remember { mutableStateOf("1") }
    val purchaseValue = purchase.toMoneyDouble()
    val suggested = viewModel.suggestedValue(purchaseValue)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Produtos e entradas", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        SectionCard("Novo produto") {
            TextFieldLine("Nome do produto", name, { name = it })
            ChoiceChips(listOf("lace", "wig", "peruca", "manutencao", "produto"), type, { type = it })
            MoneyField("Valor de compra", purchase, { purchase = it })
            TextFieldLine("Quantidade atual", quantity, { quantity = it.filter { char -> char.isDigit() } })
            TextFieldLine("Quantidade minima para alerta", minimum, { minimum = it.filter { char -> char.isDigit() } })
            TextFieldLine("Fornecedor", supplier, { supplier = it })
            TextFieldLine("Observacao", notes, { notes = it })
            Text("VALOR SUGERIDO DE VENDA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(suggested.asMoney(), fontWeight = FontWeight.Bold, fontSize = 34.sp)
            Text("Lucro esperado: ${viewModel.expectedProfit(purchaseValue).asMoney()} | Margem: 100%")
            PrimaryAction("Salvar entrada") {
                viewModel.save(
                    name,
                    type,
                    purchaseValue,
                    supplier,
                    notes,
                    quantity.toIntOrNull() ?: 1,
                    minimum.toIntOrNull() ?: 1
                )
                name = ""; purchase = ""; supplier = ""; notes = ""; quantity = "1"; minimum = "1"
            }
        }

        SectionCard("Ultimos produtos") {
            if (products.isEmpty()) {
                Text("Nenhum produto cadastrado ainda.")
            }
            products.take(8).forEach {
                val stockAlert = if (it.currentQuantity <= it.minimumQuantity) " - Estoque baixo" else ""
                Text("${it.name} - estoque ${it.currentQuantity} - ${it.purchaseValue.asMoney()} -> ${it.suggestedSaleValue.asMoney()} (${it.createdAt.asDate()})$stockAlert")
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}
