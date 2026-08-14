# SistemaCompras

Sistema de controle de compras de supermercado: cadastro de usuários, supermercados, compras e itens de compra, com um serviço de **analytics em Python** (gráficos, previsão de gastos, histórico de preço por produto) acoplado ao backend Java.

## Arquitetura

O projeto é composto por três partes:

| Camada | Tecnologia | Porta | Descrição |
|---|---|---|---|
| **Backend principal** | Java 25 (`com.sun.net.httpserver`, sem framework) | `8080` | API REST, autenticação JWT, servidor de arquivos estáticos (`public/`) e proxy para o serviço de analytics |
| **Serviço de Analytics** | Python (pandas, SQLAlchemy, matplotlib) | `8000` | Cálculos e gráficos (base64) de gasto mensal, previsão, ranking por produto/supermercado, promoções, etc. |
| **Frontend** | HTML/CSS/JS puro | — | Servido como arquivo estático pelo próprio backend Java em `public/` |

O backend Java **sobe o processo Python automaticamente** ao iniciar (`AnalyticsProcessLauncher`), usando o interpretador da `.venv` do projeto. Todas as chamadas do frontend para `/api/analytics/*` passam por um proxy Java (`AnalyticsProxyHandler`) que repassa o header `Authorization` para o serviço Python — não é preciso expor a porta 8000 publicamente.

## Pré-requisitos

- **Java 25** e **Maven**
- **Python 3.13+** com um virtualenv em `.venv` na raiz do projeto (o launcher espera `./.venv/bin/python`)
- **PostgreSQL**
- Um arquivo `.env` na raiz do projeto (veja abaixo)

## Configuração (`.env`)

```env
# Banco de dados
DB_URL=jdbc:postgresql://localhost:5432/gestormarket
DB_USER=...
DB_PASSWORD=...

# JWT (mínimo 32 caracteres)
JWT_SECRET=...

# E-mail (recuperação de senha)
SMTP_HOST=...
SMTP_PORT=...
SMTP_USER=...
SMTP_PASSWORD=...
EMAIL_FROM=...
EMAIL_FROM_NAME=...

# URLs / CORS
APP_BASE_URL=http://localhost:8080
ALLOWED_ORIGIN=*

# Opcionais (serviço de analytics)
ANALYTICS_SERVICE_PORT=8000
ANALYTICS_CACHE_TTL_SECONDS=30

# Documentação da API (Swagger UI) — desligada por padrão
ENABLE_DOCS=true
```

> O serviço Python lê o **mesmo** `.env` (via `python_analytics/config.py`) para se conectar ao banco e validar o JWT com o mesmo segredo/emissor usado pelo Java (`JwtUtil`), garantindo que um token emitido pelo login sirva para as duas camadas.

## Como rodar

1. Crie e ative o virtualenv Python na raiz do projeto e instale as dependências:
   ```bash
   python3 -m venv .venv
   source .venv/bin/activate
   pip install -r python_analytics/requirements.txt
   ```
2. Configure o `.env` (seção acima) e o banco PostgreSQL.
3. Compile e rode o backend Java:
   ```bash
   mvn clean package
   java -jar target/SistemaCompras-1.0-SNAPSHOT.jar
   ```
   Isso sobe o servidor Java na porta `8080` **e** o serviço de analytics Python na porta `8000` automaticamente.
4. Acesse `http://localhost:8080` (frontend servido como arquivo estático).

## Documentação da API

A especificação completa está em [`src/resources/Openapi.yaml`](src/resources/Openapi.yaml) (OpenAPI 3.0.3).

Com `ENABLE_DOCS=true` no `.env`, o próprio backend serve uma interface Swagger UI navegável — sem precisar duplicar nenhum arquivo em `public/`:

- `GET /docs` — Swagger UI (interface visual, lê o spec sozinho)
- `GET /openapi.yaml` — spec OpenAPI "cru"

Os dois arquivos (`Swagger.html` e `Openapi.yaml`) continuam morando só em `src/resources/`, que o Maven já empacota no classpath (`<resource><directory>src/resources</directory></resource>` no `pom.xml`). Um `DocsHandler` (`src/server/DocsHandler.java`) lê esses arquivos direto do classpath e os serve nas rotas acima — não há cópia manual nem risco de a documentação publicada ficar desatualizada em relação ao spec fonte.

Por padrão (`ENABLE_DOCS` ausente ou diferente de `true`) essas rotas ficam desligadas, já que expor o spec completo da API publicamente nem sempre é desejável em produção.

Resumo das rotas:

| Rota | Auth | Descrição |
|---|---|---|
| `POST /login` | pública | Autentica e retorna JWT (válido por 2h) |
| `POST /users` | pública | Cadastro de usuário |
| `GET/PUT/PATCH/DELETE /users(/{id})` | JWT | Perfil do próprio usuário |
| `POST /password/forgot-password`, `POST /password/reset-password` | pública | Recuperação de senha por e-mail |
| `GET/POST/PUT/DELETE /supermarkets(/{id})` | JWT | Supermercados do usuário |
| `GET/POST/PUT/DELETE /purchases(/{id})`, `GET /purchases/summary`, `GET /purchases/detail` | JWT | Compras e relatórios |
| `GET/POST/PUT/DELETE /purchase-items(/{id})`, `GET /purchase-items/product-names` | JWT | Itens de compra |
| `GET /api/analytics/*` | JWT (exceto `/health`) | Proxy para o serviço de analytics em Python |

Autenticação: envie o JWT recebido em `POST /login` no header `Authorization: Bearer <token>`. A cada requisição autenticada com sucesso, o servidor devolve um token renovado no header `X-Refreshed-Token`.

**Padrão de respostas:** as rotas de login, usuários e recuperação de senha respondem no envelope `{ success, message, data }`. Já supermercados, compras e itens de compra respondem o JSON "cru" em caso de sucesso, ou texto simples com o status HTTP em caso de erro.

## Estrutura do projeto

```
src/
├── application/    # Main.java (ponto de entrada)
├── controller/      # HttpHandlers (rotas)
├── service/          # Regras de negócio
├── repository/       # Acesso a dados (JDBC)
├── model/, dto/       # Entidades e objetos de transferência
├── security/          # JWT, CORS, rate limit, filtro de autenticação
├── server/            # HttpServer, StaticFileHandler, proxy de analytics
└── resources/          # Openapi.yaml, Swagger.html

python_analytics/    # Serviço de analytics (Flask-like, biblioteca padrão)
public/                # Frontend (HTML/CSS/JS estático)
```
