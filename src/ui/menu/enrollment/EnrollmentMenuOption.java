package ui.menu.enrollment;

import ui.menu.MenuOption;

public enum EnrollmentMenuOption implements MenuOption {
    REALIZAR_MATRICULA(1, "Realizar matrícula"),
    REGISTRAR_PAGAMENTO(2, "Registrar pagamento"),
    CANCELAR_MATRICULA(3, "Cancelar matrícula"),
    CONSULTAR_ATIVA(4, "Consultar matrícula ativa"),
    LISTAR_HISTORICO(5, "Listar histórico"),
    VOLTAR(6, "Voltar");

    private final int numero;
    private final String valorOpcao;

    EnrollmentMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    @Override public int getNumber() { return numero; }
    @Override public String getValorOpcao() { return valorOpcao; }
}