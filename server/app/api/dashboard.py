from fastapi import APIRouter

from app.core.deps import SessionDep
from app.schemas.common import ApiResponse
from app.schemas.dashboard import DashboardHomeData
from app.services import dashboard_service

router = APIRouter(prefix="/api/v1/dashboard", tags=["首页总览"])


@router.get("/home")
async def get_home_dashboard(session: SessionDep) -> ApiResponse[DashboardHomeData]:
    """首页聚合数据。"""
    return ApiResponse(data=await dashboard_service.home(session))
