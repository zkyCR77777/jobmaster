from typing import Annotated

from fastapi import APIRouter, Body, HTTPException, status

from app.schemas.auth import (
    LoginRequest,
    RefreshTokenRequest,
    RegisterRequest,
    TokenData,
    UserProfile,
)
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/api/v1/auth", tags=["认证"])


@router.post("/register")
async def register(payload: Annotated[RegisterRequest, Body()]) -> ApiResponse[UserProfile]:
    """用户注册。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/login")
async def login(payload: Annotated[LoginRequest, Body()]) -> ApiResponse[TokenData]:
    """用户登录。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/refresh")
async def refresh_token(payload: Annotated[RefreshTokenRequest, Body()]) -> ApiResponse[TokenData]:
    """刷新 Token。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
