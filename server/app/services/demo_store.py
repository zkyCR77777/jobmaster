from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from itertools import count
from threading import Lock
from uuid import uuid4

from app.schemas.chat import ChatMessageItem, ChatSessionItem
from app.schemas.common import (
    ChatRoleEnum,
    CompanyRiskLevelEnum,
    ContractRiskLevelEnum,
    DeliveryStatusEnum,
    ModuleEnum,
    TaskStage,
    TaskStatusEnum,
)
from app.schemas.company import (
    CompanyBasicProfile,
    CompanyReportDetail,
    CompanyReportListItem,
    CompanyRiskBreakdown,
)
from app.schemas.contract import (
    ContractClauseItem,
    ContractReport,
    ContractSummary,
    ContractSummaryCounts,
    ContractTask,
)
from app.schemas.dashboard import (
    DashboardAgentFeedItem,
    DashboardHeroStats,
    DashboardHomeData,
    DashboardModuleCard,
)
from app.schemas.delivery import DeliveryItem
from app.schemas.jobs import JobDetail, JobListItem, JobMatchData, JobRecommendationItem

NOW = datetime(2026, 3, 8, 10, 0, 0)


@dataclass(slots=True)
class ChatSessionRecord:
    session_id: str
    welcome_message: str
    created_at: datetime
    messages: list[ChatMessageItem] = field(default_factory=list)


JOB_ITEMS = [
    JobListItem(
        id='job-1',
        title='高级前端工程师',
        company='字节跳动',
        location='北京',
        salary_range='35-55K',
        match_score=94,
        is_new=True,
        published_at=NOW - timedelta(hours=2),
        tags=['React', 'TypeScript', '增长'],
    ),
    JobListItem(
        id='job-2',
        title='全栈开发工程师',
        company='阿里巴巴',
        location='杭州',
        salary_range='32-48K',
        match_score=91,
        is_new=True,
        published_at=NOW - timedelta(hours=4),
        tags=['Java', 'Spring', 'Vue'],
    ),
    JobListItem(
        id='job-3',
        title='产品经理',
        company='腾讯',
        location='深圳',
        salary_range='30-50K',
        match_score=82,
        is_new=False,
        published_at=NOW - timedelta(days=1),
        tags=['B端', 'SaaS'],
    ),
    JobListItem(
        id='job-4',
        title='UI/UX 设计师',
        company='美团',
        location='上海',
        salary_range='25-40K',
        match_score=78,
        is_new=False,
        published_at=NOW - timedelta(days=2),
        tags=['Figma', '用户研究'],
    ),
]

JOB_DETAILS = {
    item.id: JobDetail(
        id=item.id,
        title=item.title,
        company=item.company,
        location=item.location,
        salary_range=item.salary_range,
        company_url='https://example.com/company',
        job_type='全职',
        description=(
            f'{item.company} 正在招聘 {item.title}，'
            '欢迎具备跨团队协作和业务理解能力的候选人。'
        ),
        requirements='具备相关项目经验，沟通顺畅，结果导向。',
        tags=item.tags,
        source_url='https://example.com/jobs/' + item.id,
        published_at=item.published_at,
        last_seen_at=NOW,
        is_new=item.is_new,
    )
    for item in JOB_ITEMS
}

DELIVERY_ITEMS = [
    DeliveryItem(
        id='delivery-1',
        job_id='job-1',
        resume_id='resume-base',
        company='字节跳动',
        position='高级前端工程师',
        status=DeliveryStatusEnum.DELIVERED,
        updated_at=NOW.replace(hour=10, minute=30),
        delivered_at=NOW.replace(hour=10, minute=30),
        note='官网投递成功，待查看。',
    ),
    DeliveryItem(
        id='delivery-2',
        job_id='job-2',
        resume_id='resume-base',
        company='阿里巴巴',
        position='全栈开发工程师',
        status=DeliveryStatusEnum.DELIVERED,
        updated_at=NOW.replace(hour=10, minute=45),
        delivered_at=NOW.replace(hour=10, minute=45),
        note='系统已同步投递结果。',
    ),
    DeliveryItem(
        id='delivery-3',
        job_id='job-3',
        resume_id='resume-base',
        company='腾讯',
        position='产品经理',
        status=DeliveryStatusEnum.DELIVERING,
        updated_at=NOW.replace(hour=11, minute=0),
        note='正在等待企业官网确认。',
    ),
    DeliveryItem(
        id='delivery-4',
        job_id='job-4',
        resume_id='resume-base',
        company='美团',
        position='UI设计师',
        status=DeliveryStatusEnum.PENDING,
        updated_at=NOW.replace(hour=11, minute=15),
        note='待优化作品集后投递。',
    ),
]

