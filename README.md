# Sistema de Transações Simplificado

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

Uma API REST que simula um sistema de transferências entre usuários, desenvolvida com **Java 21** e **Spring Boot 4**.

O projeto foi construído com foco em regras de negócio de um sistema financeiro real: validação de elegibilidade, autorização externa antes de mover saldo, integridade transacional, idempotência e controle de concorrência. Além de separação clara de responsabilidades entre camadas e cobertura de testes específica para cada uma delas.

---

# Funcionalidades

- ✅ Cadastro de usuários, com dois perfis: **comum** (CPF) e **lojista** (CNPJ), validados de acordo com o tipo.
- ✅ Exclusão lógica de usuário (soft delete): histórico de transações nunca é perdido.
- ✅ Transferência entre usuários, com validação completa: valor positivo, remetente ≠ destinatário, elegibilidade do remetente (lojista não pode enviar, saldo suficiente).
- ✅ Integração com serviço externo de **autorização**, transação só é efetivada se o autorizador aprovar.
- ✅ Integração com serviço externo de **notificação**, disparada após o commit da transação (`@TransactionalEventListener`). Falha na notificação nunca desfaz uma transferência já concluída.
- ✅ **Idempotência** na criação de transferências via chave enviada pelo cliente: repetir a mesma requisição não duplica a transação.
- ✅ **Controle de concorrência** (lock otimista via `@Version`): evita corrupção de saldo em atualizações simultâneas.
- ✅ Listagem de transações (enviadas e recebidas) por usuário.
- ✅ Tratamento de erro centralizado, com um código de status HTTP e um corpo de erro padronizado para cada tipo de falha.
- ✅ Documentação interativa da API via **Swagger/OpenAPI**.
- ✅ Persistência em **PostgreSQL**, com a aplicação e o banco containerizados via **Docker Compose**.

---

# Como Executar

## Pré-requisitos

- [Docker](https://www.docker.com/) e Docker Compose (única dependência. O Postgres sobe junto, containerizado)

## 1. Clone o repositório

```bash
git clone https://github.com/nkgabriiel/simplified-transcation-system.git
cd simplified-transcation-system
```

## 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
```

Abra o `.env` e defina um usuário/senha para o banco (qualquer valor serve, é criado do zero pelo container do Postgres).

## 3. Suba a aplicação

```bash
docker compose up --build
```

A aplicação estará disponível em:

```
http://localhost:8080
```

A documentação interativa da API (Swagger UI) fica em:

```
http://localhost:8080/swagger-ui.html
```

## Rodando sem Docker

Também é possível rodar localmente com um Postgres já instalado na máquina: defina as mesmas variáveis do `.env` apontando pro seu banco local e execute `./mvnw spring-boot:run`.

---

# Exemplo de Uso

## Requisição

Cadastrar um usuário comum.

```bash
curl -X POST http://localhost:8080/api/usuarios/criar \
-H "Content-Type: application/json" \
-d '{
  "nome": "João da Silva",
  "email": "joao.silva@email.com",
  "documento": "12345678900",
  "tipo": "COMUM"
}'
```

## Resposta

**201 Created**

```json
{
  "id": 1,
  "nome": "João da Silva",
  "documento": "12345678900",
  "email": "joao.silva@email.com",
  "saldo": 0,
  "tipoUsuario": "COMUM"
}
```

---

# Referência da API

Todas as rotas possuem o prefixo `/api`. Referência completa, com todos os códigos de erro possíveis por endpoint, disponível no Swagger UI (`/swagger-ui.html`) com a aplicação rodando.

| Método | Endpoint                          | Descrição                                          | Resposta            |
|--------|------------------------------------|-----------------------------------------------------|----------------------|
| POST   | `/usuarios/criar`                 | Cadastra um novo usuário                            | **201 Created**      |
| GET    | `/usuarios/{id}`                  | Busca um usuário pelo id                            | **200 OK**            |
| DELETE | `/usuarios/{id}`                  | Desativa um usuário (exclusão lógica)                | **204 No Content**    |
| POST   | `/transacoes/transferir`          | Realiza uma transferência entre dois usuários (idempotente)       | **201 Created**       |
| GET    | `/transacoes?usuarioId={id}`      | Lista as transações (enviadas e recebidas) de um usuário | **200 OK**       |

---

# Arquitetura

O projeto segue uma arquitetura em camadas, com uma camada extra dedicada só a integrações externas.

```
Controller
     │
     ▼
