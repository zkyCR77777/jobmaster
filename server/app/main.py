from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api import auth, chat, company, contract, dashboard, delivery, jobs
from app.database import prepare_database

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        await prepare_database()
    except Exception:
        logger.exception("Failed to prepare database during startup.")
    yield


app = FastAPI(title="求职高手 Smart Pact", version="0.1.0", lifespan=lifespan)

app.include_router(dashboard.router)
app.include_router(auth.router)
app.include_router(jobs.router)
app.include_router(delivery.router)
app.include_router(company.router)
app.include_router(contract.router)
app.include_router(chat.router)


@app.get("/health")
async def health_check():
    return {"status": "ok"}
