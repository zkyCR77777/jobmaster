from __future__ import annotations

import logging
import uuid
from pathlib import Path
from datetime import datetime, timedelta

from asyncpg.exceptions import InvalidCatalogNameError
from sqlalchemy import select, text
from sqlalchemy.engine import make_url
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlmodel import SQLModel

import app.models  # noqa: F401
from app.core.security import hash_password
from app.models.chat import ChatSession
from app.models.job import Job
from app.models.company import Company
from app.models.contract import Contract
from app.models.resume import Delivery, Resume, ResumeCustomizationTask
from app.models.user import User

from app.config import settings

logger = logging.getLogger(__name__)

DEFAULT_USER_ID = uuid.UUID("00000000-0000-0000-0000-000000000001")
DEFAULT_RESUME_ID = uuid.UUID("00000000-0000-0000-0000-000000000101")
DEFAULT_CONTRACT_ID = uuid.UUID("00000000-0000-0000-0000-000000000201")
DEFAULT_CUSTOMIZATION_ID = uuid.UUID("00000000-0000-0000-0000-000000000301")
UPLOAD_ROOT = Path("uploads")

engine = create_async_engine(settings.database_url)
async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)


async def get_session():
    async with async_session() as session:
        yield session


async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(SQLModel.metadata.create_all)


async def ensure_schema_extensions() -> None:
    async with engine.begin() as conn:
        await conn.exec_driver_sql(
            "ALTER TABLE delivery ADD COLUMN IF NOT EXISTS note VARCHAR NOT NULL DEFAULT ''"
        )
        await conn.exec_driver_sql(
            "ALTER TABLE delivery ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW()"
        )


async def ensure_database_exists() -> bool:
    db_url = make_url(settings.database_url)
    database_name = db_url.database
    if not database_name:
        return False

    admin_url = db_url.set(database="postgres")
    admin_engine = create_async_engine(
        admin_url.render_as_string(hide_password=False),
        isolation_level="AUTOCOMMIT",
    )
    try:
        async with admin_engine.connect() as conn:
            exists_result = await conn.execute(
                text("SELECT 1 FROM pg_database WHERE datname = :database_name"),
                {"database_name": database_name},
            )
            if exists_result.scalar_one_or_none() is not None:
                return False

            safe_database_name = database_name.replace('"', '""')
            await conn.exec_driver_sql(f'CREATE DATABASE "{safe_database_name}"')
            logger.info("Created PostgreSQL database '%s'.", database_name)
            return True
    finally:
        await admin_engine.dispose()


async def seed_jobs_if_empty() -> None:
    async with async_session() as session:
        existing_job = await session.execute(select(Job.id).limit(1))
        if existing_job.scalar_one_or_none() is not None:
            return

        now = datetime.now()
        session.add_all(
            [
                Job(
                    title="高级前端工程师",
                    company="字节跳动",
                    company_url="https://jobs.bytedance.com",
                    location="北京",
                    salary_range="35-55K",
                    description="负责核心业务前端架构设计与性能优化。",
                    requirements="熟悉 React、TypeScript，具备复杂项目落地经验。",
                    job_type="全职",
                    tags=["React", "TypeScript", "增长"],
                    source_url="https://jobs.bytedance.com/experienced/position/1",
                    first_seen_at=now - timedelta(hours=2),
                    last_seen_at=now - timedelta(minutes=20),
                ),
                Job(
                    title="全栈开发工程师",
                    company="阿里巴巴",
                    company_url="https://talent.alibaba.com",
                    location="杭州",
                    salary_range="32-48K",
                    description="参与核心业务系统研发，覆盖前后端与工程效率建设。",
                    requirements="熟悉 Java 或 Node.js，具备前后端协作能力。",
                    job_type="全职",
                    tags=["Java", "Spring", "Vue"],
                    source_url="https://talent.alibaba.com/position/2",
                    first_seen_at=now - timedelta(hours=4),
                    last_seen_at=now - timedelta(hours=1),
                ),
                Job(
                    title="产品经理",
                    company="腾讯",
                    company_url="https://careers.tencent.com",
                    location="深圳",
                    salary_range="30-50K",
                    description="负责企业服务产品规划、需求分析与跨团队推进。",
                    requirements="有 B 端或 SaaS 产品经验，逻辑清晰，表达能力强。",
                    job_type="全职",
                    tags=["B端", "SaaS"],
                    source_url="https://careers.tencent.com/jobdesc/3",
                    first_seen_at=now - timedelta(days=1),
                    last_seen_at=now - timedelta(hours=3),
                ),
                Job(
                    title="UI/UX 设计师",
                    company="美团",
                    company_url="https://zhaopin.meituan.com",
                    location="上海",
                    salary_range="25-40K",
                    description="负责核心业务产品体验设计与设计规范建设。",
                    requirements="熟悉 Figma 与用户研究方法，具备完整作品集。",
                    job_type="全职",
                    tags=["Figma", "用户研究"],
                    source_url="https://zhaopin.meituan.com/position/4",
                    first_seen_at=now - timedelta(days=2),
                    last_seen_at=now - timedelta(hours=6),
                ),
            ]
        )
        await session.commit()
        logger.info("Seeded initial jobs into database.")


