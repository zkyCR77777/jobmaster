package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.JobOpportunity
import com.example.client.data.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val jobsRepository: JobsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { jobsRepository.getJobsSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(items = snapshot.items, errorMessage = null) }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(errorMessage = "职位数据加载失败，请稍后重试。") }
                }
        }
    }
}

data class JobsUiState(
    val items: List<JobOpportunity> = emptyList(),
    val errorMessage: String? = null,
)
