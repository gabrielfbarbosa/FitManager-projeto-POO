package ui.menu;

public interface MenuOption {
    String getValorOpcao();
    int getNumber();

    /**
     * Busca a constante do enum pelo número digitado pelo usuário.
     * Retorna null se nenhuma constante corresponder.
     */
    static MenuOption fromNumber(MenuOption[] options, int numero) {
        for (MenuOption option : options) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}