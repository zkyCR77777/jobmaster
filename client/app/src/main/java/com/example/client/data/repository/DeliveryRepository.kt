package com.example.client.data.repository

import com.example.client.data.DeliveryQueueItem
import com.example.client.data.DeliveryQueueStatus
import com.example.client.data.phantomDeliveryQueue
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject

data class DeliveryQueueSnapshot(
    val items: List<DeliveryQueueItem>,
    val simulated: Boolean,
)

class DeliveryRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getDeliveryQueue(): DeliveryQueueSnapshot {
        return runCatching {
            val items = api.listDeliveries(page = 1, pageSize = 20).data.items
            if (items.isEmpty()) {
                return@runCatching DeliveryQueueSnapshot(phantomDeliveryQueue, true)
            }

            val mapped = items.mapIndexed { index, item ->
                DeliveryQueueItem(
                    id = index + 1,
                    company = item.company,
                    position = item.position,
                    status = item.status.toUiStatus(),
                    time = item.updated_at.toTimeLabel(),
                )
            }
            DeliveryQueueSnapshot(mapped, false)
        }.getOrElse {
            DeliveryQueueSnapshot(phantomDeliveryQueue, true)
        }
    }

    private fun String.toUiStatus(): DeliveryQueueStatus {
        return when (this) {
            "delivering" -> DeliveryQueueStatus.DELIVERING
            "pending" -> DeliveryQueueStatus.PENDING
            "delivered", "viewed", "written_test", "interview", "offer", "rejected" -> DeliveryQueueStatus.DELIVERED
            else -> DeliveryQueueStatus.PENDING
        }
    }

    private fun String?.toTimeLabel(): String {
        if (this.isNullOrBlank()) return "--:--"
        if (length >= 16 && this[10] == 'T') {
            return substring(11, 16)
        }
        return this.take(5)
    }
}
