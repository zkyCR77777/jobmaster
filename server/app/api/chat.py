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
from app.services import chat_service

router = APIRouter(prefix="/api/v1/chat", tags=["AI 对话"])


@router.post("/sessions")
async def create_session() -> ApiResponse[ChatSessionCreateData]:
    """创建对话会话。"""
    return ApiResponse(data=chat_service.create_session())


@router.get("/sessions")
async def list_sessions(
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 20,
) -> ApiResponse[ChatSessionPage]:
    """会话列表。"""
    return ApiResponse(data=chat_service.list_sessions(page=page, page_size=page_size))


@router.get("/sessions/{session_id}/messages")
async def chat_history(
    session_id: Annotated[str, Path(min_length=1)],
    page: Annotated[int, Query(ge=1)] = 1,
    page_size: Annotated[int, Query(ge=1, le=100)] = 50,
) -> ApiResponse[ChatMessagePage]:
    """对话历史。"""
    return ApiResponse(
        data=chat_service.history(session_id=session_id, page=page, page_size=page_size)
    )


@router.post("/sessions/{session_id}/messages")
async def create_message(
    session_id: Annotated[str, Path(min_length=1)],
    payload: Annotated[ChatSendMessageRequest, Body()],
) -> ApiResponse[ChatSendMessageData]:
    """发送用户消息。"""
    return ApiResponse(
        data=chat_service.create_message(
            session_id=session_id,
            content=payload.content,
            current_module=payload.current_module,
        )
    )


@router.get("/sessions/{session_id}/stream", response_class=EventSourceResponse)
async def chat_stream(
    session_id: Annotated[str, Path(min_length=1)],
    message_id: Annotated[str, Query(min_length=1)],
) -> AsyncIterable[ServerSentEvent]:
    """AI 对话 SSE 流式输出。"""
    _ = (session_id, message_id)
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
