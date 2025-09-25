package br.com.gestao;

import br.com.gestao.manager.GestorSistema;
import br.com.gestao.model.Usuario;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.model.enums.TipoPersistencia;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GestorSistema gestor = new GestorSistema();
        Scanner sc = new Scanner(System.in);

        // Definir tipo de persistência
        System.out.println("Escolha o tipo de persistência: (1) JSON ou (2) CSV");
        int tipo = sc.nextInt();
        sc.nextLine();

        if (tipo == 2) {
            gestor.setTipoPersistencia(TipoPersistencia.CSV);
        } else {
            gestor.setTipoPersistencia(TipoPersistencia.JSON);
        }

        gestor.carregarTudo();

        int opcao;
        do {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Cadastrar Usuário");
            System.out.println("2. Atualizar Usuário");
            System.out.println("3. Remover Usuário");
            System.out.println("4. Cadastrar Projeto");
            System.out.println("5. Atualizar Projeto");
            System.out.println("6. Remover Projeto");
            System.out.println("7. Cadastrar Equipe");
            System.out.println("8. Atualizar Equipe");
            System.out.println("9. Remover Equipe");
            System.out.println("10. Adicionar Usuário em Equipe");
            System.out.println("11. Associar Equipe a Projeto");
            System.out.println("12. Restaurar Backup");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1 -> {
                    System.out.print("Nome: ");
                    String nome = sc.nextLine();
                    System.out.print("CPF: ");
                    String cpf = sc.nextLine();
                    System.out.print("Email: ");
                    String email = sc.nextLine();
                    System.out.print("Cargo: ");
                    String cargo = sc.nextLine();
                    System.out.print("Login: ");
                    String login = sc.nextLine();
                    System.out.print("Senha: ");
                    String senha = sc.nextLine();
                    System.out.print("Perfil (ADMIN, GERENTE, COLABORADOR): ");
                    PerfilUsuario perfil = PerfilUsuario.valueOf(sc.nextLine().toUpperCase());

                    gestor.cadastrarUsuario(nome, cpf, email, cargo, login, senha, perfil);
                }
                case 2 -> {
                    System.out.print("CPF do usuário a atualizar: ");
                    String cpf = sc.nextLine();
                    System.out.print("Novo email: ");
                    String novoEmail = sc.nextLine();
                    System.out.print("Novo cargo: ");
                    String novoCargo = sc.nextLine();
                    gestor.atualizarUsuario(cpf, novoEmail, novoCargo);
                }
                case 3 -> {
                    System.out.print("CPF do usuário a remover: ");
                    String cpf = sc.nextLine();
                    gestor.removerUsuario(cpf);
                }
                case 4 -> {
                    System.out.print("Nome do projeto: ");
                    String nome = sc.nextLine();
                    System.out.print("Descrição: ");
                    String desc = sc.nextLine();
                    System.out.print("Data de início (yyyy-MM-dd): ");
                    LocalDate inicio = LocalDate.parse(sc.nextLine());
                    System.out.print("Data de fim (yyyy-MM-dd): ");
                    LocalDate fim = LocalDate.parse(sc.nextLine());
                    System.out.print("Status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO): ");
                    StatusProjeto status = StatusProjeto.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("CPF do gerente responsável: ");
                    String cpfGerente = sc.nextLine();

                    Usuario gerente = gestor.getUsuarios().stream()
                            .filter(u -> u.getCpf().equals(cpfGerente))
                            .findFirst().orElse(null);

                    gestor.cadastrarProjeto(nome, desc, inicio, fim, status, gerente);
                }
                case 5 -> {
                    System.out.print("Nome do projeto a atualizar: ");
                    String nome = sc.nextLine();
                    System.out.print("Novo status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO): ");
                    StatusProjeto novoStatus = StatusProjeto.valueOf(sc.nextLine().toUpperCase());
                    gestor.atualizarProjeto(nome, novoStatus);
                }
                case 6 -> {
                    System.out.print("Nome do projeto a remover: ");
                    String nome = sc.nextLine();
                    gestor.removerProjeto(nome);
                }
                case 7 -> {
                    System.out.print("Nome da equipe: ");
                    String nome = sc.nextLine();
                    System.out.print("Descrição: ");
                    String desc = sc.nextLine();
                    gestor.cadastrarEquipe(nome, desc);
                }
                case 8 -> {
                    System.out.print("Nome da equipe a atualizar: ");
                    String nome = sc.nextLine();
                    System.out.print("Novo nome (ou deixe em branco): ");
                    String novoNome = sc.nextLine();
                    System.out.print("Nova descrição (ou deixe em branco): ");
                    String novaDesc = sc.nextLine();
                    gestor.atualizarEquipe(nome, novoNome, novaDesc);
                }
                case 9 -> {
                    System.out.print("Nome da equipe a remover: ");
                    String nome = sc.nextLine();
                    gestor.removerEquipe(nome);
                }
                case 10 -> {
                    System.out.print("CPF do usuário: ");
                    String cpf = sc.nextLine();
                    System.out.print("Nome da equipe: ");
                    String equipe = sc.nextLine();
                    gestor.adicionarUsuarioEmEquipe(cpf, equipe);
                }
                case 11 -> {
                    System.out.print("Nome da equipe: ");
                    String equipe = sc.nextLine();
                    System.out.print("Nome do projeto: ");
                    String projeto = sc.nextLine();
                    gestor.associarEquipeAProjeto(equipe, projeto);
                }
                case 12 -> {
                    System.out.println("Digite o caminho do backup de usuários:");
                    String arqUsuarios = sc.nextLine();
                    System.out.println("Digite o caminho do backup de projetos:");
                    String arqProjetos = sc.nextLine();
                    System.out.println("Digite o caminho do backup de equipes:");
                    String arqEquipes = sc.nextLine();
                    gestor.restaurarBackup(arqUsuarios, arqProjetos, arqEquipes);
                }
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida!");
            }

        } while (opcao != 0);

        sc.close();
    }
}
