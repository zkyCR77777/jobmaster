from __future__ import annotations

from enum import StrEnum
from typing import Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field


class SchemaModel(BaseModel):
    model_config = ConfigDict(extra="forbid", use_enum_values=True)


class ModuleEnum(StrEnum):
    EAGLE = "eagle"
    PHANTOM = "phantom"
    INVESTIGATOR = "investigator"
    GUARDIAN = "guardian"


class DeliveryStatusEnum(StrEnum):
    PENDING = "pending"
    DELIVERING = "delivering"
    DELIVERED = "delivered"
    VIEWED = "viewed"
    WRITTEN_TEST = "written_test"
    INTERVIEW = "interview"
    OFFER = "offer"
    REJECTED = "rejected"


class CompanyRiskLevelEnum(StrEnum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"


class ContractRiskLevelEnum(StrEnum):
    SAFE = "safe"
    WARNING = "warning"
    DANGER = "danger"


class TaskStatusEnum(StrEnum):
    PENDING = "pending"
    PROCESSING = "processing"
    DONE = "done"
    FAILED = "failed"


class ChatRoleEnum(StrEnum):
    SYSTEM = "system"
    USER = "user"
    ASSISTANT = "assistant"


class TaskStage(SchemaModel):
    name: str
    status: TaskStatusEnum


class TaskProgress(SchemaModel):
    id: str
    status: TaskStatusEnum
    progress: int = Field(default=0, ge=0, le=100)
    current_stage: str | None = None
    stages: list[TaskStage] = Field(default_factory=list)


DataT = TypeVar("DataT")
ItemT = TypeVar("ItemT")


class ApiResponse(SchemaModel, Generic[DataT]):
    code: int = 0
    message: str = "ok"
    data: DataT
    request_id: str | None = None


class Page(SchemaModel, Generic[ItemT]):
    items: list[ItemT] = Field(default_factory=list)
    page: int = Field(default=1, ge=1)
    page_size: int = Field(default=20, ge=1, le=100)
    total: int = Field(default=0, ge=0)
