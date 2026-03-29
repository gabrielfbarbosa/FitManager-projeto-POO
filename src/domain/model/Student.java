package domain.model;

import java.time.LocalDate;
import java.time.Period;

public class Student {

    private String name;
    private String cpf;
    private String contact;
    private LocalDate birthDate;
    private boolean active;
    private LocalDate removedAt;

    public Student(String name, String cpf, String contact, LocalDate birthDate) {
        this.name = name;
        this.cpf = cpf;
        this.contact = contact;
        this.birthDate = birthDate;
        this.active = true;
        this.removedAt = null;
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Calcula a idade do aluno a partir da data de nascimento.
     * Evita armazenar um dado que pode sempre ser derivado.
     */
    public int calculateAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Ativa o aluno no sistema.
     */
    public void activate() {
        this.active = true;
        this.removedAt = null;
    }

    /**
     * Desativa o aluno (remoção lógica).
     * Registra a data de remoção para preservar histórico.
     */
    public void deactivate() {
        this.active = false;
        this.removedAt = LocalDate.now();
    }

    /**
     * Valida um CPF completo com dígito verificador.
     * Verifica:
     * - Exatamente 11 dígitos numéricos
     * - Rejeita CPFs com todos os dígitos iguais
     * - Valida ambos os dígitos verificadores (módulo 11)
     *
     * @param cpf String contendo apenas dígitos do CPF
     * @return true se o CPF é válido
     */
    public static boolean validateCpf(String cpf) {
        if (cpf == null) {
            return false;
        }

        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        // Deve ter exatamente 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Rejeita CPFs com todos os dígitos iguais
        boolean allEqual = true;
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        // Validação do primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }
        if (Character.getNumericValue(cpf.charAt(9)) != firstDigit) {
            return false;
        }

        // Validação do segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }
        if (Character.getNumericValue(cpf.charAt(10)) != secondDigit) {
            return false;
        }

        return true;
    }

    /**
     * Formata o CPF para exibição: 123.456.789-00
     */
    public String getFormattedCpf() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." +
               cpf.substring(3, 6) + "." +
               cpf.substring(6, 9) + "-" +
               cpf.substring(9);
    }

    // ========================
    // Getters e Setters
    // ========================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getRemovedAt() {
        return removedAt;
    }

    @Override
    public String toString() {
        return "Nome: " + name + "\n" +
               "CPF: " + getFormattedCpf() + "\n" +
               "Contato: " + contact + "\n" +
               "Data de Nascimento: " + String.format("%02d/%02d/%04d",
                    birthDate.getDayOfMonth(), birthDate.getMonthValue(), birthDate.getYear()) + "\n" +
               "Idade: " + calculateAge() + " anos" + "\n" +
               "Status: " + (active ? "Ativo" : "Inativo");
    }
}
