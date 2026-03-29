package ui.menu.plan;

import application.FitManager;
import application.OperationResult;
import domain.enums.PlanType;
import domain.model.Plan;
import ui.screen.UserScreen;

import java.util.List;

/**
 * Menu de gerenciamento de planos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class PlanMenu {

    private UserScreen ui;
    private FitManager fitManager;

    public PlanMenu(UserScreen ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de planos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String option = ui.showMenu(
                    "=== GERENCIAR PLANOS ===",
                    PlanMenuOption.values()
            );

            if (option == null) { running = false; continue; }

            switch (option.trim()) {
                case "1": registerPlan(); break;
                case "2": findPlanByName(); break;
                case "3": updatePrice(); break;
                case "4": listAllPlans(); break;
                case "5": running = false; break;
                default: ui.showError("Opção inválida. Escolha de 1 a "
                        + PlanMenuOption.values().length + ".");
            }
        }
    }

    /**
     * Fluxo de cadastro de novo plano.
     * Coleta dados via UserInterface e delega ao FitManager.
     */
    private void registerPlan() {
        String name = ui.getInput("Digite o nome do plano:");
        if (name == null) return;

        String description = ui.getInput("Digite a descrição do plano:");
        if (description == null) return;

        // Seleção do tipo de plano
        PlanType type = selectPlanType();
        if (type == null) return;

        // Duração mínima
        String durationStr = ui.getInput("Digite a duração mínima em meses:");
        if (durationStr == null) return;

        int minimumDuration;
        try {
            minimumDuration = Integer.parseInt(durationStr.trim());
        } catch (NumberFormatException e) {
            ui.showError("Duração inválida. Digite um número inteiro.");
            return;
        }

        // Preço por mês
        String priceStr = ui.getInput("Digite o preço por mês (R$):");
        if (priceStr == null) return;

        double pricePerMonth;
        try {
            pricePerMonth = Double.parseDouble(priceStr.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            ui.showError("Preço inválido. Digite um valor numérico.");
            return;
        }

        OperationResult result = fitManager.registerPlan(name, description, type, minimumDuration, pricePerMonth);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Exibe menu para seleção do tipo de plano (PlanType).
     *
     * @return PlanType selecionado, ou null se cancelou
     */
    private PlanType selectPlanType() {
        StringBuilder options = new StringBuilder();
        PlanType[] types = PlanType.values();
        for (int i = 0; i < types.length; i++) {
            options.append((i + 1)).append(" - ").append(types[i].getLabel()).append("\n");
        }

        String choice = ui.showMenu("Tipo do Plano", options.toString());
        if (choice == null) return null;

        try {
            int index = Integer.parseInt(choice.trim()) - 1;
            if (index >= 0 && index < types.length) {
                return types[index];
            }
        } catch (NumberFormatException e) {
            // Ignora — será tratado abaixo
        }

        ui.showError("Tipo de plano inválido.");
        return null;
    }

    /**
     * Fluxo de consulta de plano por nome.
     */
    private void findPlanByName() {
        String name = ui.getInput("Digite o nome do plano para consulta:");
        if (name == null) return;

        OperationResult result = fitManager.findPlanByName(name);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage("Plano encontrado:\n\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de alteração de preço de um plano.
     */
    private void updatePrice() {
        String name = ui.getInput("Digite o nome do plano para alterar o preço:");
        if (name == null) return;

        // Primeiro verifica se o plano existe
        OperationResult findResult = fitManager.findPlanByName(name);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Plan currentPlan = (Plan) findResult.getData();
        ui.showMessage("Plano encontrado:\n\n" + currentPlan.toString());

        String newPriceStr = ui.getInput(
                "Preço atual: R$ " + String.format("%.2f", currentPlan.getPricePerMonth()) +
                "\n\nDigite o novo preço por mês (R$):");
        if (newPriceStr == null) return;

        double newPrice;
        try {
            newPrice = Double.parseDouble(newPriceStr.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            ui.showError("Preço inválido. Digite um valor numérico.");
            return;
        }

        OperationResult result = fitManager.updatePlanPrice(name, newPrice);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os planos.
     */
    @SuppressWarnings("unchecked")
    private void listAllPlans() {
        OperationResult result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        List<Plan> plans = (List<Plan>) result.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("======== PLANOS CADASTRADOS ========\n");
        sb.append("Total: ").append(plans.size()).append(" plano(s)\n\n");

        for (int i = 0; i < plans.size(); i++) {
            sb.append("--- Plano ").append(i + 1).append(" ---\n");
            sb.append(plans.get(i).toString());
            if (i < plans.size() - 1) {
                sb.append("\n\n");
            }
        }

        ui.showMessage(sb.toString());
    }
}
