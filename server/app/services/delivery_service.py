from __future__ import annotations

import uuid
from pathlib import Path

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.database import DEFAULT_USER_ID
from app.models.job import Job
from app.models.resume import Delivery, Resume, ResumeCustomizationTask
from app.schemas.common import TaskStage, TaskStatusEnum
from app.schemas.delivery import (
    DeliveryItem,
    DeliveryPage,
    DeliveryStats,
    ResumeCustomizationTask as ResumeCustomizationTaskSchema,
    ResumeItem,
    ResumeListPage,
    ResumeUploadData,
)
from app.services.pagination import paginate


class DeliveryService:
    async def upload_resume(
        self,
        session: AsyncSession,
        *,
        file_name: str,
        file_bytes: bytes,
        title: str | None,
        is_base: bool,
    ) -> ResumeUploadData:
        stored_path = self._store_resume_file(file_name, file_bytes)
        resume = Resume(
            user_id=DEFAULT_USER_ID,
            title=title or Path(file_name).stem or "基础简历",
            file_path=str(stored_path),
            content_text=f"已接收简历文件：{file_name}",
            is_base=is_base,
        )
        session.add(resume)
        await session.commit()
        await session.refresh(resume)
        return ResumeUploadData(
            id=str(resume.id),
            title=resume.title,
            file_name=file_name,
            status=TaskStatusEnum.DONE,
            parsed_summary=resume.content_text,
        )

    async def list_resumes(self, session: AsyncSession, *, page: int, page_size: int) -> ResumeListPage:
        records = list(
            (await session.execute(select(Resume).where(Resume.user_id == DEFAULT_USER_ID))).scalars()
        )
        items = [
            ResumeItem(
                id=str(item.id),
                title=item.title,
                file_name=Path(item.file_path).name,
                is_base=item.is_base,
                target_job_id=str(item.target_job_id) if item.target_job_id else None,
                created_at=item.created_at,
            )
            for item in records
        ]
        paged, total = paginate(items, page, page_size)
        return ResumeListPage(items=paged, page=page, page_size=page_size, total=total)

    async def create_customization(
        self,
        session: AsyncSession,
        *,
        resume_id: str,
        job_id: str,
    ) -> ResumeCustomizationTaskSchema:
        task = ResumeCustomizationTask(
            resume_id=uuid.UUID(resume_id),
            job_id=uuid.UUID(job_id),
            status=TaskStatusEnum.DONE.value,
            progress=100,
            current_stage="生成完成",
            stages=[
                {"name": "提取岗位要求", "status": "done"},
                {"name": "优化项目措辞", "status": "done"},
                {"name": "生成定制版本", "status": "done"},
            ],
        )
        session.add(task)
        await session.commit()
        await session.refresh(task)
        return self._to_customization(task)

    async def customization_detail(
        self,
        session: AsyncSession,
        customization_id: str,
    ) -> ResumeCustomizationTaskSchema:
        task = await session.get(ResumeCustomizationTask, uuid.UUID(customization_id))
        if task is None:
            raise NotFoundError("定制简历任务不存在")
        return self._to_customization(task)

    async def list_deliveries(
        self,
        session: AsyncSession,
        page: int,
        page_size: int,
        status_filter: str | None = None,
    ) -> DeliveryPage:
        deliveries = list(
            (await session.execute(select(Delivery).where(Delivery.user_id == DEFAULT_USER_ID))).scalars()
        )
        job_ids = {item.job_id for item in deliveries}
        jobs = list((await session.execute(select(Job).where(Job.id.in_(job_ids)))).scalars()) if job_ids else []
        job_map = {job.id: job for job in jobs}
        items = [self._to_delivery_item(item, job_map.get(item.job_id)) for item in deliveries]
        if status_filter:
            items = [item for item in items if item.status == status_filter]
        paged, total = paginate(items, page, page_size)
        return DeliveryPage(items=paged, page=page, page_size=page_size, total=total)

    async def create_delivery(
        self,
        session: AsyncSession,
        *,
        job_id: str,
        resume_id: str,
        note: str | None,
    ) -> DeliveryItem:
        job = await session.get(Job, uuid.UUID(job_id))
        if job is None:
            raise NotFoundError("岗位不存在")
        delivery = Delivery(
            user_id=DEFAULT_USER_ID,
            job_id=job.id,
            resume_id=uuid.UUID(resume_id),
            status="pending",
            note=note or "",
        )
        session.add(delivery)
        await session.commit()
        await session.refresh(delivery)
        return self._to_delivery_item(delivery, job)

    async def update_delivery(
        self,
        session: AsyncSession,
        *,
        delivery_id: str,
        status: str,
        note: str | None,
    ) -> DeliveryItem:
        delivery = await session.get(Delivery, uuid.UUID(delivery_id))
        if delivery is None:
            raise NotFoundError("投递记录不存在")
        delivery.status = status
        delivery.note = note or ""
        session.add(delivery)
        await session.commit()
        await session.refresh(delivery)
        job = await session.get(Job, delivery.job_id)
        return self._to_delivery_item(delivery, job)

    async def stats(self, session: AsyncSession) -> DeliveryStats:
        items = list(
            (await session.execute(select(Delivery).where(Delivery.user_id == DEFAULT_USER_ID))).scalars()
        )
        counter = {item.status: 0 for item in items}
        for item in items:
            counter[item.status] = counter.get(item.status, 0) + 1
        return DeliveryStats(
            total=len(items),
            pending=counter.get("pending", 0),
            delivering=counter.get("delivering", 0),
            delivered=counter.get("delivered", 0),
            viewed=counter.get("viewed", 0),
            written_test=counter.get("written_test", 0),
            interview=counter.get("interview", 0),
            offer=counter.get("offer", 0),
            rejected=counter.get("rejected", 0),
        )

    def _to_customization(self, task: ResumeCustomizationTask) -> ResumeCustomizationTaskSchema:
        return ResumeCustomizationTaskSchema(
            id=str(task.id),
            resume_id=str(task.resume_id),
            job_id=str(task.job_id),
            status=task.status,
            progress=task.progress,
            current_stage=task.current_stage,
            stages=[
                TaskStage(name=str(stage.get("name", "")), status=str(stage.get("status", "pending")))
                for stage in task.stages
            ],
        )

    def _to_delivery_item(self, delivery: Delivery, job: Job | None) -> DeliveryItem:
        return DeliveryItem(
            id=str(delivery.id),
            job_id=str(delivery.job_id),
            resume_id=str(delivery.resume_id),
            company=job.company if job else "未知企业",
            position=job.title if job else "未知岗位",
            status=delivery.status,
            updated_at=delivery.updated_at,
            delivered_at=delivery.delivered_at,
            note=delivery.note or None,
        )

    def _store_resume_file(self, file_name: str, file_bytes: bytes) -> Path:
        safe_name = self._safe_file_name(file_name, default_name="resume.pdf")
        stored_path = Path("uploads/resumes") / f"{uuid.uuid4().hex}-{safe_name}"
        stored_path.write_bytes(file_bytes)
        return stored_path

    def _safe_file_name(self, file_name: str, *, default_name: str) -> str:
        candidate = Path(file_name).name.strip()
        if not candidate:
            return default_name
        return "".join(char if char.isalnum() or char in {".", "-", "_"} else "_" for char in candidate)


delivery_service = DeliveryService()
