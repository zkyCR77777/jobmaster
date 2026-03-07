"""爬虫定时任务

- 定时采集头部企业官网岗位 JD
- 增量更新，检测岗位新增/下线/变更
"""

from app.tasks.celery_app import celery_app


@celery_app.task
def crawl_company_jobs(company_key: str):
    """采集指定企业官网的岗位列表"""
    ...


@celery_app.task
def crawl_all_companies():
    """批量采集所有已配置企业的岗位"""
    ...
