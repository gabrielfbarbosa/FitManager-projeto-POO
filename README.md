# FitManager

Sistema de gestão de academia desenvolvido em Java como projeto da disciplina de Programação Orientada a Objetos. O sistema permite o gerenciamento de alunos, planos, matrículas e pagamentos por meio de uma interface interativa baseada em JOptionPane.

---

## Integrantes

| Nome | GitHub |
|---|---|
| Gabriel Felipe Barbosa | — |
| Marcelle Luna Souza | — |

---

## Tecnologias

- **Java 17**
- Interface gráfica: `javax.swing.JOptionPane`
- Sem dependências externas — apenas a biblioteca padrão do Java

---

## Estrutura do Projeto

```
fitmanager/
├── src/
│   ├── FitManagerApp.java          # Ponto de entrada do sistema
│   ├── application/
│   │   ├── FitManager.java         # Orquestrador central
│   │   ├── OperationResult.java    # Retorno padronizado das operações
│   │   └── services/
│   │       ├── StudentService.java
│   │       ├── PlanService.java
│   │       └── EnrollmentService.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Student.java
│   │   │   ├── Plan.java
│   │   │   ├── Enrollment.java
│   │   │   └── Payment.java
│   │   └── enums/
│   │       ├── PlanType.java
│   │       ├── PaymentType.java
│   │       └── EnrollmentStatus.java
│   └── ui/
│       ├── menu/
│       │   ├── MenuOption.java         # Interface comum dos enums de menu
│       │   ├── main/
│       │   │   ├── MainMenu.java
│       │   │   └── MainMenuOption.java
│       │   ├── student/
│       │   │   ├── StudentMenu.java
│       │   │   └── StudentMenuOption.java
│       │   ├── plan/
│       │   │   ├── PlanMenu.java
│       │   │   └── PlanMenuOption.java
│       │   ├── enrollment/
│       │   │   ├── EnrollmentMenu.java
│       │   │   └── EnrollmentMenuOption.java
│       │   └── reports/
│       │       ├── ReportsMenu.java
│       │       └── ReportsMenuOption.java
│       └── screen/
│           └── UserInterface.java      # Toda a I/O do sistema
├── report.md
├── diagram.png
└── README.md
```

---

## Como Compilar e Executar

### Pré-requisitos

- Java 17 ou superior instalado
- Verificar versão instalada:

```bash
java -version
```

### Compilação

A partir da raiz do projeto, compile todos os arquivos `.java` de uma vez:

```bash
find src -name "*.java" -print | xargs javac -d out
```

Isso compila todos os fontes e gera os `.class` na pasta `out/`.

> **Alternativa no Windows (sem `find`):**
> ```cmd
> javac -d out src\FitManagerApp.java src\application\*.java src\application\services\*.java src\domain\model\*.java src\domain\enums\*.java src\ui\screen\*.java src\ui\menu\*.java src\ui\menu\main\*.java src\ui\menu\student\*.java src\ui\menu\plan\*.java src\ui\menu\enrollment\*.java src\ui\menu\reports\*.java
> ```

### Execução

```bash
java -cp out FitManagerApp
```

### Compilação e execução em um único comando (Linux/macOS)

```bash
find src -name "*.java" | xargs javac -d out && java -cp out FitManagerApp
```

---

## Funcionalidades

- **Gestão de alunos** — cadastro, consulta por CPF, edição, inativação e listagem
- **Gestão de planos** — cadastro, consulta por nome, atualização de preço e listagem
- **Gestão de matrículas** — realização, registro de pagamentos, cancelamento, consulta e histórico
- **Relatórios** — alunos com matrícula ativa, matrículas com saldo pendente, todas as matrículas

---

## Versão Java utilizada

Java 25.0.1 (LTS)