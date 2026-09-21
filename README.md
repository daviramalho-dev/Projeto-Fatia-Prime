# Projeto-Fatia-Prime

## Descrição

O Projeto-Fatia-Prime é uma aplicação web integrada para uma pizzaria, com foco em apresentação do cardápio, interação do cliente e gerenciamento de usuários, produtos e pedidos.

A interface oferece uma landing page com apresentação da marca, catálogo de pizzas carregado pela API, categorias, filtros, carrinho de compras e integração com WhatsApp para envio do pedido. No backend, o projeto utiliza Spring Boot, Spring Data JPA, Spring Security, Spring Validation e H2, com API REST integrada ao frontend.

## Tecnologias utilizadas

- Java
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- Spring Security
- H2 Database
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
- Para executar no Render, a aplicação utiliza a variável de ambiente `PORT`.

O projeto já inclui o Gradle Wrapper, então não é necessário instalar o Gradle manualmente.

## Como executar

1. Clone o repositório:

   git clone https://github.com/daviramalho-dev/Projeto-Fatia-Prime.git

2. Acesse a pasta do projeto:

   cd Projeto-Fatia-Prime/Fatia-Prime

3. Execute a aplicação:

   ./gradlew bootRun

4. Acesse a aplicação no navegador em:

   http://127.0.0.1:8080

Em produção, a aplicação está preparada para execução no Render em:

https://fatia-prime.onrender.com

O Render executa a aplicação Spring Boot completa, incluindo o frontend e o backend. Localmente, a aplicação escuta em `0.0.0.0` e utiliza a porta `8080` por padrão, ou a porta informada pela variável `PORT`.

## Como executar os testes

Para executar a suíte atual do projeto, utilize:

./gradlew clean test --no-daemon

Esse comando compila o projeto, inicia o contexto Spring Boot e executa os testes configurados.

## Estrutura do projeto

- `Fatia-Prime/src/main/java` — classes Java do backend
- `Fatia-Prime/src/main/java/com/example/Fatia/Prime` — entidades `Usuario`, `Categoria`, `Produto`, `Pedido` e `ItemPedido`; controllers, repositories, requests, responses, autenticação e tratamento de erros
- `Fatia-Prime/src/main/resources` — configuração e arquivos estáticos
- `Fatia-Prime/src/main/resources/data.sql` — seed inicial de categorias e produtos
- `Fatia-Prime/src/main/resources/static` — HTML, CSS e JavaScript do frontend
- `Fatia-Prime/src/test/java` — testes automatizados
- `Fatia-Prime/src/test/java/com/example/Fatia/Prime` — testes de pedidos públicos, produtos administrativos, pedidos administrativos, segurança e contexto da aplicação
- `Fatia-Prime/build.gradle` — configuração do Gradle e dependências
- `Fatia-Prime/Dockerfile` — imagem para execução e deploy no Render
- `Fatia-Prime/src/main/resources/application.properties` — configuração da aplicação e do banco H2

O projeto mantém a arquitetura simples de frontend estático servido pelo Spring Boot, com controllers, repositories e JPA no backend. Não há uma camada Service separada.

## API atual

Os endpoints REST atualmente disponíveis no projeto são:

### Usuários

- `GET /api/usuarios`  
  Retorna a lista de usuários cadastrados para administradores autenticados.

- `GET /api/usuarios/{id}`  
  Retorna um usuário específico por ID para administradores autenticados.

- `POST /api/usuarios`  
  Cadastra um novo usuário com validação dos dados. O endpoint exige autenticação administrativa e não é um cadastro público livre.

A autenticação administrativa utiliza os seguintes endpoints:

- `GET /api/auth/csrf`
  Retorna o token CSRF usado nas operações protegidas.

- `POST /api/auth/login`
  Realiza o login administrativo por e-mail e senha.

- `POST /api/auth/logout`
  Encerra a sessão administrativa.

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

- `GET /api/admin/produtos` e `GET /api/admin/produtos/{id}`
  Listam e detalham produtos para administradores autenticados, com filtros administrativos na listagem.

