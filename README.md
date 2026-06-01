# Gerenciador de Oficinas Mecânicas

Sistema para apoiar a rotina de oficinas mecânicas: cadastro de veículos e clientes, abertura de ordens de serviço, montagem de orçamentos de peças e mão de obra, e acompanhamento do andamento dos atendimentos.

O projeto é o núcleo de domínio da aplicação, desenvolvido em Java com Maven. A modelagem segue uma separação por camadas (domínio, aplicação, infraestrutura e apresentação) e é construída de forma incremental, com testes guiando cada regra de negócio.

Trabalho acadêmico da disciplina de Orientação a Objetos.

## Como executar

Pré-requisitos: JDK 21 e Maven 3.9 ou superior.

```
mvn compile
mvn test
```

## Estrutura do projeto

- `src/domain/` — entidades, objetos de valor e regras de negócio puras.
- `src/application/` — casos de uso e orquestração das regras do domínio.
- `src/infrastructure/` — adaptadores para recursos externos (persistência, integrações).
- `src/presentation/` — interfaces de entrada (CLI, futuras interfaces gráficas).
- `tests/` — testes automatizados, espelhando a estrutura de pacotes de `src/`.
- `project-meta.json` — metadados do trabalho acadêmico (curso, integrantes, tema).
- `.github/workflows/` — configuração da integração contínua.
