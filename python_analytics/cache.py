import threading
import time
from functools import wraps

_lock = threading.Lock()
_store = {}  # chave -> (expira_em, valor)


def _make_key(func_name: str, args: tuple, kwargs: dict) -> str:
    kwargs_ordenados = tuple(sorted(kwargs.items()))
    return f"{func_name}:{args}:{kwargs_ordenados}"


def ttl_cache(seconds: int):
    """
    Decorator que cacheia o retorno de uma funcao por `seconds` segundos,
    usando os argumentos da chamada como parte da chave.

    Uso:
        @ttl_cache(30)
        def get_purchases_df(user_id, start_date=None, ...):
            ...
    """

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            key = _make_key(func.__name__, args, kwargs)
            agora = time.monotonic()

            with _lock:
                entrada = _store.get(key)
                if entrada is not None:
                    expira_em, valor = entrada
                    if agora < expira_em:
                        return valor

            valor = func(*args, **kwargs)

            with _lock:
                _store[key] = (agora + seconds, valor)

            return valor

        return wrapper

    return decorator


def clear():
    """Limpa todo o cache (util em testes, ou para forcar dado fresco)."""
    with _lock:
        _store.clear()


def cleanup_expired():
    """
    Remove entradas ja expiradas do dict. Nao e obrigatorio chamar isso
    (entradas expiradas simplesmente sao ignoradas e recalculadas quando
    acessadas), mas evita que o dict cresca indefinidamente em um processo
    de longa duracao com muitos usuarios/parametros diferentes.
    """
    agora = time.monotonic()
    with _lock:
        chaves_expiradas = [k for k, (expira_em, _) in _store.items() if agora >= expira_em]
        for k in chaves_expiradas:
            del _store[k]