# Fatia Prime

## Identificação Acadêmica

| Informação                | Descrição                             |
| ------------------------- | ------------------------------------- |
| **Instituição de Ensino** | UNICEPLAC                             |
| **Curso**                 | Análise e Desenvolvimento de Sistemas |
| **Disciplina**            | Projeto Integrado em Programação Web  |
| **Orientador**            | Profº Hudson Neves                    |
| **Projeto**               | Pizzaria Fatia Prime                  |
| **Ano**                   | 2026                                  |

---

## 1. Descrição

A **Fatia Prime** é uma aplicação web desenvolvida para uma pizzaria, com o objetivo de criar uma presença digital para apresentação da marca, divulgação do cardápio e facilitação do contato com os clientes.

O projeto utiliza **Spring Boot** no backend e uma interface web desenvolvida com **HTML5, CSS3 e JavaScript**.

A aplicação apresenta a identidade visual da Fatia Prime, catálogo de pizzas, preços, descrições dos produtos, seção institucional e um carrinho de pedidos integrado ao WhatsApp.

Além da interface, o backend possui uma API REST para cadastro e consulta de usuários, utilizando **Spring Data JPA**, **H2 Database**, validação de dados e armazenamento seguro das senhas por meio de hash.

O projeto foi desenvolvido como atividade acadêmica da disciplina **Projeto Integrado em Programação Web**, do curso de Análise e Desenvolvimento de Sistemas da UNICEPLAC.

---

## 2. Objetivos

### Objetivo Geral

Desenvolver uma aplicação web para a Pizzaria Fatia Prime, proporcionando uma apresentação digital organizada da marca e de seus produtos, além de disponibilizar funcionalidades para interação com o cliente e estrutura inicial de backend para gerenciamento de usuários.

### Objetivos Específicos

* Desenvolver uma interface web responsiva para apresentação da pizzaria.
* Apresentar o cardápio com imagens, nomes, descrições e preços.
* Criar um carrinho de pedidos utilizando JavaScript.
* Permitir que o pedido seja encaminhado ao WhatsApp da pizzaria.
* Desenvolver uma API REST para cadastro e consulta de usuários.
* Implementar validação dos dados enviados à API.
* Garantir que as senhas não sejam armazenadas em texto puro.
* Utilizar banco de dados H2 para persistência durante o desenvolvimento.
* Aplicar conceitos de desenvolvimento web, programação Java, Spring Boot e persistência de dados.
* Utilizar Git e GitHub para controle de versão e colaboração da equipe.

---

## 3. Problema que o Sistema Resolve

Pequenos estabelecimentos, como pizzarias, podem necessitar de uma forma simples e organizada de apresentar seus produtos e facilitar o contato com seus clientes no ambiente digital.

A Fatia Prime busca solucionar esse problema disponibilizando uma página web centralizada onde o cliente pode:

* conhecer a pizzaria;
* visualizar o cardápio;
* consultar preços;
* visualizar os produtos;
* adicionar produtos ao carrinho;
* informar nome e observações do pedido;
* encaminhar o pedido diretamente pelo WhatsApp.

Além da interface destinada ao cliente, o projeto possui uma estrutura inicial de backend para cadastro e consulta de usuários, permitindo a evolução futura do sistema para funcionalidades mais completas de autenticação e gerenciamento de pedidos.

---

## 4. Público-Alvo

O sistema é destinado principalmente a:

* Clientes da Pizzaria Fatia Prime;
* Pessoas interessadas em conhecer o cardápio da pizzaria;
* Clientes que desejam realizar pedidos de maneira rápida através do WhatsApp;
* Usuários que acessam a aplicação por computadores, tablets ou smartphones.

No contexto acadêmico, o projeto também é direcionado à aplicação prática dos conhecimentos adquiridos no curso de Análise e Desenvolvimento de Sistemas.

---

## 5. Funcionalidades

### Interface Web

* Apresentação da identidade visual da Fatia Prime.
* Seção inicial com chamada para o cardápio.
* Seção de pizzas mais pedidas.
* Catálogo de produtos.
* Exibição de imagens dos produtos.
* Exibição de nomes, descrições e preços.
* Seção institucional "Sobre a Fatia Prime".
* Navegação entre as principais seções da página.
* Layout responsivo para diferentes tamanhos de tela.

### Carrinho de Pedidos

* Adição de pizzas ao carrinho.
* Controle da quantidade de cada produto.
* Remoção de produtos.
* Cálculo automático do valor total.
* Contador de itens no carrinho.
* Campo para nome do cliente.
* Campo para observações do pedido.
* Geração automática da mensagem do pedido.
* Encaminhamento do pedido para o WhatsApp.

### API de Usuários

