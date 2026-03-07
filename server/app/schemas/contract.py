from pydantic import Field

from app.schemas.common import ContractRiskLevelEnum, SchemaModel, TaskProgress, TaskStage, TaskStatusEnum


class ContractTask(TaskProgress):
    file_name: str


class ContractSummaryCounts(SchemaModel):
    safe: int = Field(default=0, ge=0)
    warning: int = Field(default=0, ge=0)
    danger: int = Field(default=0, ge=0)


class ContractSummary(SchemaModel):
    id: str
    file_name: str
    status: TaskStatusEnum
    progress: int = Field(default=0, ge=0, le=100)
    overall_score: int | None = Field(default=None, ge=0, le=100)
    risk_level: ContractRiskLevelEnum
    summary: str = ""
    summary_counts: ContractSummaryCounts
    stages: list[TaskStage] = Field(default_factory=list)


class ContractClauseItem(SchemaModel):
    id: str
    title: str
    content: str
    risk_level: ContractRiskLevelEnum
    explanation: str
    suggestion: str | None = None


class ContractClauseList(SchemaModel):
    items: list[ContractClauseItem] = Field(default_factory=list)


class ContractReport(SchemaModel):
    overall_score: int | None = Field(default=None, ge=0, le=100)
    risk_level: ContractRiskLevelEnum
    summary: str = ""
    key_risks: list[str] = Field(default_factory=list)
    recommendation: str = ""
