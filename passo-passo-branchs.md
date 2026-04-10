# Passo a Passo: Criando as Feature Branches do FitManager

## Visão Geral

Este documento é um guia prático e passo-a-passo para criar todas as branches de feature do projeto FitManager. Cada branch implementa um módulo completo do sistema, pronto para ser integrado via Pull Request (PR) no `stage-1`.

**Status atual**: A branch `feature/user-interface` (branch 1) foi criada e seu PR já foi aberto. Todas as branches subsequentes (2 a 6) partem de `stage-1` **DEPOIS que a PR da branch anterior foi mesclada**.

---

## Nota Importante sobre Dependências

A ordem original do `guia-branches.md` sugeria que `PaymentType` e `Payment` fossem criados na Branch 5 (payment-management). No entanto, a Branch 4 (enrollment-management) necessita desses enums e classes logo na primeira matrícula, porque `EnrollmentService.enroll()` cria um pagamento inicial. Para garantir compilação e funcionamento correto em cada etapa, **PaymentType e Payment foram movidos para a Branch 4**. Isso não afeta a funcionalidade geral — apenas reorganiza as dependências de forma lógica.

---

## Branch 2: feature/student-management

### Objetivo
Implementar o gerenciamento completo de alunos com:
- InputParser para validação segura de entradas
- UserInterface com novos métodos (getIntInput, getDoubleInput, showScrollableMessage)
- OperationResult para retorno padronizado de operações
- Domínio Student com validação de CPF
- StudentService com CRUD
- StudentMenu com todos os fluxos
- Integração com FitManager

### Criar a branch

```bash
cd /caminho/para/FitManager

# Certificar que está no stage-1 e atualizado
git checkout stage-1
git pull origin stage-1

# Criar branch a partir de stage-1
git checkout -b feature/student-management

# Confirmar que está na branch correta
git branch -v
```

### Commit 1: InputParser

Criar o arquivo `src/ui/screen/InputParser.java`:

```java
package ui.screen;

/**
 * Utilitário estático para validação e conversão de entradas do usuário.
 *
 * Centraliza a lógica de parsing que é usada tanto pela UserInterface
 * (em getIntInput e getDoubleInput) quanto diretamente pelos menus
 * (para validar a seleção de opções antes de chamar parseInt).
 *
 * Toda validação é feita caractere a caractere, sem regex e sem try/catch.
 * Responsabilidade única: transformar strings brutas de entrada em valores
 * primitivos seguros, ou sinalizar entrada inválida via valores sentinela
 * (Integer.MIN_VALUE para int, Double.NaN para double).
 */
public class InputParser {

    /**
     * Verifica se uma string representa um número inteiro não-negativo.
     * Percorre caractere a caractere — sem regex e sem try/catch.
     *
     * @param value string a verificar (pode ter espaços nas bordas)
     * @return true se contiver apenas dígitos e não for vazia
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se uma string representa um número decimal válido.
     * Aceita dígitos e no máximo um ponto como separador decimal.
     * Percorre caractere a caractere — sem regex e sem try/catch.
     *
     * @param value string já normalizada (vírgula já substituída por ponto)
     * @return true se for um decimal válido (ex: "99.90", "100", "0.5")
     */
    public static boolean isDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        int dotCount = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '.') {
                dotCount++;
                if (dotCount > 1) return false;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        // Rejeita string que é só "." sem nenhum dígito
        return !trimmed.equals(".");
    }

    /**
     * Converte uma string em inteiro de forma segura.
     * Retorna Integer.MIN_VALUE se a string for nula, vazia ou não numérica.
     *
     * @param value string a converter
     * @return valor inteiro, ou Integer.MIN_VALUE se inválido
     */
    public static int parseIntSafe(String value) {
        if (!isNumeric(value)) {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(value.trim());
    }

    /**
     * Converte uma string em double de forma segura.
     * Aceita vírgula como separador decimal.
     * Retorna Double.NaN se a string for nula, vazia ou não numérica.
     *
     * @param value string a converter
     * @return valor double, ou Double.NaN se inválido
     */
    public static double parseDoubleSafe(String value) {
        if (value == null) {
            return Double.NaN;
        }
        String normalized = value.trim().replace(",", ".");
        if (!isDecimal(normalized)) {
            return Double.NaN;
        }
        return Double.parseDouble(normalized);
    }
}
```

Fazer commit:

```bash
git add src/ui/screen/InputParser.java
git commit -m "Criar InputParser com validação e conversão segura de entradas"
```

### Commit 2: Atualizar UserInterface

Substituir o arquivo `src/ui/screen/UserInterface.java` pelo seguinte:

```java
package ui.screen;


import javax.swing.*;
import java.awt.*;

/**
 * Centraliza todas as operações de entrada e saída do sistema.
 *
 * Em vez de cada menu interagir diretamente com JOptionPane,
 * essas chamadas ficam encapsuladas em quatro métodos: showMenu(),
 * getInput(), showMessage() e showError().
 *
 * Qualquer mudança na forma de exibição afeta apenas esta classe,
 * sem impactar o restante do sistema.
 */
public class UserInterface {

    private static final String APP_TITLE = "FitManager";

    /**
     * Exibe um menu com título e opções, retornando a opção escolhida pelo usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param title   título do menu
     * @param options texto completo com as opções numeradas
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String showMenu(String title, String options) {
        return JOptionPane.showInputDialog(
                null,
                options + "\n\nEscolha uma opção:",
                APP_TITLE + " " + title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Captura uma entrada de texto do usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param prompt texto do prompt exibido
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String getInput(String prompt) {
        String input = JOptionPane.showInputDialog(
                null,
                prompt,
                APP_TITLE,
                JOptionPane.QUESTION_MESSAGE
        );
        return input;
    }

    /**
     * Exibe uma mensagem de sucesso/informação.
     *
     * @param message texto da mensagem
     */
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Exibe uma mensagem de erro.
     *
     * @param message texto do erro
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE + " | [ERRO]",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Exibe uma Tela onde pode ser feito o scroll.
     *
     * @param message texto da informação exibida
     */
    public void showScrollableMessage(String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(
                null,
                scrollPane,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Solicita e converte uma entrada inteira do usuário.
     * Delega a validação e conversão ao InputParser.
     * Retorna Integer.MIN_VALUE se o usuário cancelar ou digitar valor não numérico.
     *
     * @param prompt texto do prompt exibido
     * @return o valor inteiro, ou Integer.MIN_VALUE se cancelou ou inválido
     */
    public int getIntInput(String prompt) {
        String input = getInput(prompt);
        return InputParser.parseIntSafe(input);
    }

    /**
     * Solicita e converte uma entrada decimal do usuário.
     * Aceita vírgula como separador decimal.
     * Delega a validação e conversão ao InputParser.
     * Retorna Double.NaN se o usuário cancelar ou digitar valor não numérico.
     *
     * @param prompt texto do prompt exibido
     * @return o valor decimal, ou Double.NaN se cancelou ou inválido
     */
    public double getDoubleInput(String prompt) {
        String input = getInput(prompt);
        return InputParser.parseDoubleSafe(input);
    }
}
```

Fazer commit:

```bash
git add src/ui/screen/UserInterface.java
git commit -m "Adicionar InputParser, showScrollableMessage, getIntInput e getDoubleInput à UserInterface"
```

### Commit 3: OperationResult

Criar o arquivo `src/application/OperationResult.java`:

```java
package application;

/**
 * Classe de retorno padronizado para todas as operações do sistema.
 *
 * Em vez de retornar booleanos ou lançar exceções, os métodos retornam
 * um OperationResult contendo: sucesso, mensagem descritiva e dado opcional.
 *
 * Oferece dois construtores por sobrecarga:
 * - Simples: sucesso + mensagem
 * - Completo: sucesso + mensagem + dado
 *
 * O campo data (Object) permite que operações retornem objetos junto ao resultado.
 * Em etapas futuras, Object será substituído por um tipo genérico T.
 */
public class OperationResult {

    private boolean success;
    private String message;
    private Object data;

    /**
     * Construtor simples — apenas sucesso e mensagem.
     */
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    /**
     * Construtor completo — sucesso, mensagem e dado de retorno.
     */
    public OperationResult(
        boolean success,
        String message,
        Object data
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
```

Fazer commit:

```bash
git add src/application/OperationResult.java
git commit -m "Criar classe OperationResult com construtores por sobrecarga"
```

### Commit 4: Student (domínio)

Criar o arquivo `src/domain/model/Student.java`:

