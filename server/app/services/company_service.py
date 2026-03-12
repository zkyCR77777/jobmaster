from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.models.company import Company
from app.schemas.common import CompanyRiskLevelEnum
from app.schemas.company import (
    CompanyBasicProfile,
    CompanyReportDetail,
    CompanyReportListItem,
    CompanyReportPage,
    CompanyRiskBreakdown,
)
from app.schemas.common import TaskStatusEnum
from app.schemas.company import CompanyReportTask
from app.services.pagination import paginate


class CompanyService:
    async def create_report(self, session: AsyncSession, *, company_name: str) -> CompanyReportTask:
        existing = await session.execute(select(Company).where(Company.name == company_name))
        company = existing.scalar_one_or_none()
        if company is None:
            company = Company(
                name=company_name,
                industry="待补充",
                company_size="未知",
                status="待核验",
                risk_level="medium",
                risk_summary="已创建企业分析任务，待补充更多公开信息。",
                tianyancha_data={
                    "rating": 0.0,
                    "growth": 0,
                    "salary_range": "",
                    "risks": [],
                    "positives": [],
                    "risk_breakdown": {
                        "judicial": "medium",
                        "operational": "medium",
                        "public_opinion": "medium",
                    },
                },
            )
            session.add(company)
            await session.commit()
            await session.refresh(company)
        return CompanyReportTask(
            id=str(company.id),
            company_name=company.name,
            status=TaskStatusEnum.DONE,
            progress=100,
            current_stage="报告生成完成",
            sources=["工商信息", "舆情分析", "员工评价"],
            stages=[],
        )

    async def list_reports(
        self,
        session: AsyncSession,
        *,
        page: int,
        page_size: int,
    ) -> CompanyReportPage:
        records = list((await session.execute(select(Company))).scalars())
        items = [self._to_list_item(company) for company in records]
        items.sort(key=lambda item: item.updated_at or datetime.min, reverse=True)
        paged, total = paginate(items, page, page_size)
        return CompanyReportPage(items=paged, page=page, page_size=page_size, total=total)

    async def detail(self, session: AsyncSession, report_id: str) -> CompanyReportDetail:
        company = await session.get(Company, self._parse_report_id(report_id))
        if company is None:
            raise NotFoundError("企业报告不存在")
        return self._to_detail(company)

    def _parse_report_id(self, report_id: str) -> uuid.UUID:
        return uuid.UUID(report_id)

    def _to_list_item(self, company: Company) -> CompanyReportListItem:
        payload = company.tianyancha_data or {}
        return CompanyReportListItem(
            id=str(company.id),
            name=company.name,
            industry=company.industry,
            size=company.company_size,
            rating=float(payload.get("rating", 0.0)),
            risk_level=self._risk_level(company.risk_level),
            growth=int(payload.get("growth", 0)),
            salary_range=str(payload.get("salary_range", "")),
            risks=self._string_list(payload.get("risks")),
            positives=self._string_list(payload.get("positives")),
            updated_at=company.updated_at,
        )

    def _to_detail(self, company: Company) -> CompanyReportDetail:
        payload = company.tianyancha_data or {}
        breakdown = payload.get("risk_breakdown") or {}
        return CompanyReportDetail(
            id=str(company.id),
            name=company.name,
            industry=company.industry,
            size=company.company_size,
            rating=float(payload.get("rating", 0.0)),
            risk_level=self._risk_level(company.risk_level),
            growth=int(payload.get("growth", 0)),
            salary_range=str(payload.get("salary_range", "")),
            basic_profile=CompanyBasicProfile(
                registered_capital=company.registered_capital,
                established_date=company.established_date,
                legal_representative=company.legal_representative,
                status=company.status,
            ),
            risk_breakdown=CompanyRiskBreakdown(
                judicial=self._risk_level(str(breakdown.get("judicial", "medium"))),
                operational=self._risk_level(str(breakdown.get("operational", "medium"))),
                public_opinion=self._risk_level(str(breakdown.get("public_opinion", "medium"))),
            ),
            summary=company.risk_summary,
            risks=self._string_list(payload.get("risks")),
            positives=self._string_list(payload.get("positives")),
        )

    def _risk_level(self, value: str) -> CompanyRiskLevelEnum:
        match value:
            case "low":
                return CompanyRiskLevelEnum.LOW
            case "high":
                return CompanyRiskLevelEnum.HIGH
            case _:
                return CompanyRiskLevelEnum.MEDIUM

    def _string_list(self, value: object) -> list[str]:
        if not isinstance(value, list):
            return []
        return [str(item) for item in value]


company_service = CompanyService()
