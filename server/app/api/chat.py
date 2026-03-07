from collections.abc import AsyncIterable

from fastapi import APIRouter
from fastapi.sse import EventSourceResponse, ServerSentEvent

router = APIRouter(prefix="/api/chat", tags=["AI 对话"])


@router.post("/", response_class=EventSourceResponse)
async def chat() -> AsyncIterable[ServerSentEvent]:
    """AI 对话（SSE 流式输出，上下文感知分发到对应模块）"""
    ...


@router.get("/history")
async def chat_history():
    """对话历史"""
    ...
