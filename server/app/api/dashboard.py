from fastapi import APIRouter, HTTPException, status

from app.schemas.common import ApiResponse
from app.schemas.dashboard import DashboardHomeData

router = APIRouter(prefix="/api/v1/dashboard", tags=["首页总览"])


@router.get("/home", response_model=ApiResponse[DashboardHomeData])
async def get_home_dashboard() -> ApiResponse[DashboardHomeData]:
    """首页聚合数据。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
