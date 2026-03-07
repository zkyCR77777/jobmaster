from fastapi import FastAPI

from app.api import auth, chat, company, contract, dashboard, delivery, jobs

app = FastAPI(title="求职高手 Smart Pact", version="0.1.0")

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
