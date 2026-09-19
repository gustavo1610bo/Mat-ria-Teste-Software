# Relatório do laboratório — Central de pedidos

## 1. Escopo e abordagem

Foram criados testes JUnit 5 para as classes concretas do domínio, sem alteração do código de produção. Os testes cobrem construtores, métodos auxiliares, regras de desconto/frete/risco, tentativas de pagamento e os caminhos de colaboração de `PedidoService.fechar`.

O modelo de CFG adotado abaixo trata cada operando de uma condição de curto-circuito (`&&` e `||`) como decisão separada. Para `switch`, são consideradas as saídas distintas dos grupos de cases; `SP` e `RJ` compartilham a mesma saída. Loops têm seus retornos de repetição representados como arestas de volta.

> **Observação sobre JaCoCo:** o ambiente usado para preparar esta entrega não possui Maven nem acesso à internet para baixar as dependências/plugins. Por isso, não foram inventados percentuais de cobertura. A execução final deve ser feita no ambiente do aluno com `mvn clean test`; o projeto já contém o plugin JaCoCo no `pom.xml`.

## 2. Grafo de chamadas

```text
PedidoService.fechar
  ├─ Pedido.subtotalCentavos
  ├─ Pedido.estoqueSuficiente
  ├─ PoliticaDesconto.calcular
  ├─ CalculadoraFrete.calcular
  │    ├─ Pedido.pesoGramas
  │    └─ Pedido.temFragil
  ├─ AnaliseRisco.avaliar
  └─ PagamentoService.pagar
       └─ ProcessadorPagamento.autorizar
```

Há retornos antecipados em `fechar`: cliente bloqueado, pedido sem itens ativos, falta de estoque e risco diferente de `APROVADO`. Por isso, nem todos os caminhos internos das classes colaboradoras são alcançáveis por uma única chamada a `fechar`; alguns precisam ser exercitados diretamente pelos testes unitários.

## 3. CFGs e complexidade de McCabe

### 3.1 `AnaliseRisco.avaliar`

```text
Entrada
  ↓
total < 0? ─sim→ exceção
  ↓ não
bloqueado? ─sim→ RECUSADO
  ↓ não
compras == 0?
  ├─sim→ total > 100000? → sim→ REVISAO
  │                    └→ não→ expresso? → sim→ REVISAO / não→ APROVADO
  └─não→ total > 500000? → não→ APROVADO
                       └─sim→ !vip? → sim→ REVISAO / não→ APROVADO
```

Decisões no modelo adotado: 7 (incluindo os operandos de `||` e `&&`). Portanto, `V(G) = 8`.

Caminhos básicos representativos: total inválido; bloqueado; primeira compra com valor alto; primeira compra expressa; primeira compra normal; cliente antigo não VIP acima do limite; cliente antigo no limite/abaixo; cliente antigo VIP acima do limite.

### 3.2 `CalculadoraFrete.calcular`

```text
Entrada → liquido < 0? → exceção
       ↓
     switch UF
       ├→ PR
       ├→ SP/RJ
       └→ default
              ↓
       excedente = peso - 2000
              ↓
       excedente > 0? ─não→ frete grátis?
            ↑ sim              ↓
            └── subtrai 1000    ↓
                         cliente VIP? → metade
                              ↓
                         expresso? → +1500
                              ↓
                         frágil? → +500
                              ↓
                             saída
```

O `switch` tem três saídas distintas. Considerando também os dois operandos da condição de frete grátis (`liquido >= 30000 && !expresso`), há 9 decisões no modelo adotado e `V(G) = 10`.

Os testes exercitam UF `PR`, `SP`, `RJ` e default; peso abaixo/no/acima de 2 kg; iterações de peso; frete gratuito; VIP; expresso; frágil e combinações em que a base é zerada mas os adicionais permanecem.

### 3.3 `PoliticaDesconto.calcular`

```text
subtotal < 0? → exceção
       ↓
VIP? → 10%
 └não→ subtotal >= 50000? → 5% / 0
       ↓
cupom nulo ou branco? → retorno do desconto-base
       ↓
switch cupom
 ├─ BEMVINDO → compras == 0? → subtotal >= 10000? → +2000
 ├─ EXTRA10  → subtotal >= 20000? → +10%
 └─ default → exceção
       ↓
teto = 20% → desconto > teto? → teto / desconto
```

Com curto-circuito separado, o modelo resulta em 11 decisões e `V(G) = 12` (contando as saídas distintas do `switch` e suas decisões internas).

Foram testados desconto VIP, desconto comum no limite, ausência de cupom, normalização de cupom, elegibilidade dos dois cupons, cupom desconhecido, teto de 20% e subtotal inválido.

### 3.4 `Pedido`

`subtotalCentavos`: loop `for` + decisão de linha inativa. `V(G)=3` no modelo estrutural básico.

`pesoGramas`: loop `for`. `V(G)=2`.

