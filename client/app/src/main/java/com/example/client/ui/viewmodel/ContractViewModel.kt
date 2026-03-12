package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.ContractClause
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
    private val defaultContractId = "00000000-0000-0000-0000-000000000201"
    private val _uiState = MutableStateFlow(ContractUiState())
    val uiState: StateFlow<ContractUiState> = _uiState.asStateFlow()

    fun loadSampleContract() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { contractRepository.getContractSnapshot(contractId = defaultContractId) }
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summaryLabel = snapshot.summaryLabel,
                            clauses = snapshot.clauses,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = "合同分析数据加载失败，请稍后重试。")
                    }
                }
        }
    }

    fun uploadContract(fileName: String, bytes: ByteArray) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val task = contractRepository.uploadContract(fileName, bytes)
                contractRepository.getContractSnapshot(task.id)
            }.onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summaryLabel = snapshot.summaryLabel,
                        clauses = snapshot.clauses,
                        errorMessage = null,
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(isLoading = false, errorMessage = "合同分析数据加载失败，请稍后重试。")
                }
            }
        }
    }

    fun reset() {
        _uiState.value = ContractUiState()
    }
}

data class ContractUiState(
    val isLoading: Boolean = false,
    val summaryLabel: String = "分析中",
    val clauses: List<ContractClause> = emptyList(),
    val errorMessage: String? = null,
)
