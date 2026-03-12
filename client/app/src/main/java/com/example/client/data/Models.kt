package com.example.client.data

enum class AppModule(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val stats: String,
) {
    EAGLE(
        id = "eagle",
        title = "鹰眼猎手",
        subtitle = "智能职位雷达",
        description = "全天候扫描优质职位",
        stats = "12 个新机会",
    ),
    PHANTOM(
        id = "phantom",
        title = "幻影投递官",
        subtitle = "自动化简历定制",
        description = "智能投递一键直达",
        stats = "8 份待投递",
    ),
    INVESTIGATOR(
        id = "investigator",
        title = "深网调查员",
        subtitle = "企业风险洞察",
        description = "深度调研企业背景",
        stats = "3 家待分析",
    ),
    GUARDIAN(
        id = "guardian",
        title = "契约卫士",
        subtitle = "合同智能解读",
        description = "保障你的职场权益",
        stats = "1 份待审核",
    );

    companion object {
        fun fromId(id: String?): AppModule? = entries.firstOrNull { it.id == id }
    }
}

data class HomeAgent(
    val module: AppModule,
    val description: String,
    val stats: String,
)

enum class ChatSender {
    USER,
    AGENT,
}

data class ChatMessage(
    val id: Long,
    val sender: ChatSender,
    val content: String,
    val agent: AppModule? = null,
)

data class ChatQuickCommand(
    val label: String,
    val module: AppModule,
)

val chatQuickCommands = listOf(
    ChatQuickCommand("帮我找高薪远程工作", AppModule.EAGLE),
    ChatQuickCommand("投递字节跳动的职位", AppModule.PHANTOM),
    ChatQuickCommand("调查这家公司背景", AppModule.INVESTIGATOR),
    ChatQuickCommand("分析我的合同条款", AppModule.GUARDIAN),
)

data class JobOpportunity(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val salary: String,
    val match: Int,
    val isNew: Boolean,
    val tags: List<String>,
)

enum class DeliveryQueueStatus {
    DELIVERED,
    DELIVERING,
    PENDING,
}

data class DeliveryQueueItem(
    val id: Int,
    val company: String,
    val position: String,
    val status: DeliveryQueueStatus,
    val time: String,
)

val phantomResumeSteps = listOf(
    "正在分析目标职位要求...",
    "提取关键技能：React、TypeScript、Node.js",
    "优化项目经验描述...",
    "调整工作成果的数据化表达...",
    "生成定制化自我介绍...",
    "简历优化完成！",
)

enum class CompanyRiskLevel {
    LOW,
    MEDIUM,
    HIGH,
}

data class CompanyProfile(
    val id: Int,
    val name: String,
    val industry: String,
    val size: String,
    val rating: Double,
    val riskLevel: CompanyRiskLevel,
    val growth: Int,
    val salary: String,
    val risks: List<String>,
    val positives: List<String>,
)

val investigatorSources = listOf("工商信息", "舆情分析", "员工评价", "财务状况")

enum class ContractRiskLevel(
    val label: String,
) {
    SAFE("安全"),
    WARNING("需注意"),
    DANGER("高风险"),
}

data class ContractClause(
    val id: Int,
    val title: String,
    val content: String,
    val riskLevel: ContractRiskLevel,
    val explanation: String,
    val suggestion: String? = null,
)

val guardianScanStages = listOf(
    "文档解析" to 25,
    "条款识别" to 50,
    "风险分析" to 75,
    "生成报告" to 95,
)

fun greetingForHour(hour: Int): String = when {
    hour < 12 -> "早上好"
    hour < 18 -> "下午好"
    else -> "晚上好"
}