`temFragil`: loop `for` + condição composta `quantidade > 0 && fragil`. Com curto-circuito separado, `V(G)=4`.

`estoqueSuficiente`: loop `for` + decisão de disponibilidade + `break`. `V(G)=3`.

A matriz de testes contempla zero, uma e várias linhas, linhas inativas, fragilidade ativa/inativa e falta de estoque.

### 3.5 `PagamentoService.pagar`

```text
valida total → valida limite → tentativa = 0
        ↓
      do/while
        ↓
    tentativa++
        ↓
   autorizar(total)
    ├─ true  → true
    ├─ false → false
    └─ IllegalStateException → while tentativa < limite?
                                  ├─ sim → nova tentativa
                                  └─ não → false
```

Considerando as duas validações, a condição do `while` e o tratamento do `catch`, o fluxo possui complexidade ciclomática estrutural equivalente a 4 decisões principais (`V(G)=5`) quando o `try/catch` é representado como fluxo excepcional. O JaCoCo não conta exceções como branches; por isso, a cobertura de branches não substitui os testes de exceção.

Os testes verificam aprovação imediata, recusa definitiva sem repetição, indisponibilidade com sucesso posterior, esgotamento das tentativas, propagação de exceção diferente de `IllegalStateException` e validações de argumentos.

### 3.6 `PedidoService.fechar`

```text
referências válidas?
   ↓
bloqueado? ─sim→ BLOQUEADO
   ↓ não
subtotal == 0? ─sim→ exceção
   ↓ não
estoque suficiente? ─não→ SEM_ESTOQUE
   ↓ sim
desconto → frete → total → risco
                         ↓
                 APROVADO?
                  ├ não → resultado sem cobrança
                  └ sim → pagamento
                            ├ true  → PAGO
                            └ false → PAGAMENTO_RECUSADO
```

As condições de `fechar` geram caminhos independentes associados a validação, bloqueio, subtotal, estoque, risco e pagamento. O teste de colaboração também verifica que o processador não é chamado nos retornos anteriores ao pagamento.

## 4. Matriz de testes

| ID / método JUnit | Unidade | Entrada/estado | Resultado esperado | Caminho exercitado | Critério |
|---|---|---|---|---|---|
| C1 | Cliente | histórico 0 | objeto válido | construtor válido | classe/método |
| C2 | Cliente | histórico -1 | `IllegalArgumentException` | validação | exceção |
| I1 | ItemPedido | 4 × 1250, estoque 5 | total 5000, disponível | cálculo | método |
| I2 | ItemPedido | quantidade 0 | total 0 | linha inativa | limite |
| I3 | ItemPedido | qtd > estoque | indisponível | falso de disponibilidade | branch |
| I4 | ItemPedido | SKU nulo/branco | exceção | validação | exceção |
| I5 | ItemPedido | preço/qtd/estoque/peso inválidos | exceções | validações | limites |
| P1 | Pedido | ativa + inativa | subtotal só da ativa | `continue` | branch |
| P2 | Pedido | pesos e frágil | peso/fragilidade corretos | loops/curto-circuito | branch |
| P3 | Pedido | estoque suficiente/faltante | true/false | loop + break | branch |
| P4 | Pedido | lista original alterada | pedido preservado | cópia defensiva | contrato |
| P5 | Pedido | lista/UFs inválidas | exceções | construtor | exceção |
| D1 | PoliticaDesconto | VIP | 10% | ramo VIP | branch |
| D2 | PoliticaDesconto | comum 49999/50000 | 0/5% | limiar | boundary |
| D3 | PoliticaDesconto | cupom nulo/branco | desconto-base | retorno antecipado | branch |
| D4 | PoliticaDesconto | BEMVINDO elegível/não | +2000 ou não | `&&` | branch |
| D5 | PoliticaDesconto | EXTRA10 19999/20000 | 0/10% | limiar | boundary |
| D6 | PoliticaDesconto | combinação >20% | teto 20% | teto | branch |
| D7 | PoliticaDesconto | desconhecido | exceção | `default` | exceção |
| F1 | CalculadoraFrete | PR/SP/RJ/default | 1200/2000/2000/3000 | switch | branch |
| F2 | CalculadoraFrete | 2000/2001/3001 g | +0/+300/+600 | while | iterações |
| F3 | CalculadoraFrete | líquido 30000 normal | frete 0 | gratuidade | branch |
| F4 | CalculadoraFrete | VIP | metade | branch | regra |
| F5 | CalculadoraFrete | expresso | +1500 | branch | regra |
| F6 | CalculadoraFrete | múltiplos frágeis | +500 uma vez | loop/retorno | caminho |
| R1 | AnaliseRisco | bloqueado | RECUSADO | retorno antecipado | branch |
| R2 | AnaliseRisco | primeira compra >1000 | REVISAO | ramo novo | branch |
| R3 | AnaliseRisco | primeira compra expressa | REVISAO | `||` | branch |
| R4 | AnaliseRisco | 1000 normal | APROVADO | limite | boundary |
| R5 | AnaliseRisco | antigo não VIP >5000 | REVISAO | `&&` | branch |
| R6 | AnaliseRisco | antigo VIP >5000 | APROVADO | `&&` | branch |
| G1 | PagamentoService | autoriza true | uma chamada | retorno imediato | caminho |
| G2 | PagamentoService | autoriza false | uma chamada | recusa definitiva | caminho |
| G3 | PagamentoService | 2 indisponíveis + sucesso | true, 3 chamadas | do/while + catch | caminho |
| G4 | PagamentoService | sempre indisponível | false, limite | esgotamento | loop |
| G5 | PagamentoService | exceção comum | propaga | catch seletivo | exceção |
| G6 | PagamentoService | argumentos inválidos | exceções | validações | exceção |
| S1 | PedidoService | cliente bloqueado | BLOQUEADO + zero | retorno inicial | colaboração |
| S2 | PedidoService | subtotal zero | exceção | retorno/validação | exceção |
| S3 | PedidoService | estoque insuficiente | SEM_ESTOQUE + zero | retorno antecipado | colaboração |
| S4 | PedidoService | primeira compra + BEMVINDO | REVISAO, sem cobrança | desconto/frete/risco | colaboração |
| S5 | PedidoService | antigo não VIP >5000 | REVISAO, sem cobrança | risco | colaboração |
| S6 | PedidoService | pagamento false | PAGAMENTO_RECUSADO | pagamento | efeito |
| S7 | PedidoService | 3 indisponibilidades | PAGAMENTO_RECUSADO | tentativas | estado |
| S8 | PedidoService | expresso + frágil | frete com adicionais | combinações | caminho |
| S9 | PedidoService | referências nulas | `NullPointerException` | validação | exceção |

