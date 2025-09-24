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
        
		Scanner scanner = new Scanner(System.in);
        GestorSistema gestor = new GestorSistema();

        // Perguntar persistência
        System.out.println("Escolha o tipo de persistência:");
        System.out.println("1 - JSON");
        System.out.println("2 - CSV");
        System.out.print("Opção: ");
        int escolha = scanner.nextInt();
        scanner.nextLine();

        if (escolha == 2) {
            gestor.setTipoPersistencia(TipoPersistencia.CSV);
        } else {
            gestor.setTipoPersistencia(TipoPersistencia.JSON);
        }

        gestor.carregarTudo(); // Carregar dados do formato escolhido

		
        int opcao;

        do {
            System.out.println("\n==== MENU PRINCIPAL ====");
            System.out.println("1 - Cadastrar usuário");
            System.out.println("2 - Atualizar usuário");
            System.out.println("3 - Remover usuário");
            System.out.println("4 - Listar usuários");
            System.out.println("5 - Cadastrar projeto");
            System.out.println("6 - Atualizar projeto");
            System.out.println("7 - Remover projeto");
            System.out.println("8 - Listar projetos");
            System.out.println("9 - Cadastrar equipe");
            System.out.println("10 - Atualizar equipe");
            System.out.println("11 - Remover equipe");
            System.out.println("12 - Listar equipes");
			System.out.println("13 - Adicionar usuário em equipe");
			System.out.println("14 - Associar equipe a projeto");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                // ==== USUÁRIO ====
                case 1 -> {
                    System.out.print("Nome completo: ");
                    String nome = scanner.nextLine();
                    System.out.print("CPF: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Cargo: ");
                    String cargo = scanner.nextLine();
                    System.out.print("Login: ");
                    String login = scanner.nextLine();
                    System.out.print("Senha: ");
                    String senha = scanner.nextLine();
                    System.out.print("Perfil (1-ADMIN, 2-GERENTE, 3-COLABORADOR): ");
                    int perfilOpcao = scanner.nextInt(); scanner.nextLine();
                    PerfilUsuario perfil = switch (perfilOpcao) {
                        case 1 -> PerfilUsuario.ADMINISTRADOR;
                        case 2 -> PerfilUsuario.GERENTE;
                        default -> PerfilUsuario.COLABORADOR;
                    };
                    gestor.cadastrarUsuario(nome, cpf, email, cargo, login, senha, perfil);
                }
                case 2 -> {
                    System.out.print("Digite o CPF do usuário: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Novo email: ");
                    String novoEmail = scanner.nextLine();
                    System.out.print("Novo cargo: ");
                    String novoCargo = scanner.nextLine();
                    gestor.atualizarUsuario(cpf, novoEmail, novoCargo);
                }
                case 3 -> {
                    System.out.print("Digite o CPF do usuário: ");
                    String cpf = scanner.nextLine();
                    gestor.removerUsuario(cpf);
                }
                case 4 -> gestor.getUsuarios().forEach(System.out::println);

                // ==== PROJETO ====
                case 5 -> {
                    System.out.print("Nome do projeto: ");
                    String nomeProjeto = scanner.nextLine();
                    System.out.print("Descrição: ");
                    String descricao = scanner.nextLine();
                    System.out.print("Data início (AAAA-MM-DD): ");
                    LocalDate inicio = LocalDate.parse(scanner.nextLine());
                    System.out.print("Data fim (AAAA-MM-DD): ");
                    LocalDate fim = LocalDate.parse(scanner.nextLine());
                    System.out.print("Status (1-PLANEJADO, 2-EM_ANDAMENTO, 3-CONCLUIDO, 4-CANCELADO): ");
                    int statusOpcao = scanner.nextInt(); scanner.nextLine();
                    StatusProjeto status = switch (statusOpcao) {
                        case 2 -> StatusProjeto.EM_ANDAMENTO;
                        case 3 -> StatusProjeto.CONCLUIDO;
                        case 4 -> StatusProjeto.CANCELADO;
                        default -> StatusProjeto.PLANEJADO;
                    };
                    System.out.print("CPF do gerente responsável: ");
                    String cpfGerente = scanner.nextLine();
                    Usuario gerente = gestor.getUsuarios().stream()
                            .filter(u -> u.getCpf().equals(cpfGerente) && u.getPerfil() == PerfilUsuario.GERENTE)
                            .findFirst().orElse(null);
                    gestor.cadastrarProjeto(nomeProjeto, descricao, inicio, fim, status, gerente);
                }
                case 6 -> {
                    System.out.print("Nome do projeto: ");
                    String nome = scanner.nextLine();
                    System.out.print("Novo status (1-PLANEJADO, 2-EM_ANDAMENTO, 3-CONCLUIDO, 4-CANCELADO): ");
                    int statusOpcao = scanner.nextInt(); scanner.nextLine();
                    StatusProjeto novoStatus = switch (statusOpcao) {
                        case 2 -> StatusProjeto.EM_ANDAMENTO;
                        case 3 -> StatusProjeto.CONCLUIDO;
                        case 4 -> StatusProjeto.CANCELADO;
                        default -> StatusProjeto.PLANEJADO;
                    };
                    gestor.atualizarProjeto(nome, novoStatus);
                }
                case 7 -> {
                    System.out.print("Nome do projeto: ");
                    String nome = scanner.nextLine();
                    gestor.removerProjeto(nome);
                }
                case 8 -> gestor.getProjetos().forEach(System.out::println);

                // ==== EQUIPE ====
                case 9 -> {
                    System.out.print("Nome da equipe: ");
                    String nomeEquipe = scanner.nextLine();
                    System.out.print("Descrição: ");
                    String descEquipe = scanner.nextLine();
                    gestor.cadastrarEquipe(nomeEquipe, descEquipe);
                }
                case 10 -> {
                    System.out.print("Nome da equipe: ");
                    String nomeEquipe = scanner.nextLine();
                    System.out.print("Novo nome: ");
                    String novoNome = scanner.nextLine();
                    System.out.print("Nova descrição: ");
                    String novaDesc = scanner.nextLine();
                    gestor.atualizarEquipe(nomeEquipe, novoNome, novaDesc);
                }
                case 11 -> {
                    System.out.print("Nome da equipe: ");
                    String nomeEquipe = scanner.nextLine();
                    gestor.removerEquipe(nomeEquipe);
                }
                case 12 -> gestor.getEquipes().forEach(System.out::println);
				
				case 13 -> {
					System.out.print("CPF do usuário: ");
					String cpfUsuario = scanner.nextLine();
					System.out.print("Nome da equipe: ");
					String nomeEquipe = scanner.nextLine();
					gestor.adicionarUsuarioEmEquipe(cpfUsuario, nomeEquipe);
				}

				case 14 -> {
					System.out.print("Nome da equipe: ");
					String nomeEquipe = scanner.nextLine();
					System.out.print("Nome do projeto: ");
					String nomeProjeto = scanner.nextLine();
					gestor.associarEquipeAProjeto(nomeEquipe, nomeProjeto);
				}
				
                case 0 -> System.out.println("Encerrando sistema...");
				
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);

        scanner.close();
    }
}
