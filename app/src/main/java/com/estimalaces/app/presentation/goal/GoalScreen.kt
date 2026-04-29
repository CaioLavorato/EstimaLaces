package com.estimalaces.app.presentation.goal

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
import com.estimalaces.app.presentation.MoneyField
import com.estimalaces.app.presentation.PrimaryAction
import com.estimalaces.app.presentation.SectionCard
import com.estimalaces.app.presentation.asMoney
import com.estimalaces.app.presentation.toMoneyDouble

@Composable
fun GoalScreen(viewModel: GoalViewModel) {
    val goal by viewModel.goal.collectAsState(initial = null)
    var monthlyGoal by remember { mutableStateOf("") }
    var maxGift by remember { mutableStateOf("") }

    LaunchedEffect(goal?.updatedAt) {
        goal?.let {
            monthlyGoal = it.monthlyGoal.toString()
            maxGift = it.maxGiftValue.toString()
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
        Text("Metas e brindes", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SectionCard("Meta mensal") {
            MoneyField("Meta mensal", monthlyGoal, { monthlyGoal = it })
            MoneyField("Brinde maximo apos meta", maxGift, { maxGift = it })
            Text("Atual: ${goal?.monthlyGoal?.asMoney() ?: "sem meta"}")
            Text("Quando bater a meta, o app avisa que pode liberar brinde de ate ${maxGift.toMoneyDouble().asMoney()}.")
            PrimaryAction("Salvar meta") {
                viewModel.save(monthlyGoal.toMoneyDouble(), maxGift.toMoneyDouble())
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}
