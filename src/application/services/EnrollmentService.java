package application.services;

import application.OperationResult;
import domain.enums.EnrollmentStatus;
import domain.enums.PaymentType;
import domain.model.Enrollment;
import domain.model.Payment;
import domain.model.Plan;
import domain.model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
 *
 * O atributo estático nextCode gera códigos sequenciais únicos (MAT-001, MAT-002...).
 * Por ser static, pertence à classe — não a cada instância — garantindo
 * unicidade durante toda a execução do programa.
 */
public class EnrollmentService {

    private static int nextCode = 1;
    private List<Enrollment> enrollments;

    public EnrollmentService() {
        this.enrollments = new ArrayList<>();
    }

    // ============================
    // Geração de código
    // ============================

    /**
     * Gera o próximo código de matrícula no formato MAT-XXX.
     * Método estático — o contador pertence à classe, não à instância.
     *
     * @return código formatado (ex: "MAT-001")
     */
    public static String generateNextCode() {
        String code = String.format("MAT-%03d", nextCode);
        nextCode++;
        return code;
    }

    // ============================
    // Operações principais
    // ============================

    /**
     * Realiza a matrícula de um aluno em um plano.
     *
     * Cria Enrollment e Payment inicial de forma atômica: se qualquer
     * validação falhar, nenhum objeto é adicionado à coleção.
     *
     * Pré-condições verificadas pelo FitManager antes de chamar este método:
     * - aluno existe e está ativo
     * - plano existe
     * - aluno não possui matrícula ativa
     * - valor do pagamento inicial é positivo
     *
     * @param student           aluno a ser matriculado
     * @param plan              plano escolhido
     * @param startDate         data de início da matrícula
     * @param durationMonths    duração contratada em meses
     * @param initialAmount     valor do pagamento inicial
     * @param paymentType       tipo do pagamento inicial
     * @param paymentDescription descrição do pagamento inicial
     * @return OperationResult com o Enrollment criado em data (se sucesso)
     */
    public OperationResult enroll(Student student, Plan plan, LocalDate startDate,
                                  int durationMonths, double initialAmount,
                                  PaymentType paymentType, String paymentDescription) {

        // Valida duração mínima do plano
        if (durationMonths < plan.getMinimumDuration()) {
            return new OperationResult(false,
                    "A duração mínima para o plano \"" + plan.getName() + "\" é de "
                            + plan.getMinimumDuration()
                            + (plan.getMinimumDuration() == 1 ? " mês." : " meses."));
        }

        // Valida data de início
        if (startDate == null) {
            return new OperationResult(false, "A data de início é obrigatória.");
        }

        // Valida tipo de pagamento
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }

        // Cria a matrícula — totalPrice calculado e fixado no construtor de Enrollment
        String code = generateNextCode();
        Enrollment enrollment = new Enrollment(code, student, plan, startDate, durationMonths);

        // Cria e registra o pagamento inicial — atomicamente com a matrícula
        String desc = (paymentDescription == null || paymentDescription.trim().isEmpty())
                ? "Pagamento inicial"
                : paymentDescription.trim();
        Payment initialPayment = new Payment(LocalDate.now(), initialAmount, paymentType, desc);
        enrollment.addPayment(initialPayment);

        // Só adiciona à coleção após tudo estar validado e criado
        enrollments.add(enrollment);

