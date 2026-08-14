import pandas as pd
from sqlalchemy import create_engine, text

import config
from cache import ttl_cache

_engine = create_engine(config.SQLALCHEMY_DATABASE_URI)


def _build_where(user_id: int, start_date=None, end_date=None, supermarket_id=None, date_alias="p."):
    """
    Monta a cláusula WHERE + os parâmetros de forma parametrizada (nunca
    concatenando valor direto na query -> sem risco de SQL Injection).
    `date_alias` é o alias da tabela que tem a coluna purchase_date.
    """
    clauses = ["s.user_id = :user_id"]
    params = {"user_id": user_id}

    if start_date:
        clauses.append(f"{date_alias}purchase_date >= :start_date")
        params["start_date"] = start_date

    if end_date:
        clauses.append(f"{date_alias}purchase_date <= :end_date")
        params["end_date"] = end_date

    if supermarket_id:
        clauses.append("s.supermarkets_id = :supermarket_id")
        params["supermarket_id"] = supermarket_id

    return " AND ".join(clauses), params


@ttl_cache(config.ANALYTICS_CACHE_TTL_SECONDS)
def get_purchases_df(user_id: int, start_date=None, end_date=None, supermarket_id=None) -> pd.DataFrame:
    """
    Retorna as compras (purchases) do usuario, com o nome do supermercado,
    já filtradas por período/supermercado quando informado.
    Colunas: purchases_id, supermarket_id, supermarket_name, purchase_date, total

    Resultado fica em cache leve por ANALYTICS_CACHE_TTL_SECONDS (ver cache.py):
    o dashboard chama esta função várias vezes com os mesmos filtros, então
    cachear evita repetir a mesma query no Postgres.
    """
    where_sql, params = _build_where(user_id, start_date, end_date, supermarket_id, "p.")

    query = text(
        f"""
        SELECT p.purchases_id,
               p.supermarket_id,
               s.name AS supermarket_name,
               p.purchase_date,
               p.total
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE {where_sql}
        ORDER BY p.purchase_date
        """
    )
    with _engine.connect() as conn:
        df = pd.read_sql(query, conn, params=params)

    if not df.empty:
        df["purchase_date"] = pd.to_datetime(df["purchase_date"])
        df["total"] = df["total"].astype(float)

    return df


@ttl_cache(config.ANALYTICS_CACHE_TTL_SECONDS)
def get_purchase_items_df(
        user_id: int,
        start_date=None,
        end_date=None,
        supermarket_id=None,
        product_name=None,
) -> pd.DataFrame:
    """
    Retorna os itens de compra do usuario, com dados da compra e do supermercado,
    já filtrados por período/supermercado/produto quando informado.
    Colunas: purchase_items_id, purchase_id, product_name, quantity, unit_price,
             promotion_active, promotion_type, promotion_description, subtotal,
             purchase_date, supermarket_id, supermarket_name

    Resultado fica em cache leve por ANALYTICS_CACHE_TTL_SECONDS (ver cache.py).
    """
    where_sql, params = _build_where(user_id, start_date, end_date, supermarket_id, "p.")

    if product_name:
        where_sql += " AND pi.product_name ILIKE :product_name"
        params["product_name"] = f"%{product_name}%"

    query = text(
        f"""
        SELECT pi.purchase_items_id,
               pi.purchase_id,
               pi.product_name,
               pi.quantity,
               pi.unit_price,
               pi.promotion_active,
               pi.promotion_type,
               pi.promotion_description,
               pi.subtotal,
               p.purchase_date,
               p.supermarket_id,
               s.name AS supermarket_name
        FROM purchase_items pi
        JOIN purchases p ON p.purchases_id = pi.purchase_id
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE {where_sql}
        ORDER BY p.purchase_date
        """
    )
    with _engine.connect() as conn:
        df = pd.read_sql(query, conn, params=params)

    if not df.empty:
        df["purchase_date"] = pd.to_datetime(df["purchase_date"])
        df["unit_price"] = df["unit_price"].astype(float)
        df["subtotal"] = df["subtotal"].astype(float)
        df["quantity"] = df["quantity"].astype(int)
        df["promotion_active"] = df["promotion_active"].fillna(False).astype(bool)

    return df


@ttl_cache(config.ANALYTICS_CACHE_TTL_SECONDS)
def get_supermarkets(user_id: int) -> list:
    """Lista os supermercados do usuário, para popular o filtro no front.
    Resultado fica em cache leve por ANALYTICS_CACHE_TTL_SECONDS (ver cache.py)."""
    query = text(
        """
        SELECT supermarkets_id, name
        FROM supermarkets
        WHERE user_id = :user_id
        ORDER BY name
        """
    )
    with _engine.connect() as conn:
        rows = conn.execute(query, {"user_id": user_id}).mappings().all()

    return [{"id": row["supermarkets_id"], "nome": row["name"]} for row in rows]


@ttl_cache(config.ANALYTICS_CACHE_TTL_SECONDS)
def get_product_names(user_id: int, search: str = None, limite: int = 30) -> list:
    """
    Lista nomes de produto distintos já comprados pelo usuário (para
    autocomplete/filtro por produto no front). Se `search` for informado,
    filtra por nomes que contenham o texto (case-insensitive).

    Resultado fica em cache leve por ANALYTICS_CACHE_TTL_SECONDS (ver cache.py).
    """
    where_sql = "s.user_id = :user_id"
    params = {"user_id": user_id, "limite": limite}

    if search:
        where_sql += " AND pi.product_name ILIKE :search"
        params["search"] = f"%{search}%"

    query = text(
        f"""
        SELECT DISTINCT pi.product_name
        FROM purchase_items pi
        JOIN purchases p ON p.purchases_id = pi.purchase_id
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE {where_sql}
        ORDER BY pi.product_name
        LIMIT :limite
        """
    )
    with _engine.connect() as conn:
        rows = conn.execute(query, params).mappings().all()

    return [row["product_name"] for row in rows]