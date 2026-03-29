package ui.menu.main;

import application.FitManager;
import ui.menu.plan.PlanMenu;
import ui.menu.reports.ReportsMenu;
import ui.menu.sudent.StudentMenu;
import ui.screen.UserScreen;
import ui.menu.MenuOption;
import ui.menu.enrollment.EnrollmentMenu;

/**
 * Menu principal do sistema FitManager.
 *
 * Responsável por exibir o menu principal e direcionar o usuário
 * para os submenus específicos de cada funcionalidade.
 *
 * Utiliza Lazy Instantiation: os submenus são criados sob demanda
 * na primeira vez que o usuário acessa a opção correspondente,
 * e reutilizados nas chamadas seguintes. Isso evita criar objetos
 * desnecessários e mantém referência única ao UserInterface e FitManager.
 */
public class MainMenu {

    private UserScreen ui;
    private FitManager fitManager;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserScreen ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui, fitManager);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui, fitManager);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui, fitManager);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui, fitManager);
        }
        return reportsMenu;
    }

    /**
     * Inicia o loop principal do sistema.
     * O sistema permanece em execução até que a opção "Sair" seja escolhida.
     */
    public void start() {
        boolean running = true;

        while (running) {
            String input = ui.showMenu(
                    "=== FITMANAGER ===",
                    MainMenuOption.values()
            );

            if (input == null) {
                running = false;
                continue;
            }

            MainMenuOption option;
            try {
                option = MenuOption.fromNumero(
                        MainMenuOption.class,
                        Integer.parseInt(input.trim())
                );
            } catch (NumberFormatException e) {
                option = null;
            }

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                case RELATORIOS:           getReportsMenu().run();    break;
                case SAIR:                 running = false;           break;
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo. 👋");
    }
}
