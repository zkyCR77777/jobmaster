package com.example.client.data.repository

import com.example.client.data.ContractClause
import com.example.client.data.ContractRiskLevel
import com.example.client.data.guardianClauses
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject

data class ContractSnapshot(
    val summaryLabel: String,
    val clauses: List<ContractClause>,
    val simulated: Boolean,
)

class ContractRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getContractSnapshot(contractId: String): ContractSnapshot {
        return runCatching {
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
            val simulated = mappedClauses.isEmpty()

            ContractSnapshot(
                summaryLabel = summary.risk_level.toSummaryLabel(),
                clauses = if (mappedClauses.isEmpty()) guardianClauses else mappedClauses,
                simulated = simulated,
            )
        }.getOrElse {
            ContractSnapshot(
                summaryLabel = "需要关注",
                clauses = guardianClauses,
                simulated = true,
            )
        }
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
