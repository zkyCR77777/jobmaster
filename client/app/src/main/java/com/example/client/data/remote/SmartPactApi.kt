package com.example.client.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface SmartPactApi {
    @GET("api/v1/dashboard/home")
    suspend fun getHomeDashboard(): ApiEnvelope<DashboardHomeResponse>

    @GET("api/v1/jobs")
    suspend fun listJobs(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope<JobListPageResponse>

    @GET("api/v1/deliveries")
    suspend fun listDeliveries(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope<DeliveryListPageResponse>

    @GET("api/v1/company-reports")
    suspend fun listCompanyReports(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope<CompanyReportPageResponse>

    @GET("api/v1/contracts/{contractId}")
    suspend fun getContractSummary(
        @Path("contractId") contractId: String,
    ): ApiEnvelope<ContractSummaryResponse>

    @GET("api/v1/contracts/{contractId}/clauses")
    suspend fun getContractClauses(
        @Path("contractId") contractId: String,
    ): ApiEnvelope<ContractClauseListResponse>

    @Multipart
    @POST("api/v1/contracts")
    suspend fun uploadContract(
        @Part file: MultipartBody.Part,
    ): ApiEnvelope<ContractTaskResponse>

    @POST("api/v1/chat/sessions")
    suspend fun createChatSession(): ApiEnvelope<ChatSessionCreateResponse>

    @GET("api/v1/chat/sessions/{sessionId}/messages")
    suspend fun getChatMessages(
        @Path("sessionId") sessionId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): ApiEnvelope<ChatMessagePageResponse>

    @POST("api/v1/chat/sessions/{sessionId}/messages")
    suspend fun sendChatMessage(
        @Path("sessionId") sessionId: String,
        @Body payload: ChatSendMessageRequest,
    ): ApiEnvelope<ChatSendMessageResponse>
}
