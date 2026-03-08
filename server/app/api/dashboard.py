from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.dashboard import DashboardHomeData
from app.services.demo_store import store

router = APIRouter(prefix="/api/v1/dashboard", tags=["首页总览"])


@router.get("/home")
async def get_home_dashboard() -> ApiResponse[DashboardHomeData]:
    """首页聚合数据。"""
    return ApiResponse(data=store.dashboard())
