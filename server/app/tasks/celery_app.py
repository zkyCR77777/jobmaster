from celery import Celery

from app.config import settings

celery_app = Celery(
    "job_master",
    broker=settings.redis_url,
    backend=settings.redis_url,
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="Asia/Shanghai",
    enable_utc=True,
    task_routes={
        "app.tasks.crawl_tasks.*": {"queue": "crawl"},
        "app.tasks.ai_tasks.*": {"queue": "ai"},
        "app.tasks.delivery_tasks.*": {"queue": "delivery"},
    },
)
