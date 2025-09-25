package br.com.gestao;

import br.com.gestao.manager.GestorSistema;
import br.com.gestao.model.Projeto;
import br.com.gestao.model.Tarefa;
import br.com.gestao.model.Usuario;
import br.com.gestao.model.Tarefa.Prioridade;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.model.enums.TipoPersistencia;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GestorSistema gestor = new GestorSistema();
        Scanner sc = new Scanner(System.in);

        // Escolher tipo de persistência
        System.out.println("Escolha o tipo de persistência: (1) JSON ou (2) CSV");
        int tipo = sc.nextInt(); sc.nextLine();
        if (tipo == 2) gestor.setTipoPersistencia(TipoPersistencia.CSV);
        else gestor.setTipoPersistencia(TipoPersistencia.JSON);

        gestor.carregarTudo();

        int opcao;
        do {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1. Usuário");
            System.out.println("2. Projeto");
            System.out.println("3. Equipe");
            System.out.println("4. Backup");
            System.out.println("5. Relatório de Desempenho");
            System.out.println("6. Simular dados (2 projetos, 2 equipes, 10 usuários, 2 tarefas por usuario)");
            System.out.println("7. Relatório de desempenho do mês");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            opcao = sc.nextInt(); sc.nextLine();

            switch (opcao) {
                case 1 -> menuUsuario(sc, gestor);
                case 2 -> menuProjeto(sc, gestor);
                case 3 -> menuEquipe(sc, gestor);
                case 4 -> menuBackup(sc, gestor);
                case 5 -> {
                    System.out.print("Informe o mês/ano (MM-yyyy): ");
                    String entrada = sc.nextLine();
                    YearMonth mes = YearMonth.parse(entrada, java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"));
                    gestor.gerarRelatorioDesempenho(mes);
                }
                case 6 -> gestor.simularDadosCorporativos();
                case 7 -> {
                    System.out.print("Informe o mês (1-12): ");
                    int mes = sc.nextInt();
                    System.out.print("Informe o ano: ");
                    int ano = sc.nextInt();
                    sc.nextLine();
                    gestor.gerarRelatorioDesempenhoCompleto(YearMonth.of(ano, mes));
}

                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);

        sc.close();
    }

    // ===== MENU USUÁRIO =====
    private static void menuUsuario(Scanner sc, GestorSistema gestor) {
        int op;
        do {
            System.out.println("\n--- Menu Usuário ---");
            System.out.println("1. Cadastrar");
            System.out.println("2. Atualizar");
            System.out.println("3. Remover");
            System.out.println("4. Listar Usuários");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            op = sc.nextInt(); sc.nextLine();

            switch(op) {
                case 1 -> {
                    System.out.print("Nome: "); String nome = sc.nextLine();
                    System.out.print("CPF: "); String cpf = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Cargo: "); String cargo = sc.nextLine();
                    System.out.print("Login: "); String login = sc.nextLine();
                    System.out.print("Senha: "); String senha = sc.nextLine();
                    System.out.print("Perfil (ADMIN, GERENTE, COLABORADOR): ");
                    PerfilUsuario perfil = PerfilUsuario.valueOf(sc.nextLine().toUpperCase());
                    gestor.cadastrarUsuario(nome, cpf, email, cargo, login, senha, perfil);
                }
                case 2 -> {
                    System.out.print("CPF do usuário: "); String cpf = sc.nextLine();
                    System.out.print("Novo email: "); String email = sc.nextLine();
                    System.out.print("Novo cargo: "); String cargo = sc.nextLine();
                    gestor.atualizarUsuario(cpf, email, cargo);
                }
                case 3 -> {
                    System.out.print("CPF do usuário: "); String cpf = sc.nextLine();
                    gestor.removerUsuario(cpf);
                }
                case 4 -> {
                    if (gestor.getUsuarios().isEmpty()) System.out.println("Nenhum usuário cadastrado.");
                    else gestor.getUsuarios().forEach(System.out::println);
                }
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while(op != 0);
    }

    // ===== MENU PROJETO =====
    private static void menuProjeto(Scanner sc, GestorSistema gestor) {
        int op;
        do {
            System.out.println("\n--- Menu Projeto ---");
            System.out.println("1. Cadastrar");
            System.out.println("2. Atualizar Status");
            System.out.println("3. Remover");
            System.out.println("4. Gerenciar Tarefas");
            System.out.println("5. Listar Projetos");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            op = sc.nextInt(); sc.nextLine();

            switch(op) {
                case 1 -> {
                    System.out.print("Nome do projeto: "); String nome = sc.nextLine();
                    System.out.print("Descrição: "); String desc = sc.nextLine();
                    System.out.print("Data início (yyyy-MM-dd): "); LocalDate inicio = LocalDate.parse(sc.nextLine());
                    System.out.print("Data fim (yyyy-MM-dd): "); LocalDate fim = LocalDate.parse(sc.nextLine());
                    System.out.print("Status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO): ");
                    StatusProjeto status = StatusProjeto.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("CPF do gerente: "); String cpfGerente = sc.nextLine();
                    Usuario gerente = gestor.getUsuarios().stream()
                            .filter(u -> u.getCpf().equals(cpfGerente)).findFirst().orElse(null);
                    gestor.cadastrarProjeto(nome, desc, inicio, fim, status, gerente);
                }
                case 2 -> {
                    System.out.print("Nome do projeto: "); String nome = sc.nextLine();
                    System.out.print("Novo status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO): ");
                    StatusProjeto status = StatusProjeto.valueOf(sc.nextLine().toUpperCase());
                    gestor.atualizarProjeto(nome, status);
                }
                case 3 -> {
                    System.out.print("Nome do projeto: "); String nome = sc.nextLine();
                    gestor.removerProjeto(nome);
                }
                case 4 -> menuTarefasProjeto(sc, gestor);
                case 5 -> {
                    if (gestor.getProjetos().isEmpty()) System.out.println("Nenhum projeto cadastrado.");
                    else gestor.getProjetos().forEach(System.out::println);
                }
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while(op != 0);
    }
// ===== MENU TAREFAS =====
private static void menuTarefasProjeto(Scanner sc, GestorSistema gestor) {
    System.out.print("Nome do projeto: ");
    String nomeProjeto = sc.nextLine();

    Projeto projeto = gestor.getProjetos().stream()
            .filter(p -> p.getNome().equalsIgnoreCase(nomeProjeto))
            .findFirst().orElse(null);

    if (projeto == null) {
        System.out.println("Projeto não encontrado!");
        return;
    }

    int op;
    do {
        System.out.println("\n--- Tarefas de " + projeto.getNome() + " ---");
        System.out.println("1. Adicionar Tarefa");
        System.out.println("2. Concluir Tarefa");
        System.out.println("3. Listar Tarefas");
        System.out.println("0. Voltar");
        System.out.print("Opção: "); 
        op = sc.nextInt(); sc.nextLine();

        switch(op) {
            case 1 -> {
                System.out.print("Nome da tarefa: "); 
                String nome = sc.nextLine();
                System.out.print("Descrição: "); 
                String desc = sc.nextLine();

                LocalDate inicio = null;
                LocalDate fim = null;
                try {
                    System.out.print("Data início (yyyy-MM-dd): "); 
                    inicio = LocalDate.parse(sc.nextLine());
                    System.out.print("Data fim prevista (yyyy-MM-dd): "); 
                    fim = LocalDate.parse(sc.nextLine());
                } catch (Exception e) {
                    System.out.println("Formato de data inválido!");
                    break;
                }

                // Selecionar responsável
                System.out.println("Selecione o responsável:");
                List<Usuario> membros = projeto.getEquipe() != null ? projeto.getEquipe().getMembros() : List.of();
                if (membros.isEmpty()) {
                    System.out.println("Nenhum membro na equipe do projeto. Não é possível adicionar tarefa.");
                    break;
                }
                for (int i = 0; i < membros.size(); i++) {
                    System.out.println((i+1) + ". " + membros.get(i).getNomeCompleto());
                }
                System.out.print("Escolha usuário: "); 
                int idxUsuario = sc.nextInt() - 1; sc.nextLine();
                if (idxUsuario < 0 || idxUsuario >= membros.size()) {
                    System.out.println("Usuário inválido!");
                    break;
                }

                Usuario responsavel = membros.get(idxUsuario);
                System.out.println("Escolha a prioridade:");
                System.out.println("1. ALTA");
                System.out.println("2. MEDIA");
                System.out.println("3. BAIXA");
                int idxPrioridade = sc.nextInt() - 1; sc.nextLine();
                if (idxPrioridade < 0 || idxPrioridade >= Prioridade.values().length) {
                    System.out.println("Prioridade inválida!");
                    break;
                }
                Prioridade prioridade = Prioridade.values()[idxPrioridade];

                // Criar tarefa com prioridade
                Tarefa tarefa = new Tarefa(nome, desc, inicio, fim, projeto.getNome(), projeto.getEquipe().getNome(), responsavel.getNomeCompleto(), prioridade);

                projeto.adicionarTarefa(tarefa);
                gestor.salvarTudo();
                System.out.println("Tarefa adicionada com sucesso!");
            }
            case 2 -> {
                if (projeto.getTarefas().isEmpty()) { 
                    System.out.println("Nenhuma tarefa disponível."); 
                    break; 
                }
                for (int i = 0; i < projeto.getTarefas().size(); i++) {
                    System.out.println((i+1) + ". " + projeto.getTarefas().get(i));
                }
                System.out.print("Escolha tarefa: "); 
                int idx = sc.nextInt() - 1; sc.nextLine();
                if (idx >= 0 && idx < projeto.getTarefas().size()) {
                    System.out.print("Data conclusão (yyyy-MM-dd): ");
                    LocalDate concl;
                    try {
                        concl = LocalDate.parse(sc.nextLine());
                    } catch (Exception e) {
                        System.out.println("Data inválida!");
                        break;
                    }
                    projeto.getTarefas().get(idx).concluir(concl);
                    gestor.salvarTudo();
                    System.out.println("Tarefa concluída!");
                } else System.out.println("Tarefa inválida!");
            }
            case 3 -> {
                if (projeto.getTarefas().isEmpty()) 
                    System.out.println("Nenhuma tarefa cadastrada.");
                else 
                    projeto.getTarefas().forEach(System.out::println);
            }
            case 0 -> {}
            default -> System.out.println("Opção inválida!");
        }
    } while(op != 0);
}


    // ===== MENU EQUIPE =====
    private static void menuEquipe(Scanner sc, GestorSistema gestor) {
        int op;
        do {
            System.out.println("\n--- Menu Equipe ---");
            System.out.println("1. Cadastrar");
            System.out.println("2. Atualizar");
            System.out.println("3. Remover");
            System.out.println("4. Listar Equipes");
            System.out.println("5. Adicionar Usuário em Equipe");
            System.out.println("0. Voltar");
            System.out.print("Opção: "); op = sc.nextInt(); sc.nextLine();

            switch(op) {
                case 1 -> {
                    System.out.print("Nome da equipe: "); String nome = sc.nextLine();
                    System.out.print("Descrição: "); String desc = sc.nextLine();
                    gestor.cadastrarEquipe(nome, desc);
                }
                case 2 -> {
                    System.out.print("Nome da equipe: "); String nome = sc.nextLine();
                    System.out.print("Novo nome (ou vazio): "); String novoNome = sc.nextLine();
                    System.out.print("Nova descrição (ou vazio): "); String novaDesc = sc.nextLine();
                    gestor.atualizarEquipe(nome, novoNome, novaDesc);
                }
                case 3 -> {
                    System.out.print("Nome da equipe: "); String nome = sc.nextLine();
                    gestor.removerEquipe(nome);
                }
                case 4 -> {
                    if (gestor.getEquipes().isEmpty()) System.out.println("Nenhuma equipe cadastrada.");
                    else gestor.getEquipes().forEach(System.out::println);
                }
                case 5 -> {
                    System.out.print("CPF do usuário: "); String cpf = sc.nextLine();
                    System.out.print("Nome da equipe: "); String nomeEquipe = sc.nextLine();
                    gestor.adicionarUsuarioEmEquipe(cpf, nomeEquipe);
                }
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while(op != 0);
    }

    // ===== MENU BACKUP =====
    private static void menuBackup(Scanner sc, GestorSistema gestor) {
        int op;
        do {
            System.out.println("\n--- Menu Backup ---");
            System.out.println("1. Restaurar Backup Escolhido");
            System.out.println("2. Restaurar Backup Manual");
            System.out.println("0. Voltar");
            System.out.print("Opção: "); op = sc.nextInt(); sc.nextLine();

            switch(op) {
                case 1 -> gestor.restaurarBackupEscolhido();
                case 2 -> {
                    System.out.print("Backup Usuários: "); String u = sc.nextLine();
                    System.out.print("Backup Projetos: "); String p = sc.nextLine();
                    System.out.print("Backup Equipes: "); String e = sc.nextLine();
                    gestor.restaurarBackup(u, p, e);
                }
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while(op != 0);
    }
}
