# Gerenciador de Oficinas Mecânicas

Sistema de gerenciamento de ordens de serviço para oficinas mecânicas. Permite cadastrar clientes, veículos e peças; abrir ordens de serviço; montar orçamentos com peças e mão de obra; e acompanhar o andamento de cada atendimento do recebimento até a entrega.

Trabalho acadêmico da disciplina de Orientação a Objetos — Ciência da Computação, Tema 8.

## Stack

- Java 21
- Maven 3.9
- Javalin 6.3.0 (servidor HTTP)
- Jackson 2.17.2 (serialização JSON)
- SQLite via xerial jdbc 3.46.1.3 (persistência em arquivo local)
- JUnit 5 + AssertJ (testes)

## Como executar

Pré-requisitos: JDK 21 e Maven 3.9 ou superior instalados e no `PATH`.

```
mvn package -DskipTests
java -jar target/oficina-mecanica-1.0-SNAPSHOT.jar
```

O servidor inicia na porta 8080. Acesse `http://localhost:8080` no navegador para abrir a interface web.

O banco de dados é criado automaticamente em `oficina.db` na pasta onde o comando for executado.

## Como rodar os testes

```
mvn verify
```

A suíte cobre 822 cenários distribuídos entre testes de unidade (domínio, casos de uso, repositórios em memória), testes de integração com SQLite e testes HTTP de ponta a ponta sobre o servidor Javalin em porta aleatória.

## Estrutura do projeto

```
src/
  domain/          Entidades, objetos de valor, estados e regras de negócio puras
    cliente/
    veiculo/
    peca/
    orcamento/
    ordemservico/
    shared/        CPF, Dinheiro, Preco (value objects reutilizáveis)
  application/     Casos de uso e interfaces de repositório
    cliente/
    veiculo/
    peca/
    ordemservico/
  infrastructure/  Adaptadores de persistência
    memoria/       Repositórios em HashMap (usados nos testes de aplicação)
    sqlite/        Repositórios com JDBC direto e SQLite
  presentation/    Camada HTTP
    controller/    Quatro controladores REST (Cliente, Veiculo, Peca, OS)
    dto/           Records de entrada e saída da API
  main/resources/
    web/           Interface web estática (HTML, CSS, JavaScript vanilla)

tests/             Testes, espelhando os pacotes de src/
  domain/
  application/
  infrastructure/
  presentation/
  integracao/
```

## Decisões arquiteturais

### Domain-Driven Design

O núcleo do sistema é formado por dois agregados principais: `OrdemDeServico` e `Orcamento`. Cada agregado possui seu próprio repositório e expõe somente os métodos que representam operações de negócio válidas. Referências entre agregados são feitas exclusivamente por identificador (`OrdemDeServicoId`, `OrcamentoId`), não por referência direta de objeto, o que preserva as fronteiras transacionais e simplifica a persistência.

Objetos de valor (`CPF`, `Preco`, `Dinheiro`, `Placa`) encapsulam invariantes que de outro modo precisariam ser repetidas em múltiplos casos de uso. São imutáveis e comparam-se por valor.

### State Pattern

Tanto `Orcamento` quanto `OrdemDeServico` implementam máquinas de estado com o padrão State do GoF. Os estados são representados por interfaces seladas (`sealed interface`) cujas implementações — classes `record` ou `final class` — carregam apenas os dados relevantes para aquele estado.

Cada estado implementa somente as transições permitidas e lança `IllegalStateException` para as demais. Isso elimina blocos de `if/switch` espalhados e torna explícito em tempo de compilação quais transições existem.

`OrdemDeServico` possui sete estados: `Recebida`, `AguardandoAprovacao`, `EmExecucao`, `Concluida`, `Entregue`, `Rejeitada` e `Cancelada`.  
`Orcamento` possui quatro: `Rascunho`, `Enviado`, `Aprovado` e `Rejeitado`.

### Repositório

As interfaces de repositório (`ClienteRepository`, `VeiculoRepository`, `PecaRepository`, `OrcamentoRepository`, `OrdemDeServicoRepository`) ficam no pacote `application` e não conhecem nenhum detalhe de banco de dados. Isso permite que os testes de casos de uso usem implementações em memória enquanto o servidor usa implementações SQLite, sem alterar nenhuma linha de lógica de negócio.

A persistência SQLite usa JDBC direto, sem ORM. Cada repositório monta suas próprias instruções SQL. O padrão `INSERT OR REPLACE` (UPSERT) simplifica o ciclo salvar/atualizar sem exigir controle de versão de registro. Valores monetários são armazenados em centavos (`INTEGER`) para evitar imprecisão de ponto flutuante.

### Composition root inline

Todas as dependências (repositórios, casos de uso, controladores) são instanciadas na classe `ServidorOficina`, que atua como raiz de composição. Não há contêiner de injeção de dependências. Para o tamanho do projeto — cinco repositórios, onze casos de uso, quatro controladores — um contêiner acrescentaria complexidade de configuração sem benefício prático. A visibilidade das dependências é total: basta ler o construtor de `ServidorOficina` para entender o grafo completo de objetos.

### TDD

O desenvolvimento seguiu ciclos red-green-refactor: cada regra de negócio foi coberta por testes antes da implementação. Os testes de apresentação (`ServidorOficinaTest`, `FluxoOficinaHttpTest`, os quatro `*ControllerTest`) foram escritos contra um servidor Javalin iniciado em porta aleatória por teste, o que os torna independentes de portas ocupadas e executáveis em paralelo.

### Persistência e layout Maven não convencional

O `pom.xml` define `<sourceDirectory>src</sourceDirectory>` e `<testSourceDirectory>tests</testSourceDirectory>`, desviando do layout padrão `src/main/java`. Para que os arquivos da interface web (em `src/main/resources/web/`) fossem copiados para o classpath durante o build, foi necessário declarar explicitamente o bloco `<resources>` apontando para `src/main/resources`. Sem isso, o Maven não copiaria esses arquivos e o Javalin não os encontraria em tempo de execução.
