from typing import Annotated

from fastapi import APIRouter, Body, File, Form, HTTPException, Path, Query, UploadFile, status

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

router = APIRouter(prefix="/api/v1", tags=["幻影投递官 — 投递助手"])


@router.post("/resumes", response_model=ApiResponse[ResumeUploadData])
async def upload_resume(
    file: Annotated[UploadFile, File(description="基础简历文件")],
    title: Annotated[str | None, Form()] = None,
    is_base: Annotated[bool, Form()] = True,
) -> ApiResponse[ResumeUploadData]:
    """上传基础简历。"""
    _ = (file, title, is_base)
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/resumes", response_model=ApiResponse[ResumeListPage])
async def list_resumes(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[ResumeListPage]:
    """获取简历列表。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/resumes/{resume_id}/customizations", response_model=ApiResponse[ResumeCustomizationTask])
async def customize_resume(
    resume_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[ResumeCustomizationCreateRequest, Body()],
) -> ApiResponse[ResumeCustomizationTask]:
    """创建定制简历任务。"""
    _ = (resume_id, payload)
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/resume-customizations/{customization_id}", response_model=ApiResponse[ResumeCustomizationTask])
async def get_resume_customization(
    customization_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ResumeCustomizationTask]:
    """获取定制简历任务详情。"""
    _ = customization_id
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/deliveries", response_model=ApiResponse[DeliveryPage])
async def list_delivery_queue(
    status_filter: Annotated[str | None, Query(alias="status")] = None,
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[DeliveryPage]:
    """投递清单。"""
    _ = status_filter
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/deliveries", response_model=ApiResponse[DeliveryItem])
async def create_delivery(
    payload: Annotated[DeliveryCreateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """新建投递记录。"""
    _ = payload
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.patch("/deliveries/{delivery_id}", response_model=ApiResponse[DeliveryItem])
async def update_delivery(
    delivery_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[DeliveryUpdateRequest, Body()],
) -> ApiResponse[DeliveryItem]:
    """更新投递状态。"""
    _ = (delivery_id, payload)
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/deliveries/stats", response_model=ApiResponse[DeliveryStats])
async def delivery_stats() -> ApiResponse[DeliveryStats]:
    """投递统计面板。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
