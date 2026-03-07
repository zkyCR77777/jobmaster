from typing import Annotated

from fastapi import APIRouter, Body, HTTPException, Path, Query, status

from app.schemas.common import ApiResponse
from app.schemas.jobs import (
    JobDetail,
    JobListPage,
    JobMatchData,
    JobMatchRequest,
    JobRecommendationPage,
)

router = APIRouter(prefix="/api/v1/jobs", tags=["鹰眼猎手 — 岗位"])


@router.get("", response_model=ApiResponse[JobListPage])
async def list_jobs(
    keyword: Annotated[str | None, Query()] = None,
    company: Annotated[str | None, Query()] = None,
    location: Annotated[str | None, Query()] = None,
    tag: Annotated[list[str] | None, Query()] = None,
    min_match: Annotated[int | None, Query(ge=0, le=100)] = None,
    is_new: Annotated[bool | None, Query()] = None,
    sort: Annotated[str, Query()] = "latest_desc",
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[JobListPage]:
    """岗位列表（搜索、筛选、排序）。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/recommendations", response_model=ApiResponse[JobRecommendationPage])
async def recommend_jobs(
    limit: Annotated[int, Query(ge=1, le=50)] = 10,
) -> ApiResponse[JobRecommendationPage]:
    """基于用户画像推荐岗位。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/{job_id}", response_model=ApiResponse[JobDetail])
async def get_job(
    job_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[JobDetail]:
    """岗位详情。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/{job_id}/match", response_model=ApiResponse[JobMatchData])
async def match_job(
    job_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[JobMatchRequest, Body()],
) -> ApiResponse[JobMatchData]:
    """AI 分析岗位与用户简历的匹配度。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
