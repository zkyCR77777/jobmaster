from pydantic import Field

from app.schemas.common import ModuleEnum, SchemaModel


class DashboardHeroStats(SchemaModel):
    new_jobs_today: int = Field(default=0, ge=0)
    match_success_rate: int = Field(default=0, ge=0, le=100)
    interview_invites: int = Field(default=0, ge=0)


class DashboardModuleCard(SchemaModel):
    module: ModuleEnum
    title: str
    subtitle: str
    description: str
    stats_text: str
    pending_count: int = Field(default=0, ge=0)


class DashboardAgentFeedItem(SchemaModel):
    module: ModuleEnum
    description: str
    stats_text: str


class DashboardHomeData(SchemaModel):
    greeting: str
    app_name: str = "求职高手"
    notification_count: int = Field(default=0, ge=0)
    hero_stats: DashboardHeroStats
    modules: list[DashboardModuleCard] = Field(default_factory=list)
    agent_feed: list[DashboardAgentFeedItem] = Field(default_factory=list)
