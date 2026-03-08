from app.core.exceptions import NotFoundError
from app.schemas.jobs import JobDetail, JobListPage, JobMatchData, JobRecommendationPage
from app.services.demo_store import paginate, store


class JobService:
    def list_jobs(self, page: int, page_size: int) -> JobListPage:
        items, total = paginate(store.list_jobs(), page, page_size)
        return JobListPage(items=items, page=page, page_size=page_size, total=total)

    def recommendations(self, page: int, page_size: int) -> JobRecommendationPage:
        items, total = paginate(store.recommendations(), page, page_size)
        return JobRecommendationPage(items=items, page=page, page_size=page_size, total=total)

    def detail(self, job_id: str) -> JobDetail:
        detail = store.job_detail(job_id)
        if detail is None:
            raise NotFoundError('岗位不存在')
        return detail

    def match(self, job_id: str, resume_id: str) -> JobMatchData:
        data = store.match_job(job_id, resume_id)
        if data is None:
            raise NotFoundError('岗位不存在')
        return data


job_service = JobService()
