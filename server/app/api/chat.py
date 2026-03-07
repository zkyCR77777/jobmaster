from collections.abc import AsyncIterable
from typing import Annotated

from fastapi import APIRouter, Body, HTTPException, Path, Query, status
from fastapi.sse import EventSourceResponse, ServerSentEvent

from app.schemas.chat import (
    ChatMessagePage,
    ChatSendMessageData,
    ChatSendMessageRequest,
    ChatSessionCreateData,
    ChatSessionPage,
)
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/api/v1/chat", tags=["AI 对话"])


@router.post("/sessions", response_model=ApiResponse[ChatSessionCreateData])
async def create_session() -> ApiResponse[ChatSessionCreateData]:
    """创建对话会话。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/sessions", response_model=ApiResponse[ChatSessionPage])
async def list_sessions(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[ChatSessionPage]:
    """会话列表。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/sessions/{session_id}/messages", response_model=ApiResponse[ChatMessagePage])
async def chat_history(
    session_id: Annotated[str, Path(min_length=1)],
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 50,
) -> ApiResponse[ChatMessagePage]:
    """对话历史。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.post("/sessions/{session_id}/messages", response_model=ApiResponse[ChatSendMessageData])
async def create_message(
    session_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[ChatSendMessageRequest, Body()],
) -> ApiResponse[ChatSendMessageData]:
    """发送用户消息。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/sessions/{session_id}/stream", response_class=EventSourceResponse)
async def chat_stream(
    session_id: Annotated[str, Path(min_length=1)],
    message_id: Annotated[str, Query(min_length=1)],
) -> AsyncIterable[ServerSentEvent]:
    """AI 对话 SSE 流式输出。"""
    _ = (session_id, message_id)
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