## 5. Evolução da cobertura

A etapa inicial do projeto tinha apenas o teste de exemplo de `PedidoServiceTest`; as demais classes eram exercícios sem métodos `@Test`.

| Etapa | Testes | Linhas | Branches | Métodos | Classes | Observação |
|---|---:|---:|---:|---:|---:|---|
| Inicial | 1 | não medido neste ambiente | não medido | não medido | não medido | somente caminho feliz do serviço |
| Entrega | 40+ métodos de teste | a medir com JaCoCo | a medir com JaCoCo | a medir com JaCoCo | a medir com JaCoCo | suíte criada para cobrir os caminhos descritos |

Para gerar os números oficiais da atividade, executar:

```text
mvn clean test
```

e consultar `target/site/jacoco/index.html`. O XML/CSV também fica nessa pasta, conforme o `pom.xml` do projeto.

## 6. Análise crítica

### Cobertura de ramos não demonstra cobertura de caminhos

Na `CalculadoraFrete`, por exemplo, é possível cobrir cada decisão individual — gratuidade, VIP, expresso e frágil — sem executar necessariamente todas as combinações relevantes. Um teste que passa pelo ramo de frete grátis e outro que passa pelo ramo expresso podem cobrir os ramos, mas não demonstram a interação específica de `liquido >= 30000`, entrega expressa, VIP e item frágil. Os testes incluem combinações explícitas para complementar a cobertura de branches.

### Curto-circuito

Condições como `cliente.bloqueado()`, `total > 100_000 || expresso` e `total > 500_000 && !cliente.vip()` possuem avaliação condicional. Os testes foram escolhidos para produzir tanto avaliação verdadeira quanto falsa dos operandos relevantes, sempre que o caminho for alcançável.

### Caminhos inviáveis no serviço

`AnaliseRisco.avaliar` pode ser testada diretamente com situações que não necessariamente chegam a ela via `PedidoService.fechar`. Por exemplo, um cliente bloqueado faz `fechar` retornar imediatamente `BLOQUEADO`, portanto não chega ao risco. Por isso há testes unitários da classe de risco e testes de colaboração do serviço.

### Exceção não representada no contador de branches

`PagamentoService.pagar` captura `IllegalStateException` para repetir a tentativa. O tratamento de exceção é importante para o comportamento, mas o JaCoCo não contabiliza handlers de exceção como branches da mesma forma que decisões condicionais. Por isso `G3`, `G4` e `G5` verificam explicitamente sucesso após indisponibilidade, esgotamento e propagação de outra exceção.

### Quantidade de iterações

O `while` do frete foi testado com zero excedente, uma fração de kg e mais de uma iteração. O `do/while` do pagamento foi testado com uma chamada, repetição e esgotamento do limite.

### Alteração proposital

A alteração proposital sugerida pelo roteiro deve ser feita temporariamente em uma regra de produção, por exemplo mudar o valor do frete de PR ou a porcentagem de desconto. Um teste correspondente deve falhar, demonstrando que a suíte detecta a regressão; a alteração deve então ser desfeita antes da entrega. Nenhuma regra de produção foi deixada alterada nesta versão.
