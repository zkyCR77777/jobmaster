from datetime import datetime

from pydantic import Field

from app.schemas.common import ChatRoleEnum, ModuleEnum, Page, SchemaModel


class ChatSessionCreateData(SchemaModel):
    session_id: str
    welcome_message: str


class ChatSessionItem(SchemaModel):
    session_id: str
    last_message_preview: str = ""
    updated_at: datetime | None = None


class ChatSessionPage(Page[ChatSessionItem]):
    pass


class ChatMessageItem(SchemaModel):
    id: str
    role: ChatRoleEnum
    content: str
    module: ModuleEnum | None = None
    created_at: datetime | None = None


class ChatMessagePage(Page[ChatMessageItem]):
    pass


class ChatSendMessageRequest(SchemaModel):
    content: str = Field(min_length=1)
    current_module: ModuleEnum | None = None


class ChatSendMessageData(SchemaModel):
    message_id: str
    detected_module: ModuleEnum
