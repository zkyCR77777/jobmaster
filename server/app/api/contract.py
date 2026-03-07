from typing import Annotated

from fastapi import APIRouter, File, HTTPException, Path, UploadFile, status

from app.schemas.common import ApiResponse
from app.schemas.contract import ContractClauseList, ContractReport, ContractSummary, ContractTask

router = APIRouter(prefix="/api/v1/contracts", tags=["契约卫士 — 合同解析"])


@router.post("", response_model=ApiResponse[ContractTask])
async def upload_contract(
    file: Annotated[UploadFile, File(description="合同文件")],
) -> ApiResponse[ContractTask]:
    """上传合同文件（PDF / Word / 图片）。"""
    _ = file
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/{contract_id}", response_model=ApiResponse[ContractSummary])
async def get_contract_analysis(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractSummary]:
    """获取合同解析结果。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/{contract_id}/clauses", response_model=ApiResponse[ContractClauseList])
async def get_contract_clauses(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractClauseList]:
    """逐条款风险分析与通俗解读。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")


@router.get("/{contract_id}/report", response_model=ApiResponse[ContractReport])
async def get_contract_report(
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractReport]:
    """合同整体评估报告。"""
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="Not implemented yet")
