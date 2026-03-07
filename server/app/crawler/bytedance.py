"""字节跳动招聘官网爬虫

数据源: jobs.bytedance.com
"""

from app.crawler.base import BaseCrawler


class BytedanceCrawler(BaseCrawler):
    company_name = "字节跳动"
    base_url = "https://jobs.bytedance.com"

    async def fetch_job_list(self) -> list[dict]:
        ...

    async def fetch_job_detail(self, url: str) -> dict:
        ...
