package com.estimalaces.app.presentation.product

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.estimalaces.app.presentation.asDate
import com.estimalaces.app.presentation.asMoney
import com.estimalaces.app.presentation.toMoneyDouble

@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    val message by viewModel.message.collectAsState()
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("lace") }
    var purchase by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var minimum by remember { mutableStateOf("1") }
    var selectedReplenishProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var replenishQuantity by remember { mutableStateOf("") }
    var replenishNote by remember { mutableStateOf("") }
    val purchaseValue = purchase.toMoneyDouble()
    val suggested = viewModel.suggestedValue(purchaseValue)

    LaunchedEffect(message) {
        if (message == "Estoque reposto com sucesso") {
            selectedReplenishProduct = null
            replenishQuantity = ""
            replenishNote = ""
        }
    }

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${it.name} - estoque ${it.currentQuantity} - ${it.purchaseValue.asMoney()} -> ${it.suggestedSaleValue.asMoney()} (${it.createdAt.asDate()})$stockAlert")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            selectedReplenishProduct = it
                            replenishQuantity = ""
                            replenishNote = ""
                            viewModel.clearMessage()
                        }) {
                            Text("Repor estoque")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(72.dp))
    }

    selectedReplenishProduct?.let { product ->
        ReplenishStockDialog(
            product = product,
            quantity = replenishQuantity,
            note = replenishNote,
            message = message,
            onQuantityChange = { replenishQuantity = it.filter { char -> char.isDigit() } },
            onNoteChange = { replenishNote = it },
            onDismiss = {
                selectedReplenishProduct = null
                viewModel.clearMessage()
            },
            onConfirm = {
                viewModel.onReplenishStock(
                    productId = product.id,
                    quantity = replenishQuantity.toIntOrNull() ?: 0,
                    note = replenishNote
                )
            }
        )
    }
}

@Composable
private fun ReplenishStockDialog(
    product: ProductEntity,
    quantity: String,
    note: String,
    message: String?,
    onQuantityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val addedQuantity = quantity.toIntOrNull() ?: 0
    val projectedStock = product.currentQuantity + addedQuantity
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repor estoque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Produto: ${product.name}", fontWeight = FontWeight.Bold)
                Text("Estoque atual: ${product.currentQuantity} unidades")
                TextFieldLine("Quantidade a adicionar", quantity, onQuantityChange)
                TextFieldLine("Observacao/motivo", note, onNoteChange)
                if (projectedStock > 0) {
                    Text("Novo estoque: $projectedStock unidades")
                    if (projectedStock <= product.minimumQuantity) {
                        Text(
                            "Estoque ainda abaixo do minimo",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                message?.let {
                    Text(
                        it,
                        color = if (it == "Estoque reposto com sucesso") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar reposicao")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
