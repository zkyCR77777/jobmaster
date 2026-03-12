from typing import Annotated

from fastapi import APIRouter, Body

from app.core.deps import SessionDep
from app.schemas.auth import (
    LoginRequest,
    RefreshTokenRequest,
    RegisterRequest,
    TokenData,
    UserProfile,
)
from app.schemas.common import ApiResponse
from app.services import auth_service

router = APIRouter(prefix="/api/v1/auth", tags=["认证"])


@router.post("/register")
async def register(
    session: SessionDep,
    payload: Annotated[RegisterRequest, Body()],
) -> ApiResponse[UserProfile]:
    """用户注册。"""
    return ApiResponse(
        data=await auth_service.register(
            session,
            email=payload.email,
            password=payload.password,
            nickname=payload.nickname,
        )
    )


@router.post("/login")
async def login(
    session: SessionDep,
    payload: Annotated[LoginRequest, Body()],
) -> ApiResponse[TokenData]:
    """用户登录。"""
    return ApiResponse(
        data=await auth_service.login(session, email=payload.email, password=payload.password)
    )


@router.post("/refresh")
async def refresh_token(
    session: SessionDep,
    payload: Annotated[RefreshTokenRequest, Body()],
) -> ApiResponse[TokenData]:
    """刷新 Token。"""
    return ApiResponse(data=await auth_service.refresh(session, refresh_token=payload.refresh_token))
