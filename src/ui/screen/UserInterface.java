package ui.screen;

import ui.menu.MenuOption;

import javax.swing.JOptionPane;

/**
 * Centraliza todas as operações de entrada e saída do sistema.
 *
 * Em vez de cada menu interagir diretamente com JOptionPane,
 * essas chamadas ficam encapsuladas em quatro métodos: showMenu(),
 * getInput(), showMessage() e showError().
 *
 * Qualquer mudança na forma de exibição afeta apenas esta classe,
 * sem impactar o restante do sistema.
 */
public class UserInterface {

    private static final String APP_TITLE = "FitManager";

    /**
     * Exibe um menu com título e opções, retornando a opção escolhida pelo usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param title   título do menu
     * @param options texto completo com as opções numeradas
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String showMenu(String title, String options) {
        return JOptionPane.showInputDialog(
                null,
                options + "\n\nEscolha uma opção:",
                title + " — " + APP_TITLE,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    public String showMenu(String title, MenuOption[] options) {
        StringBuilder sb = new StringBuilder();
        for (MenuOption opt : options) {
            sb.append(opt.getNumber()).append(" - ").append(opt.getValorOpcao()).append("\n");
        }
        return JOptionPane.showInputDialog(null, sb.toString(), title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Captura uma entrada de texto do usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param prompt texto do prompt exibido
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String getInput(String prompt) {
        String input = JOptionPane.showInputDialog(
                null,
                prompt,
                APP_TITLE,
                JOptionPane.QUESTION_MESSAGE
        );
        return input;
    }

    /**
     * Solicita e converte uma entrada inteira do usuário.
     * Exibe mensagem de erro e retorna Integer.MIN_VALUE se a entrada for
     * cancelada ou não numérica.
     *
     * @param prompt       texto do prompt exibido
     * @param errorMessage mensagem exibida quando a conversão falha
     * @return o valor inteiro, ou Integer.MIN_VALUE se cancelou ou inválido
     */
    public int getIntInput(String prompt, String errorMessage) {
        String input = getInput(prompt);
        if (input == null) return Integer.MIN_VALUE;

        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            showError(errorMessage);
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Solicita e converte uma entrada decimal do usuário.
     * Aceita vírgula como separador decimal (substitui por ponto antes da conversão).
     * Exibe mensagem de erro e retorna Double.NaN se a entrada for cancelada ou inválida.
     *
     * @param prompt texto do prompt exibido
     * @return o valor decimal, ou Double.NaN se cancelou ou inválido
     */
    public double getDoubleInput(String prompt) {
        String input = getInput(prompt);
        if (input == null) return Double.NaN;

        try {
            return Double.parseDouble(input.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            showError("Valor inválido. Digite um valor numérico.");
            return Double.NaN;
        }
    }

    /**
     * Exibe uma mensagem de sucesso/informação.
     *
     * @param message texto da mensagem
     */
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Exibe uma mensagem de erro.
     *
     * @param message texto do erro
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE + " — Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
