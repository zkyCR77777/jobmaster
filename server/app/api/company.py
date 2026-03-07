from typing import Annotated

from fastapi import APIRouter, Body, HTTPException, Path, Query, status

from app.schemas.common import ApiResponse
from app.schemas.company import (
    CompanyReportCreateRequest,
    CompanyReportDetail,
    CompanyReportPage,
    CompanyReportTask,
)

router = APIRouter(prefix="/api/v1/company-reports", tags=["深网调查员 — 企业分析"])


@router.post("", response_model=ApiResponse[CompanyReportTask])
async def create_company_report(
    payload: Annotated[CompanyReportCreateRequest, Body()],
) -> ApiResponse[CompanyReportTask]:
    """创建企业分析任务。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("", response_model=ApiResponse[CompanyReportPage])
async def list_company_reports(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[CompanyReportPage]:
    """企业分析列表。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/{report_id}", response_model=ApiResponse[CompanyReportDetail])
async def get_company_report(
    report_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[CompanyReportDetail]:
    """企业分析详情。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
