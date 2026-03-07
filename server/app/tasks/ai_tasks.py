"""AI 分析异步任务

长耗时 AI 任务走 Celery 异步处理：
- 合同解析（OCR + 逐条分析）
- 简历定制生成
- 企业风险评估报告
"""

from app.tasks.celery_app import celery_app


@celery_app.task
def analyze_contract(contract_id: str):
    """合同解析任务"""
    ...


@celery_app.task
def customize_resume(resume_id: str, job_id: str):
    """AI 简历定制任务"""
    ...


@celery_app.task
def generate_risk_report(company_name: str):
    """企业风险评估报告生成"""
    ...
