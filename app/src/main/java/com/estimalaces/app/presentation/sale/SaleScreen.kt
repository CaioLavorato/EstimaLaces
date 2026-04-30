package com.estimalaces.app.presentation.sale

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
import androidx.compose.material3.Button
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
import com.estimalaces.app.data.entity.ClientEntity
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
    var allowCreateProduct by remember { mutableStateOf(false) }
    var client by remember { mutableStateOf("") }
    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var allowCreateClient by remember { mutableStateOf(false) }
    var sale by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var giftApplied by remember { mutableStateOf(false) }
    var giftType by remember { mutableStateOf("VALOR") }
    var giftValue by remember { mutableStateOf("") }
    var selectedGiftProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var giftProductName by remember { mutableStateOf("") }
    var allowCreateGiftProduct by remember { mutableStateOf(false) }
    var payment by remember { mutableStateOf("Pix") }
    var cardFeePercent by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val saleValue = sale.toMoneyDouble()
    val costValue = selectedProduct?.purchaseValue ?: cost.toMoneyDouble()
    val giftCost = if (giftApplied && giftType == "VALOR") giftValue.toMoneyDouble() else 0.0
    val cardFee = if (payment == "Cartao") cardFeePercent.toMoneyDouble() else 0.0
    val alert = viewModel.alert(saleValue, costValue, giftCost, cardFee)
    val purchaseCount = clients.firstOrNull { it.name.equals(client, ignoreCase = true) }?.purchaseCount
    val clientMessage = viewModel.clientMessage(client, purchaseCount)
    val canUseGiftProduct = !giftApplied || giftType != "PRODUTO" ||
        selectedGiftProduct != null ||
        (allowCreateGiftProduct && giftProductName.isNotBlank())
    val canSaveSale = saleValue > 0.0 &&
        (selectedProduct != null || allowCreateProduct) &&
        (selectedClient != null || allowCreateClient || client.isBlank()) &&
        canUseGiftProduct

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
            ProductSearchField(
                label = "Buscar produto",
                query = manualProduct,
                products = products,
                selectedProduct = selectedProduct,
                allowCreate = allowCreateProduct,
                onQueryChange = {
                    manualProduct = it
                    selectedProduct = null
                    allowCreateProduct = false
                },
                onSelect = {
                    selectedProduct = it
                    manualProduct = it.name
                    cost = it.purchaseValue.toString()
                    allowCreateProduct = false
                },
                onAllowCreate = { allowCreateProduct = true }
            )
            MoneyField("Custo do produto", if (selectedProduct != null) costValue.asMoney() else cost, { cost = it })
            Text("Venda ideal: ${(costValue * 2).asMoney()}", fontWeight = FontWeight.Bold)
            selectedProduct?.let {
                val stockMessage = when {
                    it.currentQuantity <= 0 -> "Estoque zerado. Confirme se essa venda veio do site ou reposicao."
                    it.currentQuantity <= it.minimumQuantity -> "Estoque baixo - necessario reposicao."
                    else -> "Estoque atual: ${it.currentQuantity}"
                }
                Text(stockMessage, color = if (it.currentQuantity <= it.minimumQuantity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            if (selectedProduct == null && allowCreateProduct) {
                Text("Produto novo sera cadastrado sem estoque inicial. Para controlar estoque, reponha depois.", color = MaterialTheme.colorScheme.error)
            }
        }

        SectionCard("Dados da venda") {
            ClientSearchField(
                query = client,
                clients = clients,
                selectedClient = selectedClient,
                allowCreate = allowCreateClient,
                onQueryChange = {
                    client = it
                    selectedClient = null
                    allowCreateClient = false
                },
                onSelect = {
                    selectedClient = it
                    client = it.name
                    allowCreateClient = false
                },
                onAllowCreate = { allowCreateClient = true }
            )
            clientMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            MoneyField("Valor da venda", sale, { sale = it })
            ChoiceChips(listOf("Pix", "Dinheiro", "Cartao", "Fiado"), payment, { payment = it })
            if (payment == "Cartao") {
                MoneyField("Taxa do cartao (%) opcional", cardFeePercent, { cardFeePercent = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = giftApplied, onCheckedChange = { giftApplied = it })
                Text("Brinde aplicado")
            }
            if (giftApplied) {
                ChoiceChips(listOf("VALOR", "PRODUTO"), giftType, { giftType = it })
                if (giftType == "VALOR") {
                    MoneyField("Valor do brinde", giftValue, { giftValue = it })
                } else {
                    ProductSearchField(
                        label = "Buscar produto do brinde",
                        query = giftProductName,
                        products = products,
                        selectedProduct = selectedGiftProduct,
                        allowCreate = allowCreateGiftProduct,
                        onQueryChange = {
                            giftProductName = it
                            selectedGiftProduct = null
                            allowCreateGiftProduct = false
                        },
                        onSelect = {
                            selectedGiftProduct = it
                            giftProductName = it.name
                            allowCreateGiftProduct = false
                        },
                        onAllowCreate = { allowCreateGiftProduct = true }
                    )
                }
            }
            TextFieldLine("Observacao", notes, { notes = it })
            Text(
                "Lucro automatico: ${(saleValue - costValue - giftCost - (saleValue * cardFee / 100.0)).asMoney()}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            alert?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            if (!canSaveSale) {
                Text("Selecione um produto/cliente existente ou confirme cadastro novo antes de salvar.", color = MaterialTheme.colorScheme.error)
            }
            PrimaryAction("Salvar venda", enabled = canSaveSale) {
                viewModel.save(
                    selectedProduct,
                    manualProduct,
                    client,
                    saleValue,
                    costValue,
                    giftApplied,
                    giftCost,
                    giftType,
                    selectedGiftProduct,
                    giftProductName,
                    payment,
                    cardFee,
                    notes
                )
                selectedProduct = null; manualProduct = ""; allowCreateProduct = false; selectedClient = null; allowCreateClient = false; client = ""; sale = ""; cost = ""; giftApplied = false; giftType = "VALOR"; giftValue = ""; selectedGiftProduct = null; giftProductName = ""; allowCreateGiftProduct = false; cardFeePercent = ""; notes = ""
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun ProductSearchField(
    label: String,
    query: String,
    products: List<ProductEntity>,
    selectedProduct: ProductEntity?,
    allowCreate: Boolean,
    onQueryChange: (String) -> Unit,
    onSelect: (ProductEntity) -> Unit,
    onAllowCreate: () -> Unit
) {
    val matches = remember(query, products) {
        if (query.length < 2) emptyList() else products.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(8)
    }
    val exact = remember(query, products) {
        products.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
    }

    TextFieldLine(label, query, onQueryChange)
    selectedProduct?.let {
        Text("Selecionado: ${it.name} - estoque ${it.currentQuantity}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
    if (matches.isNotEmpty() && selectedProduct == null) {
        Text("Produtos encontrados", fontWeight = FontWeight.Bold)
        matches.forEach { product ->
            Button(onClick = { onSelect(product) }, modifier = Modifier.fillMaxWidth()) {
                Text("${product.name} - ${product.currentQuantity} un.")
            }
        }
    }
    if (query.length >= 3 && exact == null && selectedProduct == null && !allowCreate) {
        Text("Confira se o produto ja nao esta cadastrado antes de criar novo.", color = MaterialTheme.colorScheme.error)
        Button(onClick = onAllowCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Cadastrar novo produto: $query")
        }
    }
    if (allowCreate) {
        Text("Novo produto confirmado: $query", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ClientSearchField(
    query: String,
    clients: List<ClientEntity>,
    selectedClient: ClientEntity?,
    allowCreate: Boolean,
    onQueryChange: (String) -> Unit,
    onSelect: (ClientEntity) -> Unit,
    onAllowCreate: () -> Unit
) {
    val matches = remember(query, clients) {
        if (query.length < 2) emptyList() else clients.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(8)
    }
    val exact = remember(query, clients) {
        clients.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
    }

    TextFieldLine("Buscar cliente", query, onQueryChange)
    selectedClient?.let {
        Text("Selecionado: ${it.name} - ${it.purchaseCount} compras", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
    if (matches.isNotEmpty() && selectedClient == null) {
        Text("Clientes encontrados", fontWeight = FontWeight.Bold)
        matches.forEach { client ->
            Button(onClick = { onSelect(client) }, modifier = Modifier.fillMaxWidth()) {
                Text("${client.name} - ${client.purchaseCount} compras")
            }
        }
    }
    if (query.length >= 3 && exact == null && selectedClient == null && !allowCreate) {
        Text("Confira se o cliente ja nao esta cadastrado antes de criar novo.", color = MaterialTheme.colorScheme.error)
        Button(onClick = onAllowCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Cadastrar novo cliente: $query")
        }
    }
    if (allowCreate) {
        Text("Novo cliente confirmado: $query", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}
