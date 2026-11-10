# Plataforma de Pedidos para Pequenos Comércios

Sistema fullstack de **cardápio digital e pedidos em tempo real** para pequenos comércios (lanchonetes, padarias, hamburguerias, entre outros). O cliente abre o cardápio por um link, monta o pedido e acompanha o andamento. O dono do comércio recebe os pedidos em um painel e atualiza o status de cada um.

> **Status do projeto:** em desenvolvimento. A base do backend e a conexão com o banco já funcionam. As demais funcionalidades estão no [roadmap](#roadmap).

---

## Sobre o projeto

Este projeto foi criado como **projeto de portfólio** após a conclusão do curso de Análise e Desenvolvimento de Sistemas (ADS). O objetivo é praticar o desenvolvimento de um produto completo, do banco de dados até o deploy, aplicando boas práticas de código e de DevOps.

### Problema que resolve

Muitos pequenos comércios recebem pedidos por mensagem, anotam à mão e perdem o controle do que foi pedido, do que está em preparo e do que já foi entregue. Esta plataforma centraliza o cardápio e os pedidos em um único lugar, com atualização imediata para o dono e para o cliente.

### Como o sistema funciona

1. O cliente abre o cardápio do comércio por um link público (por exemplo, `/cardapio/padaria-do-ze`).
2. Escolhe os produtos, monta o carrinho e envia o pedido.
3. O pedido é gravado no banco e aparece **na hora** no painel do dono.
4. O dono muda o status do pedido: `recebido`, `em_preparo`, `pronto`, `entregue` ou `cancelado`.
5. O cliente vê o status do seu pedido atualizar na tela.

---

## Funcionalidades

### Para o cliente
- Ver o cardápio de um comércio por link público
- Montar o carrinho e finalizar o pedido
- Acompanhar o status do pedido em tempo real

### Para o dono do comércio
- Painel com os pedidos recebidos
- Alterar o status de cada pedido
- Cadastrar e gerenciar os produtos do cardápio
- Marcar produtos como disponíveis ou indisponíveis
- Login para acesso ao painel

---

## Arquitetura

```
Cliente / Dono (React)
        │
        ▼
Backend Java (Spring Boot) ──► Supabase (PostgreSQL)
        │
        └─► Atualizações em tempo real para o painel
```

O frontend conversa apenas com o backend. O backend aplica as regras de negócio (validações, cálculo de total, controle de status) e acessa o banco de dados.

A forma de entregar as atualizações em tempo real ainda será definida entre duas opções:
- Realtime do Supabase, assinado pelo frontend
- WebSocket servido pelo próprio Spring Boot

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot, Spring Web, Spring Data JPA, Bean Validation |
| Build | Maven (com Maven Wrapper) |
| Banco de dados | PostgreSQL hospedado no Supabase |
| Frontend (planejado) | React |
| DevOps (planejado) | Docker, Docker Compose, GitHub Actions |
| Deploy (planejado) | Serviço com plano gratuito (Vercel, Render ou Railway) |

---

## Modelo de dados

O banco tem 4 tabelas:

| Tabela | O que guarda |
|---|---|
| `comercios` | Cada comércio cadastrado |
| `produtos` | Os itens do cardápio de cada comércio |
| `pedidos` | Cada pedido feito por um cliente |
| `itens_pedido` | Os produtos que fazem parte de cada pedido |

```
comercios ──< produtos
    │
    └──────< pedidos ──< itens_pedido >── produtos
```

O preço do produto é copiado para `itens_pedido.preco_unitario` no momento da compra. Assim, mudar o preço no cardápio não altera os pedidos antigos.

### Script de criação

```sql
create table comercios (
  id uuid primary key default gen_random_uuid(),
  nome text not null,
  slug text not null unique,
  telefone text,
  criado_em timestamptz not null default now()
);

create table produtos (
  id uuid primary key default gen_random_uuid(),
  comercio_id uuid not null references comercios(id) on delete cascade,
  nome text not null,
  descricao text,
  preco numeric(10,2) not null check (preco >= 0),
  disponivel boolean not null default true,
  criado_em timestamptz not null default now()
);

create table pedidos (
  id uuid primary key default gen_random_uuid(),
  comercio_id uuid not null references comercios(id),
  nome_cliente text not null,
  telefone_cliente text,
  status text not null default 'recebido'
    check (status in ('recebido','em_preparo','pronto','entregue','cancelado')),
  total numeric(10,2) not null default 0,
  criado_em timestamptz not null default now()
);

create table itens_pedido (
  id uuid primary key default gen_random_uuid(),
  pedido_id uuid not null references pedidos(id) on delete cascade,
  produto_id uuid not null references produtos(id),
  quantidade integer not null check (quantidade > 0),
  preco_unitario numeric(10,2) not null
);
```

O Row Level Security (RLS) fica habilitado nas tabelas. O backend acessa o banco diretamente pela conexão PostgreSQL, e o acesso pela API pública do Supabase fica bloqueado.

---

## Estrutura do repositório

```
plataforma-pedidos/
├── backend/                 # API em Spring Boot
│   ├── src/main/java/com/plataforma/pedidos_api/
│   │   ├── entity/          # Classes que representam as tabelas
│   │   ├── repository/      # Acesso ao banco
│   │   └── controller/      # Endpoints da API
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/                # Interface em React (planejado)
├── docker-compose.yml       # Planejado
└── README.md
```

---

## Como executar o backend

### Pré-requisitos

- Java 21 ou superior
- Uma conta no [Supabase](https://supabase.com) com um projeto criado e as tabelas do script acima

### 1. Configurar as variáveis de ambiente

O backend lê os dados de conexão de variáveis de ambiente. **Nunca coloque a senha do banco dentro do código ou do repositório.**

Use os dados do **Session pooler** do Supabase (menu **Connect** do projeto).

PowerShell (Windows):

```powershell
$env:DB_URL='jdbc:postgresql://HOST:5432/postgres'
$env:DB_USER='postgres.CODIGO_DO_PROJETO'
$env:DB_PASSWORD='sua_senha_do_banco'
```

Linux / macOS / WSL:

```bash
export DB_URL='jdbc:postgresql://HOST:5432/postgres'
export DB_USER='postgres.CODIGO_DO_PROJETO'
export DB_PASSWORD='sua_senha_do_banco'
```

A `DB_URL` usa o formato JDBC: o prefixo `jdbc:postgresql://`, seguido de host, porta e nome do banco, sem usuário e senha dentro.

### 2. Rodar a aplicação

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

---

## Endpoints disponíveis

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/comercios` | Lista todos os comércios cadastrados |

Os demais endpoints (produtos, pedidos, status) serão adicionados conforme o roadmap.

---

## Roadmap

- [x] Modelagem e criação do banco de dados no Supabase
- [x] Projeto Spring Boot criado e conectado ao banco
- [x] Entidade `Comercio` e repository
- [x] Endpoint `GET /comercios`
- [ ] Entidades `Produto`, `Pedido` e `ItemPedido`
- [ ] Endpoints do cardápio e de criação de pedidos
- [ ] Regras de negócio e validações (produto indisponível, total do pedido)
- [ ] Autenticação do dono do comércio
- [ ] Atualização de pedidos em tempo real
- [ ] Frontend do cliente (cardápio e carrinho)
- [ ] Painel do dono (pedidos e status)
- [ ] Testes automatizados
- [ ] Docker e Docker Compose
- [ ] Pipeline de CI com GitHub Actions
- [ ] Deploy online

---

## Práticas de DevOps planejadas

- **Docker** para empacotar o backend e o frontend
- **Docker Compose** para subir o ambiente completo com um único comando
- **GitHub Actions** para compilar e rodar os testes a cada push
- **Variáveis de ambiente** para todas as credenciais, sem segredos no repositório
- **Deploy automático** em um serviço com plano gratuito

---

## Autor

**Emerson Carlos Pereira**
Analista e desenvolvedor de sistemas

- GitHub: https://github.com/emersoncarlos-dev
- LinkedIn: https://www.linkedin.com/in/emerson-pereira-20b859239/