```java
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

    public Student(
        String name,
        String cpf,
        String contact,
        LocalDate birthDate
    ) {
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
     * Remove todos os caracteres não numéricos de uma string.
     *
     * @param value string de entrada (pode conter pontos, traços, espaços etc.)
     * @return string contendo apenas os dígitos numéricos
     */
    public static String cleanCpf(String value) {
        if (value == null) {
            return "";
        }

        char[] digits = new char[value.length()];
        int count = 0;

        for (char c : value.toCharArray()) {
            if (c >= '0' && c <= '9') {
                digits[count] = c;
                count++;
            }
        }

        return new String(digits, 0, count);
    }

    /**
     * Verifica se todos os dígitos de um CPF (já limpo) são iguais.
     * CPFs como "111.111.111-11" são estruturalmente inválidos.
     *
     * @param cpf string com exatamente 11 dígitos numéricos
     * @return true se todos os dígitos forem iguais
     */
    private static boolean allDigitsEqual(String cpf) {
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula um dígito verificador do CPF pelo algoritmo módulo 11.
     *
     * @param cpf           string com exatamente 11 dígitos numéricos
     * @param digitPosition posição do dígito a calcular: 9 para o primeiro, 10 para o segundo
     * @return dígito verificador esperado (0–9)
     */
    private static int calculateVerifierDigit(String cpf, int digitPosition) {
        int sum = 0;
        for (int i = 0; i < digitPosition; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (digitPosition + 1 - i);
        }
        int digit = 11 - (sum % 11);
        return (digit >= 10) ? 0 : digit;
    }

    /**
     * Valida um CPF completo com dígito verificador.
     * Verifica:
     * - Exatamente 11 dígitos numéricos
     * - Rejeita CPFs com todos os dígitos iguais
     * - Valida ambos os dígitos verificadores (módulo 11)
     *
     * @param cpf String com o CPF (pode conter formatação; será limpo internamente)
     * @return true se o CPF é válido
     */
    public static boolean validateCpf(String cpf) {
        if (cpf == null) return false;
        cpf = cleanCpf(cpf);
        if (cpf.length() != 11) return false;
        if (allDigitsEqual(cpf)) return false;
        if (Character.getNumericValue(cpf.charAt(9))  != calculateVerifierDigit(cpf, 9))  return false;
        if (Character.getNumericValue(cpf.charAt(10)) != calculateVerifierDigit(cpf, 10)) return false;
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
```

Fazer commit:

```bash
git add src/domain/model/Student.java
git commit -m "Criar classe Student com atributos, calculateAge e validateCpf"
```

### Commit 5: StudentService

Criar o arquivo `src/application/services/StudentService.java`:

```java
package application.services;

import application.OperationResult;
import domain.model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de alunos em memória
 * e implementar as operações específicas da entidade Student.
 *
 * Conhece apenas objetos do seu próprio domínio.
 */
public class StudentService {

    private ArrayList<Student> students;

    public StudentService() {
        this.students = new ArrayList<>();
    }

    /**
     * Registra um novo aluno no sistema.
     * Valida campos obrigatórios, formato do CPF e unicidade.
     *
     * @return OperationResult com o Student criado em data (se sucesso)
     */
    public OperationResult registerStudent(
        String name,
        String cpf,
        String contact,
        String birthDateStr
    ) {
        OperationResult fieldCheck = validateRequiredFields(name, cpf, contact, birthDateStr);
        if (!fieldCheck.isSuccess()) {
            return fieldCheck;
        }

        String cleanCpf = Student.cleanCpf(cpf);

        if (!Student.validateCpf(cleanCpf)) {
            return new OperationResult(false, "CPF inválido. Verifique os dígitos e tente novamente.");
        }
        if (cpfExists(cleanCpf)) {
            return new OperationResult(false, "Já existe um aluno cadastrado com este CPF.");
        }

        LocalDate birthDate = LocalDate.parse(
                birthDateStr.trim(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        if (birthDate.isAfter(LocalDate.now())) {
            return new OperationResult(false, "A data de nascimento não pode ser uma data futura.");
        }

        Student student = new Student(name.trim(), cleanCpf, contact.trim(), birthDate);
        students.add(student);

        return new OperationResult(true,
                "✅ Aluno " + student.getName() + " registrado com sucesso!", student);
    }

    /**
     * Valida que nenhum campo obrigatório do aluno está vazio.
     */
    private OperationResult validateRequiredFields(String name, String cpf,
                                                   String contact, String birthDateStr) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome é obrigatório.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório.");
        }
        if (contact == null || contact.trim().isEmpty()) {
            return new OperationResult(false, "O contato é obrigatório.");
        }
        if (birthDateStr == null || birthDateStr.trim().isEmpty()) {
            return new OperationResult(false, "A data de nascimento é obrigatória.");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Busca um aluno pelo CPF (apenas alunos ativos).
     *
     * @return OperationResult com o Student encontrado em data (se sucesso)
     */
    public OperationResult findByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                return new OperationResult(true, "Aluno encontrado.", student);
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Remove (desativa) um aluno pelo CPF.
     * A verificação de matrículas ativas é responsabilidade do FitManager.
     *
     * @return OperationResult indicando sucesso ou falha
     */
    public OperationResult removeStudent(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                student.deactivate();
                return new OperationResult(true,
                        "✅ Aluno " + student.getName() + " removido com sucesso.");
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Atualiza os dados de um aluno.
     * Apenas nome e contato podem ser alterados — CPF e data de nascimento são imutáveis.
     *
     * @return OperationResult com o Student atualizado em data (se sucesso)
     */
    public OperationResult updateStudent(
        String cpf,
        String newName,
        String newContact
    ) {
        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                if (newName != null && !newName.trim().isEmpty()) {
                    student.setName(newName.trim());
                }
                if (newContact != null && !newContact.trim().isEmpty()) {
                    student.setContact(newContact.trim());
                }
                return new OperationResult(true,
                        "✅ Cadastro do aluno atualizado com sucesso!", student);
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Lista todos os alunos ativos.
     *
     * @return OperationResult com ArrayList<Student> em data
     */
    public OperationResult listAll() {
        ArrayList<Student> activeStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.isActive()) {
                activeStudents.add(student);
            }
        }

        if (activeStudents.isEmpty()) {
            return new OperationResult(false, "Nenhum aluno cadastrado no sistema.");
        }

        return new OperationResult(true,
                activeStudents.size() + " aluno(s) encontrado(s).", activeStudents);
    }

    /**
     * Verifica se um CPF já está cadastrado no sistema (ativo ou inativo).
     */
    public boolean cpfExists(String cpf) {
        for (Student student : students) {
            if (student.getCpf().equals(cpf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se existe um aluno ativo com o CPF informado.
     */
    public boolean hasActiveStudent(String cpf) {
        for (Student student : students) {
            if (student.getCpf().equals(cpf) && student.isActive()) {
                return true;
            }
        }
        return false;
    }
}
```

Fazer commit:

```bash
git add src/application/services/StudentService.java
git commit -m "Implementar StudentService com registro, consulta, remoção e listagem de alunos"
```

### Commit 6: FitManager esqueleto + métodos de aluno

Criar o arquivo `src/application/FitManager.java`:

```java
package application;

import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.model.Student;

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
        String cleanCpf = Student.cleanCpf(cpf);

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
}
```

Também criar os esqueletos dos serviços que ainda não existem:

Criar o arquivo `src/application/services/PlanService.java`:

```java
package application.services;

import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de planos em memória
 * e implementar as operações específicas da entidade Plan.
 *
 * Conhece apenas objetos do seu próprio domínio.
 */
public class PlanService {

    public PlanService() {
    }
}
```

Criar o arquivo `src/application/services/EnrollmentService.java`:

```java
package application.services;

import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
 */
public class EnrollmentService {

    public EnrollmentService() {
    }

    /**
     * Verifica se um aluno possui matrícula ativa.
     * Usado pelo FitManager para validar remoção de alunos e nova matrícula.
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return true se o aluno possui ao menos uma matrícula ativa
     */
    public boolean hasActiveEnrollment(String cpf) {
        return false;
    }
}
```

Fazer commit:

```bash
git add src/application/FitManager.java src/application/services/PlanService.java src/application/services/EnrollmentService.java
git commit -m "Criar FitManager com operações de aluno e esqueletos dos Services"
```

### Commit 7: StudentMenu com lógica completa

Substituir o arquivo `src/ui/menus/student/StudentMenu.java`:

```java
package ui.menus.student;

import application.FitManager;
import application.OperationResult;
import domain.model.Student;
import ui.screen.InputParser;
import ui.screen.UserInterface;


import java.util.ArrayList;

/**
 * Menu de gerenciamento de alunos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class StudentMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public StudentMenu(UserInterface ui, FitManager fitManager) {
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
            String menuOptions = "";
            for (StudentMenuOption opt : StudentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR ALUNOS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + StudentMenuOption.values().length + ".");
                continue;
            }

            StudentMenuOption option = StudentMenuOption.fromNumber(Integer.parseInt(input.trim()));

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
    private void listAllStudents() {
        OperationResult result = fitManager.listAllStudents();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Student> students = (ArrayList<Student>) result.getData();
        String message = "> ALUNOS CADASTRADOS \n";
        message += "Total: " + students.size() + " aluno(s)\n\n";

        for (int i = 0; i < students.size(); i++) {
            message += "--- Aluno " + (i + 1) + " ---\n";
            message += students.get(i).toString();
            if (i < students.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/student/StudentMenu.java
git commit -m "Implementar StudentMenu com fluxos de cadastro, consulta, edição, remoção e listagem"
```

