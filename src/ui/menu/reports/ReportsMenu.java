package ui.menu.reports;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import domain.model.Student;
import ui.menu.MenuOption;
import ui.screen.UserInterface;

import java.util.List;

/**
 * Menu de relatórios e listagens.
 *
 * Consolida informações disponíveis nos serviços e as exibe de formas
 * diferentes conforme o critério solicitado.
 *
 * A lógica de filtragem reside nos serviços — este menu apenas
 * solicita os dados ao FitManager e os formata para exibição.
 */
public class ReportsMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public ReportsMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de relatórios.
     */
    public void run() {
        boolean running = true;

        while (running) {
            String input = ui.showMenu(
                    "> RELATÓRIOS",
                    ReportsMenuOption.values()
            );

            if (input == null) { running = false; continue; }

            MenuOption option;
            try {
                option = MenuOption.fromNumber(
                        ReportsMenuOption.values(),
                        Integer.parseInt(input.trim())
                );
            } catch (NumberFormatException e) {
                option = null;
            }

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a "
                        + ReportsMenuOption.values().length + ".");
                continue;
            }

            switch ((ReportsMenuOption) option) {
                case ALUNOS_ATIVOS:    reportStudentsWithActiveEnrollment(); break;
                case SALDO_PENDENTE:   reportPendingBalance();               break;
                case TODAS_MATRICULAS: reportAllEnrollments();               break;
                case VOLTAR:           running = false;                       break;
            }
        }
    }

    // ============================
    // Relatório 1 — Alunos com matrícula ativa
    // ============================

    /**
     * Lista os alunos que possuem matrícula ativa no momento.
     */
    @SuppressWarnings("unchecked")
    private void reportStudentsWithActiveEnrollment() {
        OperationResult result = fitManager.listStudentsWithActiveEnrollment();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        List<Student> students = (List<Student>) result.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("> ALUNOS COM MATRÍCULA ATIVA\n");
        sb.append("Total: ").append(students.size()).append(" aluno(s)\n\n");

        for (int i = 0; i < students.size(); i++) {
            sb.append(i + 1).append(". ")
                    .append(students.get(i).getName())
                    .append(" — CPF: ").append(students.get(i).getFormattedCpf());
            if (i < students.size() - 1) sb.append("\n");
        }

        ui.showMessage(sb.toString());
    }

    // ============================
    // Relatório 2 — Saldo pendente
    // ============================

    /**
     * Lista matrículas com saldo devedor (balance > 0).
     * Inclui ativas e canceladas com débito em aberto.
     */
    @SuppressWarnings("unchecked")
    private void reportPendingBalance() {
        OperationResult result = fitManager.listEnrollmentsWithPendingBalance();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        List<Enrollment> enrollments = (List<Enrollment>) result.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("====== MATRÍCULAS COM SALDO PENDENTE ======\n");
        sb.append("Total: ").append(enrollments.size()).append(" matrícula(s)\n\n");

        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment e = enrollments.get(i);
            sb.append("--- ").append(i + 1).append(". Matrícula ").append(e.getCode()).append(" ---\n");
            sb.append("Aluno: ").append(e.getStudent().getName()).append("\n");
            sb.append("Plano: ").append(e.getPlan().getName()).append("\n");
            sb.append("Status: ").append(e.getStatus().getLabel()).append("\n");
            sb.append("Valor total: R$ ").append(String.format("%.2f", e.getTotalPrice())).append("\n");
            sb.append("Total pago: R$ ").append(String.format("%.2f", e.calculateTotalPaid())).append("\n");
            sb.append("Saldo pendente: R$ ").append(String.format("%.2f", e.calculateBalance()));
            if (i < enrollments.size() - 1) sb.append("\n\n");
        }

        ui.showMessage(sb.toString());
    }

    // ============================
    // Relatório 3 — Todas as matrículas
    // ============================

    /**
     * Lista todas as matrículas do sistema (ativas e canceladas).
     */
    @SuppressWarnings("unchecked")
    private void reportAllEnrollments() {
        OperationResult result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        List<Enrollment> enrollments = (List<Enrollment>) result.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("====== TODAS AS MATRÍCULAS ======\n");
        sb.append("Total: ").append(enrollments.size()).append(" matrícula(s)\n\n");

        for (int i = 0; i < enrollments.size(); i++) {
            sb.append("--- Matrícula ").append(i + 1).append(" ---\n");
            sb.append(enrollments.get(i).toString());
            if (i < enrollments.size() - 1) sb.append("\n\n");
        }

        ui.showMessage(sb.toString());
    }
}