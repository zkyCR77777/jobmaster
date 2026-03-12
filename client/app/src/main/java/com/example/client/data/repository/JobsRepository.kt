package com.example.client.data.repository

import com.example.client.data.JobOpportunity
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject

data class JobsSnapshot(val items: List<JobOpportunity>)

class JobsRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getJobsSnapshot(): JobsSnapshot {
        val items = api.listJobs(page = 1, pageSize = 20).data.items
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
        return JobsSnapshot(items = mapped)
    }

    suspend fun getJobs(): List<JobOpportunity> = getJobsSnapshot().items
}
