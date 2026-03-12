package com.example.client.data.repository

import com.example.client.data.CompanyProfile
import com.example.client.data.CompanyRiskLevel
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject

data class CompanySnapshot(val items: List<CompanyProfile>)

class CompanyRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getCompaniesSnapshot(): CompanySnapshot {
        val items = api.listCompanyReports(page = 1, pageSize = 20).data.items
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
                risks = item.risks,
                positives = item.positives,
            )
        }
        return CompanySnapshot(items = mapped)
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