COMPANY_ITEMS = [
    CompanyReportDetail(
        id='company-1',
        name='字节跳动',
        industry='互联网科技',
        size='100,000+',
        rating=4.2,
        risk_level=CompanyRiskLevelEnum.LOW,
        growth=23,
        salary_range='45K',
        basic_profile=CompanyBasicProfile(
            registered_capital='10000 万人民币',
            established_date='2012-03-09',
            legal_representative='张某某',
            status='存续',
        ),
        risk_breakdown=CompanyRiskBreakdown(
            judicial=CompanyRiskLevelEnum.LOW,
            operational=CompanyRiskLevelEnum.LOW,
            public_opinion=CompanyRiskLevelEnum.MEDIUM,
        ),
        summary='整体风险较低，适合追求成长性的候选人。',
        risks=['加班文化较重', '竞争压力大'],
        positives=['技术栈先进', '晋升通道清晰', '年终奖丰厚'],
    ),
    CompanyReportDetail(
        id='company-2',
        name='创业公司A',
        industry='AI科技',
        size='50-200',
        rating=3.5,
        risk_level=CompanyRiskLevelEnum.HIGH,
        growth=-12,
        salary_range='35K',
        basic_profile=CompanyBasicProfile(
            registered_capital='500 万人民币',
            established_date='2021-05-18',
            legal_representative='李某某',
            status='存续',
        ),
        risk_breakdown=CompanyRiskBreakdown(
            judicial=CompanyRiskLevelEnum.MEDIUM,
            operational=CompanyRiskLevelEnum.HIGH,
            public_opinion=CompanyRiskLevelEnum.HIGH,
        ),
        summary='短期成长快，但稳定性偏弱，适合风险偏好更高的候选人。',
        risks=['资金链紧张', '近期裁员传闻', '核心人员流失'],
        positives=['期权激励', '扁平化管理'],
    ),
    CompanyReportDetail(
        id='company-3',
        name='阿里巴巴',
        industry='电子商务',
        size='250,000+',
        rating=4.0,
        risk_level=CompanyRiskLevelEnum.LOW,
        growth=8,
        salary_range='42K',
        basic_profile=CompanyBasicProfile(
            registered_capital='100000 万人民币',
            established_date='1999-06-28',
            legal_representative='吴某某',
            status='存续',
        ),
        risk_breakdown=CompanyRiskBreakdown(
            judicial=CompanyRiskLevelEnum.LOW,
            operational=CompanyRiskLevelEnum.MEDIUM,
            public_opinion=CompanyRiskLevelEnum.MEDIUM,
        ),
        summary='成熟大厂，培训体系完善，组织调整带来一定不确定性。',
        risks=['组织架构频繁调整'],
        positives=['完善的培训体系', '国际化机会多', '福利待遇好'],
    ),
]

CONTRACT_SUMMARY = ContractSummary(
    id='demo',
    file_name='offer-demo.pdf',
    status=TaskStatusEnum.DONE,
    progress=100,
    overall_score=72,
    risk_level=ContractRiskLevelEnum.WARNING,
    summary='合同主体清晰，但竞业限制和保密赔偿责任需要重点确认。',
    summary_counts=ContractSummaryCounts(safe=2, warning=1, danger=1),
    stages=[
        TaskStage(name='文档解析', status=TaskStatusEnum.DONE),
        TaskStage(name='条款识别', status=TaskStatusEnum.DONE),
        TaskStage(name='风险分析', status=TaskStatusEnum.DONE),
        TaskStage(name='生成报告', status=TaskStatusEnum.DONE),
    ],
)

CONTRACT_CLAUSES = [
    ContractClauseItem(
        id='1',
        title='薪资福利条款',
        content='基本工资为每月人民币 45,000 元，按月发放，发放日期为每月 15 日。',
        risk_level=ContractRiskLevelEnum.SAFE,
        explanation='薪资发放日期和方式明确，属于标准条款。',
    ),
    ContractClauseItem(
        id='2',
        title='竞业限制条款',
        content='员工离职后 24 个月内不得在竞争企业任职，补偿金为平均工资的 30%。',
        risk_level=ContractRiskLevelEnum.WARNING,
        explanation='竞业期限偏长，补偿比例偏低。',
        suggestion='建议协商缩短期限至 12 个月，或提高补偿比例。',
    ),
    ContractClauseItem(
        id='3',
        title='知识产权归属',
        content='员工在职期间创作的与公司业务相关成果归公司所有。',
        risk_level=ContractRiskLevelEnum.SAFE,
        explanation='标准职务成果归属约定。',
    ),
    ContractClauseItem(
        id='4',
        title='保密义务',
        content='员工应无限期保守商业秘密，违约需赔偿公司全部损失。',
        risk_level=ContractRiskLevelEnum.DANGER,
        explanation='“无限期”与“全部损失”表述对员工较不利。',
        suggestion='建议明确保密范围、期限与赔偿上限。',
    ),
]

