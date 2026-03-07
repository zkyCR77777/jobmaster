package com.example.client.data.remote

data class ApiEnvelope<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T,
    val request_id: String? = null,
)

data class DashboardHomeResponse(
    val greeting: String,
    val app_name: String,
    val notification_count: Int,
    val hero_stats: DashboardHeroStatsResponse,
    val modules: List<DashboardModuleResponse> = emptyList(),
    val agent_feed: List<DashboardAgentFeedResponse> = emptyList(),
)

data class DashboardHeroStatsResponse(
    val new_jobs_today: Int = 0,
    val match_success_rate: Int = 0,
    val interview_invites: Int = 0,
)

data class DashboardModuleResponse(
    val module: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val stats_text: String,
    val pending_count: Int = 0,
)

data class DashboardAgentFeedResponse(
    val module: String,
    val description: String,
    val stats_text: String,
)

data class JobListPageResponse(
    val items: List<JobListItemResponse> = emptyList(),
    val page: Int = 1,
    val page_size: Int = 20,
    val total: Int = 0,
)

data class JobListItemResponse(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val salary_range: String = "",
    val match_score: Int = 0,
    val is_new: Boolean = false,
    val published_at: String? = null,
    val tags: List<String> = emptyList(),
)

data class DeliveryListPageResponse(
    val items: List<DeliveryItemResponse> = emptyList(),
    val page: Int = 1,
    val page_size: Int = 20,
    val total: Int = 0,
)

data class DeliveryItemResponse(
    val id: String,
    val job_id: String,
    val resume_id: String,
    val company: String,
    val position: String,
    val status: String,
    val updated_at: String? = null,
    val delivered_at: String? = null,
    val note: String? = null,
)

data class CompanyReportPageResponse(
    val items: List<CompanyReportItemResponse> = emptyList(),
    val page: Int = 1,
    val page_size: Int = 20,
    val total: Int = 0,
)

data class CompanyReportItemResponse(
    val id: String,
    val name: String,
    val industry: String = "",
    val size: String = "",
    val rating: Double = 0.0,
    val risk_level: String = "medium",
    val growth: Int = 0,
    val salary_range: String = "",
    val risks: List<String> = emptyList(),
    val positives: List<String> = emptyList(),
    val updated_at: String? = null,
)

data class ContractSummaryResponse(
    val id: String,
    val file_name: String,
    val status: String,
    val progress: Int = 0,
    val overall_score: Int? = null,
    val risk_level: String = "warning",
    val summary: String = "",
    val summary_counts: ContractSummaryCountsResponse = ContractSummaryCountsResponse(),
    val stages: List<ContractStageResponse> = emptyList(),
)

data class ContractSummaryCountsResponse(
    val safe: Int = 0,
    val warning: Int = 0,
    val danger: Int = 0,
)

data class ContractStageResponse(
    val name: String,
    val status: String,
)

data class ContractClauseListResponse(
    val items: List<ContractClauseResponse> = emptyList(),
)

data class ContractClauseResponse(
    val id: String,
    val title: String,
    val content: String,
    val risk_level: String,
    val explanation: String,
    val suggestion: String? = null,
)

data class ChatSessionCreateResponse(
    val session_id: String,
    val welcome_message: String,
)

data class ChatMessagePageResponse(
    val items: List<ChatMessageItemResponse> = emptyList(),
    val page: Int = 1,
    val page_size: Int = 50,
    val total: Int = 0,
)

data class ChatMessageItemResponse(
    val id: String,
    val role: String,
    val content: String,
    val module: String? = null,
    val created_at: String? = null,
)

data class ChatSendMessageRequest(
    val content: String,
    val current_module: String? = null,
)

data class ChatSendMessageResponse(
    val message_id: String,
    val detected_module: String,
)
