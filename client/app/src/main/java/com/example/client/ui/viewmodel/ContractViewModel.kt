package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.ContractClause
import com.example.client.data.guardianClauses
import com.example.client.data.repository.ContractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContractViewModel @Inject constructor(
    private val contractRepository: ContractRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContractUiState())
    val uiState: StateFlow<ContractUiState> = _uiState.asStateFlow()

    fun loadDemoContract() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val snapshot = contractRepository.getContractSnapshot(contractId = "demo")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    summaryLabel = snapshot.summaryLabel,
                    clauses = snapshot.clauses,
                    simulated = snapshot.simulated,
                    hasLoadedDemo = true,
                )
            }
        }
    }

    fun reset() {
        _uiState.value = ContractUiState()
    }
}

data class ContractUiState(
    val isLoading: Boolean = false,
    val hasLoadedDemo: Boolean = false,
    val summaryLabel: String = "需要关注",
    val clauses: List<ContractClause> = guardianClauses,
    val simulated: Boolean = false,
)
