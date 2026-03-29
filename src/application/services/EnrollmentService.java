package application.services;

import domain.enums.EnrollmentStatus;
import domain.model.Enrollment;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por manter a coleção de matrículas em memória.
 *
 * O atributo estático nextCode gera códigos sequenciais únicos (MAT-001, MAT-002...).
 * Por ser static, pertence à classe e não a cada instância, garantindo
 * unicidade durante toda a execução do programa.
 *
 * Nesta etapa, apenas o método hasActiveEnrollment() está funcional.
 * Os demais métodos serão implementados na próxima etapa.
 */
public class EnrollmentService {

    private static int nextCode = 1;
    private List<Enrollment> enrollments;

    public EnrollmentService() {
        this.enrollments = new ArrayList<>();
    }

    /**
     * Gera o próximo código de matrícula no formato MAT-XXX.
     *
     * @return código formatado (ex: "MAT-001")
     */
    public static String generateNextCode() {
        String code = String.format("MAT-%03d", nextCode);
        nextCode++;
        return code;
    }

    /**
     * Verifica se um aluno (identificado pelo CPF) possui matrícula ativa.
     * Usado pelo FitManager para validar a remoção de alunos.
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
     * Retorna a lista interna de matrículas.
     * Acesso direto para uso pelo FitManager em coordenações futuras.
     */
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
}
