# 🍕 Projeto Fatia Prime

<p align="center"><strong>Cardápio, pedidos e administração para uma pizzaria em uma aplicação web integrada.</strong></p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-26-ED8B00?logo=openjdk&logoColor=white" alt="Java 26">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/Spring%20Data-JPA-6DB33F?logo=spring&logoColor=white" alt="Spring Data JPA">
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/H2-Database-09476B?logo=h2database&logoColor=white" alt="H2 Database">
  <img src="https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white" alt="HTML5">
  <img src="https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white" alt="CSS3">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=222" alt="JavaScript">
</p>

## Sobre o projeto

O Projeto Fatia Prime é uma aplicação web integrada para uma pizzaria, com foco na apresentação do cardápio, na experiência de pedidos dos clientes e no gerenciamento administrativo de usuários, produtos e pedidos.

A interface apresenta a marca e carrega o catálogo pela API. O frontend está integrado ao backend Spring Boot, que disponibiliza uma API REST e utiliza Spring Data JPA, Spring Security, Spring Validation e H2.

## Funcionalidades

### Para clientes

- Catálogo de pizzas salgadas e doces e de bebidas, com categorias, filtros e imagens disponíveis.
- Personalização de pizza inteira ou meio a meio; no meio a meio, o preço base usa o sabor mais caro.
- Bordas recheadas, adicionais e molhos compatíveis, com preços configuráveis e resumo atualizado.
- Entrega calculada pelo CEP, com subtotal, frete e total apresentados separadamente no pedido.
- Carrinho de compras com armazenamento local no navegador (`localStorage`).
- Checkout com validação dos dados e criação do pedido pela API.
- Confirmação do pedido com código para consulta posterior.
- Consulta pública do pedido por código ou telefone e acompanhamento do status.
- Opção de compartilhar os dados do pedido pelo WhatsApp.

### Para administradores

- Autenticação administrativa.
- Gerenciamento, consulta e atualização do status dos pedidos.
- Cadastro, edição, consulta, ativação e desativação de produtos (pizzas e bebidas).
- Gerenciamento administrativo de categorias.
- Configuração de bordas recheadas, adicionais e molhos, incluindo preço, tipo de pizza e disponibilidade.
- Consulta e cadastro administrativo de usuários.

## Acesso ao sistema

Acesse a aplicação integrada, com frontend e backend Spring Boot, no Render:

