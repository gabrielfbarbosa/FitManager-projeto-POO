package ui.menus.plan;

import application.FitManager;
import application.OperationResult;
import domain.enums.PlanType;
import domain.model.Plan;

import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de gerenciamento de planos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class PlanMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public PlanMenu(UserInterface ui, FitManager fitManager) {
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
            String menuOptions = "";
            for (PlanMenuOption opt : PlanMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR PLANOS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + PlanMenuOption.values().length + ".");
                continue;
            }

            PlanMenuOption option = PlanMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + PlanMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case CADASTRAR:      registerPlan();     break;
                case CONSULTAR_NOME: findPlanByName();   break;
                case ALTERAR_PRECO:  updatePrice();      break;
                case LISTAR:         listAllPlans();      break;
                case VOLTAR:         running = false;    break;
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

        PlanType type = selectPlanType();
        if (type == null) return;

        int minimumDuration = ui.getIntInput("Digite a duração mínima em meses:");
        if (minimumDuration == Integer.MIN_VALUE) return;

        double pricePerMonth = ui.getDoubleInput("Digite o preço por mês (R$):");
        if (Double.isNaN(pricePerMonth)) return;

        OperationResult result = fitManager.registerPlan(
                name, description, type, minimumDuration, pricePerMonth);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Exibe menu para seleção do tipo de plano (PlanType).
     * Os valores do enum são exibidos numerados — mapeamento robusto e sem strings literais.
     *
     * @return PlanType selecionado, ou null se cancelou ou entrada inválida
     */
    private PlanType selectPlanType() {
        PlanType[] types = PlanType.values();
        String options = "";
        for (int i = 0; i < types.length; i++) {
            options += (i + 1) + " - " + types[i].getLabel() + "\n";
        }

        String choice = ui.showMenu("Selecione o Tipo do Plano", options);
        if (choice == null) return null;
        if (!InputParser.isNumeric(choice)) {
            ui.showError("Tipo de plano inválido. Digite um número de 1 a " + types.length + ".");
            return null;
        }

        int index = Integer.parseInt(choice.trim()) - 1;
        if (index >= 0 && index < types.length) {
            return types[index];
        }

        ui.showError("Tipo de plano inválido. Escolha de 1 a " + types.length + ".");
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
     * Exibe os dados atuais antes de solicitar o novo valor.
     */
    private void updatePrice() {
        String name = ui.getInput("Digite o nome do plano para alterar o preço:");
        if (name == null) return;

        OperationResult findResult = fitManager.findPlanByName(name);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Plan currentPlan = (Plan) findResult.getData();
        ui.showMessage("Plano: " + currentPlan.getName()
                + "\nPreço atual: R$ " + String.format("%.2f", currentPlan.getPricePerMonth()));

        double newPrice = ui.getDoubleInput("Digite o novo preço por mês (R$):");
        if (Double.isNaN(newPrice)) return;

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
    private void listAllPlans() {
        OperationResult result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Plan> plans = (ArrayList<Plan>) result.getData();
        String message = "> PLANOS CADASTRADOS\n";
        message += "Total: " + plans.size() + " plano(s)\n\n";

        for (int i = 0; i < plans.size(); i++) {
            message += "--- Plano " + (i + 1) + " ---\n";
            message += plans.get(i).toString();
            if (i < plans.size() - 1) message += "\n\n";
        }

        ui.showScrollableMessage(message);
    }
}