* Cadastro de usuários.
* Consulta de usuários cadastrados.
* Validação de nome.
* Validação de e-mail.
* Validação de senha.
* Verificação de e-mail já cadastrado.
* Normalização do e-mail para letras minúsculas.
* Hash das senhas antes da persistência.
* Tratamento padronizado de erros da API.

---

## 6. Tecnologias Utilizadas

| Tecnologia                 | Versão | Descrição                                                     |
| -------------------------- | ------ | ------------------------------------------------------------- |
| **Java**                   | 26     | Linguagem principal utilizada no backend                      |
| **Spring Boot**            | 4.1.1  | Framework utilizado para desenvolvimento da aplicação backend |
| **Spring Web MVC**         | -      | Desenvolvimento da aplicação web e API REST                   |
| **Spring Data JPA**        | -      | Persistência e acesso aos dados utilizando JPA                |
| **H2 Database**            | -      | Banco de dados relacional em memória                          |
| **Spring Security Crypto** | -      | Utilizado para geração de hash das senhas                     |
| **Spring Validation**      | -      | Validação dos dados recebidos pela API                        |
| **Gradle**                 | 9.7.1  | Build e gerenciamento de dependências                         |
| **HTML5**                  | -      | Estrutura da interface web                                    |
| **CSS3**                   | -      | Estilização e responsividade da interface                     |
| **JavaScript**             | -      | Interatividade, carrinho e integração com WhatsApp            |
| **Git**                    | -      | Controle de versão                                            |
| **GitHub**                 | -      | Hospedagem do código e colaboração                            |
| **GitHub Actions**         | -      | Automação do deploy para GitHub Pages                         |

---

## 7. Arquitetura da Solução

A aplicação utiliza uma arquitetura dividida principalmente entre a interface web e o backend desenvolvido com Spring Boot.

O frontend é disponibilizado pelos arquivos estáticos localizados em:

```text
Fatia-Prime/src/main/resources/static/
```

O backend é responsável pela API REST de usuários e pela persistência dos dados utilizando Spring Data JPA e H2 Database.

A aplicação atualmente **não possui uma camada Service separada**. A lógica relacionada ao cadastro de usuários está implementada diretamente no `UsuarioController`.

### Diagrama Arquitetural

```text
┌────────────────────────────────────────────────────┐
│              Interface Web                         │
│                                                    │
│       HTML5 + CSS3 + JavaScript                   │
│                                                    │
│  ┌─────────────┐  ┌────────────┐  ┌────────────┐ │
│  │  Cardápio   │  │  Carrinho  │  │  WhatsApp  │ │
│  └─────────────┘  └────────────┘  └────────────┘ │
└──────────────────────┬─────────────────────────────┘
                       │
                       │ HTTP
                       ▼
┌────────────────────────────────────────────────────┐
│                 Spring Boot                        │
│                                                    │
│             UsuarioController                      │
│                                                    │
│       GET /api/usuarios                            │
│       POST /api/usuarios                           │
│                                                    │
│       Validação + Regras de cadastro               │
└──────────────────────┬─────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────┐
│              Spring Data JPA                       │
│                                                    │
│              UsuarioRepository                    │
└──────────────────────┬─────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────┐
│                 H2 Database                        │
│                                                    │
│                 Tabela usuarios                    │
└────────────────────────────────────────────────────┘
```

---

## 8. Modelagem do Banco de Dados

O projeto utiliza o banco de dados **H2 em memória** para persistência dos usuários.

A entidade principal atualmente implementada é `Usuario`.

### Entidade `Usuario`

| Campo       | Tipo   | Restrições                                |
| ----------- | ------ | ----------------------------------------- |
| `id`        | Long   | Chave primária, geração automática        |
| `nome`      | String | Obrigatório, máximo 100 caracteres        |
| `email`     | String | Obrigatório, único, máximo 254 caracteres |
| `senhaHash` | String | Obrigatório                               |

A tabela correspondente no banco de dados é:

```text
usuarios
```

### Diagrama Entidade-Relacionamento (ER)

```text
┌──────────────────────────────┐
│           USUARIOS           │
├──────────────────────────────┤
│ PK id          : Long        │
│    nome        : String      │
│    email       : String      │ UNIQUE
│    senha_hash  : String      │
└──────────────────────────────┘
```

Atualmente, o modelo de dados contempla somente usuários. Produtos, pedidos e itens do pedido ainda não são entidades persistidas no banco de dados.

---

## 9. Pré-requisitos

Antes de instalar e executar o projeto, certifique-se de possuir:

* **JDK 26** instalado;
* **Git** instalado;
* Navegador web moderno;
* Acesso à internet para download das dependências do Gradle;
* Aproximadamente 500 MB de espaço disponível para ferramentas e dependências.

