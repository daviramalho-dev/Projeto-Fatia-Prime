# 🍕 Projeto Fatia Prime

<p align="center"><strong>Cardápio, pedidos e administração para uma pizzaria em uma aplicação web integrada.</strong></p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-26-ED8B00?logo=openjdk&logoColor=white" alt="Java 26">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/Spring%20Data-JPA-6DB33F?logo=spring&logoColor=white" alt="Spring Data JPA">
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL Database">
  <img src="https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white" alt="HTML5">
  <img src="https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white" alt="CSS3">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=222" alt="JavaScript">
</p>

## Sobre o projeto

O Projeto Fatia Prime é uma aplicação web integrada para uma pizzaria, com foco na apresentação do cardápio, na experiência de pedidos dos clientes e no gerenciamento administrativo de usuários, produtos e pedidos.

A interface apresenta a marca e carrega o catálogo pela API. O frontend está integrado ao backend Spring Boot, que disponibiliza uma API REST e utiliza Spring Data JPA, Spring Security, Spring Validation e PostgreSQL. H2 permanece disponível para testes locais.

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
- **PostgreSQL** — banco relacional principal; produção pode usar Neon.
- **H2 Database** — banco em memória para a suíte de testes local.
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

3. Inicie um PostgreSQL local (por exemplo, PostgreSQL 16) e crie um banco e um usuário `fatiaprime`. O perfil `dev` é ativado por padrão e conecta em `localhost:5432/fatiaprime`; configure usuário e senha no ambiente local, sem adicioná-los ao repositório:

  ```bash
  export SPRING_DATASOURCE_USERNAME=fatiaprime
  read -rsp 'Senha do PostgreSQL: ' SPRING_DATASOURCE_PASSWORD
  export SPRING_DATASOURCE_PASSWORD
  echo
  export APP_ADMIN_BOOTSTRAP_EMAIL=admin@localhost
  read -rsp 'Senha do administrador inicial: ' APP_ADMIN_BOOTSTRAP_PASSWORD
  export APP_ADMIN_BOOTSTRAP_PASSWORD
  echo
  ```

  Para uma instância local sem TLS, o perfil `dev` usa `PGSSLMODE=disable` por padrão. Substitua `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` conforme a instalação local.

4. Inicie o Spring Boot:

  ```bash
  ./gradlew bootRun
  ```

