# Mensageria Payment Gateway

Sistema de mensageria para processamento de pagamentos construído com Spring Boot, desenhado para receber transações via API e processá-las assincronamente através do RabbitMQ. Atualmente, o processamento de pagamento está integrado com a API do Mercado Pago.

## 🏗️ Arquitetura e Módulos

O projeto adota uma arquitetura modularizada utilizando o Maven, dividido em 3 módulos principais:

- **mensageria.api**: Serviço RESTful responsável por receber as requisições de pagamento, salvar na base de dados com status inicial e publicar a mensagem na fila do RabbitMQ. Também fornece endpoints para consulta, cancelamento e reembolso.
- **mensageria.processor**: Worker assíncrono que consome as mensagens da fila do RabbitMQ e realiza o processamento real da transação junto à API de pagamento externa (Mercado Pago).
- **mensageria.commons**: Biblioteca compartilhada que contém DTOs, Enums, Exceptions e outras classes comuns usadas tanto pela API quanto pelo Processor.

## 💻 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.x**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security
  - Spring AMQP (RabbitMQ)
- **PostgreSQL**: Banco de dados relacional.
- **RabbitMQ**: Message broker para processamento assíncrono.
- **Docker & Docker Compose**: Orquestração e conteinerização de serviços.
- **Lombok**: Para redução de boilerplate.
- **Swagger/OpenAPI (Springdoc)**: Para documentação da API.

## 🚀 Como utilizar a aplicação

### 1. Pré-requisitos

Para rodar a aplicação via Docker, você precisará ter instalado em sua máquina:
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### 2. Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto (mesmo nível que o `docker-compose.yml`) baseado no arquivo `.env.example`:

```env
RABBITMQ_USER=seu-user
RABBITMQ_PASSWORD=sua-senha

DB_USERNAME=seu-user-banco-de-dados
DB_PASSWORD=sua-senha-banco-de-dados

MERCADO_PAGO_API_KEY=sua-api-key
MERCADO_PAGO_TOKEN=seu-token-mercado-pago
```

### 3. Subindo a infraestrutura

Com o Docker em execução, utilize o docker-compose para construir as imagens e subir os containers (Banco de Dados, RabbitMQ, API e Processor):

```bash
docker-compose up -d --build
```
Os seguintes serviços estarão disponíveis:
- **API**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5000` (mapeado externamente no docker-compose)
- **RabbitMQ Management**: `http://localhost:15672`

## 🔐 Autenticação da API

Para realizar requisições nos endpoints da API, é obrigatório autenticar-se fornecendo uma API Key. O projeto utiliza o Spring Security com um filtro para validar as requisições.

A validação é feita checando o Header da requisição:
```http
X-Api-Key: <SUA_API_KEY>
```

**⚠️ Importante sobre a base de dados:**
Para que uma requisição seja aceita, a *api_key* fornecida no Header deve estar cadastrada no banco de dados na tabela `api_clients` com o campo `ativo` setado para `true`. Você pode popular a base diretamente para fins de teste.

```sql
INSERT INTO api_clients (nome, api_key, ativo, data_criacao) 
VALUES ('Meu Cliente', 'SUA_API_KEY_AQUI', true, CURRENT_TIMESTAMP);
```

## 📡 Endpoints Disponíveis

A documentação interativa (Swagger) pode ser acessada em `http://localhost:8080/swagger-ui/index.html` ou `/v3/api-docs` (esses caminhos são liberados sem autenticação).

### 1. Realizar pagamento
**POST** `/api/payment`
Cria ordem de pagamento, grava na base e envia para a fila do RabbitMQ.
**Retorna:** 202 ACCEPTED

### 2. Consultar pagamento
**GET** `/api/payment/{transactionId}`
Verifica informações do pagamento e status atual na base de dados.
**Retorna:** 200 OK

### 3. Cancelar pagamento
**DELETE** `/api/payment/{transactionId}`
Cancela ordem de pagamento.
**Retorna:** 200 OK

### 4. Reembolsar pagamento
**POST** `/api/payment/refund/{transactionId}`
Reembolsa uma transação completa.
**Retorna:** 200 OK