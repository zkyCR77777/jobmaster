from typing import Annotated

from fastapi import APIRouter, File, Path, UploadFile

from app.core.deps import SessionDep
from app.schemas.common import ApiResponse
from app.schemas.contract import ContractClauseList, ContractReport, ContractSummary, ContractTask
from app.services import contract_service

router = APIRouter(prefix="/api/v1/contracts", tags=["契约卫士 — 合同解析"])


@router.post("")
async def upload_contract(
    session: SessionDep,
    file: Annotated[UploadFile, File(description="合同文件")],
) -> ApiResponse[ContractTask]:
    """上传合同文件（PDF / Word / 图片）。"""
    return ApiResponse(
        data=await contract_service.create_task(
            session,
            file_name=file.filename or "contract.pdf",
            file_bytes=await file.read(),
        )
    )


@router.get("/{contract_id}")
async def get_contract_analysis(
    session: SessionDep,
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractSummary]:
    """获取合同解析结果。"""
    return ApiResponse(data=await contract_service.summary(session, contract_id))


@router.get("/{contract_id}/clauses")
async def get_contract_clauses(
    session: SessionDep,
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractClauseList]:
    """逐条款风险分析与通俗解读。"""
    return ApiResponse(data=await contract_service.clauses(session, contract_id))


@router.get("/{contract_id}/report")
async def get_contract_report(
    session: SessionDep,
    contract_id: Annotated[str, Path(min_length=1)],
) -> ApiResponse[ContractReport]:
    """合同整体评估报告。"""
    return ApiResponse(data=await contract_service.report(session, contract_id))
