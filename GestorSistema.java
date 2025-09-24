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

    // Cadastro de usuário
    public void cadastrarUsuario(String nome, String cpf, String email, String cargo,
                                 String login, String senha, PerfilUsuario perfil) {
        Usuario usuario = new Usuario(nome, cpf, email, cargo, login, senha, perfil);
        usuarios.add(usuario);
        System.out.println("Usuário cadastrado: " + usuario);
    }

    // Cadastro de projeto
    public void cadastrarProjeto(String nome, String descricao, LocalDate inicio,
                                 LocalDate fim, StatusProjeto status, Usuario gerente) {
        Projeto projeto = new Projeto(nome, descricao, inicio, fim, status, gerente);
        projetos.add(projeto);
        System.out.println("Projeto cadastrado: " + projeto);
    }

    // Cadastro de equipe
    public void cadastrarEquipe(String nome, String descricao) {
        Equipe equipe = new Equipe(nome, descricao);
        equipes.add(equipe);
        System.out.println("Equipe cadastrada: " + equipe);
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Equipe> getEquipes() { return equipes; }
}
