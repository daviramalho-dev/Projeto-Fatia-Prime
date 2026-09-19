# Projeto-Fatia-Prime

## Descrição

O Projeto-Fatia-Prime é uma aplicação web para uma pizzaria, com foco em apresentação do cardápio, interação do cliente e estrutura inicial de backend para gerenciamento de usuários e pedidos.

A interface oferece uma landing page com apresentação da marca, lista de pizzas, carrinho de compras e integração com WhatsApp para envio do pedido. No backend, o projeto utiliza Spring Boot com Spring Data JPA e PostgreSQL para persistência e API REST simples.

## Tecnologias utilizadas

- Java
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- Spring Security Crypto
- PostgreSQL
- HTML5
- CSS3
- JavaScript
- Gradle

## Pré-requisitos

Antes de executar o projeto, verifique se você possui:

- JDK 26
- Git
- Navegador web moderno
- Conexão com a internet para baixar dependências do Gradle

O projeto já inclui o Gradle Wrapper, então não é necessário instalar o Gradle manualmente.

## Como executar

1. Clone o repositório:

   git clone https://github.com/daviramalho-dev/Projeto-Fatia-Prime.git

2. Acesse a pasta do projeto:

   cd Projeto-Fatia-Prime/Fatia-Prime

3. Execute a aplicação:

  Antes, configure as variáveis de conexão do PostgreSQL:

  DB_URL=jdbc:postgresql://localhost:5432/fatiaprime
  DB_USERNAME=postgres
  DB_PASSWORD=sua-senha

   ./gradlew bootRun

4. Acesse a aplicação no navegador em:

   http://127.0.0.1:8080

## Como executar os testes

Para executar a suíte atual do projeto, utilize:

./gradlew clean test --no-daemon

Esse comando compila o projeto, inicia o contexto Spring Boot e executa os testes configurados.

## Estrutura do projeto

- `Fatia-Prime/src/main/java` — classes Java do backend
- `Fatia-Prime/src/main/resources` — configuração e arquivos estáticos
- `Fatia-Prime/src/main/resources/static` — HTML, CSS e JavaScript do frontend
- `Fatia-Prime/src/test/java` — testes automatizados
- `Fatia-Prime/build.gradle` — configuração do Gradle e dependências
- `Fatia-Prime/src/main/resources/application.properties` — configuração da aplicação e do banco PostgreSQL

## API atual

Os endpoints REST atualmente disponíveis no projeto são:

### Usuários

- `GET /api/usuarios`  
  Retorna a lista de usuários cadastrados.

- `GET /api/usuarios/{id}`  
  Retorna um usuário específico por ID.

- `POST /api/usuarios`  
  Cadastra um novo usuário com validação dos dados.

### Categorias

- `GET /api/categorias`  
  Retorna a lista de categorias cadastradas.

- `GET /api/categorias/{id}`  
  Retorna uma categoria específica por ID.

### Produtos

- `GET /api/produtos`  
  Retorna os produtos ativos ordenados por nome.

- `GET /api/produtos/{id}`  
  Retorna um produto específico por ID.

### Pedidos

- `GET /api/pedidos`  
  Retorna a lista de pedidos cadastrados.

- `GET /api/pedidos/{id}`  
  Retorna um pedido específico por ID.

- `POST /api/pedidos`  
  Cria um pedido público com os dados do cliente e itens informados. `usuarioId` é opcional para compatibilidade com pedidos legados.

### Administração

- `GET /api/admin/pedidos` — lista pedidos com filtros por código, telefone, nome e status.
- `GET /api/admin/pedidos/{id}` — detalha um pedido.
- `PATCH /api/admin/pedidos/{id}/status` — atualiza o status respeitando as transições permitidas.
- `GET /api/admin/produtos` — lista produtos, inclusive inativos.
- `POST /api/admin/produtos` — cria produto.
- `PUT /api/admin/produtos/{id}` — edita produto.
- `PATCH /api/admin/produtos/{id}/status` — ativa ou desativa produto.

## Banco de dados

O projeto usa PostgreSQL, configurado pelas variáveis de ambiente:

- `DB_URL` — padrão: `jdbc:postgresql://localhost:5432/fatiaprime`
- `DB_USERNAME` — padrão: `postgres`
- `DB_PASSWORD` — sem valor padrão; deve ser configurada no ambiente
- driver: `org.postgresql.Driver`

A JPA está configurada com `ddl-auto=update`, o que permite que o Hibernate crie ou atualize as tabelas automaticamente durante a execução local. O PostgreSQL é o banco principal da aplicação.

Para uma demonstração, o banco precisa conter categorias, produtos ativos e uma conta `ADMIN` previamente provisionados. O projeto não cria automaticamente o primeiro administrador nem dados de catálogo.

## Frontend

A interface principal está em:

- `Fatia-Prime/src/main/resources/static/index.html`
- `Fatia-Prime/src/main/resources/static/css/style.css`
- `Fatia-Prime/src/main/resources/static/js/script.js`

Ela inclui apresentação da marca, catálogo, carrinho e integração com WhatsApp para finalizar os pedidos.

## Fluxo básico

1. O cliente acessa a página inicial.
2. Navega pelo cardápio.
3. Adiciona produtos ao carrinho.
4. Ajusta quantidades e revisa o total.
5. Finaliza o pedido pela API; após a criação bem-sucedida, o WhatsApp pode ser aberto com os dados retornados pelo backend.
6. O backend disponibiliza cadastro de usuários, consulta pública de pedidos e painel administrativo protegido.

## Observações

- A aplicação foi desenvolvida principalmente como projeto acadêmico.
- O banco PostgreSQL é necessário para a execução normal da aplicação.
- O projeto continua com a arquitetura simples atual: controllers + repositories + entidades.
- Não há autenticação ou pagamento implementados nesta etapa.
