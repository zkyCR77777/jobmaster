package com.example.client.data.repository

import com.example.client.data.AppModule
import com.example.client.data.HomeAgent
import com.example.client.data.greetingForHour
import com.example.client.data.homeAgents
import com.example.client.data.remote.SmartPactApi
import javax.inject.Inject
import java.util.Calendar

data class HomeHeroStats(
    val newJobsToday: Int,
    val matchSuccessRate: Int,
    val interviewInvites: Int,
)

data class HomeDashboardSnapshot(
    val greeting: String,
    val notificationCount: Int,
    val heroStats: HomeHeroStats,
    val agentFeed: List<HomeAgent>,
    val simulated: Boolean,
)

class HomeRepository @Inject constructor(
    private val api: SmartPactApi,
) {
    suspend fun getDashboardSnapshot(): HomeDashboardSnapshot {
        return runCatching {
            val response = api.getHomeDashboard().data
            val mappedAgents = response.agent_feed.mapNotNull { item ->
                val module = AppModule.fromId(item.module) ?: return@mapNotNull null
                HomeAgent(module = module, description = item.description, stats = item.stats_text)
            }

            HomeDashboardSnapshot(
                greeting = response.greeting,
                notificationCount = response.notification_count,
                heroStats = HomeHeroStats(
                    newJobsToday = response.hero_stats.new_jobs_today,
                    matchSuccessRate = response.hero_stats.match_success_rate,
                    interviewInvites = response.hero_stats.interview_invites,
                ),
                agentFeed = if (mappedAgents.isEmpty()) homeAgents else mappedAgents,
                simulated = false,
            )
        }.getOrElse {
            fallbackSnapshot()
        }
    }

    private fun fallbackSnapshot(): HomeDashboardSnapshot {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return HomeDashboardSnapshot(
            greeting = greetingForHour(hour),
            notificationCount = 3,
            heroStats = HomeHeroStats(
                newJobsToday = 24,
                matchSuccessRate = 89,
                interviewInvites = 5,
            ),
            agentFeed = homeAgents,
            simulated = true,
        )
    }
}
