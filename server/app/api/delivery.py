from typing import Annotated

from fastapi import APIRouter, Body, File, Form, Path, Query, UploadFile

from app.core.deps import SessionDep
from app.schemas.common import ApiResponse
from app.schemas.delivery import (
    DeliveryCreateRequest,
    DeliveryItem,
    DeliveryPage,
    DeliveryStats,
    DeliveryUpdateRequest,
    ResumeCustomizationCreateRequest,
    ResumeCustomizationTask,
    ResumeListPage,
    ResumeUploadData,
)
from app.services import delivery_service

router = APIRouter(prefix="/api/v1", tags=["幻影投递官 — 投递助手"])


@router.post("/resumes")
async def upload_resume(
    session: SessionDep,
    file: Annotated[UploadFile, File(description="基础简历文件")],
    title: Annotated[str | None, Form()] = None,
    is_base: Annotated[bool, Form()] = True,
) -> ApiResponse[ResumeUploadData]:
    """上传基础简历。"""
    return ApiResponse(
        data=await delivery_service.upload_resume(
            session,
            file_name=file.filename or "resume.pdf",
            file_bytes=await file.read(),
            title=title,
            is_base=is_base,
        )
    )


@router.get("/resumes")
async def list_resumes(
    session: SessionDep,
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[ResumeListPage]:
    """获取简历列表。"""
    return ApiResponse(data=await delivery_service.list_resumes(session, page=page, page_size=page_size))


@router.post("/resumes/{resume_id}/customizations")
async def customize_resume(
    session: SessionDep,
    resume_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[ResumeCustomizationCreateRequest, Body()],
) -> ApiResponse[ResumeCustomizationTask]:
    """创建定制简历任务。"""
    return ApiResponse(
        data=await delivery_service.create_customization(
            session,
            resume_id=resume_id,
            job_id=payload.job_id,
        )
    )


@router.get("/resume-customizations/{customization_id}")
async def get_resume_customization(
    session: SessionDep,
    customization_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ResumeCustomizationTask]:
    """获取定制简历任务详情。"""
    return ApiResponse(data=await delivery_service.customization_detail(session, customization_id))


@router.get("/deliveries")
async def list_delivery_queue(
    session: SessionDep,
    status_filter: Annotated[str | None, Query(alias="status")] = None,
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[DeliveryPage]:
    """投递清单。"""
    return ApiResponse(
        data=await delivery_service.list_deliveries(
            session=session,
            page=page,
            page_size=page_size,
            status_filter=status_filter,
        )
    )


@router.post("/deliveries")
async def create_delivery(
    session: SessionDep,
    payload: Annotated[DeliveryCreateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """新建投递记录。"""
    return ApiResponse(
        data=await delivery_service.create_delivery(
            session,
            job_id=payload.job_id,
            resume_id=payload.resume_id,
            note=payload.note,
        )
    )


@router.patch("/deliveries/{delivery_id}")
async def update_delivery(
    session: SessionDep,
    delivery_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[DeliveryUpdateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """更新投递状态。"""
    return ApiResponse(
        data=await delivery_service.update_delivery(
            session,
            delivery_id=delivery_id,
            status=payload.status,
            note=payload.note,
        )
    )


@router.get("/deliveries/stats")
async def delivery_stats(session: SessionDep) -> ApiResponse[DeliveryStats]:
    """投递统计面板。"""
    return ApiResponse(data=await delivery_service.stats(session))
