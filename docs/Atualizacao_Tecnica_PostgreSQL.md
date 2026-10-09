# Adendo técnico — PostgreSQL e persistência

**Revisão em 08/10/2026**

Este adendo complementa, sem substituir, o Relatório Parcial e o Cronograma em PDF versionados em `docs/`. Esses arquivos preservam o registro acadêmico da etapa em que foram produzidos; suas referências a H2 e os status antigos não descrevem necessariamente a configuração e o andamento atuais.

## Estado verificado no repositório

- PostgreSQL é o banco da aplicação. O perfil `dev` usa PostgreSQL local por padrão; `prod` exige URL, usuário e senha em variáveis de ambiente e TLS. Não há credenciais versionadas.
- Flyway aplica as migrações em `src/main/resources/db/migration`; Hibernate usa `ddl-auto=validate`. O seed do catálogo é aditivo e não apaga nem atualiza registros existentes.
- H2 está restrito ao classpath de testes e ao perfil `test` em memória. O artefato de execução não inclui H2 nem a console H2.
- O checkout atual calcula preços no backend. Em pizza meio a meio, o preço-base é o maior preço dos dois sabores; borda, adicionais e molhos são somados ao preço unitário. O pedido soma cada preço unitário vezes a quantidade e adiciona o frete. Portanto, não se devem somar novamente os snapshots de personalização ao `preco_unitario`.
- O endpoint atual define o status inicial como `Pedido recebido` e calcula `valor_total` antes de persistir o pedido. O pedido armazena o CEP informado; o CEP normalizado é usado na consulta do frete.

## Testes executados

Na revisão, `cd Fatia-Prime && bash ./gradlew clean test --no-daemon` concluiu com sucesso: **113 testes em 16 suítes, sem falhas, erros ou testes ignorados**. As duas verificações de integração PostgreSQL executaram com PostgreSQL 16 via Docker/Testcontainers, incluindo a migração em uma instância limpa. A compilação Java também ocorreu nessa execução.

Isso valida a configuração e o esquema em PostgreSQL local de teste; não é uma conexão ao Neon nem uma verificação dos dados existentes nele. Conforme informado para esta revisão, o Render conectou ao Neon e executou a migração inicial, mas esse acesso e essa execução não foram repetidos nem verificados independentemente aqui.

## Pedidos com valores ausentes ou divergentes

O código atual da API atribui status e total no fluxo de criação. A migração `V1__create_initial_schema.sql` declara `pedidos.valor_total` como `NOT NULL`, mas deixa `pedidos.status` anulável. Assim, um status ausente pode existir por dados legados ou escrita fora do fluxo atual; um total nulo deve levar primeiro à conferência do esquema efetivamente aplicado no Neon. Não foi possível consultar esses pedidos e nenhum dado foi alterado.

Use consultas somente de leitura para verificar as restrições e comparar totais. O cálculo abaixo considera que `preco_unitario` já inclui as personalizações:

```sql
SELECT column_name, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'pedidos'
  AND column_name IN ('status', 'valor_total', 'valor_frete')
ORDER BY column_name;

SELECT p.id, p.codigo, p.status, p.valor_total, p.valor_frete,
       COALESCE(SUM(i.quantidade * i.preco_unitario), 0) AS subtotal_itens,
       COALESCE(SUM(i.quantidade * i.preco_unitario), 0)
           + COALESCE(p.valor_frete, 0) AS total_calculado
FROM public.pedidos AS p
LEFT JOIN public.itens_pedido AS i ON i.pedido_id = p.id
GROUP BY p.id, p.codigo, p.status, p.valor_total, p.valor_frete
ORDER BY p.id DESC;
```

Não execute `UPDATE`, exclusões ou correções em massa com base apenas nessa soma. Primeiro confirme a versão do esquema, os itens e snapshots de cada pedido, a origem dos dados e a regra comercial aplicável; preserve evidências e faça backup antes de qualquer reparo manual.

## Itens que ainda dependem de validação

- Consultar o schema e os pedidos citados diretamente no projeto/banco Neon, sem expor dados pessoais, e determinar a origem dos valores ausentes ou divergentes.
- Validar no serviço Render as variáveis ativas, o perfil `prod`, TLS e endpoint pooled; confirmar um pedido de teste no Neon e sua disponibilidade depois de reiniciar a aplicação.
- Atualizar o arquivo-fonte do Cronograma: o PDF atual ainda marca a revisão da integração com banco como “Não Iniciado”, embora a migração e os testes PostgreSQL constem agora no repositório. O fonte editável do cronograma e do relatório parcial não está versionado; os PDFs originais foram preservados.
