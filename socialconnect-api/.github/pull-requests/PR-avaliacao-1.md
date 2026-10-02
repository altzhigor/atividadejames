# PR — Avaliação A1: Módulo de Produtos

**Branch:** `avalicao1` → `main`
**Data:** 02/10/2026

## O que foi feito

- **Entidade e persistência:** `Produto` + enum `CategoriaProduto` (`ALIMENTO`, `ROUPA`, `HIGIENE`, `OUTROS`) e migration Flyway `V4__create_produtos_table.sql` (PK `id_produto`, `nome` UNIQUE, checks de categoria e estoque `>= 0`).
- **DTOs e validações:** `ProdutoRequestDTO` e `ProdutoResponseDTO` como `record`, Bean Validation com mensagens i18n em `messages.properties` e validação customizada `@UnidadeMedidaValida`.
- **Service e regras de negócio:** `ProdutoService` (interface) + `ProdutoServiceImpl`:
  - estoque negativo → `EstoqueNegativoException` → **422**;
  - nome duplicado (case-insensitive) → `NomeProdutoDuplicadoException` → **409**;
  - `estoqueBaixo = estoqueAtual < estoqueMinimo` calculado no response.
- **Controller:** CRUD em `/api/v1/produtos` (GET paginado com filtros `nome` e `categoria`, GET por ID, POST com `Location`, PUT, DELETE) com semântica HTTP correta e erros em Problem Details.
- **Swagger/OpenAPI:** `@Tag`, `@Operation`, `@ApiResponse` (incluindo 400/404/409/422) e `@Schema(example)` nos DTOs.
- **Testes (bônus):** `ProdutoServiceTest` (Mockito, AAA), `ProdutoControllerIntegrationTest` (Testcontainers `postgres:17`: 201, 409, 422) e `UnidadeMedidaValidatorTest`.
- **Documentação:** README atualizado e `AI_USAGE.md` com o registro de uso de IA.

## Como testar

1. `mvn clean compile`
2. `mvn spring-boot:run` e abrir http://localhost:8080/swagger-ui.html
3. No Swagger, em **Produtos**: criar um produto (201), repetir o mesmo nome (409) e enviar `estoqueAtual: -5` (422).
4. Docker ligado: `mvn clean test` (todos os testes). Sem Docker: `mvn test -Dtest='*ServiceTest,*ValidatorTest'`.

## Checklist de Qualidade

- [ ] Código compila sem erros (`mvn clean compile`)
- [ ] Testes passam (`mvn test`)
- [x] Arquitetura em camadas respeitada (Controller → Service → Repository)
- [x] DTOs separados da Entity
- [x] Injeção de dependência via construtor (sem `@Autowired` em atributo)
- [ ] Commits atômicos e descritivos (Conventional Commits)
- [x] `AI_USAGE.md` atualizado

## Uso de IA

- [ ] Não usei IA nesta entrega
- [x] Usei IA — detalhes registrados no `AI_USAGE.md`

## Observações

- `estoqueAtual` não usa `@Min(0)` no DTO de propósito: o enunciado exige **422** (regra de negócio) para estoque negativo, e `@Min` devolveria 400. A checagem fica no Service. `estoqueMinimo` negativo continua sendo 400 (validação de formato).
- No `ProdutoControllerIntegrationTest` mantive `@Autowired` no campo `TestRestTemplate`, igual ao padrão do `DoacaoControllerIntegrationTest` (o JUnit instancia a classe de teste).
- Os testes de integração exigem Docker.
