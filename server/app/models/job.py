import uuid
from datetime import datetime

from sqlmodel import SQLModel, Field, Column
from sqlalchemy import JSON


class Job(SQLModel, table=True):
    """岗位信息 - 从企业官网采集"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    title: str = Field(index=True)
    company: str = Field(index=True)
    company_url: str = ""
    location: str = ""
    salary_range: str = ""
    description: str = ""
    requirements: str = ""
    job_type: str = ""
    tags: list[str] = Field(default=[], sa_column=Column(JSON))
    source_url: str = ""
    is_active: bool = True
    first_seen_at: datetime = Field(default_factory=datetime.now)
    last_seen_at: datetime = Field(default_factory=datetime.now)
