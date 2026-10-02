# SocialConnect API

> API RESTful de gestão para instituições sociais (ONGs, bancos de alimentos,
> CRAS, abrigos). Conecta doadores, voluntários e beneficiários.

**Disciplina:** ITE005 — Tópicos Especiais em Sistemas para Internet III
**Stack:** Java 21 · Spring Boot 4.1.1 · JPA · Flyway · H2 (dev) · PostgreSQL (prod/testes) · SpringDoc OpenAPI

---

## Como Rodar

### Pré-requisitos

- JDK 21 LTS ([Adoptium](https://adoptium.net/))
- Maven 3.9+ (ou use o wrapper: `./mvnw`)
- Docker (apenas para os testes de integração com Testcontainers)
- IDE: IntelliJ IDEA (recomendado) ou VS Code

### Passos

```bash
# 1. Clone o repositório
git clone <url-do-repo>
cd socialconnect-api

# 2. Compile o projeto
mvn clean compile

# 3. Rode a aplicação (H2 em memória; o Flyway cria as tabelas)
mvn spring-boot:run
```

Depois de subir:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs
- **Console H2:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:socialconnectdb`, usuário `sa`, senha vazia)

### Testes

```bash
# Tudo (os de integração exigem Docker rodando)
mvn clean test

# Só os unitários (não precisam de Docker)
mvn test -Dtest='*ServiceTest,*ValidatorTest'
```

---

## Endpoints principais

| Recurso | Base | Operações |
|---------|------|-----------|
| Beneficiários | `/api/v1/beneficiarios` | GET (lista/ID), POST, PUT, PATCH, DELETE |
| Doações | `/api/v1/doacoes` | GET (lista/ID), POST, PUT, PATCH, DELETE |
| **Produtos** | `/api/v1/produtos` | GET (lista/ID), POST, PUT, DELETE |

---

## Módulo de Produtos (Avaliação A1)

Controle do estoque de itens doados (alimentos, roupas, higiene).

| Método | Rota | Sucesso | Erros possíveis |
|--------|------|---------|-----------------|
| GET | `/api/v1/produtos?page=0&size=10&sort=nome,asc&categoria=ALIMENTO&nome=arroz` | 200 (paginado) | 400 |
| GET | `/api/v1/produtos/{idProduto}` | 200 | 404 |
| POST | `/api/v1/produtos` | 201 + `Location` | 400, 409, 422 |
| PUT | `/api/v1/produtos/{idProduto}` | 200 | 400, 404, 409, 422 |
| DELETE | `/api/v1/produtos/{idProduto}` | 204 | 404 |

### Regras de negócio

- **Estoque não negativo:** `estoqueAtual < 0` é rejeitado com **422 Unprocessable Entity**. Essa checagem é feita no *Service* (e não com `@Min` no DTO) justamente para devolver 422 em vez de 400.
- **Alerta de estoque baixo:** a resposta traz `estoqueBaixo = true` quando `estoqueAtual < estoqueMinimo`.
- **Nome único:** nome repetido (sem diferenciar maiúsculas/minúsculas) retorna **409 Conflict**. No PUT, o próprio produto pode manter o nome.
- Erros seguem o formato **Problem Details (RFC 7807)**, com mensagens em `messages.properties`.

### Exemplo de requisição (POST)

```json
{
  "nome": "Arroz 5kg",
  "categoria": "ALIMENTO",
  "estoqueAtual": 3,
  "estoqueMinimo": 10,
  "unidadeMedida": "unidade"
}
```

### Exemplo de resposta (201)

```json
{
  "idProduto": 1,
  "nome": "Arroz 5kg",
  "categoria": "ALIMENTO",
  "estoqueAtual": 3,
  "estoqueMinimo": 10,
  "unidadeMedida": "unidade",
  "dataCadastro": "2026-10-02",
  "estoqueBaixo": true
}
```

Categorias aceitas: `ALIMENTO`, `ROUPA`, `HIGIENE`, `OUTROS`.

### Estrutura do módulo

```
produtos/
├── controller/  ProdutoController
├── service/     ProdutoService (interface) · ProdutoServiceImpl
├── repository/  ProdutoRepository · ProdutoSpecifications
├── dto/         ProdutoRequestDTO · ProdutoResponseDTO (records)
└── model/       Produto · CategoriaProduto
```

Validação customizada: `@UnidadeMedidaValida` (pacote `validation`).
Migration: `db/migration/V4__create_produtos_table.sql`.

---

## Uso de IA

Veja o registro completo em [`AI_USAGE.md`](AI_USAGE.md).

## Declaração de Uso de IA (A1)

### Ferramentas utilizadas:
- [x] ChatGPT / Claude / Gemini
- [ ] Copilot / Codeium
- [ ] Nenhuma

### Como utilizei:
- Usei o Claude para gerar a implementação inicial do módulo de Produtos (entity, migration, DTOs, service, controller, Swagger e testes) a partir do enunciado da A1.
- Usei o Claude para gerar a descrição do PR e a atualização deste README.

### O que eu entendo 100%:
- _(preencher: ex. a lógica de validação de estoque negativo no Service e o mapeamento para 422)_
- _(preencher: ex. a configuração do Testcontainers e o padrão AAA nos testes)_

### O que precisei estudar mais:
- _(preencher: ex. `JpaSpecificationExecutor` / `Specification` para os filtros opcionais)_
