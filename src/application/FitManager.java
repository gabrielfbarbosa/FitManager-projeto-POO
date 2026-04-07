package application;

import application.services.EnrollmentService;
import application.services.PlanService;
import application.services.StudentService;
import domain.enums.PaymentType;
import domain.enums.PlanType;
import domain.model.Plan;
import domain.model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Ponto de entrada único para todas as operações do sistema.
 *
 * Os menus nunca acessam os serviços ou as classes de domínio diretamente —
 * tudo passa pelo FitManager. Isso centraliza a coordenação e evita que
 * lógica de validação fique espalhada pelo código.
 *
 * Relação de composição com os serviços: eles são criados e gerenciados
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
    public OperationResult registerStudent(String name, String cpf,
                                           String contact, String birthDate) {
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
     * Coordenação entre serviços: o FitManager consulta o EnrollmentService
     * para verificar matrículas ativas antes de delegar ao StudentService.
     * Os serviços não se comunicam diretamente entre si.
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
                    "Não é possível remover o aluno: ele possui matrícula ativa.\n"
                            + "Cancele a matrícula antes de remover o aluno.");
        }

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
     * Não afeta matrículas já registradas — totalPrice é fixado na criação do Enrollment.
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

    // ============================
    // Operações de Matrículas
    // ============================

    /**
     * Realiza a matrícula de um aluno em um plano.
     *
     * Fluxo de coordenação (Fluxo 3 do documento):
     * 1. Localiza o aluno via StudentService
     * 2. Localiza o plano via PlanService
     * 3. Verifica matrícula ativa via EnrollmentService
     * 4. Valida o pagamento inicial
     * 5. Delega a criação ao EnrollmentService
     *
     * @param cpf                CPF do aluno
     * @param planName           nome do plano
     * @param startDateStr       data de início no formato dd/MM/yyyy
     * @param durationMonths     duração em meses
     * @param initialAmount      valor do pagamento inicial
     * @param paymentType        tipo do pagamento inicial
     * @param paymentDescription descrição do pagamento inicial
     */
    public OperationResult enrollStudent(String cpf, String planName, String startDateStr,
                                         int durationMonths, double initialAmount,
                                         PaymentType paymentType, String paymentDescription) {

        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        // Localiza o aluno
        OperationResult studentResult = studentService.findByCpf(cleanCpf);
        if (!studentResult.isSuccess()) {
            return studentResult;
        }
        Student student = (Student) studentResult.getData();

        // Localiza o plano
        OperationResult planResult = planService.findByName(planName);
        if (!planResult.isSuccess()) {
            return planResult;
        }
        Plan plan = (Plan) planResult.getData();

        // Verifica se o aluno já possui matrícula ativa
        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            return new OperationResult(false,
                    "O aluno já possui uma matrícula ativa. "
                            + "Cancele a matrícula atual antes de realizar uma nova.");
        }

        // Valida o pagamento inicial
        if (initialAmount <= 0) {
            return new OperationResult(false,
                    "O valor do pagamento inicial deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }

        // Converte a data de início
        LocalDate startDate = LocalDate.parse(startDateStr.trim(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Delega ao EnrollmentService — cria Enrollment + Payment atomicamente
        return enrollmentService.enroll(student, plan, startDate, durationMonths,
                initialAmount, paymentType, paymentDescription);
    }

    /**
     * Registra um pagamento em uma matrícula existente e ativa.
     *
     * @param enrollmentCode código da matrícula (ex: "MAT-001")
     * @param amount         valor do pagamento
     * @param paymentType    tipo do pagamento
     * @param description    descrição do pagamento
     */
    public OperationResult registerPayment(String enrollmentCode, double amount,
                                           PaymentType paymentType, String description) {
        return enrollmentService.registerPayment(enrollmentCode, amount, paymentType, description);
    }

    /**
     * Cancela uma matrícula ativa.
     * O histórico e todos os pagamentos são preservados.
     *
     * @param enrollmentCode código da matrícula
     */
    public OperationResult cancelEnrollment(String enrollmentCode) {
        return enrollmentService.cancelEnrollment(enrollmentCode);
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF.
     *
     * @param cpf CPF do aluno
     */
    public OperationResult findActiveEnrollmentByStudent(String cpf) {
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        return enrollmentService.findActiveByStudentCpf(cleanCpf);
    }

    /**
     * Lista o histórico de matrículas de um aluno (ativas e canceladas).
     *
     * @param cpf CPF do aluno
     */
    public OperationResult listEnrollmentHistory(String cpf) {
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        return enrollmentService.listHistoryByStudent(cleanCpf);
    }

    // ============================
    // Operações de Relatórios
    // ============================

    /**
     * Lista todas as matrículas do sistema (ativas e canceladas).
     */
    public OperationResult listAllEnrollments() {
        return enrollmentService.listAll();
    }

    /**
     * Lista matrículas com saldo pendente.
     */
    public OperationResult listEnrollmentsWithPendingBalance() {
        return enrollmentService.listWithPendingBalance();
    }

    /**
     * Lista alunos que possuem matrícula ativa.
     * Coordena StudentService e EnrollmentService para montar a lista.
     */
    public OperationResult listStudentsWithActiveEnrollment() {
        OperationResult allStudents = studentService.listAll();
        if (!allStudents.isSuccess()) {
            return allStudents;
        }

        java.util.ArrayList<Student> active = new java.util.ArrayList<>();
        for (Object obj : (java.util.ArrayList<?>) allStudents.getData()) {
            Student s = (Student) obj;
            if (enrollmentService.hasActiveEnrollment(s.getCpf())) {
                active.add(s);
            }
        }

        if (active.isEmpty()) {
            return new OperationResult(false, "Nenhum aluno com matrícula ativa no momento.");
        }
        return new OperationResult(true,
                active.size() + " aluno(s) com matrícula ativa.", active);
    }
}