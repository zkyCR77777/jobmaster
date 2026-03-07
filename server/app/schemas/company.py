from datetime import datetime

from pydantic import Field

from app.schemas.common import CompanyRiskLevelEnum, Page, SchemaModel, TaskProgress


class CompanyReportCreateRequest(SchemaModel):
    company_name: str


class CompanyReportTask(TaskProgress):
    company_name: str
    sources: list[str] = Field(default_factory=list)


class CompanyReportListItem(SchemaModel):
    id: str
    name: str
    industry: str = ""
    size: str = ""
    rating: float = 0.0
    risk_level: CompanyRiskLevelEnum
    growth: int = 0
    salary_range: str = ""
    risks: list[str] = Field(default_factory=list)
    positives: list[str] = Field(default_factory=list)
    updated_at: datetime | None = None


class CompanyReportPage(Page[CompanyReportListItem]):
    pass


class CompanyBasicProfile(SchemaModel):
    registered_capital: str = ""
    established_date: str = ""
    legal_representative: str = ""
    status: str = ""


class CompanyRiskBreakdown(SchemaModel):
    judicial: CompanyRiskLevelEnum
    operational: CompanyRiskLevelEnum
    public_opinion: CompanyRiskLevelEnum


class CompanyReportDetail(SchemaModel):
    id: str
    name: str
    industry: str = ""
    size: str = ""
    rating: float = 0.0
    risk_level: CompanyRiskLevelEnum
    growth: int = 0
    salary_range: str = ""
    basic_profile: CompanyBasicProfile
    risk_breakdown: CompanyRiskBreakdown
    summary: str = ""
    risks: list[str] = Field(default_factory=list)
    positives: list[str] = Field(default_factory=list)
