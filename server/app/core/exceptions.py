"""统一异常处理"""

from fastapi import HTTPException, status


class NotFoundError(HTTPException):
    def __init__(self, detail: str = "资源不存在"):
        super().__init__(status_code=status.HTTP_404_NOT_FOUND, detail=detail)


class AuthError(HTTPException):
    def __init__(self, detail: str = "认证失败"):
        super().__init__(status_code=status.HTTP_401_UNAUTHORIZED, detail=detail)
