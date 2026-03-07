"""爬虫基类

所有企业官网爬虫继承此基类，统一接口：
- fetch_job_list(): 获取岗位列表
- fetch_job_detail(url): 获取岗位详情
- 内置请求频率控制、robots.txt 检查
"""

from abc import ABC, abstractmethod


class BaseCrawler(ABC):
    company_name: str = ""
    base_url: str = ""

    @abstractmethod
    async def fetch_job_list(self) -> list[dict]:
        ...

    @abstractmethod
    async def fetch_job_detail(self, url: str) -> dict:
        ...