### Commit 8: Atualizar MainMenu

Substituir o arquivo `src/ui/menus/main/MainMenu.java`:

```java
package ui.menus.main;

import application.FitManager;
import ui.menus.plan.PlanMenu;
import ui.menus.reports.ReportsMenu;
import ui.menus.student.StudentMenu;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import ui.menus.enrollment.EnrollmentMenu;

/**
 * Menu principal do sistema FitManager.
 *
 * Responsável por exibir o menu principal e direcionar o usuário
 * para os submenus específicos de cada funcionalidade.
 *
 * Utiliza Lazy Instantiation: os submenus são criados sob demanda
 * na primeira vez que o usuário acessa a opção correspondente,
 * e reutilizados nas chamadas seguintes. Isso evita criar objetos
 * desnecessários e mantém referência única ao UserInterface e FitManager.
 */
public class MainMenu {

    private UserInterface ui;
    private FitManager fitManager;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui, fitManager);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui);
        }
        return reportsMenu;
    }

    /**
     * Inicia o loop principal do sistema.
     * O sistema permanece em execução até que a opção "Sair" seja escolhida.
     */
    public void start() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (MainMenuOption opt : MainMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getOptionName() + "\n";
            }
            String input = ui.showMenu("", menuOptions);

            if (input == null) {
                running = false;
                continue;
            }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            MainMenuOption option = MainMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                case RELATORIOS:           getReportsMenu().run();    break;
                case SAIR:                 running = false;           break;
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo.");
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/main/MainMenu.java
git commit -m "Atualizar MainMenu para receber FitManager e repassar ao StudentMenu"
```

### Commit 9: Atualizar FitManagerApp

Substituir o arquivo `src/FitManagerApp.java`:

```java
import application.FitManager;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;

/**
 * Ponto de entrada do sistema FitManager.
 *
 * Responsável por instanciar os componentes principais e iniciar
 * o loop do menu principal.
 */
public class FitManagerApp {

    public static void main(String[] args) {
        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        FitManager fitManager = new FitManager();

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}
```

Fazer commit:

```bash
git add src/FitManagerApp.java
git commit -m "Atualizar FitManagerApp para instanciar FitManager"
```

### Push e criar PR

```bash
git push origin feature/student-management

# No GitHub, criar Pull Request de feature/student-management para stage-1
# Título: "Feature: Gerenciamento de Alunos"
# Descrição: Implementa CRUD completo de alunos com validação de CPF, InputParser e OperationResult
```

---

## Branch 3: feature/plan-management

Aguarde a PR da branch anterior (`feature/student-management`) ser **mesclada** no `stage-1`. Após a mesclagem:

```bash
git checkout stage-1
git pull origin stage-1
git checkout -b feature/plan-management
```

### Commit 1: PlanType enum

Criar o arquivo `src/domain/enums/PlanType.java`:

```java
package domain.enums;

public enum PlanType {
    MONTHLY("Mensal"),
    QUARTERLY("Trimestral"),
    SEMI_ANNUAL("Semestral"),
    ANNUAL("Anual");

    private final String label;

    PlanType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

Fazer commit:

```bash
git add src/domain/enums/PlanType.java
git commit -m "Criar enum PlanType com tipos de plano da academia"
```

### Commit 2: Plan (domínio)

Criar o arquivo `src/domain/model/Plan.java`:

```java
package domain.model;

import domain.enums.PlanType;

public class Plan {

    private static final double DISCOUNT_RATE = 0.10; // 10% de desconto nos meses excedentes

    private String name;
    private String description;
    private PlanType type;
    private int minimumDuration; // em meses
    private double pricePerMonth;

