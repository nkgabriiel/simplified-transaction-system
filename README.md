# PicPay Simplificado

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)

Uma API REST que simula um sistema de transferências entre usuários (baseada no desafio clássico "PicPay Simplificado"), desenvolvida com **Java 21** e **Spring Boot 4**.

O projeto foi construído com foco em regras de negócio de um sistema financeiro real — validação de elegibilidade, autorização externa antes de mover saldo, integridade transacional e notificação com falha isolada — além de separação clara de responsabilidades entre camadas e cobertura de testes específica para cada uma delas.

---

# Funcionalidades

- ✅ Cadastro de usuários, com dois perfis: **comum** (CPF) e **lojista** (CNPJ), validados de acordo com o tipo.
- ✅ Exclusão lógica de usuário (soft delete) — histórico de transações nunca é perdido.
- ✅ Transferência entre usuários, com validação completa: valor positivo, remetente ≠ destinatário, elegibilidade do remetente (lojista não pode enviar, saldo suficiente).
- ✅ Integração com serviço externo de **autorização** — a transação só é efetivada se o autorizador aprovar.
- ✅ Integração com serviço externo de **notificação** — falha na notificação nunca desfaz uma transferência já concluída.
- ✅ Listagem de transações (enviadas e recebidas) por usuário.
- ✅ Tratamento de erro centralizado, com um código de status HTTP e um corpo de erro padronizado para cada tipo de falha.

---

# Como Executar

## Pré-requisitos

- Java 21+
- Não é necessário instalar banco de dados — o projeto usa H2 em memória, embutido na aplicação.

## 1. Clone o repositório

```bash
git clone https://github.com/nkgabriiel/simplified-transaction-system.git
cd simplified-transaction-system
```

## 2. Execute a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em:

```
http://localhost:8080
```

O console do H2 (pra inspecionar o banco em memória durante o desenvolvimento) fica em:

```
http://localhost:8080/h2-console
```

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

Todas as rotas possuem o prefixo `/api`.

| Método | Endpoint                          | Descrição                                          | Resposta            |
|--------|------------------------------------|-----------------------------------------------------|----------------------|
| POST   | `/usuarios/criar`                 | Cadastra um novo usuário                            | **201 Created**      |
| GET    | `/usuarios/{id}`                  | Busca um usuário pelo id                            | **200 OK**            |
| DELETE | `/usuarios/{id}`                  | Desativa um usuário (exclusão lógica)                | **204 No Content**    |
| POST   | `/transacoes/transferir`          | Realiza uma transferência entre dois usuários       | **201 Created**       |
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
Encapsulam a comunicação com as duas APIs externas do sistema. O client de **autorização** propaga falha (a transação é interrompida se o serviço estiver indisponível). O client de **notificação** absorve falha internamente — uma notificação que não pôde ser enviada nunca desfaz uma transferência já concluída.

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
| Spring RestClient | Consumo das APIs externas |
| H2 | Banco de dados (em memória) |
| Bean Validation | Validação de entrada |
| Lombok | Redução de código boilerplate |
| JUnit 5 / Mockito / AssertJ | Testes |
| MockRestServiceServer | Testes de integração dos clients HTTP |

---

# Testes

Execute todos os testes com:

```bash
./mvnw test
```

A suíte cobre cada camada com a ferramenta apropriada para o que ela precisa provar:

- **Service** (Mockito) — regras de negócio isoladas de qualquer dependência externa.
- **Client HTTP** (`MockRestServiceServer`) — o `RestClient` real, contra um servidor HTTP fake, validando serialização/desserialização e tratamento de erro de verdade.
- **Repository** (`@DataJpaTest`) — as queries derivadas do Spring Data, executadas contra um banco H2 real.
- **Controller** (`@WebMvcTest` + `MockMvc`) — roteamento, validação de entrada e o `GlobalExceptionHandler` reagindo a cada tipo de erro.

---

# Estrutura do Projeto

```text
src
├── main
│   ├── java/com/springboot/picpay/simplified
│   │   ├── controller
│   │   ├── service
│   │   ├── client
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

Projeto em evolução — próximos passos planejados:

- [ ] Documentação interativa da API (Swagger/OpenAPI)
- [ ] Migração do H2 para um banco relacional persistente
- [ ] Containerização com Docker
- [ ] Autenticação com JWT
- [ ] Controle de concorrência no saldo (lock otimista)
