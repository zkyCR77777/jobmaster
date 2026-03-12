import uuid
from datetime import datetime

from sqlalchemy import JSON
from sqlmodel import Field, SQLModel
from sqlalchemy import Column


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
    note: str = ""
    delivered_at: datetime | None = None
    updated_at: datetime = Field(default_factory=datetime.now)
    created_at: datetime = Field(default_factory=datetime.now)


class ResumeCustomizationTask(SQLModel, table=True):
    """定制简历任务"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    resume_id: uuid.UUID = Field(foreign_key="resume.id")
    job_id: uuid.UUID = Field(foreign_key="job.id")
    status: str = "processing"
    progress: int = 0
    current_stage: str = ""
    stages: list[dict] = Field(default_factory=list, sa_column=Column(JSON))
    created_at: datetime = Field(default_factory=datetime.now)
