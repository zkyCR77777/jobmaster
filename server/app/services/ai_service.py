"""AI 服务封装。

当前阶段使用演示回复模拟多智能体协作，后续可在这里接入真实 LLM。
"""

from app.core.exceptions import NotFoundError
from app.schemas.chat import (
    ChatMessagePage,
    ChatSendMessageData,
    ChatSessionCreateData,
    ChatSessionPage,
)
from app.services.demo_store import paginate, store


class ChatService:
    def create_session(self) -> ChatSessionCreateData:
        session_id, welcome_message = store.create_session()
        return ChatSessionCreateData(session_id=session_id, welcome_message=welcome_message)

    def list_sessions(self, page: int, page_size: int) -> ChatSessionPage:
        items, total = paginate(store.list_sessions(), page, page_size)
        return ChatSessionPage(items=items, page=page, page_size=page_size, total=total)

    def history(self, session_id: str, page: int, page_size: int) -> ChatMessagePage:
        messages = store.get_messages(session_id)
        if messages is None:
            raise NotFoundError('会话不存在')
        items, total = paginate(messages, page, page_size)
        return ChatMessagePage(items=items, page=page, page_size=page_size, total=total)

    def create_message(self, session_id: str, content: str, current_module) -> ChatSendMessageData:
        result = store.add_user_message(session_id, content, current_module)
        if result is None:
            raise NotFoundError('会话不存在')
        message_id, detected_module = result
        return ChatSendMessageData(message_id=message_id, detected_module=detected_module)


chat_service = ChatService()
