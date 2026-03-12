package com.example.client.data.repository

import com.example.client.data.ContractClause
import com.example.client.data.ContractRiskLevel
import com.example.client.data.remote.ContractTaskResponse
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class ContractSnapshot(
    val summaryLabel: String,
    val clauses: List<ContractClause>,
)

class ContractRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun uploadContract(fileName: String, bytes: ByteArray): ContractTaskResponse {
        val requestBody = bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
        return api.uploadContract(filePart).data
    }

    suspend fun getContractSnapshot(contractId: String): ContractSnapshot {
        val summary = api.getContractSummary(contractId).data
        val clauses = api.getContractClauses(contractId).data.items
        val mappedClauses = clauses.mapIndexed { index, item ->
            ContractClause(
                id = item.id.toIntOrNull() ?: index + 1,
                title = item.title,
                content = item.content,
                riskLevel = item.risk_level.toUiRiskLevel(),
                explanation = item.explanation,
                suggestion = item.suggestion,
            )
        }

        return ContractSnapshot(
            summaryLabel = summary.risk_level.toSummaryLabel(),
            clauses = mappedClauses,
        )
    }

    private fun String.toUiRiskLevel(): ContractRiskLevel {
        return when (this) {
            "safe" -> ContractRiskLevel.SAFE
            "danger" -> ContractRiskLevel.DANGER
            else -> ContractRiskLevel.WARNING
        }
    }

    private fun String.toSummaryLabel(): String {
        return when (this) {
            "safe" -> "较安全"
            "danger" -> "高风险"
            else -> "需要关注"
        }
    }
}
