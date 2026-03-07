FROM python:3.12-slim

WORKDIR /app

COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

COPY pyproject.toml uv.lock* ./
RUN uv sync --frozen --no-dev

COPY . .

RUN uv run playwright install chromium --with-deps

EXPOSE 8000

CMD ["uv", "run", "fastapi", "run", "--host", "0.0.0.0"]