async def seed_companies_if_empty() -> None:
    async with async_session() as session:
        existing_company = await session.execute(select(Company.id).limit(1))
        if existing_company.scalar_one_or_none() is not None:
            return

        now = datetime.now()
        session.add_all(
            [
                Company(
                    name="字节跳动",
                    industry="互联网科技",
                    registered_capital="10000 万人民币",
                    established_date="2012-03-09",
                    legal_representative="张某某",
                    company_size="100,000+",
                    status="存续",
                    risk_level="low",
                    risk_summary="整体风险较低，适合追求成长性的候选人。",
                    tianyancha_data={
                        "rating": 4.2,
                        "growth": 23,
                        "salary_range": "45K",
                        "risks": ["加班文化较重", "竞争压力大"],
                        "positives": ["技术栈先进", "晋升通道清晰", "年终奖丰厚"],
                        "risk_breakdown": {
                            "judicial": "low",
                            "operational": "low",
                            "public_opinion": "medium",
                        },
                    },
                    updated_at=now - timedelta(minutes=20),
                ),
                Company(
                    name="创业公司A",
                    industry="AI科技",
                    registered_capital="500 万人民币",
                    established_date="2021-05-18",
                    legal_representative="李某某",
                    company_size="50-200",
                    status="存续",
                    risk_level="high",
                    risk_summary="短期成长快，但稳定性偏弱，适合风险偏好更高的候选人。",
                    tianyancha_data={
                        "rating": 3.5,
                        "growth": -12,
                        "salary_range": "35K",
                        "risks": ["资金链紧张", "近期裁员传闻", "核心人员流失"],
                        "positives": ["期权激励", "扁平化管理"],
                        "risk_breakdown": {
                            "judicial": "medium",
                            "operational": "high",
                            "public_opinion": "high",
                        },
                    },
                    updated_at=now - timedelta(hours=2),
                ),
                Company(
                    name="阿里巴巴",
                    industry="电子商务",
                    registered_capital="100000 万人民币",
                    established_date="1999-06-28",
                    legal_representative="吴某某",
                    company_size="250,000+",
                    status="存续",
                    risk_level="low",
                    risk_summary="成熟大厂，培训体系完善，组织调整带来一定不确定性。",
                    tianyancha_data={
                        "rating": 4.0,
                        "growth": 8,
                        "salary_range": "42K",
                        "risks": ["组织架构频繁调整"],
                        "positives": ["完善的培训体系", "国际化机会多", "福利待遇好"],
                        "risk_breakdown": {
                            "judicial": "low",
                            "operational": "medium",
                            "public_opinion": "medium",
                        },
                    },
                    updated_at=now - timedelta(hours=4),
                ),
            ]
        )
        await session.commit()
        logger.info("Seeded initial companies into database.")


async def seed_default_user_if_empty() -> None:
    async with async_session() as session:
        existing_user = await session.get(User, DEFAULT_USER_ID)
        if existing_user is not None:
            return

        session.add(
            User(
                id=DEFAULT_USER_ID,
                email="test@example.com",
                hashed_password=hash_password("12345678"),
                nickname="小林",
                school="某理工大学",
                major="计算机科学与技术",
                degree="本科",
            )
        )
        await session.commit()
        logger.info("Seeded default user into database.")