Service
     │
     ├──▶ Repository ──▶ Database
     │
     └──▶ Client ──▶ API externa (autorização / notificação)
```

## Camadas

### Controllers
Recebem as requisições HTTP, validam o corpo da requisição (`@Valid`) e devolvem respostas padronizadas.

### Services
Concentram toda a regra de negócio: validações, orquestração da transferência, decisão sobre autorizar ou não.

### Clients
Encapsulam a comunicação com as duas APIs externas do sistema. O client de **autorização** propaga falha (a transação é interrompida se o serviço estiver indisponível). O client de **notificação** absorve falha internamente e é disparado por um listener assíncrono ao commit (`@TransactionalEventListener`) desacoplado do fluxo principal, uma notificação que não pôde ser enviada nunca desfaz uma transferência já concluída.

### Repositories
Camada de persistência via Spring Data JPA.

### DTOs
Transportam dados entre cliente e servidor (`request`/`response`), sem expor as entidades do banco diretamente.

### GlobalExceptionHandler
Centraliza o mapeamento de cada exceção de negócio para o status HTTP e corpo de erro correspondentes.

---

# Stack Tecnológica

| Tecnologia | Utilização |
|------------|------------|
| Java 21 | Linguagem principal |
| Spring Boot 4.1 | Framework principal |
| Spring Data JPA / Hibernate | Persistência |
| PostgreSQL | Banco de dados relacional |
| Spring RestClient | Consumo das APIs externas |
| Docker / Docker Compose | Containerização da aplicação e do banco |
| Springdoc OpenAPI | Documentação interativa da API (Swagger UI) |
| Bean Validation | Validação de entrada |
| Lombok | Redução de código boilerplate |
| JUnit 5 / Mockito / AssertJ | Testes |
| MockRestServiceServer | Testes de integração dos clients HTTP |
| H2 | Banco em memória, usado apenas nos testes de repositório |

---

# Testes

Execute todos os testes com:

```bash
./mvnw test
```

A suíte cobre cada camada com a ferramenta apropriada para o que ela precisa provar:

- **Service** (Mockito): regras de negócio isoladas de qualquer dependência externa.
- **Client HTTP** (`MockRestServiceServer`): o `RestClient` real, contra um servidor HTTP fake, validando serialização/desserialização e tratamento de erro de verdade.
- **Repository** (`@DataJpaTest`): as queries derivadas do Spring Data, executadas contra um banco H2 embutido (isolado do Postgres usado em produção).
- **Controller** (`@WebMvcTest` + `MockMvc`): roteamento, validação de entrada e o `GlobalExceptionHandler` reagindo a cada tipo de erro.

---

# Estrutura do Projeto

```text
src
├── main
│   ├── java/com/springboot/picpay/simplified
│   │   ├── config
│   │   ├── controller
│   │   ├── service
│   │   ├── client
│   │   │   ├── autorizacao
│   │   │   ├── notificacao
│   │   │   └── config
│   │   ├── repository
│   │   ├── dto
│   │   │   ├── request
│   │   │   └── response
│   │   ├── model
│   │   └── exception
│   └── resources
│       └── application.properties
└── test
    └── java/com/springboot/picpay/simplified
        ├── service
        ├── client
        ├── repository
        └── controller
```

---

# Roadmap

Projeto em evolução. Próximos passos planejados:

- [ ] Pipeline de CI (rodar a suíte de testes automaticamente)
- [ ] Autenticação com JWT
- [ ] Refatoração e revisão geral de consistência entre camadas
