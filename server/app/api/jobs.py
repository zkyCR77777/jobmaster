from typing import Annotated

from fastapi import APIRouter, Body, Path, Query

from app.schemas.common import ApiResponse
from app.schemas.jobs import (
    JobDetail,
    JobListPage,
    JobMatchData,
    JobMatchRequest,
    JobRecommendationPage,
)
from app.services import job_service

router = APIRouter(prefix="/api/v1/jobs", tags=["鹰眼猎手 — 岗位"])


@router.get("")
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
    _ = (keyword, company, location, tag, min_match, is_new, sort)
    return ApiResponse(data=job_service.list_jobs(page=page, page_size=page_size))


@router.get("/recommendations")
async def recommend_jobs(
    limit: Annotated[int, Query(ge=1, le=50)] = 10,
) -> ApiResponse[JobRecommendationPage]:
    """基于用户画像推荐岗位。"""
    return ApiResponse(data=job_service.recommendations(page=1, page_size=limit))


@router.get("/{job_id}")
async def get_job(
    job_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[JobDetail]:
    """岗位详情。"""
    return ApiResponse(data=job_service.detail(job_id))


@router.post("/{job_id}/match")
async def match_job(
    job_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[JobMatchRequest, Body()],
) -> ApiResponse[JobMatchData]:
    """AI 分析岗位与用户简历的匹配度。"""
    return ApiResponse(data=job_service.match(job_id, payload.resume_id))
