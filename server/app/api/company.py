from typing import Annotated

from fastapi import APIRouter, Body, Path, Query

from app.schemas.common import ApiResponse
from app.schemas.company import (
    CompanyReportCreateRequest,
    CompanyReportDetail,
    CompanyReportPage,
    CompanyReportTask,
)
from app.services import company_service

router = APIRouter(prefix="/api/v1/company-reports", tags=["深网调查员 — 企业分析"])


@router.post("")
async def create_company_report(
    payload: Annotated[CompanyReportCreateRequest, Body()],
) -> ApiResponse[CompanyReportTask]:
    """创建企业分析任务。"""
    return ApiResponse(
        data=CompanyReportTask(
            id="company-task-demo",
            company_name=payload.company_name,
            status="processing",
            progress=40,
            current_stage="聚合工商信息",
            sources=["工商信息", "舆情分析", "员工评价"],
            stages=[],
        )
    )


@router.get("")
async def list_company_reports(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[CompanyReportPage]:
    """企业分析列表。"""
    return ApiResponse(data=company_service.list_reports(page=page, page_size=page_size))


@router.get("/{report_id}")
async def get_company_report(
    report_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[CompanyReportDetail]:
    """企业分析详情。"""
    return ApiResponse(data=company_service.detail(report_id))
