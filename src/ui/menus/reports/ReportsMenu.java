package ui.menus.reports;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import domain.model.Student;

import ui.screen.UserInterface;

import java.util.ArrayList;

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
            String menuOptions = "";
            for (ReportsMenuOption opt : ReportsMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> RELATÓRIOS", menuOptions);

            if (input == null) { running = false; continue; }

            ReportsMenuOption option = ReportsMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a "
                        + ReportsMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
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
    private void reportStudentsWithActiveEnrollment() {
        OperationResult result = fitManager.listStudentsWithActiveEnrollment();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Student> students = (ArrayList<Student>) result.getData();
        String message = "> ALUNOS COM MATRÍCULA ATIVA\n";
        message += "Total: " + students.size() + " aluno(s)\n\n";

        for (int i = 0; i < students.size(); i++) {
            message += (i + 1) + ". "
                    + students.get(i).getName()
                    + " — CPF: " + students.get(i).getFormattedCpf();
            if (i < students.size() - 1) message += "\n";
        }

        ui.showMessage(message);
    }

    // ============================
    // Relatório 2 — Saldo pendente
    // ============================

    /**
     * Lista matrículas com saldo devedor (balance > 0).
     * Inclui ativas e canceladas com débito em aberto.
     */
    private void reportPendingBalance() {
        OperationResult result = fitManager.listEnrollmentsWithPendingBalance();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> MATRÍCULAS COM SALDO PENDENTE \n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment e = enrollments.get(i);
            message += "--- " + (i + 1) + ". Matrícula " + e.getCode() + " ---\n";
            message += "Aluno: " + e.getStudent().getName() + "\n";
            message += "Plano: " + e.getPlan().getName() + "\n";
            message += "Status: " + e.getStatus().getLabel() + "\n";
            message += "Valor total: R$ " + String.format("%.2f", e.getTotalPrice()) + "\n";
            message += "Total pago: R$ " + String.format("%.2f", e.calculateTotalPaid()) + "\n";
            message += "Saldo pendente: R$ " + String.format("%.2f", e.calculateBalance());
            if (i < enrollments.size() - 1) message += "\n\n";
        }

        ui.showMessage(message);
    }

    // ============================
    // Relatório 3 — Todas as matrículas
    // ============================

    /**
     * Lista todas as matrículas do sistema (ativas e canceladas).
     */
    private void reportAllEnrollments() {
        OperationResult result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> TODAS AS MATRÍCULAS \n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += enrollments.get(i).toString();
            if (i < enrollments.size() - 1) message += "\n\n";
        }

        ui.showMessage(message);
    }
}