- `POST /api/admin/produtos` e `PUT /api/admin/produtos/{id}`
  Criam e editam produtos.

- `PATCH /api/admin/produtos/{id}/status`
  Ativa ou desativa um produto sem excluí-lo.

### Pedidos

- `GET /api/pedidos`  
  Retorna a lista administrativa de pedidos autenticados.

- `GET /api/pedidos/{id}`  
  Retorna um pedido específico por ID para administradores autenticados.

- `POST /api/pedidos`  
  Cria um pedido público com os dados do cliente e dos itens. O preço e o total são calculados pelo backend.

- `GET /api/pedidos/consulta?codigo=...` ou `?telefone=...`
  Consulta publicamente o status e o resumo de um pedido.

- `GET /api/admin/pedidos` e `GET /api/admin/pedidos/{id}`
  Listam, filtram e detalham pedidos para administradores autenticados.

- `PATCH /api/admin/pedidos/{id}/status`
  Altera o status do pedido seguindo as transições permitidas.

## Banco de dados

O projeto usa H2 em memória, configurado em:

- `spring.datasource.url=jdbc:h2:mem:fatiaprime`
- driver H2
- usuário: `sa`
- senha: vazia

A JPA está configurada com `ddl-auto=update`, o que permite que o Hibernate crie ou atualize as tabelas automaticamente durante a execução. As categorias e os produtos iniciais são carregados de `Fatia-Prime/src/main/resources/data.sql`.

As entidades persistidas são `Usuario`, `Categoria`, `Produto`, `Pedido` e `ItemPedido`. Os relacionamentos principais são produtos associados a categorias, pedidos associados opcionalmente a usuários e itens associados a pedidos e produtos.

Os dados do H2 em memória são perdidos quando a aplicação reinicia. Categorias e produtos iniciais são recriados pelo seed. Essa configuração atende ao objetivo acadêmico e demonstrativo atual, mas o H2 em memória não deve ser apresentado como banco persistente de produção.

O H2 Console existe em `/h2-console`, mas é protegido para administradores autenticados.

## Frontend

A interface principal está em:

- `Fatia-Prime/src/main/resources/static/index.html`
- `Fatia-Prime/src/main/resources/static/css/style.css`
- `Fatia-Prime/src/main/resources/static/js/script.js`

Ela inclui apresentação da marca, catálogo de produtos vindo da API, categorias e filtros, carrinho com `localStorage`, checkout, validações, confirmação do pedido, consulta pública de pedidos e integração com WhatsApp para finalizar os pedidos. As operações administrativas de pedidos e produtos também são integradas ao backend.

## Fluxo básico

1. O cliente acessa a página inicial.
2. Navega pelo cardápio.
3. Adiciona produtos ao carrinho.
4. Ajusta quantidades e revisa o total.
5. O checkout cria o pedido pela API e persiste no H2.
6. A confirmação apresenta o código retornado e oferece o envio dos dados pelo WhatsApp.
7. O pedido pode ser consultado pelo código ou telefone.

O frontend é servido pelo próprio Spring Boot e se comunica com as APIs REST do backend para catálogo, checkout, pedidos e administração.

## Observações

- A aplicação foi desenvolvida principalmente como projeto acadêmico.
- O banco H2 em memória é recriado a cada execução; os dados são perdidos após reinício e o seed recria categorias e produtos iniciais.
- O projeto continua com a arquitetura simples atual: HTML5, CSS3 e JavaScript no frontend; Spring Boot com controllers, repositories e JPA no backend; e H2 como banco.
- O login administrativo protege as rotas administrativas, e as senhas são armazenadas como hash.
- O carrinho utiliza `localStorage`, enquanto o checkout e os pedidos são processados pelo backend.
- O deploy atual está preparado para o Render: https://fatia-prime.onrender.com
- A configuração histórica de GitHub Pages está relacionada ao workflow anterior de arquivos estáticos; a aplicação completa depende do Spring Boot e do backend.
- Última atualização: setembro de 2026.
