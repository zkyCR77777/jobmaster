from __future__ import annotations

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.exceptions import AuthError
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    hash_password,
    verify_password,
)
from app.models.user import User
from app.schemas.auth import TokenData, UserProfile


class AuthService:
    async def register(
        self,
        session: AsyncSession,
        *,
        email: str,
        password: str,
        nickname: str,
    ) -> UserProfile:
        existing = await session.execute(select(User).where(User.email == email))
        if existing.scalar_one_or_none() is not None:
            raise AuthError("邮箱已注册")

        user = User(
            email=email,
            hashed_password=hash_password(password),
            nickname=nickname,
        )
        session.add(user)
        await session.commit()
        await session.refresh(user)
        return self._to_profile(user)

    async def login(self, session: AsyncSession, *, email: str, password: str) -> TokenData:
        result = await session.execute(select(User).where(User.email == email))
        user = result.scalar_one_or_none()
        if user is None or not verify_password(password, user.hashed_password):
            raise AuthError("邮箱或密码错误")
        return self._to_token(user)

    async def refresh(self, session: AsyncSession, *, refresh_token: str) -> TokenData:
        payload = decode_token(refresh_token)
        if payload is None or payload.get("token_type") != "refresh":
            raise AuthError("刷新令牌无效")
        user_id = payload.get("sub")
        if not user_id:
            raise AuthError("刷新令牌无效")
        user = await session.get(User, user_id)
        if user is None:
            raise AuthError("用户不存在")
        return self._to_token(user)

    def _to_token(self, user: User) -> TokenData:
        payload = {"sub": str(user.id), "email": user.email}
        return TokenData(
            access_token=create_access_token(payload),
            refresh_token=create_refresh_token(payload),
            expires_in=settings.access_token_expire_minutes * 60,
            user=self._to_profile(user),
        )

    def _to_profile(self, user: User) -> UserProfile:
        return UserProfile(
            id=str(user.id),
            email=user.email,
            nickname=user.nickname,
            school=user.school,
            major=user.major,
            degree=user.degree,
        )


auth_service = AuthService()
