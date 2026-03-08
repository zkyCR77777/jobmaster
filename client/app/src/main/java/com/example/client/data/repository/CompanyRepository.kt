package com.example.client.data.repository

import com.example.client.data.CompanyProfile
import com.example.client.data.CompanyRiskLevel
import com.example.client.data.investigatorCompanies
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject

data class CompanySnapshot(
    val items: List<CompanyProfile>,
    val simulated: Boolean,
)

class CompanyRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getCompaniesSnapshot(): CompanySnapshot {
        return runCatching {
            val items = api.listCompanyReports(page = 1, pageSize = 20).data.items
            if (items.isEmpty()) {
                return@runCatching CompanySnapshot(
                    items = investigatorCompanies,
                    simulated = true,
                )
            }

            val mapped = items.mapIndexed { index, item ->
                CompanyProfile(
                    id = index + 1,
                    name = item.name,
                    industry = item.industry,
                    size = item.size,
                    rating = item.rating,
                    riskLevel = item.risk_level.toUiRiskLevel(),
                    growth = item.growth,
                    salary = item.salary_range.ifBlank { "--" },
                    risks = if (item.risks.isEmpty()) listOf("暂无风险数据") else item.risks,
                    positives = if (item.positives.isEmpty()) listOf("暂无正面信息") else item.positives,
                )
            }
            CompanySnapshot(
                items = mapped,
                simulated = false,
            )
        }.getOrElse {
            CompanySnapshot(
                items = investigatorCompanies,
                simulated = true,
            )
        }
    }

    suspend fun getCompanies(): List<CompanyProfile> = getCompaniesSnapshot().items

    private fun String.toUiRiskLevel(): CompanyRiskLevel {
        return when (this) {
            "low" -> CompanyRiskLevel.LOW
            "high" -> CompanyRiskLevel.HIGH
            else -> CompanyRiskLevel.MEDIUM
        }
    }
}
