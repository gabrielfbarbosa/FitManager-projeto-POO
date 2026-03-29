package ui.menu.plan;

import ui.menu.MenuOption;

public enum PlanMenuOption implements MenuOption {
    CADASTRAR(1, "Cadastrar novo plano"),
    CONSULTAR_NOME(2, "Consultar por nome"),
    ALTERAR_PRECO(3, "Alterar preço"),
    LISTAR(4, "Listar todos"),
    VOLTAR(5, "Voltar");

    private final int numero;
    private final String valorOpcao;

    PlanMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    @Override public int getNumber() { return numero; }
    @Override public String getValorOpcao() { return valorOpcao; }
}