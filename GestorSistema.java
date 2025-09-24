package br.com.gestao.manager;

import br.com.gestao.model.*;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.util.Persistencia;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorSistema {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Projeto> projetos = new ArrayList<>();
    private List<Equipe> equipes = new ArrayList<>();

    private static final String USUARIOS_ARQ = "usuarios.json";
    private static final String PROJETOS_ARQ = "projetos.json";
    private static final String EQUIPES_ARQ = "equipes.json";

	private static final String USUARIOS_CSV = "usuarios.csv";
    private static final String PROJETOS_CSV = "projetos.csv";
    private static final String EQUIPES_CSV = "equipes.csv";

    // ==== PERSISTÊNCIA CSV ====
    public void salvarCSV() {
        // Usuários
        List<String[]> dadosUsuarios = new ArrayList<>();
        for (Usuario u : usuarios) {
            dadosUsuarios.add(new String[]{
                    u.getNomeCompleto(),
                    u.getCpf(),
                    u.getEmail(),
                    u.getPerfil().name()
            });
        }
        PersistenciaCSV.salvar(USUARIOS_CSV, dadosUsuarios);

        // Projetos
        List<String[]> dadosProjetos = new ArrayList<>();
        for (Projeto p : projetos) {
            dadosProjetos.add(new String[]{
                    p.getNome(),
                    p.getStatus().name(),
                    p.getGerenteResponsavel() != null ? p.getGerenteResponsavel().getCpf() : "",
                    p.getEquipe() != null ? p.getEquipe().getNome() : ""
            });
        }
        PersistenciaCSV.salvar(PROJETOS_CSV, dadosProjetos);

        // Equipes
        List<String[]> dadosEquipes = new ArrayList<>();
        for (Equipe e : equipes) {
            String membros = String.join(",", e.getMembros().stream().map(Usuario::getCpf).toList());
            dadosEquipes.add(new String[]{e.getNome(), e.toString(), membros});
        }
        PersistenciaCSV.salvar(EQUIPES_CSV, dadosEquipes);

        System.out.println("Dados salvos em CSV!");
    }

    public void carregarCSV() {
        // Usuários
        List<String[]> usuariosCSV = PersistenciaCSV.carregar(USUARIOS_CSV);
        for (String[] u : usuariosCSV) {
            usuarios.add(new Usuario(u[0], u[1], u[2], PerfilUsuario.valueOf(u[3])));
        }

        // Equipes
        List<String[]> equipesCSV = PersistenciaCSV.carregar(EQUIPES_CSV);
        for (String[] e : equipesCSV) {
            Equipe equipe = new Equipe(e[0], e[1]);
            // Depois ligamos usuários pelo CPF
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

        // Projetos
        List<String[]> projetosCSV = PersistenciaCSV.carregar(PROJETOS_CSV);
        for (String[] p : projetosCSV) {
            Usuario gerente = usuarios.stream()
                    .filter(u -> u.getCpf().equals(p[2]))
                    .findFirst().orElse(null);
            Equipe equipe = equipes.stream()
                    .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                    .findFirst().orElse(null);

            Projeto projeto = new Projeto(
                    p[0], "Carregado do CSV",
                    LocalDate.now(), LocalDate.now().plusDays(30),
                    StatusProjeto.valueOf(p[1]), gerente
            );
            projeto.setEquipe(equipe);
            projetos.add(projeto);
        }

        System.out.println("Dados carregados do CSV!");
    }

    // ==== PERSISTÊNCIA ====
    public void salvarTudo() {
        Persistencia.salvar(USUARIOS_ARQ, usuarios);
        Persistencia.salvar(PROJETOS_ARQ, projetos);
        Persistencia.salvar(EQUIPES_ARQ, equipes);
        System.out.println("Dados salvos com sucesso!");
    }

    public void carregarTudo() {
        Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
        Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
        Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

        List<Usuario> u = Persistencia.carregar(USUARIOS_ARQ, usuariosType);
        List<Projeto> p = Persistencia.carregar(PROJETOS_ARQ, projetosType);
        List<Equipe> e = Persistencia.carregar(EQUIPES_ARQ, equipesType);

        if (u != null) usuarios = u;
        if (p != null) projetos = p;
        if (e != null) equipes = e;

        System.out.println("Dados carregados!");
    }

    // ==== USUÁRIOS ====
    public void cadastrarUsuario(String nome, String cpf, String email, String cargo,
                                 String login, String senha, PerfilUsuario perfil) {
        Usuario usuario = new Usuario(nome, cpf, email, cargo, login, senha, perfil);
        usuarios.add(usuario);
        System.out.println("Usuário cadastrado: " + usuario);
    }

    public void atualizarUsuario(String cpf, String novoEmail, String novoCargo) {
        usuarios.stream()
                .filter(u -> u.getCpf().equals(cpf))
                .findFirst()
                .ifPresentOrElse(u -> {
                    if (novoEmail != null && !novoEmail.isBlank()) u.setEmail(novoEmail);
                    if (novoCargo != null && !novoCargo.isBlank()) u.setCargo(novoCargo);
                    System.out.println("Usuário atualizado: " + u);
                }, () -> System.out.println("Usuário não encontrado."));
    }

    public void removerUsuario(String cpf) {
        boolean removido = usuarios.removeIf(u -> u.getCpf().equals(cpf));
        System.out.println(removido ? "Usuário removido." : "Usuário não encontrado.");
    }

    // ==== PROJETOS ====
    public void cadastrarProjeto(String nome, String descricao, LocalDate inicio,
                                 LocalDate fim, StatusProjeto status, Usuario gerente) {
        Projeto projeto = new Projeto(nome, descricao, inicio, fim, status, gerente);
        projetos.add(projeto);
        System.out.println("Projeto cadastrado: " + projeto);
    }

    public void atualizarProjeto(String nome, StatusProjeto novoStatus) {
        projetos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(p -> {
                    p.setStatus(novoStatus);
                    System.out.println("Projeto atualizado: " + p);
                }, () -> System.out.println("Projeto não encontrado."));
    }

    public void removerProjeto(String nome) {
        boolean removido = projetos.removeIf(p -> p.getNome().equalsIgnoreCase(nome));
        System.out.println(removido ? "Projeto removido." : "Projeto não encontrado.");
    }

    // ==== EQUIPES ====
    public void cadastrarEquipe(String nome, String descricao) {
        Equipe equipe = new Equipe(nome, descricao);
        equipes.add(equipe);
        System.out.println("Equipe cadastrada: " + equipe);
    }

    public void atualizarEquipe(String nome, String novoNome, String novaDesc) {
        equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(e -> {
                    if (novoNome != null && !novoNome.isBlank()) e.setNome(novoNome);
                    if (novaDesc != null && !novaDesc.isBlank()) e.setDescricao(novaDesc);
                    System.out.println("Equipe atualizada: " + e);
                }, () -> System.out.println("Equipe não encontrada."));
    }

    public void removerEquipe(String nome) {
        boolean removido = equipes.removeIf(e -> e.getNome().equalsIgnoreCase(nome));
        System.out.println(removido ? "Equipe removida." : "Equipe não encontrada.");
    }
	
	// ==== ASSOCIAÇÕES ====

	// Adicionar usuário em equipe
	public void adicionarUsuarioEmEquipe(String cpf, String nomeEquipe) {
		Usuario usuario = usuarios.stream()
				.filter(u -> u.getCpf().equals(cpf))
				.findFirst().orElse(null);

		Equipe equipe = equipes.stream()
				.filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
				.findFirst().orElse(null);

		if (usuario != null && equipe != null) {
			equipe.adicionarMembro(usuario);
			System.out.println("Usuário " + usuario.getNomeCompleto() + " adicionado à equipe " + equipe.getNome());
		} else {
			System.out.println("Usuário ou equipe não encontrados.");
		}
	}

	// Associar equipe a projeto
	public void associarEquipeAProjeto(String nomeEquipe, String nomeProjeto) {
		Equipe equipe = equipes.stream()
				.filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
				.findFirst().orElse(null);

		Projeto projeto = projetos.stream()
				.filter(p -> p.getNome().equalsIgnoreCase(nomeProjeto))
				.findFirst().orElse(null);

		if (equipe != null && projeto != null) {
			projeto.setEquipe(equipe);
			System.out.println("Equipe " + equipe.getNome() + " atribuída ao projeto " + projeto.getNome());
		} else {
			System.out.println("Equipe ou projeto não encontrados.");
		}
	}


    // ==== GETTERS ====
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Equipe> getEquipes() { return equipes; }
}
