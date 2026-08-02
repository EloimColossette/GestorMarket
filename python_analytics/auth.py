import jwt

import config


class AuthError(Exception):
    """Erro de autenticacao, com o codigo HTTP que deve ser respondido."""

    def __init__(self, status_code: int, message: str):
        self.status_code = status_code
        self.message = message
        super().__init__(message)


def get_user_id_from_headers(headers) -> int:
    """
    Le o header Authorization: Bearer <token>, valida e devolve o userId.
    Lanca AuthError (401) se o token faltar, for invalido ou tiver expirado.
    """
    auth_header = headers.get("Authorization", "")

    if not auth_header.startswith("Bearer "):
        raise AuthError(401, "Token nao informado")

    token = auth_header.replace("Bearer ", "", 1).strip()

    try:
        payload = jwt.decode(
            token,
            config.JWT_SECRET,
            algorithms=[config.JWT_ALGORITHM],
            issuer=config.JWT_ISSUER,
        )
    except jwt.ExpiredSignatureError:
        raise AuthError(401, "Token expirado")
    except jwt.InvalidTokenError:
        raise AuthError(401, "Token invalido")

    return int(payload["userId"])