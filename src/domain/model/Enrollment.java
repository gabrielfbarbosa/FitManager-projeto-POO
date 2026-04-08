package domain.model;

import domain.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.util.ArrayList;

public class Enrollment {

    private int code;
    private Student student;
    private Plan plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMonths;
    private double totalPrice;
    private EnrollmentStatus status;
    private ArrayList<Payment> payments;

    public Enrollment(
        int code,
        Student student,
        Plan plan,
        LocalDate startDate,
        int durationMonths
    ) {
        this.code = code;
        this.student = student;
        this.plan = plan;
        this.startDate = startDate;
        this.durationMonths = durationMonths;
        this.endDate = startDate.plusMonths(durationMonths);
        // Valor total fixado no momento da criação — alterações futuras no plano não afetam este contrato
        this.totalPrice = plan.calculateTotalPrice(durationMonths);
        this.status = EnrollmentStatus.ACTIVE;
        this.payments = new ArrayList<>();
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Adiciona um pagamento à matrícula.
     *
     * @param payment pagamento a ser registrado
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    /**
     * Calcula o total pago somando todos os pagamentos registrados.
     * Opera apenas sobre dados internos da matrícula — pertence ao objeto.
     *
     * @return soma dos valores de todos os pagamentos
     */
    public double calculateTotalPaid() {
        double total = 0;
        for (Payment payment : payments) {
            total += payment.getAmount();
        }
        return total;
    }

    /**
     * Calcula o saldo pendente (valor total - total pago).
     *
     * @return saldo restante a ser pago
     */
    public double calculateBalance() {
        return totalPrice - calculateTotalPaid();
    }

    /**
     * Cancela a matrícula alterando seu status.
     */
    public boolean cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            return false; // já cancelada, operação ignorada
        }
        this.status = EnrollmentStatus.CANCELLED;
        return true;
    }

    // ========================
    // Getters
    // ========================

    public int getCode() {
        return code;
    }

    public Student getStudent() {
        return student;
    }

    public Plan getPlan() {
        return plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public ArrayList<Payment> getPayments() {
        return new ArrayList<>(payments); // cópia — ninguém modifica a lista interna
    }

    @Override
    public String toString() {
        return "Código: " + code + "\n" +
               "Aluno: " + student.getName() + " (CPF: " + student.getFormattedCpf() + ")\n" +
               "Plano: " + plan.getName() + " (" + plan.getType().getLabel() + ")\n" +
               "Período: " + String.format("%02d/%02d/%04d",
                    startDate.getDayOfMonth(), startDate.getMonthValue(), startDate.getYear()) +
               " a " + String.format("%02d/%02d/%04d",
                    endDate.getDayOfMonth(), endDate.getMonthValue(), endDate.getYear()) + "\n" +
               "Duração: " + durationMonths + (durationMonths == 1 ? " mês" : " meses") + "\n" +
               "Valor total: R$ " + String.format("%.2f", totalPrice) + "\n" +
               "Total pago: R$ " + String.format("%.2f", calculateTotalPaid()) + "\n" +
               "Saldo pendente: R$ " + String.format("%.2f", calculateBalance()) + "\n" +
               "Status: " + status.getLabel();
    }
}