    public Plan(
        String name,
        String description,
        PlanType type,
        int minimumDuration,
        double pricePerMonth
    ) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.minimumDuration = minimumDuration;
        this.pricePerMonth = pricePerMonth;
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Calcula o preço total para uma determinada quantidade de meses.
     * Aplica desconto de 10% nos meses que excedem a duração mínima do plano.
     *
     * Exemplo: Plano mensal (min. 1 mês, R$100/mês) contratado por 12 meses:
     * 1 × R$100 + 11 × R$90 = R$1.090,00
     *
     * @param months quantidade de meses contratados
     * @return valor total calculado
     */
    public double calculateTotalPrice(int months) {
        if (months <= 0) {
            return 0;
        }

        if (months <= minimumDuration) {
            return months * pricePerMonth;
        }

        // Meses dentro da duração mínima: preço cheio
        double basePart = minimumDuration * pricePerMonth;
        // Meses excedentes: preço com desconto
        int extraMonths = months - minimumDuration;
        double discountedPrice = pricePerMonth * (1 - DISCOUNT_RATE);
        double extraPart = extraMonths * discountedPrice;

        return basePart + extraPart;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlanType getType() {
        return type;
    }

    public void setType(PlanType type) {
        this.type = type;
    }

    public int getMinimumDuration() {
        return minimumDuration;
    }

    public void setMinimumDuration(int minimumDuration) {
        this.minimumDuration = minimumDuration;
    }

    public double getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(double pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    @Override
    public String toString() {
        return "Nome: " + name + "\n" +
                "Descrição: " + description + "\n" +
                "Tipo: " + type.getLabel() + "\n" +
                "Duração mínima: " + minimumDuration + (minimumDuration == 1 ? " mês" : " meses") + "\n" +
                "Preço/mês: R$ " + String.format("%.2f", pricePerMonth);
    }
}
```

Fazer commit:

```bash
git add src/domain/model/Plan.java
git commit -m "Criar classe Plan com calculateTotalPrice e desconto por duração"
```

### Commit 3: PlanService

Substituir o arquivo `src/application/services/PlanService.java`:

```java
package application.services;

import application.OperationResult;
import domain.enums.PlanType;
import domain.model.Plan;

import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de planos em memória
 * e implementar as operações específicas da entidade Plan.
 *
 * Conhece apenas objetos do seu próprio domínio.
 */
public class PlanService {

    private ArrayList<Plan> plans;

    public PlanService() {
        this.plans = new ArrayList<>();
    }

    /**
     * Registra um novo plano no sistema.
     * Valida campos obrigatórios, valores e unicidade de nome.
     *
     * @return OperationResult com o Plan criado em data (se sucesso)
     */
    public OperationResult registerPlan(
        String name,
        String description,
        PlanType type,
        int minimumDuration,
        double pricePerMonth
    ) {
        // Validação de campos obrigatórios
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome do plano é obrigatório.");
        }
        if (description == null || description.trim().isEmpty()) {
            return new OperationResult(false, "A descrição do plano é obrigatória.");
        }
        if (type == null) {
            return new OperationResult(false, "O tipo do plano é obrigatório.");
        }

        // Validação de valores
        if (minimumDuration <= 0) {
            return new OperationResult(false, "A duração mínima deve ser maior que zero.");
        }
        if (pricePerMonth <= 0) {
            return new OperationResult(false, "O preço por mês deve ser um valor positivo.");
        }

        // Verifica unicidade do nome
        if (nameExists(name.trim())) {
            return new OperationResult(false, "Já existe um plano cadastrado com este nome.");
        }

        // Cria e registra o plano
        Plan plan = new Plan(name.trim(), description.trim(), type, minimumDuration, pricePerMonth);
        plans.add(plan);

        return new OperationResult(true,
                "✅ Plano \"" + plan.getName() + "\" registrado com sucesso!", plan);
    }

    /**
     * Busca um plano pelo nome (busca case-insensitive).
     *
     * @return OperationResult com o Plan encontrado em data (se sucesso)
     */
    public OperationResult findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome é obrigatório para consulta.");
        }

        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                return new OperationResult(true, "Plano encontrado.", plan);
            }
        }

        return new OperationResult(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Atualiza o preço mensal de um plano.
     * Não afeta matrículas já registradas (totalPrice é fixado na Enrollment).
     *
     * @return OperationResult com o Plan atualizado em data (se sucesso)
     */
    public OperationResult updatePrice(String name, double newPrice) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome do plano é obrigatório.");
        }
        if (newPrice <= 0) {
            return new OperationResult(false, "O novo preço deve ser um valor positivo.");
        }

        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                double oldPrice = plan.getPricePerMonth();
                plan.setPricePerMonth(newPrice);
                return new OperationResult(true,
                        "✅ Preço do plano \"" + plan.getName() + "\" atualizado de R$ " +
                        String.format("%.2f", oldPrice) + " para R$ " +
                        String.format("%.2f", newPrice) + ".", plan);
            }
        }

        return new OperationResult(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Lista todos os planos cadastrados.
     *
     * @return OperationResult com ArrayList<Plan> em data
     */
    public OperationResult listAll() {
        if (plans.isEmpty()) {
            return new OperationResult(false, "Nenhum plano cadastrado no sistema.");
        }

        return new OperationResult(true,
                plans.size() + " plano(s) encontrado(s).", new ArrayList<>(plans));
    }

    /**
     * Verifica se um nome de plano já existe (case-insensitive).
     */
    public boolean nameExists(String name) {
        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
```

Fazer commit:

```bash
git add src/application/services/PlanService.java
git commit -m "Implementar PlanService com registro, consulta, atualização de preço e listagem"
```

### Commit 4: Adicionar métodos de plano ao FitManager

Substituir o arquivo `src/application/FitManager.java` (COMPLETO):

```java
package application;

import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.enums.PlanType;
import domain.model.Plan;
import domain.model.Student;

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
        String cleanCpf = Student.cleanCpf(cpf);

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
}
```

Fazer commit:

```bash
git add src/application/FitManager.java
git commit -m "Adicionar operações de plano ao FitManager"
```

### Commit 5: Implementar PlanMenu

Substituir o arquivo `src/ui/menus/plan/PlanMenu.java`:

```java
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
                case CADASTRAR:    registerPlan();      break;
                case CONSULTAR:    findPlanByName();    break;
                case ATUALIZAR:    updatePrice();       break;
                case LISTAR:       listAllPlans();      break;
                case VOLTAR:       running = false;     break;
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

        String minDurationStr = ui.getInput("Digite a duração mínima (em meses):");
        if (minDurationStr == null) return;

        int minimumDuration = InputParser.parseIntSafe(minDurationStr);
        if (minimumDuration == Integer.MIN_VALUE || minimumDuration <= 0) {
            ui.showError("A duração mínima deve ser um número positivo.");
            return;
        }

        String priceStr = ui.getInput("Digite o preço por mês (ex: 99.90):");
        if (priceStr == null) return;

        double pricePerMonth = InputParser.parseDoubleSafe(priceStr);
        if (Double.isNaN(pricePerMonth) || pricePerMonth <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult result = fitManager.registerPlan(name, description, type, minimumDuration, pricePerMonth);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta de plano pelo nome.
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
     * Fluxo de atualização do preço de um plano.
     */
    private void updatePrice() {
        String name = ui.getInput("Digite o nome do plano a atualizar:");
        if (name == null) return;

        OperationResult findResult = fitManager.findPlanByName(name);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Plan plan = (Plan) findResult.getData();
        ui.showMessage("Plano encontrado:\n\n" + plan.toString());

        String newPriceStr = ui.getInput("Digite o novo preço por mês (ex: 99.90):");
        if (newPriceStr == null) return;

        double newPrice = InputParser.parseDoubleSafe(newPriceStr);
        if (Double.isNaN(newPrice) || newPrice <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult result = fitManager.updatePlanPrice(name, newPrice);

        if (result.isSuccess()) {
            Plan updated = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados atualizados:\n" + updated.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os planos cadastrados.
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
            if (i < plans.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Auxilia na seleção de um tipo de plano.
     * Exibe todas as opções e retorna a escolha do usuário.
     */
    private PlanType selectPlanType() {
        String options = "Escolha o tipo de plano:\n";
        int count = 1;
        for (PlanType type : PlanType.values()) {
            options += count + " - " + type.getLabel() + "\n";
            count++;
        }

        while (true) {
            String input = ui.getInput(options + "\nOpção:");
            if (input == null) return null;

            if (!InputParser.isNumeric(input)) {
                ui.showError("Digite um número válido.");
                continue;
            }

            int choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= PlanType.values().length) {
                return PlanType.values()[choice - 1];
            } else {
                ui.showError("Opção inválida. Escolha de 1 a " + PlanType.values().length + ".");
            }
        }
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/plan/PlanMenu.java
git commit -m "Implementar PlanMenu com fluxos de cadastro, consulta e atualização de preço"
```

### Commit 6: Atualizar MainMenu

Substituir o arquivo `src/ui/menus/main/MainMenu.java` (COMPLETO):

```java
package ui.menus.main;

import application.FitManager;
import ui.menus.plan.PlanMenu;
import ui.menus.reports.ReportsMenu;
import ui.menus.student.StudentMenu;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import ui.menus.enrollment.EnrollmentMenu;

/**
 * Menu principal do sistema FitManager.
 *
 * Responsável por exibir o menu principal e direcionar o usuário
 * para os submenus específicos de cada funcionalidade.
 *
 * Utiliza Lazy Instantiation: os submenus são criados sob demanda
 * na primeira vez que o usuário acessa a opção correspondente,
 * e reutilizados nas chamadas seguintes. Isso evita criar objetos
 * desnecessários e mantém referência única ao UserInterface e FitManager.
 */
public class MainMenu {

    private UserInterface ui;
    private FitManager fitManager;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui, fitManager);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui, fitManager);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui);
        }
        return reportsMenu;
    }

    /**
     * Inicia o loop principal do sistema.
     * O sistema permanece em execução até que a opção "Sair" seja escolhida.
     */
    public void start() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (MainMenuOption opt : MainMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getOptionName() + "\n";
            }
            String input = ui.showMenu("", menuOptions);

            if (input == null) {
                running = false;
                continue;
            }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            MainMenuOption option = MainMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                case RELATORIOS:           getReportsMenu().run();    break;
                case SAIR:                 running = false;           break;
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo.");
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/main/MainMenu.java
git commit -m "Atualizar MainMenu para repassar FitManager ao PlanMenu"
```

### Push e criar PR

```bash
git push origin feature/plan-management

# No GitHub, criar Pull Request de feature/plan-management para stage-1
# Título: "Feature: Gerenciamento de Planos"
# Descrição: Implementa CRUD de planos com cálculo de preço total com desconto
```

---

## Branch 4: feature/enrollment-management

Aguarde a PR da branch anterior (`feature/plan-management`) ser **mesclada** no `stage-1`. Após a mesclagem:

```bash
git checkout stage-1
git pull origin stage-1
git checkout -b feature/enrollment-management
```

**NOTA IMPORTANTE**: Esta branch inclui `PaymentType` e `Payment` (inicialmente previstos para a Branch 5) porque o fluxo de matrícula necessita deles para criar o pagamento inicial. Isso garante compilação e funcionamento correto em cada etapa.

### Commit 1: EnrollmentStatus enum

Criar o arquivo `src/domain/enums/EnrollmentStatus.java`:

```java
package domain.enums;

public enum EnrollmentStatus {
    ACTIVE("Ativa"),
    CANCELLED("Cancelada");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

Fazer commit:

```bash
git add src/domain/enums/EnrollmentStatus.java
git commit -m "Criar enum EnrollmentStatus com estados ACTIVE e CANCELLED"
```

### Commit 2: PaymentType enum

Criar o arquivo `src/domain/enums/PaymentType.java`:

```java
package domain.enums;

public enum PaymentType {
    PIX("PIX"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    CASH("Dinheiro");

    private final String label;

    PaymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

Fazer commit:

```bash
git add src/domain/enums/PaymentType.java
git commit -m "Criar enum PaymentType com tipos de pagamento"
```

### Commit 3: Payment (domínio)

Criar o arquivo `src/domain/model/Payment.java`:

```java
package domain.model;

import domain.enums.PaymentType;

import java.time.LocalDate;

public class Payment {

    private static int nextCode = 1;

    private int code;
    private double amount;
    private LocalDate paymentDate;
    private PaymentType paymentType;
    private String description;

    public Payment(
        double amount,
        LocalDate paymentDate,
        PaymentType paymentType,
        String description
    ) {
        this.code = nextCode++;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentType = paymentType;
        this.description = description;
    }

    // ========================
    // Getters e Setters
    // ========================

    public int getCode() {
        return code;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Código: " + code + "\n" +
               "Valor: R$ " + String.format("%.2f", amount) + "\n" +
               "Data: " + String.format("%02d/%02d/%04d", paymentDate.getDayOfMonth(),
                    paymentDate.getMonthValue(), paymentDate.getYear()) + "\n" +
               "Tipo: " + paymentType.getLabel() + "\n" +
               "Descrição: " + description;
    }
}
```

Fazer commit:

```bash
git add src/domain/model/Payment.java
git commit -m "Criar classe Payment com código auto-incrementado"
```

### Commit 4: Enrollment (domínio)

Criar o arquivo `src/domain/model/Enrollment.java`:

```java
package domain.model;

import domain.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class Enrollment {

    private static int nextCode = 1;

    private int code;
    private String studentCpf;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMonths;
    private double totalPrice;
    private EnrollmentStatus status;
    private ArrayList<Payment> payments;
    private LocalDate cancelledAt;

    public Enrollment(
        String studentCpf,
        String planName,
        LocalDate startDate,
        int durationMonths,
        double totalPrice
    ) {
        this.code = nextCode++;
        this.studentCpf = studentCpf;
        this.planName = planName;
        this.startDate = startDate;
        this.durationMonths = durationMonths;
        this.endDate = startDate.plusMonths(durationMonths).minusDays(1);
        this.totalPrice = totalPrice;
        this.status = EnrollmentStatus.ACTIVE;
        this.payments = new ArrayList<>();
        this.cancelledAt = null;
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Adiciona um pagamento à matrícula.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    /**
     * Cancela a matrícula (soft delete).
     */
    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = LocalDate.now();
    }

    /**
     * Calcula o saldo pendente da matrícula.
     * Saldo = totalPrice - soma de todos os pagamentos.
     */
    public double calculateBalance() {
        double totalPaid = 0;
        for (Payment payment : payments) {
            totalPaid += payment.getAmount();
        }
        return totalPrice - totalPaid;
    }

    /**
     * Verifica se a matrícula está vencida (hoje é depois da endDate).
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Calcula quantos meses faltam para o fim da matrícula.
     * Retorna 0 se já está vencida ou cancelada.
     */
    public int getMonthsRemaining() {
        if (status == EnrollmentStatus.CANCELLED || isExpired()) {
            return 0;
        }
        YearMonth today = YearMonth.now();
        YearMonth end = YearMonth.from(endDate);
        int months = 0;
        YearMonth current = today;
        while (current.isBefore(end)) {
            months++;
            current = current.plusMonths(1);
        }
        return months;
    }

    // ========================
    // Getters e Setters
    // ========================

    public int getCode() {
        return code;
    }

    public String getStudentCpf() {
        return studentCpf;
    }

    public String getPlanName() {
        return planName;
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
        return new ArrayList<>(payments);
    }

    public LocalDate getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public String toString() {
        String formatDate = "%02d/%02d/%04d";
        return "Código: " + code + "\n" +
               "CPF: " + studentCpf + "\n" +
               "Plano: " + planName + "\n" +
               "Data Início: " + String.format(formatDate,
                    startDate.getDayOfMonth(), startDate.getMonthValue(), startDate.getYear()) + "\n" +
               "Data Fim: " + String.format(formatDate,
                    endDate.getDayOfMonth(), endDate.getMonthValue(), endDate.getYear()) + "\n" +
               "Duração: " + durationMonths + (durationMonths == 1 ? " mês" : " meses") + "\n" +
               "Preço Total: R$ " + String.format("%.2f", totalPrice) + "\n" +
               "Saldo Pendente: R$ " + String.format("%.2f", calculateBalance()) + "\n" +
               "Status: " + status.getLabel() + "\n" +
               "Pagamentos: " + payments.size();
    }
}
```

Fazer commit:

```bash
git add src/domain/model/Enrollment.java
git commit -m "Criar classe Enrollment com totalPrice fixado, cancel e calculateBalance"
```

### Commit 5: EnrollmentService (completo, sem registerPayment como método separado)

Substituir o arquivo `src/application/services/EnrollmentService.java`:

```java
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

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
 */
public class EnrollmentService {

    private ArrayList<Enrollment> enrollments;

    public EnrollmentService() {
        this.enrollments = new ArrayList<>();
    }

    /**
     * Realiza a matrícula de um aluno em um plano.
     * Cria um Enrollment e registra um pagamento inicial.
     *
     * @return OperationResult com o Enrollment criado em data (se sucesso)
     */
    public OperationResult enroll(
        Student student,
        Plan plan,
        LocalDate startDate,
        int durationMonths,
        double initialAmount,
        PaymentType paymentType,
        String paymentDescription
    ) {
        OperationResult validationResult = validateEnrollmentParams(
                student, plan, startDate, durationMonths);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        double totalPrice = plan.calculateTotalPrice(durationMonths);

        Enrollment enrollment = new Enrollment(
                student.getCpf(),
                plan.getName(),
                startDate,
                durationMonths,
                totalPrice
        );

        // Cria pagamento inicial
        Payment initialPayment = buildPayment(
                initialAmount,
                LocalDate.now(),
                paymentType,
                paymentDescription
        );
        enrollment.addPayment(initialPayment);

        enrollments.add(enrollment);

        String message = "✅ Matrícula realizada com sucesso!\n\n" +
                "Código: " + enrollment.getCode() + "\n" +
                "Aluno: " + student.getName() + "\n" +
                "Plano: " + plan.getName() + "\n" +
                "Preço Total: R$ " + String.format("%.2f", totalPrice) + "\n" +
                "Pagamento Inicial: R$ " + String.format("%.2f", initialAmount) + "\n" +
                "Saldo Pendente: R$ " + String.format("%.2f", totalPrice - initialAmount);

        return new OperationResult(true, message, enrollment);
    }

    /**
     * Valida os parâmetros necessários para uma matrícula.
     */
    private OperationResult validateEnrollmentParams(
        Student student,
        Plan plan,
        LocalDate startDate,
        int durationMonths
    ) {
        if (student == null) {
            return new OperationResult(false, "Aluno não pode ser nulo.");
        }
        if (plan == null) {
            return new OperationResult(false, "Plano não pode ser nulo.");
        }
        if (startDate == null) {
            return new OperationResult(false, "Data de início não pode ser nula.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            return new OperationResult(false, "A data de início não pode ser anterior a hoje.");
        }
        if (durationMonths < plan.getMinimumDuration()) {
            return new OperationResult(false,
                    "A duração deve ser no mínimo " + plan.getMinimumDuration() +
                    (plan.getMinimumDuration() == 1 ? " mês" : " meses") + ".");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Cria um objeto Payment com os parâmetros fornecidos.
     * Usado durante a matrícula para criar o pagamento inicial.
     */
    private Payment buildPayment(
        double amount,
        LocalDate paymentDate,
        PaymentType paymentType,
        String description
    ) {
        return new Payment(amount, paymentDate, paymentType, description);
    }

    /**
     * Cancela uma matrícula ativa.
     *
     * @return OperationResult indicando sucesso ou falha
     */
    public OperationResult cancelEnrollment(int enrollmentCode) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCode() == enrollmentCode) {
                if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
                    return new OperationResult(false, "Esta matrícula já foi cancelada.");
                }
                enrollment.cancel();
                return new OperationResult(true,
                        "✅ Matrícula " + enrollmentCode + " cancelada com sucesso.");
            }
        }
        return new OperationResult(false, "Matrícula não encontrada.");
    }

    /**
     * Busca a matrícula ativa de um aluno pelo CPF.
     *
     * @return OperationResult com o Enrollment encontrado em data (se sucesso)
     */
    public OperationResult findActiveByStudentCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentCpf().equals(cpf) &&
                enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return new OperationResult(true, "Matrícula encontrada.", enrollment);
            }
        }

        return new OperationResult(false, "Nenhuma matrícula ativa encontrada para este aluno.");
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
            if (enrollment.getStudentCpf().equals(cpf) &&
                enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lista o histórico de matrículas de um aluno (ativas e canceladas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listHistoryByStudent(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        ArrayList<Enrollment> studentEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentCpf().equals(cpf)) {
                studentEnrollments.add(enrollment);
            }
        }

        if (studentEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula encontrada para este aluno.");
        }

        return new OperationResult(true,
                studentEnrollments.size() + " matrícula(s) encontrada(s).",
                studentEnrollments);
    }

    /**
     * Busca uma matrícula pelo código.
     * Utilizado internamente para validações e operações.
     */
    public Enrollment findByCode(int code) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCode() == code) {
                return enrollment;
            }
        }
        return null;
    }
}
```

Fazer commit:

```bash
git add src/application/services/EnrollmentService.java
git commit -m "Implementar EnrollmentService com matrícula, cancelamento e consultas"
```

### Commit 6: Adicionar métodos de matrícula ao FitManager

Substituir o arquivo `src/application/FitManager.java` (COMPLETO):

```java
package application;

