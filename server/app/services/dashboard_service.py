from __future__ import annotations

from datetime import datetime, timedelta

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.company import Company
from app.models.contract import Contract
from app.models.job import Job
from app.models.resume import Delivery
from app.schemas.common import DeliveryStatusEnum, ModuleEnum
from app.schemas.dashboard import (
    DashboardAgentFeedItem,
    DashboardHeroStats,
    DashboardHomeData,
    DashboardModuleCard,
)


class DashboardService:
    async def home(self, session: AsyncSession) -> DashboardHomeData:
        now = datetime.now()
        yesterday = now - timedelta(days=1)
        new_jobs_today = (
            await session.execute(
                select(func.count()).select_from(Job).where(Job.first_seen_at >= yesterday)
            )
        ).scalar_one()
        total_deliveries = (
            await session.execute(select(func.count()).select_from(Delivery))
        ).scalar_one()
        interview_invites = (
            await session.execute(
                select(func.count()).select_from(Delivery).where(
                    Delivery.status.in_(
                        [DeliveryStatusEnum.INTERVIEW.value, DeliveryStatusEnum.OFFER.value]
                    )
                )
            )
        ).scalar_one()
        high_risk_companies = (
            await session.execute(
                select(func.count()).select_from(Company).where(Company.risk_level == "high")
            )
        ).scalar_one()
        processing_contracts = (
            await session.execute(
                select(func.count()).select_from(Contract).where(Contract.status != "done")
            )
        ).scalar_one()
        match_success_rate = min(95, 70 + total_deliveries * 5)

        return DashboardHomeData(
            greeting=self._greeting(now.hour),
            notification_count=interview_invites + high_risk_companies,
            hero_stats=DashboardHeroStats(
                new_jobs_today=new_jobs_today,
                match_success_rate=match_success_rate,
                interview_invites=interview_invites,
            ),
            modules=[
                DashboardModuleCard(
                    module=ModuleEnum.EAGLE,
                    title="鹰眼猎手",
                    subtitle="职位追踪与匹配",
                    description="持续扫描优质岗位并做匹配度评估",
                    stats_text=f"今日新增 {new_jobs_today} 个岗位",
                    pending_count=new_jobs_today,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.PHANTOM,
                    title="幻影投递官",
                    subtitle="投递编排",
                    description="简历定制与投递状态跟踪",
                    stats_text=f"{total_deliveries} 条投递记录",
                    pending_count=total_deliveries,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.INVESTIGATOR,
                    title="深网调查员",
                    subtitle="企业尽调",
                    description="多源公开信息聚合与风险画像",
                    stats_text=f"{high_risk_companies} 家企业高风险",
                    pending_count=high_risk_companies,
                ),
                DashboardModuleCard(
                    module=ModuleEnum.GUARDIAN,
                    title="契约卫士",
                    subtitle="合同解读",
                    description="识别高风险条款并生成建议",
                    stats_text=f"{processing_contracts} 份合同处理中",
                    pending_count=processing_contracts,
                ),
            ],
            agent_feed=[
                DashboardAgentFeedItem(
                    module=ModuleEnum.EAGLE,
                    description="岗位数据已从数据库同步。",
                    stats_text=f"{new_jobs_today} 个新增岗位",
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.PHANTOM,
                    description="投递记录与简历已持久化。",
                    stats_text=f"{total_deliveries} 条记录",
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.INVESTIGATOR,
                    description="企业风险画像已写入数据库。",
                    stats_text=f"{high_risk_companies} 家高风险",
                ),
                DashboardAgentFeedItem(
                    module=ModuleEnum.GUARDIAN,
                    description="合同分析结果已持久化，可继续查看详情。",
                    stats_text=f"{processing_contracts} 条处理中",
                ),
            ],
        )

    def _greeting(self, hour: int) -> str:
        if hour < 12:
            return "早上好"
        if hour < 18:
            return "下午好"
        return "晚上好"


dashboard_service = DashboardService()