5. Acesse [http://localhost:8080](http://localhost:8080).

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
- **PostgreSQL** é o banco da aplicação no perfil local `dev` e no perfil de implantação `prod`. No Render, ele deve ser um banco externo persistente, como Neon; a aplicação não usa o disco local do Render para guardar registros.
- **Flyway** aplica as migrações versionadas em `src/main/resources/db/migration`; Hibernate valida o esquema em vez de alterar tabelas automaticamente.
- `data.sql` insere categorias, produtos e opções iniciais de forma idempotente. Não substitui dados já existentes nem apaga produtos, pedidos ou itens na inicialização.
- **H2** fica isolado no perfil `test` para a suíte existente. Os testes PostgreSQL usam PostgreSQL 16 real em contêiner local (Testcontainers).
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
- As faixas de frete usadas pelo cálculo são configuração da aplicação (`app.frete.faixas`); a tabela `faixas_frete` mapeada pela entidade não é usada pelo fluxo atual de cotação.
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

## PostgreSQL, Neon e Render

### Configuração local

O perfil `dev` é o padrão e usa PostgreSQL em `localhost:5432/fatiaprime`. É possível sobrescrever a conexão com `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`. `PGSSLMODE` controla TLS local e assume `disable`. O projeto não fornece senha local.

### Configuração de produção

1. No console do Neon, crie um projeto PostgreSQL e um banco dedicado para Fatia Prime.
2. Na tela **Connect**, selecione a conexão **pooled** (endpoint cujo host inclui `-pooler`) para limitar conexões concorrentes no plano gratuito. Copie o host, o nome do banco e o usuário; mantenha a senha fora do código.
3. No serviço web do Render, configure o repositório com o diretório raiz `Fatia-Prime` para usar o `Dockerfile` do projeto.
4. Cadastre estas variáveis no painel **Environment** do Render:

   | Variável | Valor |
   | --- | --- |
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host-pooled-neon>/<banco>?sslmode=require` |
   | `SPRING_DATASOURCE_USERNAME` | Usuário indicado pelo Neon |
   | `SPRING_DATASOURCE_PASSWORD` | Senha indicada pelo Neon |
   | `APP_ADMIN_BOOTSTRAP_EMAIL` | E-mail inicial do administrador |
   | `APP_ADMIN_BOOTSTRAP_PASSWORD` | Senha inicial forte e exclusiva |
   | `APP_ADMIN_BOOTSTRAP_NAME` | Nome do administrador (opcional) |

   O URL é somente JDBC host/banco e não deve conter usuário ou senha. O perfil `prod` também exige TLS (`sslmode=require`) e limita o pool Hikari a cinco conexões por padrão (`DB_POOL_MAX_SIZE=5`, `DB_POOL_MIN_IDLE=0`). Ajuste esses limites somente considerando as cotas do Neon e o número de instâncias.

5. Não habilite `H2_CONSOLE_ENABLED` nem exponha console de banco no site. O console administrativo do Neon permanece no domínio e no painel do próprio Neon. Não configure armazenamento no filesystem do Render para persistência; pedidos e catálogo são gravados no PostgreSQL externo.

O Render injeta `PORT`, usado pela aplicação. Não é necessário configurar um disco persistente pago para o banco, pois ele não fica no container do Render. Isso não significa disponibilidade contínua: serviços gratuitos podem suspender ou reiniciar, e cotas, suspensão, retenção de backup e armazenamento do Neon dependem do plano e das políticas vigentes. Consulte os limites atuais nos painéis antes de publicar.

### Console SQL do Neon

No Neon, abra o projeto, selecione o branch e o banco corretos e use **SQL Editor**. As tabelas da aplicação ficam no schema `public`. Exemplos para inspecionar pedidos e itens:

```sql
SELECT id, codigo, status, cliente_nome, cliente_telefone, valor_total, data_criacao
FROM public.pedidos
ORDER BY data_criacao DESC;

SELECT p.codigo, p.status, i.id AS item_id, i.nome_produto_snapshot,
       i.quantidade, i.preco_unitario
FROM public.pedidos AS p
JOIN public.itens_pedido AS i ON i.pedido_id = p.id
ORDER BY p.data_criacao DESC, i.id;
```

Para o esquema atual, veja as tabelas no SQL Editor ou consulte `information_schema.tables`. Usuários e hashes de senha são dados administrativos: não os exponha em consultas públicas nem os compartilhe.

### Esquema, seed e migração de dados preexistentes

Flyway aplica `V1__create_initial_schema.sql` em um banco vazio. `spring.jpa.hibernate.ddl-auto=validate` faz a aplicação falhar se o esquema não corresponder; não usa `create`, `create-drop` ou alterações silenciosas de esquema. Migrações futuras devem ser adicionadas como novos arquivos versionados `V<n>__descricao.sql`, revisadas e testadas contra PostgreSQL antes do deploy. Não edite uma migração que já tenha sido aplicada.

O seed aditivo em `data.sql` pode ser repetido: adiciona somente nomes de catálogo ainda inexistentes e não apaga nem atualiza dados. Ele não é uma migração de banco e não transfere registros do H2.

Não há exportação automática do banco anterior. Antes de migrar dados importantes de uma instância H2 ainda em execução, suspenda temporariamente novos pedidos, faça uma cópia/exportação consistente do H2, confira os arquivos exportados e carregue no Neon vazio em ordem de dependências (categorias/usuários/opções/faixas, produtos, pedidos, itens e adicionais). Preserve IDs e snapshots, adapte tipos/valores ao esquema PostgreSQL e atualize as sequências de identidade após importação. Compare contagens por tabela e confira pedidos e itens por código antes de reabrir o checkout. Mantenha o backup original até concluir essa validação. Não use scripts que apaguem tabelas ou configure `baseline-on-migrate` para contornar falhas sem uma revisão do schema e do histórico. Se o H2 anterior já foi reiniciado, os registros em memória não podem ser recuperados pela aplicação.

O perfil `test` continua usando H2 em memória; ele não prova conectividade com Neon. A suíte `PostgreSqlPersistenceTests` usa PostgreSQL 16 via Docker/Testcontainers para verificar migrations, seed idempotente, checkout, pedido/itens, autenticação administrativa, painel e transição de status.

As entidades persistidas incluem `Usuario`, `Categoria`, `Produto`, `OpcaoPizza`, `FaixaFrete`, `Pedido` e `ItemPedido`. Opções selecionadas ficam no item como snapshots de nome, tipo e preço; o item também guarda tipo de produto, nome e preço unitário cobrado. O pedido guarda o CEP normalizado e o frete cobrado como snapshot; pedidos existentes não são recalculados se as faixas mudarem.

## Frontend

A interface principal está em:

- `Fatia-Prime/src/main/resources/static/index.html`
- `Fatia-Prime/src/main/resources/static/css/style.css`
- `Fatia-Prime/src/main/resources/static/js/script.js`

Ela inclui apresentação da marca, catálogo de produtos vindo da API, categorias e filtros, carrinho com `localStorage`, checkout, validações, confirmação do pedido, consulta pública de pedidos e integração com WhatsApp para finalizar os pedidos. As operações administrativas de pedidos e produtos também são integradas ao backend.

## Protótipo no Figma

[Visualizar protótipo no Figma](https://www.figma.com/design/tyNXJswozPjT9W5TddLoRi/Fatia-Prime-%25E2%2580%2594-Prot%25C3%25B3tipo-M%25C3%25A9dia-Fidelidade?node-id=0-1&t=zAM0bcqzzKuyRwPT-0)

## Fluxo básico

1. O cliente acessa a página inicial.
2. Navega pelo cardápio.
3. Adiciona produtos ao carrinho.
4. Ajusta quantidades e revisa o total.
5. O checkout envia o pedido à API; o backend recalcula preços/frete e grava pedido e itens em uma transação no PostgreSQL.
6. A confirmação apresenta o código retornado e oferece o envio dos dados pelo WhatsApp.
7. O pedido pode ser consultado pelo código ou telefone.

O frontend é servido pelo próprio Spring Boot e se comunica com as APIs REST do backend para catálogo, checkout, pedidos e administração.

## Observações

- A aplicação foi desenvolvida principalmente como projeto acadêmico.
- A aplicação em execução depende do PostgreSQL configurado no perfil ativo. H2 é usado somente para testes.
- O projeto continua com a arquitetura simples atual: HTML5, CSS3 e JavaScript no frontend; Spring Boot com controllers, repositories e JPA no backend; e PostgreSQL como banco principal.
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
