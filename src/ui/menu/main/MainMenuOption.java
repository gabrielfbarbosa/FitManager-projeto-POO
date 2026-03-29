package ui.menu.main;

import ui.menu.MenuOption;

public enum MainMenuOption implements MenuOption {
    GERENCIAR_ALUNOS(1, "Gerenciar alunos"),
    GERENCIAR_PLANOS(2, "Gerenciar planos"),
    GERENCIAR_MATRICULAS(3, "Gerenciar matrículas"),
    RELATORIOS(4, "Relatórios / listagens"),
    SAIR(5, "Sair");

    private final int numero;
    private final String valorOpcao;

    MainMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    @Override public int getNumber() { return numero; }
    @Override public String getValorOpcao() { return valorOpcao; }
}