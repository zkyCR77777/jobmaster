package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.CompanyProfile
import com.example.client.data.repository.CompanyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { companyRepository.getCompaniesSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(companies = snapshot.items, errorMessage = null) }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(errorMessage = "企业分析数据加载失败，请稍后重试。") }
                }
        }
    }
}

data class CompanyUiState(
    val companies: List<CompanyProfile> = emptyList(),
    val errorMessage: String? = null,
)
