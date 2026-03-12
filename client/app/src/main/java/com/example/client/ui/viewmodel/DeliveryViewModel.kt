package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.DeliveryQueueItem
import com.example.client.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { deliveryRepository.getDeliveryQueue() }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(queue = snapshot.items, errorMessage = null) }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(errorMessage = "投递数据加载失败，请稍后重试。") }
                }
        }
    }
}

data class DeliveryUiState(
    val queue: List<DeliveryQueueItem> = emptyList(),
    val errorMessage: String? = null,
)