import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.enums.PlanType;
import domain.enums.PaymentType;
import domain.model.Plan;
import domain.model.Student;
import domain.model.Enrollment;

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
        String cleanCpf = Student.cleanCpf(cpf);

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
     */
    public OperationResult enrollStudent(String cpf, String planName, String startDateStr,
                                         int durationMonths, double initialAmount,
                                         PaymentType paymentType, String paymentDescription) {

        String cleanCpf = Student.cleanCpf(cpf);

        OperationResult studentResult = studentService.findByCpf(cleanCpf);
        if (!studentResult.isSuccess()) {
            return studentResult;
        }

        OperationResult planResult = planService.findByName(planName);
        if (!planResult.isSuccess()) {
            return planResult;
        }

        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            return new OperationResult(false,
                    "O aluno já possui uma matrícula ativa. "
                            + "Cancele a matrícula atual antes de realizar uma nova.");
        }

        OperationResult paymentCheck = validateInitialPayment(initialAmount, paymentType);
        if (!paymentCheck.isSuccess()) {
            return paymentCheck;
        }

        LocalDate startDate = LocalDate.parse(
                startDateStr.trim(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Student student = (Student) studentResult.getData();
        Plan plan = (Plan) planResult.getData();
        return enrollmentService.enroll(student, plan, startDate, durationMonths,
                initialAmount, paymentType, paymentDescription);
    }

    /**
     * Valida o valor e o tipo do pagamento inicial de uma matrícula.
     */
    private OperationResult validateInitialPayment(double initialAmount, PaymentType paymentType) {
        if (initialAmount <= 0) {
            return new OperationResult(false, "O valor do pagamento inicial deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Cancela uma matrícula ativa.
     */
    public OperationResult cancelEnrollment(int enrollmentCode) {
        return enrollmentService.cancelEnrollment(enrollmentCode);
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF.
     */
    public OperationResult findActiveEnrollmentByStudent(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.findActiveByStudentCpf(cleanCpf);
    }

    /**
     * Lista o histórico de matrículas de um aluno.
     */
    public OperationResult listEnrollmentHistory(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.listHistoryByStudent(cleanCpf);
    }
}
```

Fazer commit:

```bash
git add src/application/FitManager.java
git commit -m "Adicionar operações de matrícula ao FitManager com coordenação de três serviços"
```

### Commit 7: Implementar EnrollmentMenu

Substituir o arquivo `src/ui/menus/enrollment/EnrollmentMenu.java`:

```java
package ui.menus.enrollment;

import application.FitManager;
import application.OperationResult;
import domain.enums.PaymentType;
import domain.model.Enrollment;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de gerenciamento de matrículas.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class EnrollmentMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public EnrollmentMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de matrículas.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (EnrollmentMenuOption opt : EnrollmentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR MATRÍCULAS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            EnrollmentMenuOption option = EnrollmentMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case MATRICULAR:        enrollStudent();          break;
                case CONSULTAR_ATIVA:   findActiveEnrollment();   break;
                case HISTORICO:         listHistory();            break;
                case CANCELAR:          cancelEnrollment();       break;
                case REGISTRAR_PAGAMENTO: registerPayment();     break;
                case VOLTAR:            running = false;          break;
            }
        }
    }

    /**
     * Fluxo de matrícula de um aluno em um plano.
     */
    private void enrollStudent() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        String planName = ui.getInput("Digite o nome do plano:");
        if (planName == null) return;

        String startDateStr = ui.getInput("Digite a data de início da matrícula (dd/mm/aaaa):");
        if (startDateStr == null) return;

        String durationStr = ui.getInput("Digite a duração (em meses):");
        if (durationStr == null) return;

        int durationMonths = InputParser.parseIntSafe(durationStr);
        if (durationMonths == Integer.MIN_VALUE || durationMonths <= 0) {
            ui.showError("A duração deve ser um número positivo.");
            return;
        }

        String initialAmountStr = ui.getInput("Digite o valor do pagamento inicial (ex: 99.90):");
        if (initialAmountStr == null) return;

        double initialAmount = InputParser.parseDoubleSafe(initialAmountStr);
        if (Double.isNaN(initialAmount) || initialAmount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String paymentDescription = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (paymentDescription == null) paymentDescription = "Pagamento inicial de matrícula";

        OperationResult result = fitManager.enrollStudent(cpf, planName, startDateStr,
                durationMonths, initialAmount, paymentType, paymentDescription);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage(result.getMessage() + "\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta da matrícula ativa de um aluno.
     */
    private void findActiveEnrollment() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage("Matrícula ativa encontrada:\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem do histórico de matrículas de um aluno.
     */
    private void listHistory() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult result = fitManager.listEnrollmentHistory(cpf);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> HISTÓRICO DE MATRÍCULAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += enrollments.get(i).toString();
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Fluxo de cancelamento de uma matrícula.
     */
    private void cancelEnrollment() {
        String codeStr = ui.getInput("Digite o código da matrícula a cancelar:");
        if (codeStr == null) return;

        int code = InputParser.parseIntSafe(codeStr);
        if (code == Integer.MIN_VALUE) {
            ui.showError("Código inválido.");
            return;
        }

        String confirm = ui.getInput("Tem certeza que deseja cancelar a matrícula " + code +
                "?\nDigite 'S' para confirmar ou qualquer outra tecla para cancelar:");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("S")) {
            ui.showMessage("Operação cancelada.");
            return;
        }

        OperationResult result = fitManager.cancelEnrollment(code);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de registro de pagamento (será implementado na Branch 5).
     * Por enquanto, apenas exibe uma mensagem informativa.
     */
    private void registerPayment() {
        ui.showMessage("Funcionalidade será implementada na próxima versão do FitManager.");
    }

    /**
     * Auxilia na seleção de um tipo de pagamento.
     * Exibe todas as opções e retorna a escolha do usuário.
     */
    private PaymentType selectPaymentType() {
        String options = "Escolha o tipo de pagamento:\n";
        int count = 1;
        for (PaymentType type : PaymentType.values()) {
            options += count + " - " + type.getLabel() + "\n";
            count++;
        }

        while (true) {
            String input = ui.getInput(options + "\nOpção:");
            if (input == null) return null;

            if (!InputParser.isNumeric(input)) {
                ui.showError("Digite um número válido.");
                continue;
            }

            int choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= PaymentType.values().length) {
                return PaymentType.values()[choice - 1];
            } else {
                ui.showError("Opção inválida. Escolha de 1 a " + PaymentType.values().length + ".");
            }
        }
    }

    /**
     * Constrói um sumário formatado de uma matrícula para exibição.
     */
    private String buildEnrollmentSummary(Enrollment enrollment) {
        String formatDate = "%02d/%02d/%04d";
        return "Código: " + enrollment.getCode() + "\n" +
               "Plano: " + enrollment.getPlanName() + "\n" +
               "Data Início: " + String.format(formatDate,
                    enrollment.getStartDate().getDayOfMonth(),
                    enrollment.getStartDate().getMonthValue(),
                    enrollment.getStartDate().getYear()) + "\n" +
               "Data Fim: " + String.format(formatDate,
                    enrollment.getEndDate().getDayOfMonth(),
                    enrollment.getEndDate().getMonthValue(),
                    enrollment.getEndDate().getYear()) + "\n" +
               "Duração: " + enrollment.getDurationMonths() +
               (enrollment.getDurationMonths() == 1 ? " mês" : " meses") + "\n" +
               "Preço Total: R$ " + String.format("%.2f", enrollment.getTotalPrice()) + "\n" +
               "Saldo Pendente: R$ " + String.format("%.2f", enrollment.calculateBalance()) + "\n" +
               "Status: " + enrollment.getStatus().getLabel() + "\n" +
               "Pagamentos Registrados: " + enrollment.getPayments().size();
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/enrollment/EnrollmentMenu.java
git commit -m "Implementar EnrollmentMenu com matrícula, cancelamento, consulta e histórico"
```

### Commit 8: Atualizar MainMenu

Substituir o arquivo `src/ui/menus/main/MainMenu.java` (COMPLETO):

```java
package ui.menus.main;

import application.FitManager;
import ui.menus.plan.PlanMenu;
import ui.menus.reports.ReportsMenu;
import ui.menus.student.StudentMenu;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import ui.menus.enrollment.EnrollmentMenu;

/**
 * Menu principal do sistema FitManager.
 *
 * Responsável por exibir o menu principal e direcionar o usuário
 * para os submenus específicos de cada funcionalidade.
 *
 * Utiliza Lazy Instantiation: os submenus são criados sob demanda
 * na primeira vez que o usuário acessa a opção correspondente,
 * e reutilizados nas chamadas seguintes. Isso evita criar objetos
 * desnecessários e mantém referência única ao UserInterface e FitManager.
 */
public class MainMenu {

    private UserInterface ui;
    private FitManager fitManager;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui, fitManager);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui, fitManager);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui, fitManager);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui);
        }
        return reportsMenu;
    }

    /**
     * Inicia o loop principal do sistema.
     * O sistema permanece em execução até que a opção "Sair" seja escolhida.
     */
    public void start() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (MainMenuOption opt : MainMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getOptionName() + "\n";
            }
            String input = ui.showMenu("", menuOptions);

            if (input == null) {
                running = false;
                continue;
            }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            MainMenuOption option = MainMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                case RELATORIOS:           getReportsMenu().run();    break;
                case SAIR:                 running = false;           break;
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo.");
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/main/MainMenu.java
git commit -m "Atualizar MainMenu para repassar FitManager ao EnrollmentMenu"
```

### Push e criar PR

```bash
git push origin feature/enrollment-management

