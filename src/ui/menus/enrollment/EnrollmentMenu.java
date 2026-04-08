package ui.menus.enrollment;

import application.FitManager;
import application.OperationResult;
import domain.enums.PaymentType;
import domain.model.Enrollment;
import domain.model.Payment;

import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de gerenciamento de matrículas.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class EnrollmentMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public EnrollmentMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de matrículas.
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (EnrollmentMenuOption opt : EnrollmentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR MATRÍCULAS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            EnrollmentMenuOption option = EnrollmentMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a "
                        + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case REALIZAR_MATRICULA:   enrollStudent();        break;
                case REGISTRAR_PAGAMENTO:  registerPayment();      break;
                case CANCELAR_MATRICULA:   cancelEnrollment();     break;
                case CONSULTAR_ATIVA:      findActiveEnrollment(); break;
                case LISTAR_HISTORICO:     listHistory();          break;
                case VOLTAR:               running = false;        break;
            }
        }
    }

    // ============================
    // Fluxo 3 — Realizar matrícula
    // ============================

    /**
     * Coleta todos os dados necessários — incluindo o pagamento inicial —
     * antes de qualquer chamada ao FitManager.
     * A matrícula só é efetivada após o pagamento inicial válido.
     */
    private void enrollStudent() {
        String cpf = ui.getInput("CPF do aluno (apenas números):");
        if (cpf == null) return;

        String planName = ui.getInput("Nome do plano:");
        if (planName == null) return;

        String startDate = ui.getInput("Data de início (dd/mm/aaaa):");
        if (startDate == null) return;

        int durationMonths = ui.getIntInput("Duração em meses:");
        if (durationMonths == Integer.MIN_VALUE) return;

        // Coleta os dados do pagamento inicial antes de qualquer processamento
        ui.showMessage("Agora informe os dados do pagamento inicial.\n"
                + "A matrícula só será efetivada após o registro do pagamento.");

        double initialAmount = ui.getDoubleInput("Valor do pagamento inicial (R$):");
        if (Double.isNaN(initialAmount)) return;

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String paymentDesc = ui.getInput("Descrição do pagamento (opcional — Enter para pular):");

        // Todos os dados coletados — delega ao FitManager
        OperationResult result = fitManager.enrollStudent(
                cpf, planName, startDate, durationMonths,
                initialAmount, paymentType, paymentDesc);

        displayResult(result);
    }

    /**
     * Exibe o resultado de uma operação de matrícula.
     * Se sucesso, inclui o resumo completo da matrícula criada.
     */
    private void displayResult(OperationResult result) {
        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage(result.getMessage() + "\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Fluxo 4 — Registrar pagamento
    // ============================

    /**
     * Registra um pagamento em uma matrícula ativa.
     * Exibe o saldo atualizado após o pagamento.
     */
    private void registerPayment() {
        int intCode = ui.getIntInput("Código da matrícula:");
        if (intCode == Integer.MIN_VALUE) return;

        double amount = ui.getDoubleInput("Valor do pagamento (R$):");
        if (Double.isNaN(amount)) return;

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String description = ui.getInput("Descrição do pagamento (opcional — Enter para pular):");

        OperationResult result = fitManager.registerPayment(intCode, amount, paymentType, description);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage(result.getMessage() + "\n\n"
                    + "Total pago: R$ " + String.format("%.2f", enrollment.calculateTotalPaid()) + "\n"
                    + "Saldo pendente: R$ " + String.format("%.2f", enrollment.calculateBalance()));
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Fluxo 5 — Cancelar matrícula
    // ============================

    /**
     * Cancela uma matrícula ativa.
     * Exibe resumo financeiro completo no momento do cancelamento.
     */
    private void cancelEnrollment() {
        int code = ui.getIntInput("Código da matrícula a cancelar:");
        if (code == Integer.MIN_VALUE) return;

        OperationResult result = fitManager.cancelEnrollment(code);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage(result.getMessage() + "\n\n"
                    + "> RESUMO FINANCEIRO \n"
                    + "Valor total do contrato: R$ "
                    + String.format("%.2f", enrollment.getTotalPrice()) + "\n"
                    + "Total pago: R$ "
                    + String.format("%.2f", enrollment.calculateTotalPaid()) + "\n"
                    + "Saldo pendente: R$ "
                    + String.format("%.2f", enrollment.calculateBalance()));
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Consultar matrícula ativa
    // ============================

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF.
     */
    private void findActiveEnrollment() {
        String cpf = ui.getInput("CPF do aluno (apenas números):");
        if (cpf == null) return;

        OperationResult result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage("Matrícula ativa encontrada:\n\n" + enrollment.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Listar histórico
    // ============================

    /**
     * Lista o histórico de matrículas de um aluno (ativas e canceladas).
     */
    private void listHistory() {
        String cpf = ui.getInput("CPF do aluno (apenas números):");
        if (cpf == null) return;

        OperationResult result = fitManager.listEnrollmentHistory(cpf);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> HISTÓRICO DE MATRÍCULAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += enrollments.get(i).toString();
            if (i < enrollments.size() - 1) message += "\n\n";
        }

        ui.showScrollableMessage(message);
    }

    // ============================
    // Métodos auxiliares
    // ============================

    /**
     * Exibe menu para seleção do tipo de pagamento (PaymentType).
     * Valores do enum exibidos numerados — sem strings literais.
     *
     * @return PaymentType selecionado, ou null se cancelou ou inválido
     */
    private PaymentType selectPaymentType() {
        PaymentType[] types = PaymentType.values();
        String options = "";
        for (int i = 0; i < types.length; i++) {
            options += (i + 1) + " - " + types[i].getLabel() + "\n";
        }

        String choice = ui.showMenu("Selecione o Tipo de Pagamento", options);
        if (choice == null) return null;
        if (!InputParser.isNumeric(choice)) {
            ui.showError("Tipo de pagamento inválido. Digite um número de 1 a " + types.length + ".");
            return null;
        }

        int index = Integer.parseInt(choice.trim()) - 1;
        if (index >= 0 && index < types.length) {
            return types[index];
        }

        ui.showError("Tipo de pagamento inválido. Escolha de 1 a " + types.length + ".");
        return null;
    }

    /**
     * Monta o resumo de uma matrícula para exibição após criação.
     */
    private String buildEnrollmentSummary(Enrollment enrollment) {
        ArrayList<Payment> payments = enrollment.getPayments();
        double pago = enrollment.calculateTotalPaid();
        double saldo = enrollment.calculateBalance();

        return "> RESUMO DA MATRÍCULA \n"
                + enrollment.toString() + "\n\n"
                + "Pagamentos registrados: " + payments.size() + "\n"
                + "Total pago: R$ " + String.format("%.2f", pago) + "\n"
                + "Saldo pendente: R$ " + String.format("%.2f", saldo);
    }
}