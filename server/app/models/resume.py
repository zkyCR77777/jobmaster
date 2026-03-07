import uuid
from datetime import datetime

from sqlmodel import SQLModel, Field


class Resume(SQLModel, table=True):
    """用户简历"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    user_id: uuid.UUID = Field(index=True, foreign_key="user.id")
    title: str = ""
    file_path: str = ""
    content_text: str = ""
    is_base: bool = False  # 是否为基础简历
    target_job_id: uuid.UUID | None = Field(default=None, foreign_key="job.id")
    created_at: datetime = Field(default_factory=datetime.now)


class Delivery(SQLModel, table=True):
    """投递记录"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    user_id: uuid.UUID = Field(index=True, foreign_key="user.id")
    job_id: uuid.UUID = Field(foreign_key="job.id")
    resume_id: uuid.UUID = Field(foreign_key="resume.id")
    status: str = "pending"  # pending / delivered / viewed / interview / rejected
    delivered_at: datetime | None = None
    created_at: datetime = Field(default_factory=datetime.now)
