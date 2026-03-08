# 求职高手 - 后端服务

为 Android 客户端提供岗位、投递、企业分析、合同解析、AI 对话等 API。

## 技术栈

- Python `3.10+`
- FastAPI + Pydantic
- SQLModel + PostgreSQL (`asyncpg`)
- Redis + Celery
- `uv`（依赖与命令管理）

## 本地运行（推荐）

### 1) 前置条件

- 已安装 Python `3.10+`
- 已安装 `uv`
- 已安装 Docker（用于启动 PostgreSQL / Redis，推荐）

### 2) 进入目录并配置环境变量

```bash
cd server
cp .env.example .env
```

按需修改 `.env`，至少确认：

- `DATABASE_URL`
- `REDIS_URL`
- `SECRET_KEY`
- AI 相关 Key（如需要联调 AI）

### 3) 启动依赖服务（PostgreSQL + Redis）

```bash
docker compose up -d postgres redis
```

### 4) 安装依赖

```bash
uv sync --dev
```

### 5) 启动 API 服务

```bash
uv run uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 6) 验证服务

- 健康检查：`http://127.0.0.1:8000/health`
- OpenAPI 文档：`http://127.0.0.1:8000/docs`

## 可选：启动异步任务进程

在 `server` 目录下另开终端：

```bash
# worker
uv run celery -A app.tasks.celery_app worker -l info -Q crawl,ai,delivery

# beat（定时任务）
uv run celery -A app.tasks.celery_app beat -l info
```

## Docker Compose 一键启动（API + Worker + Beat + DB + Redis）

```bash
cd server
docker compose up --build
```

> 说明：`Dockerfile` 使用 `uv sync --frozen`。如果你本地没有 `uv.lock`，请先在 `server` 目录执行 `uv lock` 再构建。

## 当前开发状态

- 路由与 Schema 已搭好骨架，`/health` 可正常返回。
- 业务接口当前多数仍返回 `501 Not implemented yet`（正在逐步实现）。
- 客户端已按这些路由联调，后端实现完成后可直接去掉大量 mock 回退。

## 常见问题

### 1) 端口被占用

- PostgreSQL 默认 `5432`
- Redis 默认 `6379`
- API 默认 `8000`

自行修改 `docker-compose.yml` 端口映射或关闭占用进程。

### 2) `.env` 不生效

请确保命令在 `server` 目录执行（`app/config.py` 默认读取当前目录下 `.env`）。

### 3) 需要爬虫（Playwright）时报浏览器依赖缺失

可执行：

```bash
uv run playwright install chromium --with-deps
```