O projeto possui **Gradle Wrapper**, portanto não é necessário instalar manualmente o Gradle para executar a aplicação.

A versão utilizada pelo Gradle Wrapper é:

```text
Gradle 9.7.1
```

---

## 10. Instalação

### Passo a Passo

#### 1. Clone o Repositório

```bash
git clone https://github.com/daviramalho-dev/Projeto-Fatia-Prime.git
```

Entre na pasta da aplicação:

```bash
cd Projeto-Fatia-Prime/Fatia-Prime
```

#### 2. Verifique a Instalação do Java

```bash
java -version
```

A aplicação está configurada para utilizar:

```text
Java 26
```

#### 3. Compile o Projeto

No Linux/macOS:

```bash
./gradlew build
```

No Windows:

```bash
gradlew.bat build
```

#### 4. Dependências

As dependências utilizadas pelo projeto são baixadas automaticamente pelo Gradle durante a compilação.

---

## 11. Como Executar

### Executar a Aplicação

No Linux/macOS:

```bash
./gradlew bootRun
```

No Windows:

```bash
gradlew.bat bootRun
```

O servidor será iniciado na porta:

```text
8080
```

### Acessar a Aplicação

Após iniciar o servidor, acesse:

```text
http://127.0.0.1:8080
```

A aplicação está configurada para aceitar conexões locais através do endereço:

```text
127.0.0.1
```

### Acessar o Console H2

O console H2 está disponível em:

```text
http://127.0.0.1:8080/h2-console
```

### Configurações do H2

```text
JDBC URL: jdbc:h2:mem:fatiaprime
User Name: sa
Password: deixe em branco
```

O banco é configurado como banco **em memória**, portanto os dados são perdidos quando a aplicação é encerrada.

---

## 12. Estrutura do Projeto

```text
Projeto-Fatia-Prime/
├── .github/
│   └── workflows/
│       └── pages.yml
│
├── docs/
│   └── Relatório parcial - Fatia Prime .pdf
│
├── Fatia-Prime/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           └── Fatia/
│   │   │   │               └── Prime/
│   │   │   │                   ├── CadastroRequest.java
│   │   │   │                   ├── FatiaPrimeApplication.java
│   │   │   │                   ├── TratamentoErros.java
│   │   │   │                   ├── Usuario.java
│   │   │   │                   ├── UsuarioController.java
│   │   │   │                   ├── UsuarioRepository.java
│   │   │   │                   └── UsuarioResponse.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/
│   │   │           ├── index.html
│   │   │           ├── css/
│   │   │           │   └── style.css
│   │   │           ├── js/
│   │   │           │   └── script.js
│   │   │           └── assets/
│   │   │               ├── calabresa prime.jpg
│   │   │               ├── costela.jpg
│   │   │               ├── havaiana.jpg
│   │   │               └── maguerita.jpg
│   │   │
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── example/
│   │                   └── Fatia/
│   │                       └── Prime/
│   │                           └── FatiaPrimeApplicationTests.java
│   │
│   ├── gradle/
│   │   └── wrapper/
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   │
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   └── gradlew.bat
│
├── .nojekyll
└── README.md
```

### Descrição dos Diretórios

| Diretório/Arquivo                | Descrição                                              |
| -------------------------------- | ------------------------------------------------------ |
| `Fatia-Prime/src/main/java`      | Código-fonte Java do backend                           |
| `Fatia-Prime/src/main/resources` | Configurações e recursos da aplicação                  |
| `static/`                        | Arquivos do frontend disponibilizados pelo Spring Boot |
| `static/css/`                    | Arquivos de estilização                                |
| `static/js/`                     | Código JavaScript da interface                         |
| `static/assets/`                 | Imagens utilizadas no site                             |
| `src/test`                       | Testes automatizados                                   |
| `gradle/wrapper`                 | Gradle Wrapper utilizado pelo projeto                  |
| `.github/workflows`              | Automação do GitHub Actions                            |
| `docs`                           | Documentação acadêmica do projeto                      |

---

## 13. Exemplos de Uso

### Exemplo 1: Acessar a Página Inicial

```http
GET http://127.0.0.1:8080/
```

A aplicação apresenta a página inicial da Fatia Prime.

### Exemplo 2: Adicionar Produto ao Carrinho

O usuário pode selecionar um produto através do botão **ADICIONAR**.

O JavaScript adiciona o produto ao carrinho e atualiza:

* quantidade de itens;
* quantidade individual;
* valor total do pedido.

### Exemplo 3: Realizar Pedido

Após adicionar produtos ao carrinho, o usuário pode informar:

```text
Nome
Observações
```

Ao confirmar o pedido, a aplicação gera uma mensagem contendo os produtos, quantidades, valores e total e abre o WhatsApp para envio.

