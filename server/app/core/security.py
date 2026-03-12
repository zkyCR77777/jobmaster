"""JWT 认证与密码加密"""

from datetime import datetime, timedelta

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

ALGORITHM = "HS256"


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


def create_access_token(data: dict) -> str:
    expire = datetime.now() + timedelta(minutes=settings.access_token_expire_minutes)
    return jwt.encode({**data, "exp": expire}, settings.secret_key, algorithm=ALGORITHM)


def create_refresh_token(data: dict) -> str:
    expire = datetime.now() + timedelta(days=7)
    return jwt.encode(
        {**data, "exp": expire, "token_type": "refresh"},
        settings.secret_key,
        algorithm=ALGORITHM,
    )


def decode_token(token: str) -> dict | None:
    try:
        return jwt.decode(token, settings.secret_key, algorithms=[ALGORITHM])
    except JWTError:
        return None
