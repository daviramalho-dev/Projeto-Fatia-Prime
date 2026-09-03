# Fatia Prime

## Identificação Acadêmica

| Informação | Descrição |
|-----------|-----------|
| **Instituição de Ensino** | UNICEPLAC |
| **Curso** | Análise e Desenvolvimento de Sistemas |
| **Disciplina** | Projeto Integrado em Programação Web |
| **Orientador** | Profº Hudson Neves |

---

## 1. Descrição

Fatia Prime é uma aplicação web desenvolvida em **Spring Boot** com foco em fornecer soluções integradas para **A ser definido pela equipe**.

---

## 2. Objetivos

### Objetivo Geral

**A ser definido pela equipe**

### Objetivos Específicos

- **A ser definido pela equipe**

---

## 3. Problema que o Sistema Resolve

**A ser definido pela equipe**

---

## 4. Público-Alvo

**A ser definido pela equipe**

---

## 5. Funcionalidades

- **A ser definido pela equipe**

---

## 6. Tecnologias Utilizadas

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **Java** | 21 | Linguagem de programação principal |
| **Spring Boot** | 4.1.1 | Framework web para desenvolvimento backend |
| **Spring Data JPA** | - | ORM para persistência de dados |
| **H2 Database** | - | Banco de dados em memória |
| **Gradle** | - | Ferramenta de build e gerenciamento de dependências |
| **HTML5/CSS3** | - | Tecnologias para interface web |

---

## 7. Arquitetura da Solução

**A ser definido pela equipe**

### Diagrama Arquitetural

```
┌─────────────────────────────────────────────────┐
│           Camada de Apresentação                │
│     (HTML5, CSS3, JavaScript no Frontend)       │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│        Camada de Aplicação (Spring Boot)        │
│  ┌──────────────────────────────────────────┐   │
│  │  Controllers & REST APIs                 │   │
│  └──────────────────┬───────────────────────┘   │
│                     │                            │
│  ┌──────────────────▼───────────────────────┐   │
│  │  Services & Business Logic               │   │
│  └──────────────────┬───────────────────────┘   │
└────────────────────┼────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│   Camada de Persistência (Spring Data JPA)      │
│              H2 Database                        │
└─────────────────────────────────────────────────┘
```

---

## 8. Modelagem do Banco de Dados

**A ser definido pela equipe**

### Diagrama Entidade-Relacionamento (ER)

```
A ser definido pela equipe
```

---

## 9. Pré-requisitos

Antes de instalar e executar o projeto, certifique-se de ter os seguintes requisitos:

- **JDK 21** ou superior instalado
- **Gradle 8.0** ou superior
- **Git** para controle de versão
- **Navegador web** moderno (Chrome, Firefox, Safari, Edge)
- **Espaço em disco** mínimo de 500MB

---

## 10. Instalação

### Passo a Passo

#### 1. Clone o Repositório

```bash
git clone <url-do-repositorio>
cd Projeto-Fatia-Prime/Fatia-Prime
```

#### 2. Verifique a Instalação do Java

```bash
java -version
```

Deve exibir Java 21 ou superior.

#### 3. Compile o Projeto

```bash
./gradlew build
```

No Windows, use:

```bash
gradlew.bat build
```

#### 4. Instale as Dependências

As dependências são automaticamente baixadas durante a compilação via Gradle.

---

## 11. Como Executar

### Executar a Aplicação

Use o comando abaixo para iniciar o servidor:

```bash
./gradlew bootRun
```

No Windows:

```bash
gradlew.bat bootRun
```

### Acessar a Aplicação

Após iniciar o servidor, acesse a aplicação através do navegador:

```
http://localhost:8080
```

### Acessar o Console H2

Para acessar o console do banco de dados H2:

```
http://localhost:8080/h2-console
```

**Configurações padrão do H2:**
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **User Name:** `sa`
- **Password:** *(deixe em branco)*

---

## 12. Estrutura do Projeto

```
Fatia-Prime/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── Fatia/
│   │   │               └── Prime/
│   │   │                   └── FatiaPrimeApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/
│   │           │   └── style.css
│   │           └── assets/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── Fatia/
│                       └── Prime/
│                           └── FatiaPrimeApplicationTests.java
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

### Descrição dos Diretórios

| Diretório | Descrição |
|-----------|-----------|
| `src/main/java` | Código-fonte Java da aplicação |
| `src/main/resources` | Arquivos de configuração e recursos estáticos |
| `src/test` | Testes automatizados |
| `gradle/` | Configurações do Gradle Wrapper |

---

## 13. Exemplos de Uso

### Exemplo 1: Acessar a Página Inicial

```bash
GET http://localhost:8080/
```

Resposta: Página HTML inicial da aplicação.

### Exemplos Adicionais

**A ser definido pela equipe**

---

## 14. API

### Endpoints Disponíveis

**A ser definido pela equipe**

### Exemplo de Requisição

**A ser definido pela equipe**

### Exemplo de Resposta

**A ser definido pela equipe**

---

## 15. Capturas de Tela

**Inserir capturas de tela aqui:**

- Página inicial
- Telas principais
- Exemplos de funcionalidades

*Obs: Adicionar imagens em formato PNG/JPG no diretório `docs/screenshots/` e referenciar aqui.*

---

## 16. Equipe do Projeto

| Nome | Papel | Email |
|------|-------|-------|
| A ser definido | Desenvolvedor | A ser definido |
| A ser definido | Desenvolvedor | A ser definido |
| A ser definido | Scrum Master | A ser definido |

---

## 17. Melhorias Futuras

- **A ser definido pela equipe**
- **A ser definido pela equipe**
- **A ser definido pela equipe**

---

## 18. Licença

**A ser definido pela equipe**

---

## Notas Adicionais

- O projeto utiliza **H2 Database** como banco de dados em memória para desenvolvimento e testes.
- Para ambiente de produção, considere migrar para um banco de dados persistente (PostgreSQL, MySQL, etc.).
- Consulte a documentação oficial do [Spring Boot](https://spring.io/projects/spring-boot) para mais informações.

---

**Última atualização:** Setembro de 2026
