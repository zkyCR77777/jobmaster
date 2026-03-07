package com.example.client.data.repository

import com.example.client.data.remote.NetworkClient

object RepositoryProvider {
    private val api = NetworkClient.api

    val homeRepository: HomeRepository by lazy {
        HomeRepository(api)
    }

    val jobsRepository: JobsRepository by lazy {
        JobsRepository(api)
    }

    val deliveryRepository: DeliveryRepository by lazy {
        DeliveryRepository(api)
    }

    val companyRepository: CompanyRepository by lazy {
        CompanyRepository(api)
    }

    val contractRepository: ContractRepository by lazy {
        ContractRepository(api)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(api)
    }
}
