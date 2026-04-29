package com.estimalaces.app.presentation.home

import androidx.lifecycle.ViewModel
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.domain.usecase.DateRangeUseCase

class HomeViewModel(repository: EstimaLacesRepository) : ViewModel() {
    private val month = DateRangeUseCase().month()
    val summary = repository.observeDashboard(month)
}