# No GitHub, criar Pull Request de feature/enrollment-management para stage-1
# Título: "Feature: Gerenciamento de Matrículas"
# Descrição: Implementa matrícula completa de alunos em planos com pagamento inicial
```

---

## Branch 5: feature/payment-management

Aguarde a PR da branch anterior (`feature/enrollment-management`) ser **mesclada** no `stage-1`. Após a mesclagem:

```bash
git checkout stage-1
git pull origin stage-1
git checkout -b feature/payment-management
```

### Commit 1: Adicionar método registerPayment ao EnrollmentService

Modificar o arquivo `src/application/services/EnrollmentService.java` - adicionar o seguinte método após o método `listHistoryByStudent`:

```java
    /**
     * Registra um novo pagamento para uma matrícula.
     * O pagamento é validado e então adicionado à matrícula.
     *
     * @return OperationResult indicando sucesso ou falha
     */
    public OperationResult registerPayment(
        int enrollmentCode,
        double amount,
        PaymentType paymentType,
        String description
    ) {
        OperationResult validationResult = validatePaymentParams(amount, paymentType);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult(false, "Não é possível registrar pagamento em matrícula cancelada.");
        }

        double remainingBalance = enrollment.calculateBalance();
        if (amount > remainingBalance) {
            return new OperationResult(false,
                    "O valor do pagamento (R$ " + String.format("%.2f", amount) +
                    ") excede o saldo pendente (R$ " + String.format("%.2f", remainingBalance) + ").");
        }

        Payment payment = buildPayment(amount, LocalDate.now(), paymentType, description);
        enrollment.addPayment(payment);

        String message = "✅ Pagamento registrado com sucesso!\n\n" +
                "Código do Pagamento: " + payment.getCode() + "\n" +
                "Valor: R$ " + String.format("%.2f", amount) + "\n" +
                "Tipo: " + paymentType.getLabel() + "\n" +
                "Novo Saldo Pendente: R$ " + String.format("%.2f", enrollment.calculateBalance());

        return new OperationResult(true, message, payment);
    }

    /**
     * Valida os parâmetros de um pagamento.
     */
    private OperationResult validatePaymentParams(double amount, PaymentType paymentType) {
        if (amount <= 0) {
            return new OperationResult(false, "O valor do pagamento deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }
        return new OperationResult(true, "ok");
    }
```

Fazer commit:

```bash
git add src/application/services/EnrollmentService.java
git commit -m "Adicionar registerPayment ao EnrollmentService com validação"
```

### Commit 2: Adicionar método registerPayment ao FitManager

Modificar o arquivo `src/application/FitManager.java` - adicionar o seguinte método após o método `listEnrollmentHistory`:

```java
    /**
     * Registra um novo pagamento para uma matrícula.
     */
    public OperationResult registerPayment(
        int enrollmentCode,
        double amount,
        PaymentType paymentType,
        String description
    ) {
        return enrollmentService.registerPayment(enrollmentCode, amount, paymentType, description);
    }
```

Fazer commit:

```bash
git add src/application/FitManager.java
git commit -m "Adicionar método registerPayment ao FitManager"
```

### Commit 3: Implementar registerPayment no EnrollmentMenu

Modificar o arquivo `src/ui/menus/enrollment/EnrollmentMenu.java` - substituir o método `registerPayment` pelo seguinte:

```java
    /**
     * Fluxo de registro de um novo pagamento para uma matrícula.
     */
    private void registerPayment() {
        String codeStr = ui.getInput("Digite o código da matrícula:");
        if (codeStr == null) return;

        int code = InputParser.parseIntSafe(codeStr);
        if (code == Integer.MIN_VALUE) {
            ui.showError("Código inválido.");
            return;
        }

        String amountStr = ui.getInput("Digite o valor do pagamento (ex: 99.90):");
        if (amountStr == null) return;

        double amount = InputParser.parseDoubleSafe(amountStr);
        if (Double.isNaN(amount) || amount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String description = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (description == null) description = "Pagamento adicional";

        OperationResult result = fitManager.registerPayment(code, amount, paymentType, description);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }
```

Fazer commit:

```bash
git add src/ui/menus/enrollment/EnrollmentMenu.java
git commit -m "Implementar registro de pagamento no EnrollmentMenu com validação"
```

### Push e criar PR

```bash
git push origin feature/payment-management

# No GitHub, criar Pull Request de feature/payment-management para stage-1
# Título: "Feature: Gerenciamento de Pagamentos"
# Descrição: Implementa registro de pagamentos adicionais para matrículas ativas
```

---

## Branch 6: feature/reports

Aguarde a PR da branch anterior (`feature/payment-management`) ser **mesclada** no `stage-1`. Após a mesclagem:

```bash
git checkout stage-1
git pull origin stage-1
git checkout -b feature/reports
```

### Commit 1: Adicionar métodos de relatório ao EnrollmentService

Modificar o arquivo `src/application/services/EnrollmentService.java` - adicionar os seguintes métodos após o método `validatePaymentParams`:

```java
    /**
     * Lista todas as matrículas (ativas e canceladas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listAll() {
        if (enrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula cadastrada no sistema.");
        }

        return new OperationResult(true,
                enrollments.size() + " matrícula(s) encontrada(s).",
                new ArrayList<>(enrollments));
    }

    /**
     * Lista apenas as matrículas ativas.
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listActive() {
        ArrayList<Enrollment> activeEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                activeEnrollments.add(enrollment);
            }
        }

        if (activeEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula ativa encontrada.");
        }

        return new OperationResult(true,
                activeEnrollments.size() + " matrícula(s) ativa(s) encontrada(s).",
                activeEnrollments);
    }

    /**
     * Lista apenas as matrículas que possuem saldo pendente (não estão totalmente pagas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listWithPendingBalance() {
        ArrayList<Enrollment> pendingEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.calculateBalance() > 0) {
                pendingEnrollments.add(enrollment);
            }
        }

        if (pendingEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula com saldo pendente.");
        }

        return new OperationResult(true,
                pendingEnrollments.size() + " matrícula(s) com saldo pendente encontrada(s).",
                pendingEnrollments);
    }
```

Fazer commit:

```bash
git add src/application/services/EnrollmentService.java
git commit -m "Adicionar métodos de listagem de matrículas ao EnrollmentService"
```

### Commit 2: Adicionar métodos de relatório ao FitManager

Modificar o arquivo `src/application/FitManager.java` - adicionar os seguintes métodos após o método `registerPayment`:

```java
    // ============================
    // Operações de Relatórios
    // ============================

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    public OperationResult listAllEnrollments() {
        return enrollmentService.listAll();
    }

    /**
     * Lista apenas as matrículas ativas.
     */
    public OperationResult listActiveEnrollments() {
        return enrollmentService.listActive();
    }

    /**
     * Lista as matrículas com saldo pendente.
     */
    public OperationResult listEnrollmentsWithPendingBalance() {
        return enrollmentService.listWithPendingBalance();
    }

    /**
     * Calcula e retorna estatísticas gerais do sistema.
     */
    public OperationResult getSystemStatistics() {
        OperationResult allStudents = listAllStudents();
        OperationResult allEnrollments = listAllEnrollments();
        OperationResult allPlans = listAllPlans();

        int totalStudents = 0;
        if (allStudents.isSuccess()) {
            ArrayList<Student> students = (ArrayList<Student>) allStudents.getData();
            totalStudents = students.size();
        }

        int totalEnrollments = 0;
        int totalActiveEnrollments = 0;
        double totalBalance = 0;
        if (allEnrollments.isSuccess()) {
            ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) allEnrollments.getData();
            totalEnrollments = enrollments.size();
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getStatus().toString().equals("Ativa")) {
                    totalActiveEnrollments++;
                }
                totalBalance += enrollment.calculateBalance();
            }
        }

        int totalPlans = 0;
        if (allPlans.isSuccess()) {
            ArrayList<Plan> plans = (ArrayList<Plan>) allPlans.getData();
            totalPlans = plans.size();
        }

        String stats = "ESTATÍSTICAS DO SISTEMA\n\n" +
                "Alunos Cadastrados: " + totalStudents + "\n" +
                "Planos Disponíveis: " + totalPlans + "\n" +
                "Total de Matrículas: " + totalEnrollments + "\n" +
                "Matrículas Ativas: " + totalActiveEnrollments + "\n" +
                "Saldo Pendente Total: R$ " + String.format("%.2f", totalBalance);

        return new OperationResult(true, stats);
    }
