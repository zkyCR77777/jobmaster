"""阿里巴巴招聘官网爬虫

数据源: talent.alibaba.com
"""

from app.crawler.base import BaseCrawler


class AlibabaCrawler(BaseCrawler):
    company_name = "阿里巴巴"
    base_url = "https://talent.alibaba.com"

    async def fetch_job_list(self) -> list[dict]:
        ...

    async def fetch_job_detail(self, url: str) -> dict:
        ...
