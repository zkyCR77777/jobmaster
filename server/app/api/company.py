from typing import Annotated

from fastapi import APIRouter, Body, Path, Query

from app.core.deps import SessionDep
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
    session: SessionDep,
    payload: Annotated[CompanyReportCreateRequest, Body()],
) -> ApiResponse[CompanyReportTask]:
    """创建企业分析任务。"""
    return ApiResponse(data=await company_service.create_report(session, company_name=payload.company_name))


@router.get("")
async def list_company_reports(
    session: SessionDep,
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[CompanyReportPage]:
    """企业分析列表。"""
    return ApiResponse(
        data=await company_service.list_reports(session, page=page, page_size=page_size)
    )


@router.get("/{report_id}")
async def get_company_report(
    session: SessionDep,
    report_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[CompanyReportDetail]:
    """企业分析详情。"""
    return ApiResponse(data=await company_service.detail(session, report_id))
