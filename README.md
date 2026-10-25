# WePayU - Sistema de Folha de Pagamento

Projeto desenvolvido para a disciplina de Programação 2 (POO) - Instituto de Computação (IC) / UFAL.

## Milestones

- **Milestone 1:** Implementação das User Stories 1 a 8 (cadastro, alteração e remoção de empregados, cartões de ponto, vendas, taxas sindicais, folha de pagamento, undo/redo e persistência).
- **Milestone 2:** Refatoração de responsabilidades e encapsulamento (hierarquia de empregados, métodos de pagamento e sindicato por composição), criação de exceções de domínio, e implementação das User Stories 9 e 10 (agendas de pagamento padrão e customizadas).

### Observações sobre os Testes de Aceitação
- No arquivo `tests/us9.txt`, linha 100, havia um erro de digitação no script de testes original (`expect 1292,30totalFolha data=14/1/2005`, sem espaço antes do comando). Foi adicionado o espaço (`expect 1292,30 totalFolha data=14/1/2005`) para que o interpretador do EasyAccept pudesse processar o comando corretamente.
