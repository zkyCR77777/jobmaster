import uuid
from datetime import datetime

from sqlalchemy import JSON
from sqlmodel import Column, Field, SQLModel


class Contract(SQLModel, table=True):
    """合同解析记录"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    user_id: uuid.UUID = Field(index=True, foreign_key="user.id")
    file_path: str
    file_type: str = ""  # pdf / docx / image
    raw_text: str = ""
    overall_score: int | None = None
    risk_level: str = ""  # safe / warning / danger
    analysis_result: dict = Field(default={}, sa_column=Column(JSON))
    status: str = "pending"  # pending / processing / done / failed
    created_at: datetime = Field(default_factory=datetime.now)
