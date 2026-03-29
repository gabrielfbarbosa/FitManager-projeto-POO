package ui.menu.enrollment;

import application.FitManager;
import ui.screen.UserScreen;
import ui.menu.MenuOption;

/**
 * Menu de gerenciamento de matrículas.
 *
 * Nesta etapa, a estrutura do menu é visível, mas as operações
 * ainda não estão implementadas — serão desenvolvidas na próxima etapa.
 */
public class EnrollmentMenu {

    private UserScreen ui;
    private FitManager fitManager;

    public EnrollmentMenu(UserScreen ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de matrículas.
     */

    public void run() {
        boolean running = true;

        while (running) {
            String input = ui.showMenu(
                    "=== GERENCIAR MATRÍCULAS ===",
                    EnrollmentMenuOption.values()
            );

            if (input == null) { running = false; continue; }

            EnrollmentMenuOption option;
            try {
                option = MenuOption.fromNumero(
                        EnrollmentMenuOption.class,
                        Integer.parseInt(input.trim())
                );

            } catch (NumberFormatException e) {
                option = null;
            }

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case REALIZAR_MATRICULA:
                case REGISTRAR_PAGAMENTO:
                case CANCELAR_MATRICULA:
                case CONSULTAR_ATIVA:
                case LISTAR_HISTORICO:
                    ui.showMessage("⏳ Funcionalidade será implementada na próxima etapa.");
                    break;
                case VOLTAR: running = false; break;
                default: ui.showError("Opção inválida. Escolha de 1 a "
                        + EnrollmentMenuOption.values().length + ".");
            }
        }
    }
}
