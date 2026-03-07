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

val homeAgents = listOf(
    HomeAgent(AppModule.EAGLE, "全天候扫描优质职位", "12 个新机会"),
    HomeAgent(AppModule.PHANTOM, "智能投递一键直达", "8 份待投递"),
    HomeAgent(AppModule.INVESTIGATOR, "深度调研企业背景", "3 家待分析"),
    HomeAgent(AppModule.GUARDIAN, "保障你的职场权益", "1 份待审核"),
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

val simulatedResponses = mapOf(
    AppModule.EAGLE to listOf(
        "好的，我正在扫描全网招聘平台...",
        "已发现 23 个符合条件的高薪远程职位，其中 5 个来自一线大厂。要我为你筛选出最匹配的吗？",
        "根据你的技能画像，我推荐以下 3 个职位：1. 字节跳动 - 前端架构师 (60-80K)；2. 阿里云 - 高级工程师 (50-70K)；3. 腾讯 - 技术专家 (55-75K)。",
    ),
    AppModule.PHANTOM to listOf(
        "收到，正在为你定制简历...",
        "我已根据字节跳动的职位要求优化了你的简历：突出了 React 和性能优化经验，调整了项目描述的措辞。准备好投递了吗？",
        "简历已投递成功！我会持续追踪投递状态，一旦有回复立即通知你。",
    ),
    AppModule.INVESTIGATOR to listOf(
        "正在启动深度调查...",
        "已从 12 个数据源收集信息，包括工商信息、新闻舆情、员工评价等。",
        "调查报告已生成：该公司成立 5 年，融资到 C 轮，近一年无重大负面新闻，员工满意度评分 4.2/5。整体风险等级：低风险。",
    ),
    AppModule.GUARDIAN to listOf(
        "正在解析你的合同文件...",
        "我发现了 2 处需要注意的条款：1. 竞业限制期限较长（24 个月）；2. 保密条款的赔偿责任不明确。",
        "建议你在签署前与 HR 协商：将竞业期限缩短至 12 个月，并明确保密条款的具体范围和赔偿上限。需要我帮你拟定协商话术吗？",
    ),
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

val eagleJobs = listOf(
    JobOpportunity(1, "高级前端工程师", "字节跳动", "北京", "40-60K", 95, true, listOf("React", "TypeScript")),
    JobOpportunity(2, "全栈开发工程师", "阿里巴巴", "杭州", "35-55K", 88, true, listOf("Node.js", "Vue")),
    JobOpportunity(3, "产品经理", "腾讯", "深圳", "30-50K", 82, false, listOf("B端", "SaaS")),
    JobOpportunity(4, "UI/UX 设计师", "美团", "上海", "25-40K", 78, false, listOf("Figma", "用户研究")),
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

val phantomDeliveryQueue = listOf(
    DeliveryQueueItem(1, "字节跳动", "高级前端工程师", DeliveryQueueStatus.DELIVERED, "10:30"),
    DeliveryQueueItem(2, "阿里巴巴", "全栈开发工程师", DeliveryQueueStatus.DELIVERED, "10:45"),
    DeliveryQueueItem(3, "腾讯", "产品经理", DeliveryQueueStatus.DELIVERING, "11:00"),
    DeliveryQueueItem(4, "美团", "UI设计师", DeliveryQueueStatus.PENDING, "11:15"),
    DeliveryQueueItem(5, "京东", "数据分析师", DeliveryQueueStatus.PENDING, "11:30"),
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

val investigatorCompanies = listOf(
    CompanyProfile(
        id = 1,
        name = "字节跳动",
        industry = "互联网科技",
        size = "100,000+",
        rating = 4.2,
        riskLevel = CompanyRiskLevel.LOW,
        growth = 23,
        salary = "45K",
        risks = listOf("加班文化较重", "竞争压力大"),
        positives = listOf("技术栈先进", "晋升通道清晰", "年终奖丰厚"),
    ),
    CompanyProfile(
        id = 2,
        name = "创业公司A",
        industry = "AI科技",
        size = "50-200",
        rating = 3.5,
        riskLevel = CompanyRiskLevel.HIGH,
        growth = -12,
        salary = "35K",
        risks = listOf("资金链紧张", "近期裁员传闻", "核心人员流失"),
        positives = listOf("期权激励", "扁平化管理"),
    ),
    CompanyProfile(
        id = 3,
        name = "阿里巴巴",
        industry = "电子商务",
        size = "250,000+",
        rating = 4.0,
        riskLevel = CompanyRiskLevel.LOW,
        growth = 8,
        salary = "42K",
        risks = listOf("组织架构频繁调整"),
        positives = listOf("完善的培训体系", "国际化机会多", "福利待遇好"),
    ),
)

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

val guardianClauses = listOf(
    ContractClause(
        id = 1,
        title = "薪资福利条款",
        content = "基本工资为每月人民币 45,000 元，按月发放，发放日期为每月 15 日。年终奖金根据公司业绩及个人绩效考核结果发放。",
        riskLevel = ContractRiskLevel.SAFE,
        explanation = "薪资发放日期和方式明确，年终奖条款符合常规。",
    ),
    ContractClause(
        id = 2,
        title = "竞业限制条款",
        content = "员工在离职后 24 个月内，不得在与公司存在竞争关系的企业任职。竞业补偿金为离职前 12 个月平均工资的 30%。",
        riskLevel = ContractRiskLevel.WARNING,
        explanation = "竞业限制期限为 24 个月，属于较长期限。补偿金比例 30% 略低于市场平均水平（通常为 40-50%）。",
        suggestion = "建议协商将竞业期限缩短至 12 个月，或提高补偿金比例至 40% 以上。",
    ),
    ContractClause(
        id = 3,
        title = "知识产权归属",
        content = "员工在职期间创作的与公司业务相关的所有作品、发明、技术成果等，其知识产权归公司所有。",
        riskLevel = ContractRiskLevel.SAFE,
        explanation = "这是标准的职务发明条款，符合劳动法规定。",
    ),
    ContractClause(
        id = 4,
        title = "保密义务",
        content = "员工应对公司商业秘密保密，保密期限为无限期。违反保密义务需赔偿公司全部损失。",
        riskLevel = ContractRiskLevel.DANGER,
        explanation = "保密期限“无限期”表述不够明确，“全部损失”赔偿条款可能给员工带来较大风险。",
        suggestion = "建议明确保密信息范围，设定合理的保密期限，并将赔偿上限明确化。",
    ),
)

fun greetingForHour(hour: Int): String = when {
    hour < 12 -> "早上好"
    hour < 18 -> "下午好"
    else -> "晚上好"
}
