from app.core.exceptions import NotFoundError
from app.schemas.company import CompanyReportDetail, CompanyReportPage
from app.services.demo_store import paginate, store


class CompanyService:
    def list_reports(self, page: int, page_size: int) -> CompanyReportPage:
        items, total = paginate(store.list_company_reports(), page, page_size)
        return CompanyReportPage(items=items, page=page, page_size=page_size, total=total)

    def detail(self, report_id: str) -> CompanyReportDetail:
        detail = store.company_detail(report_id)
        if detail is None:
            raise NotFoundError('企业报告不存在')
        return detail


company_service = CompanyService()
