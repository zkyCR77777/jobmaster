from typing import Annotated

from fastapi import APIRouter, File, Path, UploadFile

from app.schemas.common import ApiResponse
from app.schemas.contract import ContractClauseList, ContractReport, ContractSummary, ContractTask
from app.services import contract_service

router = APIRouter(prefix="/api/v1/contracts", tags=["契约卫士 — 合同解析"])


@router.post("")
async def upload_contract(
    file: Annotated[UploadFile, File(description="合同文件")],
) -> ApiResponse[ContractTask]:
    """上传合同文件（PDF / Word / 图片）。"""
    return ApiResponse(data=contract_service.create_task(file.filename or "contract.pdf"))


@router.get("/{contract_id}")
async def get_contract_analysis(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractSummary]:
    """获取合同解析结果。"""
    return ApiResponse(data=contract_service.summary(contract_id))


@router.get("/{contract_id}/clauses")
async def get_contract_clauses(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractClauseList]:
    """逐条款风险分析与通俗解读。"""
    return ApiResponse(data=contract_service.clauses(contract_id))


@router.get("/{contract_id}/report")
async def get_contract_report(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractReport]:
    """合同整体评估报告。"""
    return ApiResponse(data=contract_service.report(contract_id))
