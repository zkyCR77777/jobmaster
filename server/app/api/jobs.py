from fastapi import APIRouter

router = APIRouter(prefix="/api/jobs", tags=["鹰眼猎手 — 岗位"])


@router.get("/")
async def list_jobs():
    """岗位列表（搜索、筛选、匹配度排序）"""
    ...


@router.get("/{job_id}")
async def get_job(job_id: str):
    """岗位详情"""
    ...


@router.get("/{job_id}/match")
async def match_job(job_id: str):
    """AI 分析岗位与用户简历的匹配度"""
    ...


@router.get("/recommend/")
async def recommend_jobs():
    """基于用户画像推荐岗位"""
    ...
