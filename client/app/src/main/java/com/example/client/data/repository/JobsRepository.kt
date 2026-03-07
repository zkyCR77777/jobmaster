package com.example.client.data.repository

import com.example.client.data.JobOpportunity
import com.example.client.data.eagleJobs
import com.example.client.data.remote.SmartPactApi

data class JobsSnapshot(
    val items: List<JobOpportunity>,
    val simulated: Boolean,
)

class JobsRepository(
    private val api: SmartPactApi,
) {
    suspend fun getJobsSnapshot(): JobsSnapshot {
        return runCatching {
            val items = api.listJobs(page = 1, pageSize = 20).data.items
            if (items.isEmpty()) {
                return@runCatching JobsSnapshot(
                    items = eagleJobs,
                    simulated = true,
                )
            }

            val mapped = items.mapIndexed { index, item ->
                JobOpportunity(
                    id = index + 1,
                    title = item.title,
                    company = item.company,
                    location = item.location,
                    salary = item.salary_range,
                    match = item.match_score,
                    isNew = item.is_new,
                    tags = item.tags,
                )
            }
            JobsSnapshot(
                items = mapped,
                simulated = false,
            )
        }.getOrElse {
            JobsSnapshot(
                items = eagleJobs,
                simulated = true,
            )
        }
    }

    suspend fun getJobs(): List<JobOpportunity> = getJobsSnapshot().items
}
