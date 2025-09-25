package br.com.gestao.manager;

import br.com.gestao.model.*;
import br.com.gestao.model.Tarefa.Prioridade;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.model.enums.TipoPersistencia;
import br.com.gestao.util.Persistencia;
import br.com.gestao.util.PersistenciaCSV;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.io.File;

import java.time.YearMonth;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class GestorSistema {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Projeto> projetos = new ArrayList<>();
    private List<Equipe> equipes = new ArrayList<>();

    private TipoPersistencia tipoPersistencia = TipoPersistencia.JSON; // padrão

    private static final String USUARIOS_ARQ = "usuarios.json";
    private static final String PROJETOS_ARQ = "projetos.json";
    private static final String EQUIPES_ARQ = "equipes.json";

    private static final String USUARIOS_CSV = "usuarios.csv";
    private static final String PROJETOS_CSV = "projetos.csv";
    private static final String EQUIPES_CSV = "equipes.csv";

    // ==== Configuração de persistência ====
    public void setTipoPersistencia(TipoPersistencia tipo) {
        this.tipoPersistencia = tipo;
    }

    public TipoPersistencia getTipoPersistencia() {
        return tipoPersistencia;
    }

    // ==== Salvar tudo ====
    public void salvarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            salvarJSON();
            salvarBackupJSON();
        } else {
            salvarCSV();
            salvarBackupCSV();
        }
    }

    // ==== Carregar tudo ====
    public void carregarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            carregarJSON();
        } else {
            carregarCSV();
        }
    }

    public List<String> listarBackups(String palavraChave) {
        File pasta = new File("."); // pasta atual
        String[] arquivos = pasta.list((dir, name) ->
                name.startsWith("backup_") && name.contains(palavraChave)
        );
        if (arquivos != null) {
            Arrays.sort(arquivos);
            return Arrays.asList(arquivos);
        }
        return new ArrayList<>();
    }

    // ==== Backup JSON ====
    private void salvarBackupJSON() {
		String timestamp = LocalDate.now().toString(); // só data
		Persistencia.salvar("backup_usuarios_" + timestamp + ".json", usuarios);
		Persistencia.salvar("backup_projetos_" + timestamp + ".json", projetos);
		Persistencia.salvar("backup_equipes_" + timestamp + ".json", equipes);
		System.out.println("Backup JSON criado: " + timestamp);
	}


    // ==== Backup CSV ====
    private void salvarBackupCSV() {
        String timestamp = LocalDate.now().toString(); // só data
		PersistenciaCSV.salvar("backup_usuarios_" + timestamp + ".csv", gerarDadosUsuariosCSV());
		PersistenciaCSV.salvar("backup_projetos_" + timestamp + ".csv", gerarDadosProjetosCSV());
		PersistenciaCSV.salvar("backup_equipes_" + timestamp + ".csv", gerarDadosEquipesCSV());
		System.out.println("Backup CSV criado: " + timestamp);
	}

    // ==== Restaurar Backup ====
    public void restaurarBackup(String caminhoUsuarios, String caminhoProjetos, String caminhoEquipes) {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
            Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
            Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

            List<Usuario> u = Persistencia.carregar(caminhoUsuarios, usuariosType);
            List<Projeto> p = Persistencia.carregar(caminhoProjetos, projetosType);
            List<Equipe> e = Persistencia.carregar(caminhoEquipes, equipesType);

            if (u != null) usuarios = u;
            if (p != null) projetos = p;
            if (e != null) equipes = e;

        } else {
            List<String[]> usuariosCSV = PersistenciaCSV.carregar(caminhoUsuarios);
            usuarios.clear();
            for (String[] u : usuariosCSV) {
                usuarios.add(new Usuario(u[0], u[1], u[2], u[3], u[4], u[5], PerfilUsuario.valueOf(u[6])));
            }

            List<String[]> equipesCSV = PersistenciaCSV.carregar(caminhoEquipes);
            equipes.clear();
            for (String[] e : equipesCSV) {
                Equipe equipe = new Equipe(e[0], e[1]);
                if (e.length > 2 && !e[2].isEmpty()) {
                    String[] cpfs = e[2].split(",");
                    for (String cpf : cpfs) {
                        usuarios.stream()
                                .filter(u -> u.getCpf().equals(cpf))
                                .findFirst()
                                .ifPresent(equipe::adicionarMembro);
                    }
                }
                equipes.add(equipe);
            }

            List<String[]> projetosCSV = PersistenciaCSV.carregar(caminhoProjetos);
            projetos.clear();
            for (String[] p : projetosCSV) {
                Usuario gerente = usuarios.stream()
                        .filter(u -> u.getCpf().equals(p[2]))
                        .findFirst().orElse(null);
                Equipe equipe = equipes.stream()
                        .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                        .findFirst().orElse(null);

                Projeto projeto = new Projeto(
                        p[0], p[1],
                        LocalDate.parse(p[4]), LocalDate.parse(p[5]),
                        StatusProjeto.valueOf(p[6]), gerente
                );
                projeto.setEquipe(equipe);
                projetos.add(projeto);
            }
        }

        System.out.println("Backup restaurado com sucesso!");
    }

	public void restaurarBackupEscolhido() {
		String extensao = tipoPersistencia == TipoPersistencia.JSON ? ".json" : ".csv";

		// Listar arquivos de backup disponíveis
		List<String> backupsUsuarios = listarBackups("usuarios" + extensao);
		List<String> backupsProjetos = listarBackups("projetos" + extensao);
		List<String> backupsEquipes = listarBackups("equipes" + extensao);

		if (backupsUsuarios.isEmpty() || backupsProjetos.isEmpty() || backupsEquipes.isEmpty()) {
			System.out.println("Nenhum backup encontrado!");
			return;
		}

		System.out.println("\nBackups disponíveis de usuários:");
		for (int i = 0; i < backupsUsuarios.size(); i++) {
			System.out.println((i + 1) + " - " + backupsUsuarios.get(i));
		}

		Scanner sc = new Scanner(System.in);
		System.out.print("Escolha o backup a restaurar (usuários/projetos/equipes com mesmo índice): ");
		int escolha = sc.nextInt() - 1;

		if (escolha >= 0 && escolha < backupsUsuarios.size()) {
			restaurarBackup(
					backupsUsuarios.get(escolha),
					backupsProjetos.get(escolha),
					backupsEquipes.get(escolha)
			);
		} else {
			System.out.println("Índice inválido!");
		}
	}


    // ==== Persistência JSON ====
    private void salvarJSON() {
        Persistencia.salvar(USUARIOS_ARQ, usuarios);
        Persistencia.salvar(PROJETOS_ARQ, projetos);
        Persistencia.salvar(EQUIPES_ARQ, equipes);
        System.out.println("Dados salvos em JSON!");
    }

    private void carregarJSON() {
        Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
        Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
        Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

        List<Usuario> u = Persistencia.carregar(USUARIOS_ARQ, usuariosType);
        List<Projeto> p = Persistencia.carregar(PROJETOS_ARQ, projetosType);
        List<Equipe> e = Persistencia.carregar(EQUIPES_ARQ, equipesType);

        if (u != null) usuarios = u;
        if (p != null) projetos = p;
        if (e != null) equipes = e;

        System.out.println("Dados carregados de JSON!");
    }

    // ==== Persistência CSV ====
    public void salvarCSV() {
        PersistenciaCSV.salvar(USUARIOS_CSV, gerarDadosUsuariosCSV());
        PersistenciaCSV.salvar(PROJETOS_CSV, gerarDadosProjetosCSV());
        PersistenciaCSV.salvar(EQUIPES_CSV, gerarDadosEquipesCSV());
        System.out.println("Dados salvos em CSV!");
    }

    public void carregarCSV() {
        usuarios.clear();
        for (String[] u : PersistenciaCSV.carregar(USUARIOS_CSV)) {
            usuarios.add(new Usuario(u[0], u[1], u[2], u[3], u[4], u[5], PerfilUsuario.valueOf(u[6])));
        }

        equipes.clear();
        for (String[] e : PersistenciaCSV.carregar(EQUIPES_CSV)) {
            Equipe equipe = new Equipe(e[0], e[1]);
            if (e.length > 2 && !e[2].isEmpty()) {
                String[] cpfs = e[2].split(",");
                for (String cpf : cpfs) {
                    usuarios.stream()
                            .filter(u -> u.getCpf().equals(cpf))
                            .findFirst()
                            .ifPresent(equipe::adicionarMembro);
                }
            }
            equipes.add(equipe);
        }

        projetos.clear();
        for (String[] p : PersistenciaCSV.carregar(PROJETOS_CSV)) {
            Usuario gerente = usuarios.stream()
                    .filter(u -> u.getCpf().equals(p[2]))
                    .findFirst().orElse(null);
            Equipe equipe = equipes.stream()
                    .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                    .findFirst().orElse(null);

            Projeto projeto = new Projeto(
                    p[0], p[1],
                    LocalDate.parse(p[4]), LocalDate.parse(p[5]),
                    StatusProjeto.valueOf(p[6]), gerente
            );
            projeto.setEquipe(equipe);
            projetos.add(projeto);
        }

        System.out.println("Dados carregados de CSV!");
    }

    // ==== USUÁRIOS ====
    public void cadastrarUsuario(String nome, String cpf, String email, String cargo,
                                 String login, String senha, PerfilUsuario perfil) {
        Usuario usuario = new Usuario(nome, cpf, email, cargo, login, senha, perfil);
        usuarios.add(usuario);
        salvarTudo();
        System.out.println("Usuário " + nome + " cadastrado com sucesso!");
    }

    public void cadastrarUsuarioSim(String nome, String cpf, String email, String cargo,
                                 String login, String senha, PerfilUsuario perfil) {
        Usuario usuario = new Usuario(nome, cpf, email, cargo, login, senha, perfil);
        usuarios.add(usuario);
        System.out.println("Usuário " + nome +" cadastrado com sucesso!");
    }

    public void atualizarUsuario(String cpf, String novoEmail, String novoCargo) {
        for (Usuario u : usuarios) {
            if (u.getCpf().equals(cpf)) {
                u.setEmail(novoEmail);
                u.setCargo(novoCargo);
                salvarTudo();
                System.out.println("Usuário " + cpf +" atualizado!");
                return;
            }
        }
        System.out.println("Usuário não encontrado!");
    }

    public void removerUsuario(String cpf) {
        boolean removido = usuarios.removeIf(u -> u.getCpf().equals(cpf));
        if (removido) {
            salvarTudo();
            System.out.println("Usuário removido!");
        } else {
            System.out.println("Usuário não encontrado!");
        }
    }

    // ==== PROJETOS ====
    public void cadastrarProjeto(String nome, String descricao, LocalDate inicio,
                                 LocalDate fim, StatusProjeto status, Usuario gerente) {
        Projeto projeto = new Projeto(nome, descricao, inicio, fim, status, gerente);
        projetos.add(projeto);
        salvarTudo();
        System.out.println("Projeto cadastrado!");
    }

    public void atualizarProjeto(String nome, StatusProjeto novoStatus) {
        projetos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(p -> {
                    p.setStatus(novoStatus);
                    salvarTudo();
                    System.out.println("Projeto atualizado: " + p);
                }, () -> System.out.println("Projeto não encontrado."));
    }

    public void removerProjeto(String nomeProjeto) {
        boolean removido = projetos.removeIf(p -> p.getNome().equalsIgnoreCase(nomeProjeto));
        if (removido) {
            salvarTudo();
            System.out.println("Projeto removido!");
        } else {
            System.out.println("Projeto não encontrado!");
        }
    }

    // ==== EQUIPES ====
    public void cadastrarEquipe(String nome, String descricao) {
        Equipe equipe = new Equipe(nome, descricao);
        equipes.add(equipe);
        salvarTudo();
        System.out.println("Equipe cadastrada!");
    }

    public void cadastrarEquipeSim(String nome, String descricao) {
        Equipe equipe = new Equipe(nome, descricao);
        equipes.add(equipe);
        System.out.println("Equipe cadastrada!");
    }

    public void atualizarEquipe(String nome, String novoNome, String novaDesc) {
        equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(e -> {
                    if (novoNome != null && !novoNome.isBlank()) e.setNome(novoNome);
                    if (novaDesc != null && !novaDesc.isBlank()) e.setDescricao(novaDesc);
                    salvarTudo();
                    System.out.println("Equipe atualizada: " + e);
                }, () -> System.out.println("Equipe não encontrada."));
    }

    public void removerEquipe(String nomeEquipe) {
        boolean removido = equipes.removeIf(e -> e.getNome().equalsIgnoreCase(nomeEquipe));
        if (removido) {
            salvarTudo();
            System.out.println("Equipe removida!");
        } else {
            System.out.println("Equipe não encontrada!");
        }
    }

    // ==== ASSOCIAÇÕES ====
    public void adicionarUsuarioEmEquipe(String cpf, String nomeEquipe) {
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getCpf().equals(cpf))
                .findFirst().orElse(null);

        Equipe equipe = equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
                .findFirst().orElse(null);

        if (usuario != null && equipe != null) {
            equipe.adicionarMembro(usuario);
            salvarTudo();
            System.out.println("Usuário adicionado à equipe!");
        } else {
            System.out.println("Usuário ou equipe não encontrados.");
        }
    }

        public void adicionarUsuarioEmEquipeSim(String cpf, String nomeEquipe) {
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getCpf().equals(cpf))
                .findFirst().orElse(null);

        Equipe equipe = equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
                .findFirst().orElse(null);

        if (usuario != null && equipe != null) {
            equipe.adicionarMembro(usuario);
            System.out.println("Usuário adicionado à equipe!");
        } else {
            System.out.println("Usuário ou equipe não encontrados.");
        }
    }

    public void associarEquipeAProjeto(String nomeEquipe, String nomeProjeto) {
        Equipe equipe = equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
                .findFirst().orElse(null);

        Projeto projeto = projetos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nomeProjeto))
                .findFirst().orElse(null);

        if (equipe != null && projeto != null) {
            projeto.setEquipe(equipe);
            salvarTudo();
            System.out.println("Equipe atribuída ao projeto!");
        } else {
            System.out.println("Equipe ou projeto não encontrados.");
        }
    }

    // ==== CSV helpers ====
    private List<String[]> gerarDadosUsuariosCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Usuario u : usuarios) {
            dados.add(new String[]{
                    u.getNomeCompleto(),
                    u.getCpf(),
                    u.getEmail(),
                    u.getCargo(),
                    u.getLogin(),
                    u.getSenha(),
                    u.getPerfil().name()
            });
        }
        return dados;
    }

    private List<String[]> gerarDadosProjetosCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Projeto p : projetos) {
            dados.add(new String[]{
                    p.getNome(),
                    p.getDescricao(),
                    p.getGerenteResponsavel() != null ? p.getGerenteResponsavel().getCpf() : "",
                    p.getEquipe() != null ? p.getEquipe().getNome() : "",
                    p.getDataInicio().toString(),
                    p.getDataFimPrevista().toString(),
                    p.getStatus().name()
            });
        }
        return dados;
    }

    private List<String[]> gerarDadosEquipesCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Equipe e : equipes) {
            String membros = String.join(",", e.getMembros().stream().map(Usuario::getCpf).toList());
            dados.add(new String[]{e.getNome(), e.getDescricao(), membros});
        }
        return dados;
    }

    // ==== GETTERS ====
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Equipe> getEquipes() { return equipes; }

	// ==== Relatorios ====

	public void gerarRelatorioDesempenho(YearMonth mes) {
		System.out.println("\n===== RELATÓRIO DE DESEMPENHO - " + mes + " =====");

		// Projetos
		System.out.println("\n--- Projetos ---");
		for (Projeto p : projetos) {
			long tarefasConcluidas = p.getTarefas().stream()
					.filter(Tarefa::isConcluida)
					.filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
					.count();
			long tarefasDentroPrazo = p.getTarefas().stream()
					.filter(Tarefa::isConcluida)
					.filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
					.filter(t -> !t.getDataConclusao().isAfter(t.getDataFimPrevista()))
					.count();
			System.out.println(p.getNome() + ": " + tarefasConcluidas + " tarefas concluídas, " +
					tarefasDentroPrazo + " dentro do prazo");
		}

		// Equipes
		System.out.println("\n--- Equipes ---");
		for (var e : equipes) {
			long totalConcluidas = e.getMembros().stream()
					.flatMap(u -> projetos.stream().flatMap(p -> p.getTarefas().stream()))
					.filter(Tarefa::isConcluida)
					.filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
					.count();
			System.out.println(e.getNome() + ": " + totalConcluidas + " tarefas concluídas");
		}

		// Usuários
		System.out.println("\n--- Usuários ---");
		for (Usuario u : usuarios) {
			long totalConcluidas = projetos.stream()
					.flatMap(p -> p.getTarefas().stream())
					.filter(Tarefa::isConcluida)
					.filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
					.count();
			System.out.println(u.getNomeCompleto() + ": " + totalConcluidas + " tarefas concluídas");
		}
	}

    public void simularDadosCorporativos() {
        Random rand = new Random();
        String[] prioridades = {"BAIXA", "MEDIA", "ALTA"};

        // ==== Criar usuários ====
        for (int i = 1; i <= 10; i++) {
            String nome = "Usuario " + i;
            String cpf = "0000000000" + i;
            String email = "user" + i + "@exemplo.com";
            String cargo = "Cargo " + i;
            String login = "user" + i;
            String senha = "senha" + i;
            PerfilUsuario perfil = i % 2 == 0 ? PerfilUsuario.COLABORADOR : PerfilUsuario.GERENTE;
            cadastrarUsuarioSim(nome, cpf, email, cargo, login, senha, perfil);
        }

        // ==== Criar equipes ====
        cadastrarEquipeSim("Equipe A", "Equipe de teste A");
        cadastrarEquipeSim("Equipe B", "Equipe de teste B");

        // Adicionar 5 usuários por equipe
        for (int i = 0; i < 5; i++) {
            adicionarUsuarioEmEquipeSim(usuarios.get(i).getCpf(), "Equipe A");
            adicionarUsuarioEmEquipeSim(usuarios.get(i + 5).getCpf(), "Equipe B");
        }

        // ==== Criar projetos ====
        Projeto p1 = new Projeto(
                "Projeto 1", "Descrição do Projeto 1",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(10),
                StatusProjeto.PLANEJADO,
                usuarios.get(0)
        );

        Projeto p2 = new Projeto(
                "Projeto 2", "Descrição do Projeto 2",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(15),
                StatusProjeto.PLANEJADO,
                usuarios.get(5)
        );

        System.out.println("Simulação corporativa em processo para " + p1.toString());
        // Associar equipes
        p1.setEquipe(equipes.stream().filter(e -> e.getNome().equals("Equipe A")).findFirst().orElse(null));
        p2.setEquipe(equipes.stream().filter(e -> e.getNome().equals("Equipe B")).findFirst().orElse(null));

        // ==== Gerar tarefas: 2 por usuário por projeto ====
        for (Projeto projeto : new Projeto[]{p1, p2}) {
            Equipe equipe = projeto.getEquipe();
            if (equipe == null || equipe.getMembros().isEmpty()) continue;
            System.out.println("Simulação corporativa em processo para " + projeto.getNome());
    
            for (Usuario u : equipe.getMembros()) {
                for (int t = 1; t <= 2; t++) {
                    LocalDate inicio = LocalDate.now().minusDays(rand.nextInt(5));
                    LocalDate fimPrevisto = inicio.plusDays(2 + rand.nextInt(5));
                    Prioridade prioridade = Prioridade.valueOf(prioridades[rand.nextInt(prioridades.length)]);

                    Tarefa tarefa = new Tarefa(
                            u.getNomeCompleto() + " - Tarefa " + t + " (" + projeto.getNome() + ")",
                            "Descrição da tarefa " + t + " de " + u.getNomeCompleto(),
                            inicio,
                            fimPrevisto,
                            projeto.getNome(),
                            equipe.getNome(),
                            u.getNomeCompleto(),
                            prioridade
                    );

                    // Conclusão aleatória: 70% chance
                    if (rand.nextDouble() > 0.3) {
                        LocalDate conclusao = inicio.plusDays(rand.nextInt(7));
                        tarefa.concluir(conclusao);
                    }

                    projeto.adicionarTarefa(tarefa);
                }
            }
        }

        System.out.println("Simulação corporativa em processo para " + projetos.size());
        System.out.println("Simulação corporativa em processo para " + p1.toString());
        // Adicionar projetos à lista geral
        projetos.add(p1);
        projetos.add(p2);

        System.out.println("Simulação corporativa em processo para " + projetos.size() + " tentar salvar agora");
        salvarTudo(); // salvar todos os dados
        System.out.println("Simulação corporativa completa concluída: 2 projetos, 2 equipes, 5 usuários cada, 2 tarefas por usuário.");
        
    }



    // ==== Método auxiliar para gerar tarefas com prioridade, status e datas variadas ====
    public void gerarTarefasCorporativas() {
        Random rand = new Random();
    String[] prioridades = {"BAIXA", "MEDIA", "ALTA"};

        for (Projeto projeto : projetos) {
            Equipe equipe = projeto.getEquipe();
            if (equipe == null || equipe.getMembros().isEmpty()) {
                System.out.println("Projeto '" + projeto.getNome() + "' não possui equipe ou membros.");
                continue;
            }

            // Para cada usuário da equipe, criar 2 tarefas
            for (Usuario u : equipe.getMembros()) {
                for (int t = 1; t <= 2; t++) {
                    LocalDate inicio = LocalDate.now().minusDays(rand.nextInt(5)); // últimas 5 dias
                    LocalDate fimPrevisto = inicio.plusDays(2 + rand.nextInt(5));  // duração 2-6 dias
                    Prioridade prioridade = Prioridade.valueOf(prioridades[rand.nextInt(prioridades.length)]);

                    Tarefa tarefa = new Tarefa(
                            u.getNomeCompleto() + " - Tarefa " + t + " (" + projeto.getNome() + ")",
                            "Descrição da tarefa " + t + " de " + u.getNomeCompleto(),
                            inicio,
                            fimPrevisto,
                            projeto.getNome(),
                            equipe.getNome(),
                            u.getNomeCompleto(),
                            prioridade
                    );

                    // Conclusão aleatória: 70% de chance de ser concluída
                    double chance = rand.nextDouble();
                    if (chance > 0.3) {
                        LocalDate conclusao = inicio.plusDays(rand.nextInt(7)); // possível atraso
                        tarefa.concluir(conclusao);
                    }

                    projeto.adicionarTarefa(tarefa);
                }
            }
        }

        salvarTudo(); // Salvar todas as tarefas geradas
        System.out.println("Tarefas corporativas geradas: 2 tarefas por usuário em cada projeto.");
    }

    public void gerarRelatorioDesempenhoCompleto(YearMonth mes) {
        System.out.println("\n===== RELATÓRIO DE DESEMPENHO - " + mes + " =====");

        // ==== Projetos ====
        System.out.println("\n--- Projetos ---");
        for (Projeto p : projetos) {
            List<Tarefa> tarefasNoMes = p.getTarefas().stream()
                    .filter(t -> t.getDataConclusao() != null)
                    .filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
                    .toList();

            long total = p.getTarefas().size();
            long concluidas = tarefasNoMes.size();
            long noPrazo = tarefasNoMes.stream()
                    .filter(Tarefa::concluidaNoPrazo)
                    .count();

            double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

            System.out.println("Projeto: " + p.getNome());
            System.out.println("Tarefas atribuídas: " + total);
            System.out.println("Concluídas: " + concluidas);
            System.out.println("Dentro do prazo: " + noPrazo);
            System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
        }

        // ==== Equipes ====
        System.out.println("\n--- Equipes ---");
        for (Equipe e : equipes) {
            List<Tarefa> tarefasEquipe = e.getMembros().stream()
                    .flatMap(u -> projetos.stream()
                            .flatMap(p -> p.getTarefas().stream())
                            .filter(t -> t.getResponsavel().equals(u)))
                    .filter(t -> t.getDataConclusao() != null)
                    .filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
                    .toList();

            long total = e.getMembros().stream()
                    .flatMap(u -> projetos.stream()
                            .flatMap(p -> p.getTarefas().stream())
                            .filter(t -> t.getResponsavel().equals(u)))
                    .count();

            long concluidas = tarefasEquipe.size();
            long noPrazo = tarefasEquipe.stream()
                    .filter(Tarefa::concluidaNoPrazo)
                    .count();

            double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

            System.out.println("Equipe: " + e.getNome());
            System.out.println("Tarefas atribuídas: " + total);
            System.out.println("Concluídas: " + concluidas);
            System.out.println("Dentro do prazo: " + noPrazo);
            System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
        }

        // ==== Usuários ====
        System.out.println("\n--- Usuários ---");
        for (Usuario u : usuarios) {
            List<Tarefa> tarefasUsuario = projetos.stream()
                    .flatMap(p -> p.getTarefas().stream())
                    .filter(t -> t.getResponsavel().equals(u))
                    .filter(t -> t.getDataConclusao() != null)
                    .filter(t -> YearMonth.from(t.getDataConclusao()).equals(mes))
                    .toList();

            long total = projetos.stream()
                    .flatMap(p -> p.getTarefas().stream())
                    .filter(t -> t.getResponsavel().equals(u))
                    .count();

            long concluidas = tarefasUsuario.size();
            long noPrazo = tarefasUsuario.stream()
                    .filter(Tarefa::concluidaNoPrazo)
                    .count();

            double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

            System.out.println("Usuário: " + u.getNomeCompleto());
            System.out.println("Tarefas atribuídas: " + total);
            System.out.println("Concluídas: " + concluidas);
            System.out.println("Dentro do prazo: " + noPrazo);
            System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
        }
    }


	public void relatorioDesempenhoMes(Month mes, int ano) {
    System.out.println("\n===== RELATÓRIO DE DESEMPENHO: " + mes + "/" + ano + " =====\n");

    // ==== Projetos ====
    System.out.println("** Projetos **");
    for (Projeto p : projetos) {
        List<Tarefa> tarefasProjeto = p.getTarefas().stream()
                .filter(t -> t.getDataInicio().getMonth() == mes && t.getDataInicio().getYear() == ano)
                .toList();

        long total = tarefasProjeto.size();
        long concluidas = tarefasProjeto.stream().filter(Tarefa::isConcluida).count();
        long noPrazo = tarefasProjeto.stream()
                .filter(t -> t.getStatusPrazo() == Tarefa.StatusPrazo.NO_PRAZO)
                .count();

        double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

        System.out.println("Projeto: " + p.getNome());
        System.out.println("Tarefas atribuídas: " + total);
        System.out.println("Concluídas: " + concluidas);
        System.out.println("Dentro do prazo: " + noPrazo);
        System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
    }

    // ==== Equipes ====
    System.out.println("** Equipes **");
    for (Equipe e : equipes) {
        List<Tarefa> tarefasEquipe = e.getMembros().stream()
                .flatMap(u -> projetos.stream().flatMap(p -> p.getTarefas().stream()))
                .filter(t -> t.getDataInicio().getMonth() == mes && t.getDataInicio().getYear() == ano)
                .toList();

        long total = tarefasEquipe.size();
        long concluidas = tarefasEquipe.stream().filter(Tarefa::isConcluida).count();
        long noPrazo = tarefasEquipe.stream()
                .filter(t -> t.getStatusPrazo() == Tarefa.StatusPrazo.NO_PRAZO)
                .count();

        double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

        System.out.println("Equipe: " + e.getNome());
        System.out.println("Tarefas atribuídas: " + total);
        System.out.println("Concluídas: " + concluidas);
        System.out.println("Dentro do prazo: " + noPrazo);
        System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
    }

    // ==== Usuários ====
    System.out.println("** Usuários **");
    for (Usuario u : usuarios) {
        List<Tarefa> tarefasUsuario = projetos.stream()
                .flatMap(p -> p.getTarefas().stream())
                .filter(t -> t.getDataInicio().getMonth() == mes && t.getDataInicio().getYear() == ano)
                .toList();

        long total = tarefasUsuario.size();
        long concluidas = tarefasUsuario.stream().filter(Tarefa::isConcluida).count();
        long noPrazo = tarefasUsuario.stream()
                .filter(t -> t.getStatusPrazo() == Tarefa.StatusPrazo.NO_PRAZO)
                .count();

        double desempenho = total == 0 ? 0.0 : ((double) noPrazo / total) * 100.0;

        System.out.println("Usuário: " + u.getNomeCompleto());
        System.out.println("Tarefas atribuídas: " + total);
        System.out.println("Concluídas: " + concluidas);
        System.out.println("Dentro do prazo: " + noPrazo);
        System.out.printf("Desempenho: %.2f%%\n\n", desempenho);
    }
}



}
