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
- Metas mensais
- Relatórios por hoje, semana e mês
- Exportação CSV compatível com Excel

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
