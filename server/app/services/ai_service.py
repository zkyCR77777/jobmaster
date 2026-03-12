from __future__ import annotations

import uuid
from collections.abc import AsyncIterable
from datetime import datetime

from fastapi.sse import ServerSentEvent
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.models.chat import ChatMessage, ChatSession
from app.schemas.chat import (
    ChatMessageItem,
    ChatMessagePage,
    ChatSendMessageData,
    ChatSessionCreateData,
    ChatSessionItem,
    ChatSessionPage,
)
from app.schemas.common import ChatRoleEnum, ModuleEnum
from app.services.pagination import paginate


class ChatService:
    async def create_session(self, session: AsyncSession) -> ChatSessionCreateData:
        welcome_message = (
            "你好，我是求职高手 AI 助手。"
            "告诉我你要找工作、优化投递、调查公司还是分析合同。"
        )
        chat_session = ChatSession(title="新对话")
        session.add(chat_session)
        await session.commit()
        await session.refresh(chat_session)

        session.add(
            ChatMessage(
                session_id=chat_session.id,
                role=ChatRoleEnum.ASSISTANT.value,
                content=welcome_message,
            )
        )
        await session.commit()
        return ChatSessionCreateData(session_id=str(chat_session.id), welcome_message=welcome_message)

    async def list_sessions(self, session: AsyncSession, page: int, page_size: int) -> ChatSessionPage:
        records = list(
            (await session.execute(select(ChatSession).order_by(ChatSession.updated_at.desc()))).scalars()
        )
        items = [
            ChatSessionItem(
                session_id=str(record.id),
                last_message_preview=record.title,
                updated_at=record.updated_at,
            )
            for record in records
        ]
        items, total = paginate(items, page, page_size)
        return ChatSessionPage(items=items, page=page, page_size=page_size, total=total)

    async def history(
        self,
        session: AsyncSession,
        session_id: str,
        page: int,
        page_size: int,
    ) -> ChatMessagePage:
        chat_session = await session.get(ChatSession, uuid.UUID(session_id))
        if chat_session is None:
            raise NotFoundError("会话不存在")

        messages = list(
            (
                await session.execute(
                    select(ChatMessage)
                    .where(ChatMessage.session_id == chat_session.id)
                    .order_by(ChatMessage.created_at.asc())
                )
            ).scalars()
        )
        items = [
            ChatMessageItem(
                id=str(message.id),
                role=message.role,
                content=message.content,
                module=message.module,
                created_at=message.created_at,
            )
            for message in messages
        ]
        items, total = paginate(items, page, page_size)
        return ChatMessagePage(items=items, page=page, page_size=page_size, total=total)

    async def create_message(
        self,
        session: AsyncSession,
        session_id: str,
        content: str,
        current_module: ModuleEnum | None,
    ) -> ChatSendMessageData:
        chat_session = await session.get(ChatSession, uuid.UUID(session_id))
        if chat_session is None:
            raise NotFoundError("会话不存在")

        detected_module = self._detect_module(content, current_module)
        user_message = ChatMessage(
            session_id=chat_session.id,
            role=ChatRoleEnum.USER.value,
            content=content,
            module=detected_module.value,
        )
        session.add(user_message)
        await session.flush()

        assistant_message = ChatMessage(
            session_id=chat_session.id,
            role=ChatRoleEnum.ASSISTANT.value,
            content=self._build_reply(content, detected_module),
            module=detected_module.value,
        )
        session.add(assistant_message)
        chat_session.title = content[:40]
        chat_session.updated_at = datetime.now()
        session.add(chat_session)
        await session.commit()
        return ChatSendMessageData(message_id=str(user_message.id), detected_module=detected_module)

    async def stream(
        self,
        session: AsyncSession,
        session_id: str,
        message_id: str,
    ) -> AsyncIterable[ServerSentEvent]:
        chat_session = await session.get(ChatSession, uuid.UUID(session_id))
        if chat_session is None:
            raise NotFoundError("会话不存在")

        user_message = await session.get(ChatMessage, uuid.UUID(message_id))
        if user_message is None:
            raise NotFoundError("消息不存在")

        assistant_message = (
            await session.execute(
                select(ChatMessage)
                .where(
                    ChatMessage.session_id == chat_session.id,
                    ChatMessage.role == ChatRoleEnum.ASSISTANT.value,
                    ChatMessage.created_at >= user_message.created_at,
                )
                .order_by(ChatMessage.created_at.asc())
            )
        ).scalars().first()
        if assistant_message is None:
            raise NotFoundError("AI 回复不存在")

        for chunk in self._chunk_text(assistant_message.content):
            yield ServerSentEvent(data=chunk)
        yield ServerSentEvent(event="done", data="[DONE]")

    def _chunk_text(self, text: str) -> list[str]:
        return [text[index:index + 12] for index in range(0, len(text), 12)] or [text]

    def _detect_module(self, content: str, current_module: ModuleEnum | None) -> ModuleEnum:
        lowered = content.lower()
        if "投递" in content or "简历" in content:
            return ModuleEnum.PHANTOM
        if "调查" in content or "公司" in content or "背景" in content:
            return ModuleEnum.INVESTIGATOR
        if "合同" in content or "offer" in lowered or "条款" in content:
            return ModuleEnum.GUARDIAN
        if "找工作" in content or "职位" in content or "招聘" in content:
            return ModuleEnum.EAGLE
        return current_module or ModuleEnum.EAGLE

    def _build_reply(self, content: str, module: ModuleEnum) -> str:
        match module:
            case ModuleEnum.PHANTOM:
                return "我已记录你的投递诉求，建议优先完善结果指标并推进高匹配岗位。"
            case ModuleEnum.INVESTIGATOR:
                return "我建议优先核对工商变更、近半年舆情和员工评价，当前风险信号已同步到企业画像。"
            case ModuleEnum.GUARDIAN:
                return "合同里需要优先确认竞业限制期限、赔偿边界以及试用期转正条件。"
            case _:
                return f"基于你的描述“{content[:18]}”，我建议优先关注匹配度高且发布时间较新的岗位。"


chat_service = ChatService()
