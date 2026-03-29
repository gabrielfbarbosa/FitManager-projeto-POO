package ui.menu.reports;

import application.FitManager;
import ui.screen.UserScreen;

/**
 * Menu de relatórios e listagens.
 *
 * Nesta etapa, a estrutura do menu é visível, mas as operações
 * ainda não estão implementadas — serão desenvolvidas na próxima etapa.
 */
public class ReportsMenu {

    private UserScreen ui;
    private FitManager fitManager;

    public ReportsMenu(UserScreen ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de relatórios.
     */
    public void run() {
        boolean running = true;

        while (running) {
            String option = ui.showMenu(
                    "=========== RELATÓRIOS ===========",
                    ReportsMenuOption.values()
            );

            if (option == null) { running = false; continue; }

            switch (option.trim()) {
                case "1": case "2": case "3":
                    ui.showMessage("⏳ Funcionalidade será implementada na próxima etapa.");
                    break;
                case "4": running = false; break;
                default: ui.showError("Opção inválida. Escolha de 1 a "
                        + ReportsMenuOption.values().length + ".");
            }
        }
    }
}
