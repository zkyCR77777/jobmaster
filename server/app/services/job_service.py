from __future__ import annotations

import uuid
from datetime import datetime, timedelta

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.models.job import Job
from app.schemas.jobs import (
    JobDetail,
    JobListItem,
    JobListPage,
    JobMatchData,
    JobRecommendationItem,
    JobRecommendationPage,
)
from app.services.pagination import paginate


class JobService:
    async def list_jobs(
        self,
        session: AsyncSession,
        *,
        page: int,
        page_size: int,
        keyword: str | None = None,
        company: str | None = None,
        location: str | None = None,
        tag: list[str] | None = None,
        min_match: int | None = None,
        is_new: bool | None = None,
        sort: str = "latest_desc",
    ) -> JobListPage:
        statement = select(Job).where(Job.is_active.is_(True))
        if keyword:
            fuzzy = f"%{keyword.strip()}%"
            statement = statement.where(
                or_(
                    Job.title.ilike(fuzzy),
                    Job.company.ilike(fuzzy),
                    Job.description.ilike(fuzzy),
                    Job.requirements.ilike(fuzzy),
                )
            )
        if company:
            statement = statement.where(Job.company.ilike(f"%{company.strip()}%"))
        if location:
            statement = statement.where(Job.location.ilike(f"%{location.strip()}%"))

        records = list((await session.execute(statement)).scalars())
        items = [self._to_list_item(job) for job in records]

        if tag:
            tags = {item.strip() for item in tag if item.strip()}
            items = [item for item in items if tags.intersection(item.tags)]
        if min_match is not None:
            items = [item for item in items if item.match_score >= min_match]
        if is_new is not None:
            items = [item for item in items if item.is_new is is_new]

        items = self._sort_list_items(items, sort)
        paged, total = paginate(items, page, page_size)
        return JobListPage(items=paged, page=page, page_size=page_size, total=total)

    async def recommendations(
        self,
        session: AsyncSession,
        *,
        page: int,
        page_size: int,
    ) -> JobRecommendationPage:
        records = list(
            (
                await session.execute(select(Job).where(Job.is_active.is_(True)))
            ).scalars()
        )
        items = [
            JobRecommendationItem(
                **self._to_list_item(job).model_dump(),
                reason=self._recommendation_reason(job),
            )
            for job in records
        ]
        items.sort(
            key=lambda item: (
                item.match_score,
                item.published_at or datetime.min,
            ),
            reverse=True,
        )
        paged, total = paginate(items, page, page_size)
        return JobRecommendationPage(items=paged, page=page, page_size=page_size, total=total)

    async def detail(self, session: AsyncSession, job_id: str) -> JobDetail:
        job = await session.get(Job, self._parse_job_id(job_id))
        if job is None or not job.is_active:
            raise NotFoundError("岗位不存在")
        return self._to_detail(job)

    async def match(self, session: AsyncSession, job_id: str, resume_id: str) -> JobMatchData:
        job = await session.get(Job, self._parse_job_id(job_id))
        if job is None or not job.is_active:
            raise NotFoundError("岗位不存在")
        match_score = self._estimate_match_score(job)
        return JobMatchData(
            job_id=str(job.id),
            resume_id=resume_id,
            match_score=match_score,
            summary=f"你的经历与 {job.title} 的核心要求整体匹配度较高。",
            strengths=[
                f"{job.company} 当前岗位技术栈与你的目标方向一致",
                "岗位职责清晰，适合整理项目成果后重点投递",
                "岗位发布时间较近，仍处于优先跟进窗口",
            ],
            gaps=["缺少更明确的量化成果描述"],
            suggestions=["补充项目结果指标", "突出与岗位标签最相关的项目经验"],
        )

    def _parse_job_id(self, job_id: str) -> uuid.UUID:
        return uuid.UUID(job_id)

    def _to_list_item(self, job: Job) -> JobListItem:
        return JobListItem(
            id=str(job.id),
            title=job.title,
            company=job.company,
            location=job.location,
            salary_range=job.salary_range,
            match_score=self._estimate_match_score(job),
            is_new=self._is_new(job),
            published_at=job.first_seen_at,
            tags=job.tags,
        )

    def _to_detail(self, job: Job) -> JobDetail:
        return JobDetail(
            id=str(job.id),
            title=job.title,
            company=job.company,
            company_url=job.company_url,
            location=job.location,
            salary_range=job.salary_range,
            job_type=job.job_type,
            description=job.description,
            requirements=job.requirements,
            tags=job.tags,
            source_url=job.source_url,
            published_at=job.first_seen_at,
            last_seen_at=job.last_seen_at,
            is_new=self._is_new(job),
        )

    def _estimate_match_score(self, job: Job) -> int:
        score = 72
        hot_tags = {"React", "TypeScript", "Java", "Spring", "Vue", "B端", "SaaS"}
        score += min(len(set(job.tags).intersection(hot_tags)) * 6, 18)
        if "高级" in job.title:
            score += 6
        if any(city in job.location for city in ("北京", "杭州", "深圳", "上海")):
            score += 4
        return min(score, 98)

    def _is_new(self, job: Job) -> bool:
        return job.first_seen_at >= datetime.now() - timedelta(days=3)

    def _recommendation_reason(self, job: Job) -> str:
        if any(tag in {"React", "TypeScript", "Vue"} for tag in job.tags):
            return "技术栈匹配度高，适合作为优先投递目标。"
        if any(tag in {"B端", "SaaS"} for tag in job.tags):
            return "岗位方向明确，适合有业务理解和需求分析能力的候选人。"
        return "岗位画像完整，建议纳入本周重点跟进清单。"

    def _sort_list_items(self, items: list[JobListItem], sort: str) -> list[JobListItem]:
        match sort:
            case "latest_asc":
                return sorted(items, key=lambda item: item.published_at or datetime.min)
            case "company_asc":
                return sorted(items, key=lambda item: (item.company, item.title))
            case "match_desc":
                return sorted(
                    items,
                    key=lambda item: (item.match_score, item.published_at or datetime.min),
                    reverse=True,
                )
            case _:
                return sorted(
                    items,
                    key=lambda item: item.published_at or datetime.min,
                    reverse=True,
                )


job_service = JobService()
