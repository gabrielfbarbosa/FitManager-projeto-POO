package ui.menu.sudent;

import application.FitManager;
import application.OperationResult;
import domain.model.Student;
import ui.screen.UserScreen;
import ui.menu.MenuOption;

import java.util.List;

/**
 * Menu de gerenciamento de alunos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class StudentMenu {

    private UserScreen ui;
    private FitManager fitManager;

    public StudentMenu(UserScreen ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de alunos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String input = ui.showMenu(
                    "==== GERENCIAR ALUNOS ====",
                    StudentMenuOption.values()
            );

            if (input == null) { running = false; continue; }

            StudentMenuOption option;
            try {
                option = MenuOption.fromNumero(StudentMenuOption.class, Integer.parseInt(input.trim()));
            } catch (NumberFormatException e) {
                option = null;
            }

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + StudentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case CADASTRAR:    registerStudent();   break;
                case CONSULTAR_CPF: findStudentByCpf(); break;
                case EDITAR:       editStudent();        break;
                case EXCLUIR:      removeStudent();      break;
                case LISTAR:       listAllStudents();    break;
                case VOLTAR:       running = false;      break;
            }
        }
    }

    /**
     * Fluxo de cadastro de novo aluno.
     * Coleta dados via UserInterface e delega ao FitManager.
     */
    private void registerStudent() {
        String name = ui.getInput("Digite o nome:");
        if (name == null) return;

        String cpf = ui.getInput("Digite o CPF (apenas números):");
        if (cpf == null) return;

        String contact = ui.getInput("Digite o contato (e-mail ou telefone):");
        if (contact == null) return;

        String birthDate = ui.getInput("Digite a data de nascimento (dd/mm/aaaa):");
        if (birthDate == null) return;

        OperationResult result = fitManager.registerStudent(name, cpf, contact, birthDate);

        if (result.isSuccess()) {
            Student student = (Student) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta de aluno por CPF.
     */
    private void findStudentByCpf() {
        String cpf = ui.getInput("Digite o CPF para consulta:");
        if (cpf == null) return;

        OperationResult result = fitManager.findStudentByCpf(cpf);

        if (result.isSuccess()) {
            Student student = (Student) result.getData();
            ui.showMessage("Aluno encontrado:\n\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de edição de cadastro do aluno.
     * Permite alterar nome e contato. Campos deixados em branco mantêm o valor atual.
     */
    private void editStudent() {
        String cpf = ui.getInput("Digite o CPF do aluno a editar:");
        if (cpf == null) return;

        // Primeiro verifica se o aluno existe
        OperationResult findResult = fitManager.findStudentByCpf(cpf);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Student currentStudent = (Student) findResult.getData();
        ui.showMessage("Aluno encontrado:\n\n" + currentStudent.toString() +
                "\n\nDeixe em branco os campos que não deseja alterar.");

        String newName = ui.getInput("Novo nome (atual: " + currentStudent.getName() + "):");
        if (newName == null) return;

        String newContact = ui.getInput("Novo contato (atual: " + currentStudent.getContact() + "):");
        if (newContact == null) return;

        OperationResult result = fitManager.updateStudent(cpf, newName, newContact);

        if (result.isSuccess()) {
            Student updated = (Student) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados atualizados:\n" + updated.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de remoção (inativação) de aluno.
     */
    private void removeStudent() {
        String cpf = ui.getInput("Digite o CPF do aluno a remover:");
        if (cpf == null) return;

        // Mostra o aluno antes de confirmar a remoção
        OperationResult findResult = fitManager.findStudentByCpf(cpf);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Student student = (Student) findResult.getData();
        String confirm = ui.getInput(
                "Confirma a remoção do aluno?\n\n" + student.toString() +
                "\n\nDigite 'S' para confirmar ou qualquer outra tecla para cancelar:");

        if (confirm == null || !confirm.trim().equalsIgnoreCase("S")) {
            ui.showMessage("Operação cancelada.");
            return;
        }

        OperationResult result = fitManager.removeStudent(cpf);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os alunos ativos.
     */
    @SuppressWarnings("unchecked")
    private void listAllStudents() {
        OperationResult result = fitManager.listAllStudents();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        List<Student> students = (List<Student>) result.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("======== ALUNOS CADASTRADOS ========\n");
        sb.append("Total: ").append(students.size()).append(" aluno(s)\n\n");

        for (int i = 0; i < students.size(); i++) {
            sb.append("--- Aluno ").append(i + 1).append(" ---\n");
            sb.append(students.get(i).toString());
            if (i < students.size() - 1) {
                sb.append("\n\n");
            }
        }

        ui.showMessage(sb.toString());
    }
}
