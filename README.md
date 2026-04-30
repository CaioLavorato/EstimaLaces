# EstimaLaces

Aplicativo Android nativo para controle simples de vendas de laces, wigs e perucas.

Slogan: **Seu lucro começa no preço certo.**

## MVP

- Kotlin + Android Nativo + Jetpack Compose
- Arquitetura MVVM
- Room Database com SQLite local
- Repository Pattern
- Sem backend, sem Firebase e sem login
- Cadastro de produtos e entradas
- Registro de vendas com lucro automático
- Sugestão de venda com 100% de lucro
- Controle de clientes recorrentes e brindes
- Controle de estoque com alerta local de reposição
- Produtos e clientes flexíveis: selecionar cadastrado ou digitar novo
- Brinde por valor ou por produto do estoque
- Taxa de cartão opcional no cálculo de lucro
- Metas mensais
- Relatórios por hoje, semana e mês
- Exportação CSV compatível com Excel
- Sincronização periódica de vendas do site

## Sincronização com vendas do site

Quando o app estiver aberto, ele consulta periodicamente o endpoint de pedidos do site e importa as vendas novas para o banco local.

Configure no arquivo `local.properties`:

```properties
estimalaces.ordersApiKey=SUA_CHAVE
estimalaces.ordersSyncUrl=https://jeanniewigselaces.com.br/api/orders/sync?limit=10
```

Requisição usada pelo app:

```http
GET /api/orders/sync?limit=10
x-api-key: SUA_CHAVE
```

Ao receber as vendas, o app salva no banco local, cria produto/cliente se não existir, evita duplicidade por ID do pedido e baixa o estoque quando houver quantidade disponível.

## Estrutura

```text
app/src/main/java/com/estimalaces/app
├── data
│   ├── database
│   ├── dao
│   ├── entity
│   └── repository
├── domain
│   ├── model
│   ├── usecase
│   └── rules
├── presentation
│   ├── home
│   ├── product
│   ├── sale
│   ├── client
│   ├── goal
│   └── report
└── export
```

## Como abrir

Abra esta pasta no Android Studio e sincronize o Gradle. O projeto usa Gradle Wrapper com Android Gradle Plugin, Kotlin, Compose, Room e KSP.

Para gerar CSV, use a tela **Relatórios** e toque em **EXPORTAR PLANILHA**.
