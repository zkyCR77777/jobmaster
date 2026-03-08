from typing import Annotated

from fastapi import APIRouter, Body, File, Form, Path, Query, UploadFile

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
    file: Annotated[UploadFile, File(description="基础简历文件")],
    title: Annotated[str | None, Form()] = None,
    is_base: Annotated[bool, Form()] = True,
) -> ApiResponse[ResumeUploadData]:
    """上传基础简历。"""
    _ = (file, title, is_base)
    return ApiResponse(
        data=ResumeUploadData(
            id="resume-base",
            title=title or "基础简历",
            file_name=file.filename or "resume.pdf",
            status="processing",
            parsed_summary="简历已上传，正在解析。",
        )
    )


@router.get("/resumes")
async def list_resumes(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[ResumeListPage]:
    """获取简历列表。"""
    return ApiResponse(data=ResumeListPage(page=page, page_size=page_size, total=0, items=[]))


@router.post("/resumes/{resume_id}/customizations")
async def customize_resume(
    resume_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[ResumeCustomizationCreateRequest, Body()],
) -> ApiResponse[ResumeCustomizationTask]:
    """创建定制简历任务。"""
    _ = (resume_id, payload)
    return ApiResponse(
        data=ResumeCustomizationTask(
            id="resume-customization-demo",
            resume_id=resume_id,
            job_id=payload.job_id,
            status="processing",
            progress=35,
            current_stage="提取岗位要求",
            stages=[],
        )
    )


@router.get("/resume-customizations/{customization_id}")
async def get_resume_customization(
    customization_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ResumeCustomizationTask]:
    """获取定制简历任务详情。"""
    _ = customization_id
    return ApiResponse(
        data=ResumeCustomizationTask(
            id=customization_id,
            resume_id="resume-base",
            job_id="job-1",
            status="done",
            progress=100,
            current_stage="生成完成",
            stages=[],
        )
    )


@router.get("/deliveries")
async def list_delivery_queue(
    status_filter: Annotated[str | None, Query(alias="status")] = None,
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[DeliveryPage]:
    """投递清单。"""
    return ApiResponse(
        data=delivery_service.list_deliveries(
            page=page,
            page_size=page_size,
            status_filter=status_filter,
        )
    )


@router.post("/deliveries")
async def create_delivery(
    payload: Annotated[DeliveryCreateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """新建投递记录。"""
    return ApiResponse(
        data=DeliveryItem(
            id="delivery-created-demo",
            job_id=payload.job_id,
            resume_id=payload.resume_id,
            company="待确认企业",
            position="待确认岗位",
            status="pending",
            note=payload.note,
        )
    )


@router.patch("/deliveries/{delivery_id}")
async def update_delivery(
    delivery_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[DeliveryUpdateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """更新投递状态。"""
    return ApiResponse(
        data=DeliveryItem(
            id=delivery_id,
            job_id="job-1",
            resume_id="resume-base",
            company="字节跳动",
            position="高级前端工程师",
            status=payload.status,
            note=payload.note,
        )
    )


@router.get("/deliveries/stats")
async def delivery_stats() -> ApiResponse[DeliveryStats]:
    """投递统计面板。"""
    return ApiResponse(data=delivery_service.stats())