```

Fazer commit:

```bash
git add src/application/FitManager.java
git commit -m "Adicionar métodos de relatório ao FitManager com estatísticas do sistema"
```

### Commit 3: Criar DataMock para testes

Criar o arquivo `src/helpers/DataMock.java`:

```java
package helpers;

import application.FitManager;
import domain.enums.PlanType;
import domain.enums.PaymentType;

import java.time.LocalDate;

/**
 * Classe utilitária para popular o sistema com dados de demonstração.
 * Utilizada quando DEV_MODE está ativo em FitManagerApp.
 *
 * Facilita testes e apresentações sem necessidade de inserção manual de dados.
 */
public class DataMock {

    /**
     * Popula o FitManager com dados de demonstração.
     * Inclui alunos, planos e matrículas com seus respectivos pagamentos.
     */
    public static void populateDemo(FitManager fitManager) {
        // ===== Alunos =====
        fitManager.registerStudent(
                "João Silva",
                "12345678901",
                "joao@email.com",
                "15/05/1990"
        );

        fitManager.registerStudent(
                "Maria Santos",
                "98765432101",
                "maria@email.com",
                "22/08/1995"
        );

        fitManager.registerStudent(
                "Pedro Oliveira",
                "55544433322",
                "pedro@email.com",
                "10/03/1988"
        );

        // ===== Planos =====
        fitManager.registerPlan(
                "Plano Básico",
                "Acesso a equipamentos e aulas coletivas",
                PlanType.MONTHLY,
                1,
                99.90
        );

        fitManager.registerPlan(
                "Plano Gold",
                "Acesso completo com personal trainer",
                PlanType.MONTHLY,
                3,
                199.90
        );

        fitManager.registerPlan(
                "Plano Anual",
                "Melhor custo-benefício para o ano todo",
                PlanType.ANNUAL,
                12,
                1899.90
        );

        // ===== Matrículas =====
        fitManager.enrollStudent(
                "12345678901",
                "Plano Básico",
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                12,
                99.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );

        fitManager.enrollStudent(
                "98765432101",
                "Plano Gold",
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                6,
                199.90,
                PaymentType.CREDIT_CARD,
                "Pagamento inicial"
        );
    }
}
```

Fazer commit:

```bash
git add src/helpers/DataMock.java
git commit -m "Criar DataMock para popular sistema com dados de demonstração"
```

### Commit 4: Implementar ReportsMenu

Substituir o arquivo `src/ui/menus/reports/ReportsMenu.java`:

```java
package ui.menus.reports;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de relatórios do sistema.
 * Apresenta estatísticas e listas de matrículas com diferentes filtros.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para consultar dados).
 */
