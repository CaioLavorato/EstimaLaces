package com.estimalaces.app.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.domain.usecase.DateRangeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

enum class ReportFilter { TODAY, WEEK, MONTH }

class ReportViewModel(private val repository: EstimaLacesRepository) : ViewModel() {
    private val ranges = DateRangeUseCase()
    val filter = MutableStateFlow(ReportFilter.MONTH)
    @OptIn(ExperimentalCoroutinesApi::class)
    val report = filter.flatMapLatest {
        val range = when (it) {
            ReportFilter.TODAY -> ranges.today()
            ReportFilter.WEEK -> ranges.week()
            ReportFilter.MONTH -> ranges.month()
        }
        repository.observeReport(range)
    }

    fun setFilter(value: ReportFilter) {
        filter.value = value
    }

    fun export(onRows: suspend (rows: List<com.estimalaces.app.data.dao.SaleExportRow>) -> Unit) {
        viewModelScope.launch {
            onRows(repository.exportRows())
        }
    }
}
