package ui.menu.main;

public enum MainMenuOption {
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

    public int getNumber() { return numero; }
    public String getValorOpcao() { return valorOpcao; }

    public static MainMenuOption fromNumber(int numero) {
        for (MainMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}