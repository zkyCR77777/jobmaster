from pydantic import Field

from app.schemas.common import SchemaModel


class UserProfile(SchemaModel):
    id: str
    email: str
    nickname: str = ""
    school: str = ""
    major: str = ""
    degree: str = ""


class RegisterRequest(SchemaModel):
    email: str
    password: str = Field(min_length=8)
    nickname: str = ""


class LoginRequest(SchemaModel):
    email: str
    password: str = Field(min_length=8)


class RefreshTokenRequest(SchemaModel):
    refresh_token: str


class TokenData(SchemaModel):
    access_token: str
    refresh_token: str | None = None
    token_type: str = "bearer"
    expires_in: int = Field(default=3600, ge=1)
    user: UserProfile
