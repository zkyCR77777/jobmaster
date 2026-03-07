from fastapi import APIRouter

router = APIRouter(prefix="/api/contract", tags=["契约卫士 — 合同解析"])


@router.post("/upload")
async def upload_contract():
    """上传合同文件（PDF / Word / 图片）"""
    ...


@router.get("/{contract_id}")
async def get_contract_analysis(contract_id: str):
    """获取合同解析结果"""
    ...


@router.get("/{contract_id}/clauses")
async def get_contract_clauses(contract_id: str):
    """逐条款风险分析与通俗解读"""
    ...


@router.get("/{contract_id}/report")
async def get_contract_report(contract_id: str):
    """合同整体评估报告（综合评分 + 风险摘要）"""
    ...
