from fastapi import APIRouter

router = APIRouter(prefix="/api/auth", tags=["认证"])


@router.post("/register")
async def register():
    """用户注册"""
    ...


@router.post("/login")
async def login():
    """用户登录"""
    ...


@router.post("/refresh")
async def refresh_token():
    """刷新 Token"""
    ...
