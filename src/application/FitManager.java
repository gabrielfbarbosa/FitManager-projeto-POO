package application;

import application.services.EnrollmentService;
import application.services.PlanService;
import application.services.StudentService;
import domain.enums.PlanType;

/**
 * Ponto de entrada único para todas as operações do sistema.
 *
 * Os menus nunca acessam os serviços ou as classes de domínio diretamente —
 * tudo passa pelo FitManager. Isso centraliza a coordenação e evita que
 * lógica de validação fique espalhada pelo código.
 *
 * Relação de composição com os serviços — eles são criados e gerenciados
 * pelo próprio FitManager e não existem de forma independente.
 */
public class FitManager {

    private StudentService studentService;
    private PlanService planService;
    private EnrollmentService enrollmentService;

    public FitManager() {
        this.studentService = new StudentService();
        this.planService = new PlanService();
        this.enrollmentService = new EnrollmentService();
    }

    // ============================
    // Operações de Alunos
    // ============================

    /**
     * Registra um novo aluno.
     * Delega a validação e criação ao StudentService.
     */
    public OperationResult registerStudent(String name, String cpf, String contact, String birthDate) {
        return studentService.registerStudent(name, cpf, contact, birthDate);
    }

    /**
     * Consulta um aluno pelo CPF.
     */
    public OperationResult findStudentByCpf(String cpf) {
        return studentService.findByCpf(cpf);
    }

    /**
     * Atualiza os dados de um aluno (nome e/ou contato).
     */
    public OperationResult updateStudent(String cpf, String newName, String newContact) {
        return studentService.updateStudent(cpf, newName, newContact);
    }

    /**
     * Remove (desativa) um aluno.
     *
     * Coordenação: consulta o EnrollmentService para verificar a existência
     * de matrículas ativas antes de delegar a remoção ao StudentService.
     * Os serviços não se comunicam diretamente entre si — é o FitManager
     * quem os coordena.
     */
    public OperationResult removeStudent(String cpf) {
        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se o aluno existe e está ativo
        OperationResult findResult = studentService.findByCpf(cleanCpf);
        if (!findResult.isSuccess()) {
            return findResult;
        }

        // Verifica se o aluno possui matrícula ativa
        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            return new OperationResult(false,
                    "Não é possível remover o aluno: ele possui matrícula ativa.\n" +
                    "Cancele a matrícula antes de remover o aluno.");
        }

        // Delega a remoção ao StudentService
        return studentService.removeStudent(cleanCpf);
    }

    /**
     * Lista todos os alunos ativos.
     */
    public OperationResult listAllStudents() {
        return studentService.listAll();
    }

    // ============================
    // Operações de Planos
    // ============================

    /**
     * Registra um novo plano.
     * Delega a validação e criação ao PlanService.
     */
    public OperationResult registerPlan(String name, String description, PlanType type,
                                         int minimumDuration, double pricePerMonth) {
        return planService.registerPlan(name, description, type, minimumDuration, pricePerMonth);
    }

    /**
     * Consulta um plano pelo nome.
     */
    public OperationResult findPlanByName(String name) {
        return planService.findByName(name);
    }

    /**
     * Atualiza o preço mensal de um plano.
     */
    public OperationResult updatePlanPrice(String name, double newPrice) {
        return planService.updatePrice(name, newPrice);
    }

    /**
     * Lista todos os planos cadastrados.
     */
    public OperationResult listAllPlans() {
        return planService.listAll();
    }
}