public class ReportsMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public ReportsMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de relatórios.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (ReportsMenuOption opt : ReportsMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> RELATÓRIOS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + ReportsMenuOption.values().length + ".");
                continue;
            }

            ReportsMenuOption option = ReportsMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + ReportsMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case ESTATISTICAS:      showStatistics();           break;
                case TODAS_MATRICULAS:  listAllEnrollments();       break;
                case MATRICULAS_ATIVAS: listActiveEnrollments();    break;
                case SALDO_PENDENTE:    listPendingBalance();       break;
                case VOLTAR:            running = false;            break;
            }
        }
    }

    /**
     * Exibe estatísticas gerais do sistema.
     */
    private void showStatistics() {
        OperationResult result = fitManager.getSystemStatistics();

        if (result.isSuccess()) {
            String stats = (String) result.getMessage();
            ui.showMessage(stats);
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    private void listAllEnrollments() {
        OperationResult result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> TODAS AS MATRÍCULAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Lista apenas as matrículas ativas.
     */
    private void listActiveEnrollments() {
        OperationResult result = fitManager.listActiveEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> MATRÍCULAS ATIVAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s) ativa(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Lista as matrículas com saldo pendente.
     */
    private void listPendingBalance() {
        OperationResult result = fitManager.listEnrollmentsWithPendingBalance();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> MATRÍCULAS COM SALDO PENDENTE\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Constrói uma representação detalhada de uma matrícula para exibição em relatórios.
     */
    private String buildEnrollmentDetails(Enrollment enrollment) {
        String formatDate = "%02d/%02d/%04d";
        return "Código: " + enrollment.getCode() + "\n" +
               "CPF do Aluno: " + enrollment.getStudentCpf() + "\n" +
               "Plano: " + enrollment.getPlanName() + "\n" +
               "Data Início: " + String.format(formatDate,
                    enrollment.getStartDate().getDayOfMonth(),
                    enrollment.getStartDate().getMonthValue(),
                    enrollment.getStartDate().getYear()) + "\n" +
               "Data Fim: " + String.format(formatDate,
                    enrollment.getEndDate().getDayOfMonth(),
                    enrollment.getEndDate().getMonthValue(),
                    enrollment.getEndDate().getYear()) + "\n" +
               "Duração: " + enrollment.getDurationMonths() +
               (enrollment.getDurationMonths() == 1 ? " mês" : " meses") + "\n" +
               "Preço Total: R$ " + String.format("%.2f", enrollment.getTotalPrice()) + "\n" +
               "Saldo Pendente: R$ " + String.format("%.2f", enrollment.calculateBalance()) + "\n" +
               "Status: " + enrollment.getStatus().getLabel() + "\n" +
               "Pagamentos: " + enrollment.getPayments().size();
    }
}
```

Fazer commit:

```bash
git add src/ui/menus/reports/ReportsMenu.java
git commit -m "Implementar ReportsMenu com estatísticas, listagens e filtros"
```

### Commit 5: Atualizar MainMenu para repassar FitManager

Modificar o arquivo `src/ui/menus/main/MainMenu.java` - atualizar o lazy getter de ReportsMenu:

```java
    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui, fitManager);
        }
        return reportsMenu;
    }
```

Fazer commit:

```bash
git add src/ui/menus/main/MainMenu.java
git commit -m "Atualizar MainMenu para repassar FitManager ao ReportsMenu"
```

### Commit 6: Atualizar FitManagerApp com DEV_MODE

Substituir o arquivo `src/FitManagerApp.java`:

```java
import application.FitManager;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;
import helpers.DataMock;

/**
 * Ponto de entrada do sistema FitManager.
 *
 * Responsável por instanciar os componentes principais e iniciar
 * o loop do menu principal.
 *
 * DEV_MODE: Quando ativado, popula o sistema com dados de demonstração.
 * Útil para testes e apresentações sem necessidade de inserção manual de dados.
 */
public class FitManagerApp {

    private static final boolean DEV_MODE = false; // Alterar para true para carregar dados de demonstração

    public static void main(String[] args) {
        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        FitManager fitManager = new FitManager();

        // Carrega dados de demonstração se DEV_MODE está ativo
        if (DEV_MODE) {
            DataMock.populateDemo(fitManager);
            ui.showMessage("Modo de Desenvolvimento ativado!\n\n" +
                    "O sistema foi carregado com dados de demonstração.\n" +
                    "Você pode explorar todas as funcionalidades sem inserir dados manualmente.");
        }

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}
```

Fazer commit:

```bash
git add src/FitManagerApp.java src/helpers/DataMock.java
git commit -m "Adicionar DEV_MODE à FitManagerApp com DataMock para testes"
```

### Push e criar PR

```bash
git push origin feature/reports

# No GitHub, criar Pull Request de feature/reports para stage-1
# Título: "Feature: Relatórios e Estatísticas"
# Descrição: Implementa relatórios com estatísticas do sistema e filtros de matrículas. Inclui DEV_MODE com DataMock.
```

---

## Etapa Final: Mesclar feature/reports em stage-1

Após a PR da `feature/reports` ser aprovada e mesclada:

```bash
# Atualizar stage-1 localmente
git checkout stage-1
git pull origin stage-1

# Conferir que todas as branches foram mescladas
git log --oneline | head -20
```

---

## Resumo do Desenvolvimento

### Branches Criadas

| Branch | Objetivo | Commits |
|--------|----------|---------|
| feature/student-management | CRUD de alunos | 9 |
| feature/plan-management | CRUD de planos | 6 |
| feature/enrollment-management | Matrícula com pagamento inicial | 8 |
| feature/payment-management | Registro de pagamentos adicionais | 3 |
| feature/reports | Relatórios e estatísticas | 6 |

### Total

- **32 commits** estruturados e bem documentados
- Cada branch pronta para ser integrada após a anterior ser mesclada
- Código compilável em cada etapa
- Escalabilidade: futuras branches podem adicionar novos módulos sem alterar o existente

---

## Notas Importantes

1. **Ordem de mesclagem**: Respeite a ordem sequencial. Cada branch depende do código da anterior.

2. **Conflitos**: Se houver conflitos durante o merge, resolva-os localmente antes de fazer push.

3. **Testes**: Após cada mesclagem, teste as funcionalidades implementadas.

4. **DEV_MODE**: Em `FitManagerApp.java`, mude `DEV_MODE = false` para `true` se quiser testar com dados pré-carregados.

5. **Estrutura de pacotes**: Respeite a estrutura:
    - `src/` - código-fonte
    - `ui/` - interface do usuário
    - `ui/screen/` - componentes básicos de entrada/saída
    - `ui/menus/` - menus específicos do sistema
    - `application/` - camada de aplicação (FitManager, Services)
    - `application/services/` - serviços de domínio
    - `domain/` - modelos de negócio
    - `domain/model/` - classes de entidade
    - `domain/enums/` - enumerações
    - `helpers/` - utilitários (DataMock, etc.)

6. **Imports e compilação**: Certifique-se de que todos os imports estão corretos antes de fazer commit. Compile e teste cada branch antes de fazer push.