**[Abrir Projeto Fatia Prime](https://fatia-prime.onrender.com)**

O workflow configurado do GitHub Pages publica somente os arquivos estáticos do frontend. O Pages não executa o Spring Boot nem disponibiliza o backend ou a API; para utilizar o sistema completo, acesse o link do Render acima.

## Tecnologias utilizadas

- **Java 26** e **Spring Boot 4.1.1** — aplicação backend e servidor web.
- **Spring Web MVC** — endpoints REST e entrega do frontend estático.
- **Spring Data JPA** e **Hibernate** — persistência e mapeamento das entidades.
- **Spring Security** e **Spring Security Crypto** — autenticação, autorização e BCrypt.
- **Spring Validation** — validação de dados recebidos pela API.
- **H2 Database** — banco relacional em memória.
- **HTML5, CSS3 e JavaScript** — interface do sistema, sem framework frontend.
- **Gradle Wrapper** — compilação, execução e testes do projeto.
- **Docker** — imagem de execução definida no `Dockerfile` para o deploy.

## Pré-requisitos

Antes de executar o projeto, verifique se você possui:

- JDK 26.
- Git.
- Navegador web moderno.
- Conexão com a internet para baixar as dependências do Gradle.

O projeto já inclui o Gradle Wrapper, então não é necessário instalar o Gradle manualmente. No Render, a aplicação utiliza a variável de ambiente `PORT`.

## Como executar localmente

1. Clone o repositório:

  ```bash
  git clone https://github.com/daviramalho-dev/Projeto-Fatia-Prime.git
  ```

2. Acesse a pasta da aplicação:

  ```bash
  cd Projeto-Fatia-Prime/Fatia-Prime
  ```

3. Inicie o Spring Boot:

  ```bash
  ./gradlew bootRun
  ```

4. Acesse [http://localhost:8080](http://localhost:8080).

Localmente, o servidor escuta em `0.0.0.0` e usa a porta `8080` por padrão, ou a porta definida pela variável `PORT`.

## Como executar os testes

Para executar a suíte atual do projeto, utilize:

```bash
./gradlew clean test --no-daemon
```

Esse comando compila o projeto, inicia o contexto Spring Boot e executa os testes configurados.

## Como a aplicação funciona

- **Spring Boot** inicializa o servidor, entrega os arquivos do frontend e integra os endpoints REST.
- **Spring Data JPA**, com Hibernate, mapeia as entidades `Usuario`, `Categoria`, `Produto`, `Pedido` e `ItemPedido` para tabelas e simplifica o acesso aos dados por repositories.
- **H2** é usado como banco relacional em memória. Os dados cadastrados são perdidos quando a aplicação reinicia; o seed recria as categorias e os produtos iniciais.
- **Spring Security** autentica administradores por e-mail e senha, mantém a sessão e restringe as rotas administrativas por perfil. As operações protegidas também usam token CSRF.
- **BCrypt** é usado pelo `PasswordEncoder` para gerar e verificar hashes de senha; as senhas não são armazenadas em texto puro.

## Estrutura do projeto

```text
Fatia-Prime/
├── build.gradle                         # plugins, toolchain e dependências
├── Dockerfile                           # imagem usada no deploy
└── src/
    ├── main/
    │   ├── java/com/example/Fatia/Prime/
    │   │   ├── *Controller.java         # endpoints REST
    │   │   ├── *Repository.java         # persistência JPA
    │   │   ├── *Request.java / *Response.java
    │   │   ├── Categoria.java, Produto.java, Pedido.java, ItemPedido.java, Usuario.java
    │   │   └── configuração, segurança, frete e tratamento de erros
    │   └── resources/
    │       ├── application.properties   # configuração da aplicação
    │       ├── data.sql                 # seed inicial
    │       └── static/
    │           ├── assets/
    │           ├── css/style.css
    │           ├── js/script.js
    │           ├── favicon.svg
    │           ├── index.html
    │           └── robots.txt
    └── test/java/com/example/Fatia/Prime/
        ├── *ApiTests.java               # testes de integração da API
        ├── *Tests.java                  # demais testes automatizados
        └── FreteTestCoordinates.java    # suporte aos testes de frete
```

As classes Java ficam em um único pacote e são distinguidas pelo papel indicado nos nomes; a árvore acima descreve a organização existente, sem introduzir subpacotes ou uma camada Service.

Os arquivos de publicação estática do GitHub Pages estão em `.github/workflows/pages.yml`. Os documentos e materiais de apoio ficam em `docs/` e `Fotos_Projeto_FatiaPrime/`.

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

- `GET /api/admin/categorias` e `GET /api/admin/categorias/{id}`
  Listam e detalham categorias para administradores autenticados.

- `POST /api/admin/categorias` e `PUT /api/admin/categorias/{id}`
  Criam e atualizam categorias no painel administrativo.

- `DELETE /api/admin/categorias/{id}`
  Remove uma categoria sem produtos vinculados.

### Produtos

- `GET /api/produtos`  
  Retorna pizzas e bebidas ativas ordenadas por nome. O campo `tipo` pode ser `SALGADA`, `DOCE` ou `BEBIDA`.

- `GET /api/produtos/{id}`  
  Retorna um produto específico por ID.

- `GET /api/admin/produtos` e `GET /api/admin/produtos/{id}`
  Listam e detalham produtos para administradores autenticados, com filtros administrativos na listagem.

- `POST /api/admin/produtos` e `PUT /api/admin/produtos/{id}`
  Criam e editam produtos.

- `PATCH /api/admin/produtos/{id}/status`
  Ativa ou desativa um produto sem excluí-lo.

### Opções de pizza

- `GET /api/opcoes-pizza?tipoProduto=SALGADA` ou `?tipoProduto=DOCE`
  Retorna bordas, adicionais e molhos ativos compatíveis com o tipo de pizza.
- `GET /api/admin/opcoes-pizza`
  Lista as opções para administradores autenticados.
- `POST /api/admin/opcoes-pizza` e `PUT /api/admin/opcoes-pizza/{id}`
  Criam e editam bordas, adicionais ou molhos e seus preços.
- `PATCH /api/admin/opcoes-pizza/{id}/status`
  Ativa ou desativa uma opção sem removê-la.

### Entrega

- `GET /api/frete/consulta?cep=99990000`
  Normaliza o CEP e retorna a região atendida e o valor da faixa ativa; CEP inválido retorna erro de validação e CEP sem faixa ativa não pode ser usado para finalizar pedidos.
- As faixas iniciais ficam no seed local e estão identificadas como dados de demonstração da aplicação.
- O catálogo inicial inclui pizzas salgadas, doces, bordas, adicionais, molhos e bebidas padronizadas para atendimento da pizzaria.
- Ao criar o pedido, o backend resolve novamente a faixa e grava o CEP normalizado e o valor do frete cobrado naquele momento. O total é recalculado como subtotal dos produtos mais frete; valores financeiros enviados pelo navegador não são usados.

### Bebidas, adicionais e molhos

- Bebidas são produtos do catálogo e não passam pelo customizador de pizzas.
- Adicionais e molhos são opções associadas ao item de pizza. O backend valida cada ID, disponibilidade e preço antes de salvar o pedido.
- O carrinho mantém a configuração dos itens no `localStorage`; o pedido salva o tipo do produto, os nomes e preços cobrados das opções, além do preço unitário calculado.

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

As entidades persistidas incluem `Usuario`, `Categoria`, `Produto`, `OpcaoPizza`, `FaixaFrete`, `Pedido` e `ItemPedido`. Opções selecionadas ficam no item como snapshots de nome, tipo e preço; o item também guarda tipo de produto, nome e preço unitário cobrado. O pedido guarda o CEP normalizado e o frete cobrado como snapshot; pedidos existentes não são recalculados se as faixas mudarem.

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
- O login administrativo protege as rotas administrativas, e as senhas são armazenadas como hash BCrypt.
- O carrinho utiliza `localStorage`, enquanto o checkout e os pedidos são processados pelo backend.
- O deploy atual está preparado para o Render: https://fatia-prime.onrender.com
- O workflow de GitHub Pages publica somente o frontend estático; a aplicação completa depende do backend Spring Boot, disponibilizado no Render.
- Última atualização: outubro de 2026.

## Equipe

Integrantes conforme a seção “Equipe do Projeto” da documentação do projeto:

- Davi Sousa Ramalho
- Fabio Henrique Martins dos Santos
- Gabriel Fontes de Alcântara Valadares
- João Vitor da Silva Lopes
- Leonardo Matos Santos
- Leonardo Nunes Lima
- Lucca Eustáquio de Souza
- Matheus Pereira Cunha de Souza
