package ui.menu.student;

import ui.menu.MenuOption;

public enum StudentMenuOption implements MenuOption {
    CADASTRAR(1, "Cadastrar novo aluno"),
    CONSULTAR_CPF(2, "Consultar por CPF"),
    EDITAR(3, "Editar cadastro"),
    EXCLUIR(4, "Excluir aluno"),
    LISTAR(5, "Listar todos"),
    VOLTAR(6, "Voltar");

    private final int numero;
    private final String valorOpcao;

    StudentMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    @Override public int getNumber() { return numero; }
    @Override public String getValorOpcao() { return valorOpcao; }
}