CONTRACT_REPORT = ContractReport(
    overall_score=72,
    risk_level=ContractRiskLevelEnum.WARNING,
    summary='整体可签，但建议就竞业与保密责任补充书面确认。',
    key_risks=['竞业限制期限偏长', '保密赔偿责任边界不清'],
    recommendation='若 HR 同意对关键条款做补充说明，则整体可接受。',
)


class DemoStore:
    def __init__(self) -> None:
        self._lock = Lock()
        self._session_seq = count(1)
        self._message_seq = count(1)
        self._sessions: dict[str, ChatSessionRecord] = {}

    def dashboard(self) -> DashboardHomeData:
        return DashboardHomeData(
            greeting='下午好',
            app_name='求职高手',
            notification_count=3,
            hero_stats=DashboardHeroStats(
                new_jobs_today=24,
                match_success_rate=89,
                interview_invites=5,
            ),
            modules=[
                DashboardModuleCard(
                    module=ModuleEnum.EAGLE,
                    title='鹰眼猎手',
                    subtitle='职位追踪与匹配',
                    description='持续扫描优质岗位并做匹配度评估',
                    stats_text='今日新增 24 个高匹配岗位',
                    pending_count=8,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.PHANTOM,
                    title='幻影投递官',
                    subtitle='投递编排',
                    description='简历定制与投递状态跟踪',
                    stats_text='3 条投递正在推进',
                    pending_count=3,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.INVESTIGATOR,
                    title='深网调查员',
                    subtitle='企业尽调',
                    description='多源公开信息聚合与风险画像',
                    stats_text='2 家企业存在重点风险',
                    pending_count=2,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.GUARDIAN,
                    title='契约卫士',
                    subtitle='合同解读',
                    description='识别高风险条款并生成建议',
                    stats_text='1 份 offer 待确认条款',
                    pending_count=1,
                ),
            ],
            agent_feed=[
                DashboardAgentFeedItem(
                    module=ModuleEnum.EAGLE,
                    description='发现字节跳动新增前端岗位，与你的技能画像高度匹配。',
                    stats_text='匹配度 94%',
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.PHANTOM,
                    description='你的产品经理简历已完成一轮优化，可直接投递腾讯。',
                    stats_text='优化完成度 100%',
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.INVESTIGATOR,
                    description='创业公司A近期舆情波动较大，建议谨慎进入终面。',
                    stats_text='高风险预警',
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.GUARDIAN,
                    description='竞业限制与保密条款存在协商空间，建议补充确认。',
                    stats_text='2 处重点关注',
                ),
            ],
        )

    def list_jobs(self) -> list[JobListItem]:
        return JOB_ITEMS

    def recommendations(self) -> list[JobRecommendationItem]:
        return [
            JobRecommendationItem(
                **item.model_dump(),
                reason='与你的项目经验和目标城市偏好高度一致。',
            )
            for item in JOB_ITEMS[:3]
        ]

    def job_detail(self, job_id: str) -> JobDetail | None:
        return JOB_DETAILS.get(job_id)

    def match_job(self, job_id: str, resume_id: str) -> JobMatchData | None:
        if job_id not in JOB_DETAILS:
            return None
        detail = JOB_DETAILS[job_id]
        return JobMatchData(
            job_id=job_id,
            resume_id=resume_id,
            match_score=detail.match_score if hasattr(detail, 'match_score') else 88,
            summary=f'你的经历与 {detail.title} 的岗位要求整体匹配度较高。',
            strengths=['项目落地经验完整', '技术栈吻合', '具备跨团队协作能力'],
            gaps=['业务量化成果可再加强'],
            suggestions=['补充结果指标', '突出核心业务场景'],
        )

    def list_deliveries(self) -> list[DeliveryItem]:
        return DELIVERY_ITEMS

    def list_company_reports(self) -> list[CompanyReportListItem]:
        return [
            CompanyReportListItem(
                id=item.id,
                name=item.name,
                industry=item.industry,
                size=item.size,
                rating=item.rating,
                risk_level=item.risk_level,
                growth=item.growth,
                salary_range=item.salary_range,
                risks=item.risks,
                positives=item.positives,
                updated_at=NOW,
            )
            for item in COMPANY_ITEMS
        ]

    def company_detail(self, report_id: str) -> CompanyReportDetail | None:
        return next((item for item in COMPANY_ITEMS if item.id == report_id), None)

    def contract_summary(self, contract_id: str) -> ContractSummary | None:
        if contract_id != 'demo':
            return None
        return CONTRACT_SUMMARY

    def contract_clauses(self, contract_id: str) -> list[ContractClauseItem] | None:
        if contract_id != 'demo':
            return None
        return CONTRACT_CLAUSES

    def contract_report(self, contract_id: str) -> ContractReport | None:
        if contract_id != 'demo':
            return None
        return CONTRACT_REPORT

    def create_contract_task(self, file_name: str) -> ContractTask:
        return ContractTask(
            id='contract-task-demo',
            file_name=file_name,
            status=TaskStatusEnum.PROCESSING,
            progress=20,
            current_stage='文档解析',
            stages=[
                TaskStage(name='文档解析', status=TaskStatusEnum.PROCESSING),
                TaskStage(name='条款识别', status=TaskStatusEnum.PENDING),
                TaskStage(name='风险分析', status=TaskStatusEnum.PENDING),
            ],
        )

    def create_session(self) -> tuple[str, str]:
        with self._lock:
            session_id = f'session-{next(self._session_seq)}-{uuid4().hex[:8]}'
            welcome = (
                '你好，我是求职高手 AI 助手。'
                '告诉我你要找工作、优化投递、调查公司还是分析合同。'
            )
            session = ChatSessionRecord(
                session_id=session_id,
                welcome_message=welcome,
                created_at=datetime.now(),
            )
            session.messages.append(
                ChatMessageItem(
                    id=self._next_message_id(),
                    role=ChatRoleEnum.ASSISTANT,
                    content=welcome,
                    created_at=datetime.now(),
                )
            )
            self._sessions[session_id] = session
            return session_id, welcome

    def list_sessions(self) -> list[ChatSessionItem]:
        with self._lock:
            records = sorted(
                self._sessions.values(),
                key=lambda item: item.created_at,
                reverse=True,
            )
            return [
                ChatSessionItem(
                    session_id=record.session_id,
                    last_message_preview=(
                        record.messages[-1].content[:40] if record.messages else ''
                    ),
                    updated_at=(
                        record.messages[-1].created_at
                        if record.messages
                        else record.created_at
                    ),
                )
                for record in records
            ]

    def get_messages(self, session_id: str) -> list[ChatMessageItem] | None:
        with self._lock:
            record = self._sessions.get(session_id)
            if record is None:
                return None
            return list(record.messages)

    def add_user_message(
        self,
        session_id: str,
        content: str,
        current_module: ModuleEnum | None,
    ) -> tuple[str, ModuleEnum] | None:
        with self._lock:
            record = self._sessions.get(session_id)
            if record is None:
                return None

            detected_module = self._detect_module(content, current_module)
            created_at = datetime.now()
            user_message_id = self._next_message_id()
            record.messages.append(
                ChatMessageItem(
                    id=user_message_id,
                    role=ChatRoleEnum.USER,
                    content=content,
                    module=detected_module,
                    created_at=created_at,
                )
            )
            assistant_reply = self._build_reply(content, detected_module)
            record.messages.append(
                ChatMessageItem(
                    id=self._next_message_id(),
                    role=ChatRoleEnum.ASSISTANT,
                    content=assistant_reply,
                    module=detected_module,
                    created_at=created_at + timedelta(seconds=1),
                )
            )
            return user_message_id, detected_module

    def _next_message_id(self) -> str:
        return f'msg-{next(self._message_seq)}'

    def _detect_module(self, content: str, current_module: ModuleEnum | None) -> ModuleEnum:
        lowered = content.lower()
        if '投递' in content or '简历' in content:
            return ModuleEnum.PHANTOM
        if '调查' in content or '公司' in content or '背景' in content:
            return ModuleEnum.INVESTIGATOR
        if '合同' in content or 'offer' in lowered or '条款' in content:
            return ModuleEnum.GUARDIAN
        if '找工作' in content or '职位' in content or '招聘' in content:
            return ModuleEnum.EAGLE
        return current_module or ModuleEnum.EAGLE

    def _build_reply(self, content: str, module: ModuleEnum) -> str:
        match module:
            case ModuleEnum.PHANTOM:
                return (
                    '我已按你的目标岗位整理投递建议：'
                    '先突出结果指标，再投递优先级最高的 3 个岗位。'
                )
            case ModuleEnum.INVESTIGATOR:
                return '这家公司我建议重点核对工商变更、近半年舆情和员工评价，当前风险信号偏中高。'
            case ModuleEnum.GUARDIAN:
                return '从合同风险看，你需要优先确认竞业限制期限、赔偿边界以及试用期转正条件。'
            case _:
                return (
                    f'基于你的描述“{content[:18]}”，'
                    '我建议优先关注匹配度高、地点合适且发布时间较新的岗位。'
                )


store = DemoStore()


def paginate(items: Iterable, page: int, page_size: int) -> tuple[list, int]:
    items_list = list(items)
    total = len(items_list)
    start = (page - 1) * page_size
    end = start + page_size
    return items_list[start:end], total
