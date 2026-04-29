package com.estimalaces.app.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estimalaces.app.data.repository.EstimaLacesRepository
import kotlinx.coroutines.launch

class GoalViewModel(private val repository: EstimaLacesRepository) : ViewModel() {
    val goal = repository.observeGoal()

    fun save(monthlyGoal: Double, maxGift: Double) {
        viewModelScope.launch {
            repository.saveGoal(monthlyGoal, maxGift)
        }
    }
}
