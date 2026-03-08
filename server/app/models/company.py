import uuid
from datetime import datetime

from sqlalchemy import JSON
from sqlmodel import Column, Field, SQLModel


class Company(SQLModel, table=True):
    """企业信息 - 来自天眼查/企查查 API"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(unique=True, index=True)
    industry: str = ""
    registered_capital: str = ""
    established_date: str = ""
    legal_representative: str = ""
    company_size: str = ""
    status: str = ""
    risk_level: str = ""  # low / medium / high
    risk_summary: str = ""
    tianyancha_data: dict = Field(default={}, sa_column=Column(JSON))
    updated_at: datetime = Field(default_factory=datetime.now)
