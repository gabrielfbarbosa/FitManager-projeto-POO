package domain.model;

import domain.enums.PaymentType;

import java.time.LocalDate;

public class Payment {

    private LocalDate date;
    private double amount;
    private PaymentType type;
    private String description;

    public Payment(
        LocalDate date,
        double amount,
        PaymentType type,
        String description
    ) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // ========================
    // Getters
    // ========================

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Data: " + String.format("%02d/%02d/%04d",
                    date.getDayOfMonth(), date.getMonthValue(), date.getYear()) + "\n" +
               "Valor: R$ " + String.format("%.2f", amount) + "\n" +
               "Tipo: " + type.getLabel() + "\n" +
               "Descrição: " + description;
    }
}