---

## 14. API

### Endpoints Disponíveis

#### Cadastrar Usuário

```http
POST /api/usuarios
```

Exemplo:

```json
{
  "nome": "Davi Ramalho",
  "email": "davi@example.com",
  "senha": "123456"
}
```

Resposta de sucesso:

```http
HTTP/1.1 201 Created
```

Exemplo:

```json
{
  "id": 1,
  "nome": "Davi Ramalho",
  "email": "davi@example.com"
}
```

A senha não é retornada na resposta.

#### Listar Usuários

```http
GET /api/usuarios
```

Exemplo de resposta:

```json
[
  {
    "id": 1,
    "nome": "Davi Ramalho",
    "email": "davi@example.com"
  }
]
```

### Validações

O cadastro possui as seguintes validações:

* Nome obrigatório;
* Nome com no máximo 100 caracteres;
* E-mail obrigatório;
* E-mail válido;
* E-mail com no máximo 254 caracteres;
* Senha obrigatória;
* Senha entre 6 e 64 caracteres;
* Senha limitada a 72 bytes em UTF-8 para o processo de hash;
* E-mail não pode estar previamente cadastrado.

### Tratamento de Erros

A classe `TratamentoErros` centraliza respostas para erros de:

* validação de campos;
* JSON inválido;
* e-mail duplicado;
* dados inválidos;
* violações de integridade.

As respostas de erro são retornadas em formato JSON.

Exemplo:

```json
{
  "mensagem": "O e-mail é obrigatório"
}
```

---

## 15. Capturas de Tela

As principais telas da aplicação incluem:

* Página inicial;
* Seção de produtos em destaque;
* Cardápio;
* Carrinho de pedidos;
* Seção "Sobre a Fatia Prime";
* Interface responsiva.

### Página Inicial

A página inicial apresenta a identidade visual da Fatia Prime e direciona o usuário para o cardápio.

### Cardápio

O cardápio apresenta os produtos disponíveis, suas descrições, preços e botão para adicionar cada item ao carrinho.

### Carrinho

O carrinho permite controlar quantidades, visualizar o valor total e informar dados adicionais antes de encaminhar o pedido.

> As capturas de tela podem ser adicionadas posteriormente ao diretório `docs/screenshots/`.

---

## 16. Equipe do Projeto

| Nome                                      | Curso | Papel     |
| ----------------------------------------- | ----- | --------- |
| **Davi Sousa Ramalho**                    | ADS   | A definir |
| **Fabio Henrique Martins dos Santos**     | ADS   | A definir |
| **Gabriel Fontes de Alcântara Valadares** | ADS   | A definir |
| **João Vitor da Silva Lopes**             | ADS   | A definir |
| **Leonardo Nunes Lima**                   | ADS   | A definir |
| **Lucca Eustáquio de Souza**              | ADS   | A definir |
| **Leonardo Matos Santos**                 | ADS   | A definir |
| **Matheus Pereira Cunha de Souza**        | ADS   | A definir |

---

## 17. Melhorias Futuras

Entre as possíveis evoluções previstas para o projeto estão:

* Implementação completa de autenticação de usuários;
* Implementação de login;
* Persistência de produtos no banco de dados;
* Criação de entidades para pedidos e itens dos pedidos;
* Persistência do carrinho e dos pedidos;
* Criação de um fluxo de gerenciamento de pedidos;
* Integração mais completa entre cliente e estabelecimento;
* Criação de área administrativa;
* Utilização de banco de dados persistente em ambiente de produção;
* Evolução da API REST;
* Ampliação dos testes automatizados.

---

## 18. Licença

Este projeto foi desenvolvido no contexto acadêmico do curso de **Análise e Desenvolvimento de Sistemas da UNICEPLAC**.

**Licença de distribuição do código: a definir pela equipe.**

---

## Notas Adicionais

* O projeto utiliza **H2 Database** como banco de dados em memória.
* Os dados armazenados no H2 são perdidos quando a aplicação é encerrada.
* As senhas dos usuários não são armazenadas em texto puro. O backend utiliza `PasswordEncoder` para gerar o hash antes da persistência.
* O frontend é disponibilizado pelo Spring Boot através do diretório `src/main/resources/static`.
* O carrinho de compras é implementado atualmente no JavaScript do frontend.
* O envio do pedido é realizado através do WhatsApp.
* A aplicação possui um workflow do **GitHub Actions** configurado para publicar os arquivos estáticos no GitHub Pages.
* O projeto possui um teste automatizado que verifica o carregamento do contexto da aplicação Spring Boot.
* A aplicação está configurada para execução local através de `127.0.0.1:8080`.

---

**Última atualização:** Setembro de 2026
