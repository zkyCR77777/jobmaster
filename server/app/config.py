from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "求职高手"
    debug: bool = False

    database_url: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/job_master"
    redis_url: str = "redis://localhost:6379/0"

    secret_key: str = "change-me-in-production"
    access_token_expire_minutes: int = 60

    deepseek_api_key: str = ""
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-chat"

    zhipu_api_key: str = ""

    tianyancha_api_token: str = ""

    qichacha_app_key: str = ""
    qichacha_secret_key: str = ""

    model_config = {"env_file": ".env"}


settings = Settings()
