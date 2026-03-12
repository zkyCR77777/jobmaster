from __future__ import annotations

import uuid
from pathlib import Path

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.database import DEFAULT_USER_ID
from app.models.contract import Contract
from app.schemas.common import TaskStage, TaskStatusEnum
from app.schemas.contract import (
    ContractClauseItem,
    ContractClauseList,
    ContractReport,
    ContractSummary,
    ContractSummaryCounts,
    ContractTask,
)


class ContractService:
    async def create_task(
        self,
        session: AsyncSession,
        *,
        file_name: str,
        file_bytes: bytes,
    ) -> ContractTask:
        stored_path = self._store_contract_file(file_name, file_bytes)
        raw_text = self._extract_raw_text(file_name, file_bytes)
        analysis = self._build_analysis(file_name=file_name, raw_text=raw_text)
        contract = Contract(
            user_id=DEFAULT_USER_ID,
            file_path=str(stored_path),
            file_type=Path(file_name).suffix.lstrip(".") or "pdf",
            raw_text=raw_text,
            overall_score=analysis["report"]["overall_score"],
            risk_level=analysis["report"]["risk_level"],
            analysis_result=analysis,
            status=TaskStatusEnum.DONE.value,
        )
        session.add(contract)
        await session.commit()
        await session.refresh(contract)
        return self._to_task(contract)

    async def summary(self, session: AsyncSession, contract_id: str) -> ContractSummary:
        contract = await session.get(Contract, uuid.UUID(contract_id))
        if contract is None:
            raise NotFoundError("合同解析结果不存在")
        return self._to_summary(contract)

    async def clauses(self, session: AsyncSession, contract_id: str) -> ContractClauseList:
        contract = await session.get(Contract, uuid.UUID(contract_id))
        if contract is None:
            raise NotFoundError("合同条款不存在")
        analysis = contract.analysis_result or {}
        return ContractClauseList(
            items=[
                ContractClauseItem(
                    id=str(item.get("id", "")),
                    title=str(item.get("title", "")),
                    content=str(item.get("content", "")),
                    risk_level=str(item.get("risk_level", "warning")),
                    explanation=str(item.get("explanation", "")),
                    suggestion=item.get("suggestion"),
                )
                for item in analysis.get("clauses", [])
            ]
        )

    async def report(self, session: AsyncSession, contract_id: str) -> ContractReport:
        contract = await session.get(Contract, uuid.UUID(contract_id))
        if contract is None:
            raise NotFoundError("合同报告不存在")
        report = (contract.analysis_result or {}).get("report", {})
        return ContractReport(
            overall_score=report.get("overall_score", contract.overall_score),
            risk_level=report.get("risk_level", contract.risk_level or "warning"),
            summary=str(report.get("summary", "")),
            key_risks=[str(item) for item in report.get("key_risks", [])],
            recommendation=str(report.get("recommendation", "")),
        )

    def _to_task(self, contract: Contract) -> ContractTask:
        stages = (contract.analysis_result or {}).get("stages", [])
        return ContractTask(
            id=str(contract.id),
            file_name=Path(contract.file_path).name,
            status=contract.status,
            progress=100 if contract.status == TaskStatusEnum.DONE.value else 20,
            current_stage="生成报告" if contract.status == TaskStatusEnum.DONE.value else "文档解析",
            stages=[
                TaskStage(name=str(stage.get("name", "")), status=str(stage.get("status", "pending")))
                for stage in stages
            ],
        )

    def _to_summary(self, contract: Contract) -> ContractSummary:
        analysis = contract.analysis_result or {}
        summary_counts = analysis.get("summary_counts", {})
        return ContractSummary(
            id=str(contract.id),
            file_name=Path(contract.file_path).name,
            status=contract.status,
            progress=100 if contract.status == TaskStatusEnum.DONE.value else 20,
            overall_score=contract.overall_score,
            risk_level=contract.risk_level or "warning",
            summary=str(analysis.get("summary_text", "")),
            summary_counts=ContractSummaryCounts(
                safe=int(summary_counts.get("safe", 0)),
                warning=int(summary_counts.get("warning", 0)),
                danger=int(summary_counts.get("danger", 0)),
            ),
            stages=[
                TaskStage(name=str(stage.get("name", "")), status=str(stage.get("status", "pending")))
                for stage in analysis.get("stages", [])
            ],
        )

    def _store_contract_file(self, file_name: str, file_bytes: bytes) -> Path:
        safe_name = self._safe_file_name(file_name, default_name="contract.pdf")
        stored_path = Path("uploads/contracts") / f"{uuid.uuid4().hex}-{safe_name}"
        stored_path.write_bytes(file_bytes)
        return stored_path

    def _extract_raw_text(self, file_name: str, file_bytes: bytes) -> str:
        if not file_bytes:
            return f"已接收合同文件：{file_name}"
        preview = file_bytes[:2048].decode("utf-8", errors="ignore").strip()
        if preview:
            return " ".join(preview.split())[:600]
        return f"已接收合同文件：{file_name}"

    def _build_analysis(self, *, file_name: str, raw_text: str) -> dict:
        lowered = raw_text.lower()
        clauses = [
            {
                "id": "1",
                "title": "薪资发放条款",
                "content": "请核对工资金额、发薪日期以及绩效奖金发放条件。",
                "risk_level": "safe",
                "explanation": "薪酬结构是否明确，决定后续争议处理成本。",
            },
            {
                "id": "2",
                "title": "试用期与转正条件",
                "content": "重点确认试用期长度、薪资折扣比例和转正标准。",
                "risk_level": "warning",
                "explanation": "试用期过长或标准模糊，会影响稳定预期。",
                "suggestion": "建议要求 HR 明确转正评估标准和生效时间。",
            },
            {
                "id": "3",
                "title": "保密与知识产权",
                "content": "确认保密义务范围、期限，以及职务成果归属约定。",
                "risk_level": "safe",
                "explanation": "常规条款可接受，但仍要核对适用边界。",
            },
        ]
        key_risks = ["试用期条款需要核对", "违约责任边界需要明确"]
        recommendation = "建议在签署前确认试用期、违约责任和竞业限制的书面说明。"

        if "竞业" in raw_text or "non-compete" in lowered:
            clauses.append(
                {
                    "id": "4",
                    "title": "竞业限制",
                    "content": "合同包含竞业限制约定，请重点确认期限、地域和补偿标准。",
                    "risk_level": "danger",
                    "explanation": "竞业限制直接影响离职后的择业空间。",
                    "suggestion": "建议将期限控制在 12 个月内，并写明补偿金额。",
                }
            )
            key_risks = ["竞业限制影响后续择业", "违约责任边界需要明确"]
            recommendation = "存在竞业限制时，建议先补充期限、范围和补偿金额再签署。"

        if "保密" in raw_text or "confidential" in lowered:
            clauses.append(
                {
                    "id": "5",
                    "title": "保密责任",
                    "content": "核对保密信息范围、期限和违约赔偿上限。",
                    "risk_level": "warning",
                    "explanation": "若赔偿责任没有上限，个人风险会被放大。",
                    "suggestion": "建议把赔偿责任限定为直接损失并约定上限。",
                }
            )

        summary_counts = {
            "safe": sum(1 for item in clauses if item["risk_level"] == "safe"),
            "warning": sum(1 for item in clauses if item["risk_level"] == "warning"),
            "danger": sum(1 for item in clauses if item["risk_level"] == "danger"),
        }
        overall_score = max(58, 88 - summary_counts["warning"] * 8 - summary_counts["danger"] * 18)
        risk_level = "danger" if summary_counts["danger"] else "warning" if summary_counts["warning"] else "safe"

        return {
            "summary_text": f"已分析文件《{Path(file_name).name}》，建议优先核对风险条款后再签署。",
            "summary_counts": summary_counts,
            "stages": [
                {"name": "文档解析", "status": "done"},
                {"name": "条款识别", "status": "done"},
                {"name": "风险分析", "status": "done"},
                {"name": "生成报告", "status": "done"},
            ],
            "clauses": clauses,
            "report": {
                "overall_score": overall_score,
                "risk_level": risk_level,
                "summary": "合同可继续推进，但建议先确认关键约束条款。",
                "key_risks": key_risks,
                "recommendation": recommendation,
            },
        }

    def _safe_file_name(self, file_name: str, *, default_name: str) -> str:
        candidate = Path(file_name).name.strip()
        if not candidate:
            return default_name
        return "".join(char if char.isalnum() or char in {".", "-", "_"} else "_" for char in candidate)


contract_service = ContractService()
