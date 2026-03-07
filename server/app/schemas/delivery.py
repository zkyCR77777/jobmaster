from datetime import datetime

from pydantic import Field

from app.schemas.common import DeliveryStatusEnum, Page, SchemaModel, TaskProgress, TaskStatusEnum


class ResumeItem(SchemaModel):
    id: str
    title: str = ""
    file_name: str = ""
    is_base: bool = False
    target_job_id: str | None = None
    created_at: datetime | None = None


class ResumeListPage(Page[ResumeItem]):
    pass


class ResumeUploadData(SchemaModel):
    id: str
    title: str = ""
    file_name: str
    status: TaskStatusEnum
    parsed_summary: str = ""


class ResumeCustomizationCreateRequest(SchemaModel):
    job_id: str


class ResumeCustomizationTask(TaskProgress):
    resume_id: str
    job_id: str


class DeliveryItem(SchemaModel):
    id: str
    job_id: str
    resume_id: str
    company: str
    position: str
    status: DeliveryStatusEnum
    updated_at: datetime | None = None
    delivered_at: datetime | None = None
    note: str | None = None


class DeliveryPage(Page[DeliveryItem]):
    pass


class DeliveryCreateRequest(SchemaModel):
    job_id: str
    resume_id: str
    channel: str = "official_site"
    note: str | None = None


class DeliveryUpdateRequest(SchemaModel):
    status: DeliveryStatusEnum
    note: str | None = None


class DeliveryStats(SchemaModel):
    total: int = Field(default=0, ge=0)
    pending: int = Field(default=0, ge=0)
    delivering: int = Field(default=0, ge=0)
    delivered: int = Field(default=0, ge=0)
    viewed: int = Field(default=0, ge=0)
    written_test: int = Field(default=0, ge=0)
    interview: int = Field(default=0, ge=0)
    offer: int = Field(default=0, ge=0)
    rejected: int = Field(default=0, ge=0)
