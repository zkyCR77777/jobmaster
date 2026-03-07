from fastapi import APIRouter

router = APIRouter(prefix="/api/company", tags=["深网调查员 — 企业分析"])


@router.get("/{company_name}")
async def get_company_profile(company_name: str):
    """企业基本画像（工商、规模、融资）"""
    ...


@router.get("/{company_name}/risk")
async def get_company_risk(company_name: str):
    """企业风险扫描（司法、经营、舆情）"""
    ...


@router.get("/{company_name}/report")
async def get_risk_report(company_name: str):
    """AI 生成企业风险评估报告"""
    ...
