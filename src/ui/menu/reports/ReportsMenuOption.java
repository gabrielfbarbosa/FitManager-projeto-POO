package ui.menu.reports;

import ui.menu.MenuOption;

public enum ReportsMenuOption implements MenuOption {
    ALUNOS_ATIVOS(1, "Alunos com matrícula ativa"),
    SALDO_PENDENTE(2, "Matrículas com saldo pendente"),
    TODAS_MATRICULAS(3, "Todas as matrículas"),
    VOLTAR(4, "Voltar");

    private final int numero;
    private final String valorOpcao;

    ReportsMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    @Override public int getNumber() { return numero; }
    @Override public String getValorOpcao() { return valorOpcao; }
}