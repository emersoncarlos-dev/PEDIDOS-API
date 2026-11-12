# Plataforma de Pedidos para Pequenos Comércios

Sistema fullstack de **cardápio digital e pedidos em tempo real** para pequenos comércios (lanchonetes, padarias, hamburguerias, entre outros). O cliente abre o cardápio por um link, monta o pedido e acompanha o andamento. O dono do comércio recebe os pedidos em um painel e atualiza o status de cada um.

> **Status do projeto:** em desenvolvimento. O núcleo do backend (comércios, cardápio, criação de pedidos, listagem e atualização de status) já funciona. Testes automatizados, Docker, frontend, autenticação, tempo real e deploy estão no [roadmap](#roadmap).

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
- Painel com os pedidos recebidos e os itens de cada um
- Alterar o status de cada pedido
- Cadastrar o comércio e gerenciar os produtos do cardápio
- Marcar produtos como disponíveis ou indisponíveis
- Login para acesso ao painel

> As funcionalidades já disponíveis na API estão na seção [Endpoints](#endpoints). As demais estão no [roadmap](#roadmap).

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

### Organização do backend

O backend é dividido em camadas, cada uma com uma responsabilidade:

| Camada | Responsabilidade |
|---|---|
| `controller` | Recebe as requisições HTTP e devolve as respostas |
| `dto` | Define os dados que entram e saem da API (separados das entidades) |
| `service` | Concentra as regras de negócio |
| `repository` | Acessa o banco de dados |
| `entity` | Representa as tabelas do banco |
| `exception` | Trata os erros e devolve mensagens claras |

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

## Regras de negócio

As regras abaixo são aplicadas pelo backend, e não dependem do cliente:

- **O total do pedido é calculado no servidor**, a partir dos preços cadastrados no banco. O cliente informa apenas os produtos e as quantidades.
- **O preço do produto é copiado para o item do pedido** no momento da compra. Mudar o preço no cardápio depois não altera os pedidos antigos.
- **Um pedido só aceita produtos do próprio comércio.** Produtos de outro comércio são recusados.
- **Produtos indisponíveis não podem ser pedidos.**
- **O pedido precisa ter pelo menos um item**, com quantidade maior que zero.
- **Pedidos entregues ou cancelados não podem mais mudar de status.**
- **Pedido e itens são gravados em uma única transação:** se algo falhar, nada é salvo pela metade.
- **O `slug` do comércio é único** e só aceita letras minúsculas, números e hífens.

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

O Hibernate está configurado com `ddl-auto=validate`: ele apenas confere se as classes batem com as tabelas, sem criar nem alterar nada. As tabelas são controladas pelo script SQL acima.

---

## Estrutura do repositório

```
PEDIDOS-API/
├── backend/                         # API em Spring Boot
│   ├── src/main/java/com/plataforma/pedidos_api/
│   │   ├── controller/              # Endpoints da API
│   │   ├── dto/                     # Dados de entrada e saída
│   │   ├── entity/                  # Classes que representam as tabelas
│   │   ├── exception/               # Tratamento de erros
│   │   ├── repository/              # Acesso ao banco
│   │   └── service/                 # Regras de negócio
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/                        # Interface em React (planejado)
├── docker-compose.yml               # Planejado
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

A `DB_URL` usa o formato JDBC: o prefixo `jdbc:postgresql://`, seguido de host, porta e nome do banco, sem usuário e senha dentro. As variáveis valem apenas para a janela do terminal em que foram definidas.

### 2. Rodar a aplicação

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/comercios` | Lista os comércios cadastrados |
| `POST` | `/comercios` | Cadastra um comércio (o `slug` precisa ser único, senão retorna 409) |
| `GET` | `/produtos/comercio/{comercioId}` | Lista o cardápio de um comércio |
| `POST` | `/produtos` | Cadastra um produto |
| `POST` | `/pedidos` | Cria um pedido (valida os produtos e calcula o total no servidor) |
| `GET` | `/pedidos/comercio/{comercioId}` | Lista os pedidos do comércio, do mais recente ao mais antigo, com os itens |
| `PATCH` | `/pedidos/{id}/status` | Atualiza o status do pedido |

### Exemplo: cadastrar um comércio

`POST /comercios`

```json
{
  "nome": "Padaria do Zé",
  "slug": "padaria-do-ze",
  "telefone": "81999999999"
}
```

O `slug` é o nome do comércio na URL: só aceita letras minúsculas, números e hífens, e não pode se repetir.

### Exemplo: criar um pedido

`POST /pedidos`

```json
{
  "comercioId": "uuid-do-comercio",
  "nomeCliente": "Maria",
  "telefoneCliente": "81999999999",
  "itens": [
    { "produtoId": "uuid-do-produto", "quantidade": 2 }
  ]
}
```

O cliente não envia preço nem total. Resposta (`201 Created`):

```json
{
  "id": "uuid-do-pedido",
  "comercioId": "uuid-do-comercio",
  "nomeCliente": "Maria",
  "telefoneCliente": "81999999999",
  "status": "recebido",
  "total": 37.00,
  "criadoEm": "2026-11-10T21:39:17.161190Z"
}
```

Exemplo no PowerShell:

```powershell
$body = @{
  comercioId = "uuid-do-comercio"
  nomeCliente = "Maria"
  telefoneCliente = "81999999999"
  itens = @(@{ produtoId = "uuid-do-produto"; quantidade = 2 })
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Method Post -Uri http://localhost:8080/pedidos -ContentType "application/json" -Body $body
```

### Exemplo: listar os pedidos de um comércio

`GET /pedidos/comercio/{comercioId}`

```json
[
  {
    "id": "uuid-do-pedido",
    "comercioId": "uuid-do-comercio",
    "nomeCliente": "Maria",
    "telefoneCliente": "81999999999",
    "status": "recebido",
    "total": 37.00,
    "criadoEm": "2026-11-10T21:39:17.161190Z",
    "itens": [
      {
        "produtoId": "uuid-do-produto",
        "nomeProduto": "Pão Francês",
        "quantidade": 2,
        "precoUnitario": 18.50,
        "subtotal": 37.00
      }
    ]
  }
]
```

### Exemplo: atualizar o status

`PATCH /pedidos/{id}/status`

```json
{ "status": "em_preparo" }
```

Valores aceitos: `recebido`, `em_preparo`, `pronto`, `entregue` e `cancelado`.

### Respostas de erro

Os erros voltam em JSON, com uma mensagem clara:

| Código | Quando acontece |
|---|---|
| `400 Bad Request` | Dado inválido ou regra de negócio violada |
| `404 Not Found` | O recurso da URL não existe (por exemplo, pedido inexistente) |
| `409 Conflict` | O dado colide com um que já existe (por exemplo, `slug` repetido) |

Regra de negócio (`400`):

```json
{ "erro": "O produto X-Burger está indisponível" }
```

Dados inválidos (`400`):

```json
{
  "erro": "Dados inválidos",
  "detalhes": ["itens[0].quantidade: deve ser maior que 0"]
}
```

Recurso inexistente (`404`):

```json
{ "erro": "Pedido não encontrado" }
```

Conflito (`409`):

```json
{ "erro": "Já existe um comércio com o slug: padaria-do-ze" }
```

---

## Roadmap

### Concluído
- [x] Modelagem e criação do banco de dados no Supabase
- [x] Projeto Spring Boot criado e conectado ao banco
- [x] Entidades `Comercio`, `Produto`, `Pedido` e `ItemPedido`, com seus repositories
- [x] Listagem de comércios e do cardápio, e cadastro de produtos
- [x] Cadastro de comércios, com slug único (409 em caso de duplicidade)
- [x] Criação de pedidos com validação e cálculo de total no servidor
- [x] Regras de negócio (produto indisponível, produto de outro comércio, transação)
- [x] Tratamento de erros com mensagens claras
- [x] Respostas `404 Not Found` para recursos que não existem
- [x] Listagem de pedidos com os itens de cada um
- [x] Atualização de status do pedido, com regra para pedidos finalizados

### Próximos passos
- [ ] DTOs e validação no cadastro de produtos
- [ ] Opção de marcar produto como indisponível
- [ ] Testes automatizados
- [ ] Docker e Docker Compose
- [ ] Pipeline de CI com GitHub Actions
- [ ] Frontend do cliente (cardápio e carrinho)
- [ ] Painel do dono (pedidos e status)
- [ ] Autenticação do dono do comércio
- [ ] Atualização de pedidos em tempo real
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