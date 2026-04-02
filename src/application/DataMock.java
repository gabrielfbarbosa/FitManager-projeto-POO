package application;

import domain.enums.PaymentType;
import domain.enums.PlanType;

/**
 * Popula o sistema com dados de teste para facilitar os testes manuais.
 *
 * Deve ser chamado apenas durante o desenvolvimento — desative comentando
 * a linha DataSeeder.seed(fitManager) no FitManagerApp antes da entrega.
 *
 * Dados criados:
 *   Alunos:    4 alunos ativos, 1 inativo
 *   Planos:    4 planos (um por PlanType)
 *   Matrículas: 3 ativas (com pagamentos variados), 1 cancelada
 */
public class DataMock {

    /**
     * Executa toda a carga de dados de teste no FitManager.
     * Falhas silenciosas — dados duplicados são ignorados.
     *
     * @param fm instância do FitManager já inicializado
     */
    public static void mock(FitManager fm) {
        mockPlans(fm);
        mockStudents(fm);
        mockEnrollments(fm);
    }

    // ============================
    // Planos
    // ============================

    private static void mockPlans(FitManager fm) {
        // Plano Mensal — mínimo 1 mês, R$ 99,90/mês
        fm.registerPlan(
                "Plano Mensal",
                "Acesso à academia por 1 mês, renovável mensalmente.",
                PlanType.MONTHLY,
                1,
                99.90
        );

        // Plano Trimestral — mínimo 3 meses, R$ 89,90/mês
        fm.registerPlan(
                "Plano Trimestral",
                "Acesso à academia por trimestre com desconto progressivo.",
                PlanType.QUARTERLY,
                3,
                89.90
        );

        // Plano Semestral — mínimo 6 meses, R$ 79,90/mês
        fm.registerPlan(
                "Plano Semestral",
                "Acesso à academia por semestre com desconto progressivo.",
                PlanType.SEMI_ANNUAL,
                6,
                79.90
        );

        // Plano Anual — mínimo 12 meses, R$ 69,90/mês
        fm.registerPlan(
                "Plano Anual",
                "Acesso à academia por um ano inteiro com melhor custo-benefício.",
                PlanType.ANNUAL,
                12,
                69.90
        );
    }

    // ============================
    // Alunos
    // ============================

    private static void mockStudents(FitManager fm) {
        // Aluno 1 — CPF válido, terá matrícula ativa com pagamentos em dia
        fm.registerStudent(
                "Carlos Eduardo Silva",
                "52998224725",
                "carlos.silva@email.com",
                "15/03/1995"
        );

        // Aluno 2 — terá matrícula ativa com saldo pendente
        fm.registerStudent(
                "Ana Paula Ferreira",
                "71428793860",
                "(67) 99123-4567",
                "22/07/1998"
        );

        // Aluno 3 — terá matrícula ativa quitada
        fm.registerStudent(
                "Bruno Henrique Costa",
                "87748248800",
                "bruno.costa@gmail.com",
                "08/11/1990"
        );

        // Aluno 4 — terá matrícula cancelada (útil para testar relatórios)
        fm.registerStudent(
                "Fernanda Lima Rocha",
                "34650463280",
                "fernanda.rocha@email.com",
                "30/01/2000"
        );

        // Aluno 5 — sem matrícula (útil para testar cadastro de nova matrícula)
        fm.registerStudent(
                "Ricardo Mendes Alves",
                "47093450819",
                "(67) 98765-4321",
                "14/06/1985"
        );

        // Aluno 6 — será inativado (útil para testar busca de inativo)
        fm.registerStudent(
                "Juliana Torres Souza",
                "07859546431",
                "ju.torres@email.com",
                "03/09/1993"
        );
        fm.removeStudent("07859546431");
    }

    // ============================
    // Matrículas e Pagamentos
    // ============================

    private static void mockEnrollments(FitManager fm) {

        // Matrícula 1 — Carlos / Plano Anual / 12 meses
        // Pagamentos: 3 parcelas pagas → saldo pendente
        fm.enrollStudent(
                "52998224725",
                "Plano Anual",
                "01/01/2026",
                12,
                69.90,
                PaymentType.PIX,
                "1ª parcela — janeiro"
        );
        fm.registerPayment("MAT-001", 69.90, PaymentType.PIX,    "2ª parcela — fevereiro");
        fm.registerPayment("MAT-001", 69.90, PaymentType.DEBIT_CARD, "3ª parcela — março");

        // Matrícula 2 — Ana Paula / Plano Mensal / 1 mês
        // Pagamento inicial apenas → saldo pendente de parte do mês
        fm.enrollStudent(
                "71428793860",
                "Plano Mensal",
                "01/03/2026",
                1,
                50.00,
                PaymentType.CASH,
                "Entrada parcial"
        );

        // Matrícula 3 — Bruno / Plano Trimestral / 3 meses / quitada
        // Total: 3 × R$89,90 = R$269,70 — pago integralmente
        fm.enrollStudent(
                "87748248800",
                "Plano Trimestral",
                "01/01/2026",
                3,
                89.90,
                PaymentType.CREDIT_CARD,
                "1ª parcela"
        );
        fm.registerPayment("MAT-003", 89.90, PaymentType.CREDIT_CARD, "2ª parcela");
        fm.registerPayment("MAT-003", 89.90, PaymentType.CREDIT_CARD, "3ª parcela — quitado");

        // Matrícula 4 — Fernanda / Plano Semestral / 6 meses → cancelada
        // Útil para testar: cancelamento, relatório de saldo pendente em cancelada
        fm.enrollStudent(
                "34650463280",
                "Plano Semestral",
                "01/02/2026",
                6,
                79.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );
        fm.cancelEnrollment("MAT-004");
    }
}