async def seed_resume_delivery_data_if_empty() -> None:
    async with async_session() as session:
        existing_resume = await session.get(Resume, DEFAULT_RESUME_ID)
        if existing_resume is None:
            session.add(
                Resume(
                    id=DEFAULT_RESUME_ID,
                    user_id=DEFAULT_USER_ID,
                    title="基础简历",
                    file_path="uploads/resumes/base-resume.pdf",
                    content_text="熟悉前端开发、全栈协作与产品需求分析。",
                    is_base=True,
                )
            )

        existing_delivery = await session.execute(select(Delivery.id).limit(1))
        if existing_delivery.scalar_one_or_none() is None:
            jobs = list((await session.execute(select(Job))).scalars())
            job_map = {(job.company, job.title): job for job in jobs}
            now = datetime.now()
            for company, title, status, delivered_at, note in [
                ("字节跳动", "高级前端工程师", "delivered", now.replace(hour=10, minute=30), "官网投递成功，待查看。"),
                ("阿里巴巴", "全栈开发工程师", "delivered", now.replace(hour=10, minute=45), "系统已同步投递结果。"),
                ("腾讯", "产品经理", "delivering", None, "正在等待企业官网确认。"),
                ("美团", "UI/UX 设计师", "pending", None, "待补充作品集后投递。"),
            ]:
                job = job_map.get((company, title))
                if job is None:
                    continue
                session.add(
                    Delivery(
                        user_id=DEFAULT_USER_ID,
                        job_id=job.id,
                        resume_id=DEFAULT_RESUME_ID,
                        status=status,
                        note=note,
                        delivered_at=delivered_at,
                        updated_at=delivered_at or now,
                    )
                )

        existing_customization = await session.get(ResumeCustomizationTask, DEFAULT_CUSTOMIZATION_ID)
        if existing_customization is None:
            first_job = (
                await session.execute(select(Job).order_by(Job.first_seen_at.desc()).limit(1))
            ).scalar_one_or_none()
            if first_job is not None:
                session.add(
                    ResumeCustomizationTask(
                        id=DEFAULT_CUSTOMIZATION_ID,
                        resume_id=DEFAULT_RESUME_ID,
                        job_id=first_job.id,
                        status="done",
                        progress=100,
                        current_stage="生成完成",
                        stages=[
                            {"name": "提取岗位要求", "status": "done"},
                            {"name": "优化项目描述", "status": "done"},
                            {"name": "生成定制版本", "status": "done"},
                        ],
                    )
                )

        await session.commit()
        logger.info("Seeded resume and delivery data into database.")


def build_contract_analysis_template() -> dict:
    return {
        "summary_text": "合同主体清晰，但竞业限制和保密赔偿责任需要重点确认。",
        "summary_counts": {"safe": 2, "warning": 1, "danger": 1},
        "stages": [
            {"name": "文档解析", "status": "done"},
            {"name": "条款识别", "status": "done"},
            {"name": "风险分析", "status": "done"},
            {"name": "生成报告", "status": "done"},
        ],
        "clauses": [
            {
                "id": "1",
                "title": "薪资福利条款",
                "content": "基本工资为每月人民币 45,000 元，按月发放，发放日期为每月 15 日。",
                "risk_level": "safe",
                "explanation": "薪资发放日期和方式明确，属于标准条款。",
            },
            {
                "id": "2",
                "title": "竞业限制条款",
                "content": "员工离职后 24 个月内不得在竞争企业任职，补偿金为平均工资的 30%。",
                "risk_level": "warning",
                "explanation": "竞业期限偏长，补偿比例偏低。",
                "suggestion": "建议协商缩短期限至 12 个月，或提高补偿比例。",
            },
            {
                "id": "3",
                "title": "知识产权归属",
                "content": "员工在职期间创作的与公司业务相关成果归公司所有。",
                "risk_level": "safe",
                "explanation": "标准职务成果归属约定。",
            },
            {
                "id": "4",
                "title": "保密义务",
                "content": "员工应无限期保守商业秘密，违约需赔偿公司全部损失。",
                "risk_level": "danger",
                "explanation": "“无限期”与“全部损失”表述对员工较不利。",
                "suggestion": "建议明确保密范围、期限与赔偿上限。",
            },
        ],
        "report": {
            "overall_score": 72,
            "risk_level": "warning",
            "summary": "整体可签，但建议就竞业与保密责任补充书面确认。",
            "key_risks": ["竞业限制期限偏长", "保密赔偿责任边界不清"],
            "recommendation": "若 HR 同意对关键条款做补充说明，则整体可接受。",
        },
    }


async def seed_contracts_if_empty() -> None:
    async with async_session() as session:
        existing_contract = await session.get(Contract, DEFAULT_CONTRACT_ID)
        if existing_contract is not None:
            return

        session.add(
            Contract(
                id=DEFAULT_CONTRACT_ID,
                user_id=DEFAULT_USER_ID,
                file_path="uploads/contracts/offer-sample.pdf",
                file_type="pdf",
                raw_text="劳动合同示例文本",
                overall_score=72,
                risk_level="warning",
                analysis_result=build_contract_analysis_template(),
                status="done",
            )
        )
        await session.commit()
        logger.info("Seeded default contract into database.")


async def ensure_upload_dirs() -> None:
    (UPLOAD_ROOT / "contracts").mkdir(parents=True, exist_ok=True)
    (UPLOAD_ROOT / "resumes").mkdir(parents=True, exist_ok=True)


async def prepare_database() -> None:
    try:
        await init_db()
    except InvalidCatalogNameError:
        created = await ensure_database_exists()
        if not created:
            raise
        await init_db()
    await ensure_schema_extensions()
    await ensure_upload_dirs()
    await seed_jobs_if_empty()
    await seed_companies_if_empty()
    await seed_default_user_if_empty()
    await seed_resume_delivery_data_if_empty()
    await seed_contracts_if_empty()
