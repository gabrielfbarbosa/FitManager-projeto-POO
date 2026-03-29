package ui.menu;

public interface MenuOption {
    String getValorOpcao();
    int getNumber();

    /**
     * Busca a constante do enum pelo número digitado pelo usuário.
     * Retorna null se nenhuma constante corresponder.
     */
    static <T extends Enum<T> & MenuOption> T fromNumero(Class<T> enumClass, int number) {
        for (T option : enumClass.getEnumConstants()) {
            if (option.getNumber() == number) {
                return option;
            }
        }
        return null;
    }
}