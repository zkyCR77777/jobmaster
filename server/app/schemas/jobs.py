from datetime import datetime

from pydantic import Field

from app.schemas.common import Page, SchemaModel


class JobListItem(SchemaModel):
    id: str
    title: str
    company: str
    location: str
    salary_range: str = ""
    match_score: int = Field(default=0, ge=0, le=100)
    is_new: bool = False
    published_at: datetime | None = None
    tags: list[str] = Field(default_factory=list)


class JobRecommendationItem(JobListItem):
    reason: str | None = None


class JobListPage(Page[JobListItem]):
    pass


class JobRecommendationPage(Page[JobRecommendationItem]):
    pass


class JobDetail(SchemaModel):
    id: str
    title: str
    company: str
    company_url: str = ""
    location: str = ""
    salary_range: str = ""
    job_type: str = ""
    description: str = ""
    requirements: str = ""
    tags: list[str] = Field(default_factory=list)
    source_url: str = ""
    published_at: datetime | None = None
    last_seen_at: datetime | None = None
    is_new: bool = False


class JobMatchRequest(SchemaModel):
    resume_id: str


class JobMatchData(SchemaModel):
    job_id: str
    resume_id: str
    match_score: int = Field(ge=0, le=100)
    summary: str
    strengths: list[str] = Field(default_factory=list)
    gaps: list[str] = Field(default_factory=list)
    suggestions: list[str] = Field(default_factory=list)
