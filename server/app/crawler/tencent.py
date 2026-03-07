"""腾讯招聘官网爬虫

数据源: careers.tencent.com
"""

from app.crawler.base import BaseCrawler


class TencentCrawler(BaseCrawler):
    company_name = "腾讯"
    base_url = "https://careers.tencent.com"

    async def fetch_job_list(self) -> list[dict]:
        ...

    async def fetch_job_detail(self, url: str) -> dict:
        ...
