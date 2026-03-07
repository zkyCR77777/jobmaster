"""投递异步任务

通过 Playwright 操作企业官网公开投递表单：
- 填写表单字段
- 上传简历文件
- 遇到验证码时暂停，通知用户手动处理
"""

from app.tasks.celery_app import celery_app


@celery_app.task
def submit_delivery(delivery_id: str):
    """执行单次投递"""
    ...