        return new OperationResult(true,
                "✅ Matrícula " + code + " realizada com sucesso!", enrollment);
    }

    /**
     * Registra um pagamento em uma matrícula existente e ativa.
     *
     * @param enrollmentCode código da matrícula (ex: "MAT-001")
     * @param amount         valor do pagamento
     * @param paymentType    tipo do pagamento
     * @param description    descrição do pagamento
     * @return OperationResult com o Enrollment atualizado em data (se sucesso)
     */
    public OperationResult registerPayment(String enrollmentCode, double amount,
                                           PaymentType paymentType, String description) {

        if (enrollmentCode == null || enrollmentCode.trim().isEmpty()) {
            return new OperationResult(false, "O código da matrícula é obrigatório.");
        }
        if (amount <= 0) {
            return new OperationResult(false, "O valor do pagamento deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }

        Enrollment enrollment = findByCode(enrollmentCode.trim());
        if (enrollment == null) {
            return new OperationResult(false,
                    "Matrícula \"" + enrollmentCode + "\" não encontrada.");
        }

        // Pagamentos em matrículas canceladas não são permitidos
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult(false,
                    "Não é possível registrar pagamento em uma matrícula cancelada.");
        }

        String desc = (description == null || description.trim().isEmpty())
                ? "Pagamento"
                : description.trim();
        Payment payment = new Payment(LocalDate.now(), amount, paymentType, desc);
        enrollment.addPayment(payment);

        return new OperationResult(true,
                "✅ Pagamento de R$ " + String.format("%.2f", amount)
                        + " registrado com sucesso!", enrollment);
    }

    /**
     * Cancela uma matrícula ativa.
     * O histórico e todos os pagamentos são preservados.
     *
     * @param enrollmentCode código da matrícula
     * @return OperationResult com o Enrollment cancelado em data (se sucesso)
     */
    public OperationResult cancelEnrollment(String enrollmentCode) {
        if (enrollmentCode == null || enrollmentCode.trim().isEmpty()) {
            return new OperationResult(false, "O código da matrícula é obrigatório.");
        }

        Enrollment enrollment = findByCode(enrollmentCode.trim());
        if (enrollment == null) {
            return new OperationResult(false,
                    "Matrícula \"" + enrollmentCode + "\" não encontrada.");
        }

        // cancel() retorna false se já estava cancelada (verificação no objeto)
        boolean cancelled = enrollment.cancel();
        if (!cancelled) {
            return new OperationResult(false,
                    "Esta matrícula já está cancelada.");
        }

        return new OperationResult(true,
                "✅ Matrícula " + enrollmentCode + " cancelada com sucesso.", enrollment);
    }

    // ============================
    // Consultas
    // ============================

    /**
     * Busca a matrícula ativa de um aluno pelo CPF.
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return OperationResult com o Enrollment ativo em data (se encontrado)
     */
    public OperationResult findActiveByStudentCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudent().getCpf().equals(cleanCpf)
                    && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return new OperationResult(true, "Matrícula ativa encontrada.", enrollment);
            }
        }

        return new OperationResult(false,
                "Nenhuma matrícula ativa encontrada para o CPF informado.");
    }

    /**
     * Verifica se um aluno possui matrícula ativa.
     * Usado pelo FitManager para validar remoção de alunos e nova matrícula.
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return true se o aluno possui ao menos uma matrícula ativa
     */
    public boolean hasActiveEnrollment(String cpf) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudent().getCpf().equals(cpf)
                    && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     *
     * @return OperationResult com List<Enrollment> em data
     */
    public OperationResult listAll() {
        if (enrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula registrada no sistema.");
        }
        return new OperationResult(true,
                enrollments.size() + " matrícula(s) encontrada(s).",
                new ArrayList<>(enrollments));
    }

    /**
     * Lista apenas as matrículas ativas.
     *
     * @return OperationResult com List<Enrollment> em data
     */
    public OperationResult listActive() {
        List<Enrollment> active = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                active.add(enrollment);
            }
        }

        if (active.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula ativa no sistema.");
        }
        return new OperationResult(true,
                active.size() + " matrícula(s) ativa(s).", active);
    }

    /**
     * Lista matrículas com saldo pendente (balance > 0).
     * Inclui matrículas ativas e canceladas com débito em aberto.
     *
     * @return OperationResult com List<Enrollment> em data
     */
    public OperationResult listWithPendingBalance() {
        List<Enrollment> pending = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.calculateBalance() > 0) {
                pending.add(enrollment);
            }
        }

        if (pending.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula com saldo pendente.");
        }
        return new OperationResult(true,
                pending.size() + " matrícula(s) com saldo pendente.", pending);
    }

    /**
     * Lista o histórico de matrículas de um aluno (todas, ativas e canceladas).
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return OperationResult com List<Enrollment> em data
     */
    public OperationResult listHistoryByStudent(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        List<Enrollment> history = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudent().getCpf().equals(cleanCpf)) {
                history.add(enrollment);
            }
        }

        if (history.isEmpty()) {
            return new OperationResult(false,
                    "Nenhuma matrícula encontrada para o CPF informado.");
        }
        return new OperationResult(true,
                history.size() + " matrícula(s) no histórico.", history);
    }

    // ============================
    // Método auxiliar privado
    // ============================

    /**
     * Busca uma matrícula pelo código.
     * Retorna null se não encontrada.
     */
    private Enrollment findByCode(String code) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCode().equalsIgnoreCase(code)) {
                return enrollment;
            }
        }
        return null;
    }
}