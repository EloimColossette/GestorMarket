import pandas as pd
from sqlalchemy import create_engine, text

import config

_engine = create_engine(config.SQLALCHEMY_DATABASE_URI)


def get_purchases_df(user_id: int) -> pd.DataFrame:
    """
    Retorna todas as compras (purchases) do usuario, com o nome do supermercado.
    Colunas: purchases_id, supermarket_id, supermarket_name, purchase_date, total
    """
    query = text(
        """
        SELECT p.purchases_id,
               p.supermarket_id,
               s.name AS supermarket_name,
               p.purchase_date,
               p.total
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE s.user_id = :user_id
        ORDER BY p.purchase_date
        """
    )
    with _engine.connect() as conn:
        df = pd.read_sql(query, conn, params={"user_id": user_id})

    if not df.empty:
        df["purchase_date"] = pd.to_datetime(df["purchase_date"])
        df["total"] = df["total"].astype(float)

    return df


def get_purchase_items_df(user_id: int) -> pd.DataFrame:
    """
    Retorna todos os itens de compra do usuario, com dados da compra e do supermercado.
    Colunas: purchase_items_id, purchase_id, product_name, quantity, unit_price,
             promotion_active, promotion_type, promotion_description, subtotal,
             purchase_date, supermarket_id, supermarket_name
    """
    query = text(
        """
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
        WHERE s.user_id = :user_id
        ORDER BY p.purchase_date
        """
    )
    with _engine.connect() as conn:
        df = pd.read_sql(query, conn, params={"user_id": user_id})

    if not df.empty:
        df["purchase_date"] = pd.to_datetime(df["purchase_date"])
        df["unit_price"] = df["unit_price"].astype(float)
        df["subtotal"] = df["subtotal"].astype(float)
        df["quantity"] = df["quantity"].astype(int)
        df["promotion_active"] = df["promotion_active"].fillna(False).astype(bool)

    return df