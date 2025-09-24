package br.com.gestao.manager;

import br.com.gestao.model.*;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorSistema {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Projeto> projetos = new ArrayList<>();
    private List<Equipe> equipes = new ArrayList<>();

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
