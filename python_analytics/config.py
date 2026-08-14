import os
import re
from urllib.parse import quote_plus

ENV_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".env")


def _load_env(path: str) -> dict:
    env = {}
    if not os.path.exists(path):
        raise RuntimeError(
            f"Arquivo .env nao encontrado em: {path}\n"
            f"Ajuste a constante ENV_PATH em config.py para apontar para o .env "
            f"do seu projeto SistemaCompras."
        )

    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                continue
            key, value = line.split("=", 1)
            env[key.strip()] = value.strip()
    return env


_ENV = _load_env(ENV_PATH)


def get_env(key: str, default=None):
    return _ENV.get(key, os.environ.get(key, default))


def _parse_jdbc_url(jdbc_url: str):
    """
    Converte jdbc:postgresql://host:port/dbname?params
    em (host, port, dbname).
    """
    match = re.match(
        r"jdbc:postgresql://([^:/]+):(\d+)/([^?]+)",
        jdbc_url,
    )
    if not match:
        raise RuntimeError(f"Nao foi possivel interpretar DB_URL: {jdbc_url}")
    host, port, dbname = match.groups()
    return host, int(port), dbname


DB_URL_JDBC = get_env("DB_URL")
DB_USER = get_env("DB_USER")
DB_PASSWORD = get_env("DB_PASSWORD")

DB_HOST, DB_PORT, DB_NAME = _parse_jdbc_url(DB_URL_JDBC)

# String de conexao para SQLAlchemy (driver pg8000, puro Python, sem precisar compilar)
# IMPORTANTE: usuario/senha precisam ser url-encoded, pois podem conter
# caracteres especiais (ex: "@", "!") que quebrariam o parsing da URL.
_db_user_encoded = quote_plus(DB_USER)
_db_password_encoded = quote_plus(DB_PASSWORD)

SQLALCHEMY_DATABASE_URI = (
    f"postgresql+pg8000://{_db_user_encoded}:{_db_password_encoded}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
)

# Mesmo segredo/issuer usado pelo backend Java (src/security/JwtUtil.java)
# para validar o token enviado pelo frontend.
JWT_SECRET = get_env("JWT_SECRET")
JWT_ISSUER = "SistemaCompras"
JWT_ALGORITHM = "HS256"


ANALYTICS_SERVICE_PORT = int(get_env("ANALYTICS_SERVICE_PORT", "8000"))


ANALYTICS_CACHE_TTL_SECONDS = int(get_env("ANALYTICS_CACHE_TTL_SECONDS", "30"))


_raw_allowed_origin = get_env("ALLOWED_ORIGIN", "*")

if _raw_allowed_origin.strip() == "*":
    ALLOWED_ORIGINS = ["*"]
else:
    ALLOWED_ORIGINS = [o.strip() for o in _raw_allowed_origin.split(",") if o.strip()]
    # sempre libera localhost/127.0.0.1:8080, que e onde o front roda em dev
    for dev_origin in ("http://localhost:8080", "http://127.0.0.1:8080"):
        if dev_origin not in ALLOWED_ORIGINS:
            ALLOWED_ORIGINS.append(dev_origin)