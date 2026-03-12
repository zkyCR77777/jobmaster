import uuid
from datetime import datetime

from sqlmodel import Field, SQLModel


class ChatSession(SQLModel, table=True):
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    title: str = ""
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now)


class ChatMessage(SQLModel, table=True):
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    session_id: uuid.UUID = Field(foreign_key="chatsession.id", index=True)
    role: str
    content: str
    module: str | None = None
    created_at: datetime = Field(default_factory=datetime.now)
