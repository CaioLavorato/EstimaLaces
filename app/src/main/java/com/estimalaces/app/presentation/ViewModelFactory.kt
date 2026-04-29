package com.estimalaces.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.presentation.goal.GoalViewModel
import com.estimalaces.app.presentation.home.HomeViewModel
import com.estimalaces.app.presentation.product.ProductViewModel
import com.estimalaces.app.presentation.report.ReportViewModel
import com.estimalaces.app.presentation.sale.SaleViewModel

class EstimaLacesViewModelFactory(
    private val repository: EstimaLacesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(repository)
            ProductViewModel::class.java -> ProductViewModel(repository)
            SaleViewModel::class.java -> SaleViewModel(repository)
            GoalViewModel::class.java -> GoalViewModel(repository)
            ReportViewModel::class.java -> ReportViewModel(repository)
            else -> error("ViewModel nao registrado: ${modelClass.name}")
        } as T
    }
}
